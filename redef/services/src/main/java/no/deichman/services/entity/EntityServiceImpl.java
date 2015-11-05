package no.deichman.services.entity;

import no.deichman.services.entity.kohaadapter.KohaAdapter;
import no.deichman.services.entity.kohaadapter.MarcConstants;
import no.deichman.services.entity.kohaadapter.MarcRecord;
import no.deichman.services.entity.patch.PatchParser;
import no.deichman.services.entity.patch.PatchParserException;
import no.deichman.services.entity.repository.RDFRepository;
import no.deichman.services.entity.repository.SPARQLQueryBuilder;
import no.deichman.services.uridefaults.BaseURI;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.groupingBy;

/**
 * Responsibility: TODO.
 */
public final class EntityServiceImpl implements EntityService {

    private final RDFRepository repository;
    private final KohaAdapter kohaAdapter;
    private final BaseURI baseURI;
    private static final String LANGUAGE_TTL_FILE = "language.ttl";
    private static final String FORMAT_TTL_FILE = "format.ttl";
    private final Property hasItemProperty;
    private final Property nameProperty;

    private Model getLinkedLexvoResource(Model input) {

        NodeIterator objects = input.listObjects();
        if (objects.hasNext()) {
            Set<RDFNode> objectResources = objects.toSet();
            objectResources.stream()
                    .filter(node -> node.toString()
                            .contains("http://lexvo.org/id/iso639-3/")).collect(Collectors.toList())
                    .forEach(lv -> {
                        input.add(extractNamedResourceFromModel(lv.toString(), EntityServiceImpl.class.getClassLoader().getResourceAsStream(LANGUAGE_TTL_FILE), Lang.TURTLE));
                    });
        }

        return input;
    }

    private Model getLinkedFormatResource(Model input) {

        NodeIterator objects = input.listObjects();
        if (objects.hasNext()) {
            Set<RDFNode> objectResources = objects.toSet();
            objectResources.stream()
                    .filter(node -> node.toString()
                            .contains("http://data.deichman.no/format#")).collect(Collectors.toList())
                    .forEach(result -> {
                        input.add(extractNamedResourceFromModel(result.toString(), EntityServiceImpl.class.getClassLoader().getResourceAsStream(FORMAT_TTL_FILE), Lang.TURTLE));
                    });
        }

        return input;
    }

    private Model extractNamedResourceFromModel(String resource, InputStream input, Lang lang) {
        Model tempModel = ModelFactory.createDefaultModel();
        RDFDataMgr.read(tempModel, input, lang);
        QueryExecution qexec = QueryExecutionFactory.create(
                QueryFactory.create("DESCRIBE <" + resource + ">"),
                tempModel);
        return qexec.execDescribe();
    }

    public EntityServiceImpl(BaseURI baseURI, RDFRepository repository, KohaAdapter kohaAdapter) {
        this.baseURI = baseURI;
        this.repository = repository;
        this.kohaAdapter = kohaAdapter;
        hasItemProperty = ResourceFactory.createProperty(baseURI.ontology("hasItem"));
        nameProperty = ResourceFactory.createProperty(baseURI.ontology("name"));
    }

    @Override
    public Model retrieveById(EntityType type, String id) {
        Model m = ModelFactory.createDefaultModel();
        switch (type) {
            case PUBLICATION:
                m.add(repository.retrievePublicationById(id));
                break;
            case PERSON:
                m.add(repository.retrievePersonById(id));
                break;
            case WORK:
                m.add(repository.retrieveWorkById(id));
                m = getLinkedLexvoResource(m);
                m = getLinkedFormatResource(m);
                break;
            default:
                throw new IllegalArgumentException("Unknown entity type:" + type);
        }
        return m;
    }


    @Override
    public void updateWork(String work) {
        repository.updateWork(work);
    }

    @Override
    public Model retrieveWorkItemsById(String id) {
        Model allItemsModel = ModelFactory.createDefaultModel();

        Model workModel = repository.retrieveWorkById(id);
        QueryExecution queryExecution = QueryExecutionFactory.create(QueryFactory.create(
                "PREFIX  deichman: <" + baseURI.ontology() + "> "
                        + "SELECT  * "
                        + "WHERE { ?publicationUri deichman:recordID ?biblioId }"), workModel);
        ResultSet publicationSet = queryExecution.execSelect();

        while (publicationSet.hasNext()) {
            QuerySolution solution = publicationSet.next();
            RDFNode publicationUri = solution.get("publicationUri");
            String biblioId = solution.get("biblioId").asLiteral().getString();

            Model itemsForBiblio = kohaAdapter.getBiblio(biblioId);

            SPARQLQueryBuilder sqb = new SPARQLQueryBuilder(baseURI);
            Query query = sqb.getItemsFromModelQuery(publicationUri.toString());
            QueryExecution qexec = QueryExecutionFactory.create(query, itemsForBiblio);
            Model itemsModelWithBlankNodes = qexec.execConstruct();

            allItemsModel.add(itemsModelWithBlankNodes);
        }
        return allItemsModel;
    }

    @Override
    public String create(EntityType type, Model inputModel) {
        String uri;
        switch (type) {
            case PUBLICATION:
                Set<Resource> items = objectsOfProperty(hasItemProperty, inputModel);
                if (items.isEmpty()) {
                    uri = repository.createPublication(inputModel, kohaAdapter.getNewBiblio());
                } else {
                    uri = createPublicationWithItems(inputModel, items);
                }
                break;
            case WORK:
                uri = repository.createWork(inputModel);
                break;
            case PERSON:
                uri = repository.createPerson(inputModel);
                break;
            default:
                throw new IllegalArgumentException("Unknown entity type:" + type);
        }
        return uri;
    }

    private String createPublicationWithItems(Model inputModel, Set<Resource> items) {
        Model modelWithoutItems = ModelFactory.createDefaultModel();
        MarcRecord marcRecord = new MarcRecord();
        streamFrom(inputModel.listStatements())
                .collect(groupingBy(Statement::getSubject))
                .forEach((subject, statements) -> {
                    if (items.contains(subject)) {
                        as952Subfields(statements).forEach((subfield, value) -> marcRecord.addGroup(MarcConstants.FIELD_952, subfield, value));
                    } else {
                        statements.forEach(s -> {
                            if (!s.getPredicate().equals(hasItemProperty)) {
                                if (s.getPredicate().equals(nameProperty)) {
                                    marcRecord.addTitle(s.getObject().asLiteral().toString());
                                }
                                modelWithoutItems.add(s);

                            }
                        });
                    }
                });

        return repository.createPublication(
                modelWithoutItems,
                kohaAdapter.getNewBiblioWithItems(marcRecord)
        );
    }

    private static <T> Map[] asArray(List<T> list) {
        return list.toArray(new Map[list.size()]);
    }

    private Map<Character, String> as952Subfields(List<Statement> statements) {
        Map<Character, String> itemSubfieldMap = new TreeMap<>();
        statements.forEach(s -> {
            String predicate = s.getPredicate().getURI();
            String fieldCode = predicate.substring(predicate.lastIndexOf("/") + 1);

            RDFNode object = s.getObject();
            if (object.isLiteral() && !(fieldCode.length() > 1)) {
                itemSubfieldMap.put(fieldCode.charAt(0), object.asLiteral().getString());
            }
        });
        return itemSubfieldMap;
    }

    private static Set<Resource> objectsOfProperty(Property property, Model inputModel) {
        Set<Resource> resources = new HashSet<>();
        inputModel
                .listObjectsOfProperty(property)
                .forEachRemaining(r -> resources.add(r.asResource()));
        return resources;
    }

    private static <T> Stream<T> streamFrom(Iterator<T> sourceIterator) {
        Iterable<T> iterable = () -> sourceIterator;
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    @Override
    public void delete(Model model) {
        repository.delete(model);
    }

    @Override
    public Model patch(EntityType type, String id, String ldPatchJson) throws PatchParserException {
        repository.patch(PatchParser.parse(ldPatchJson));
        Model model = retrieveById(type, id);

        if(type.equals(EntityType.PUBLICATION)) {
            MarcRecord marcRecord = new MarcRecord();
            streamFrom(model.listStatements())
                    .collect(groupingBy(Statement::getSubject))
                    .forEach((subject, statements) -> {
                        statements.forEach(s -> {
                            if (s.getPredicate().equals(nameProperty)) {
                                marcRecord.addTitle(s.getObject().asLiteral().toString());
                            }
                        });
                    });
            kohaAdapter.updateRecord(model.getProperty(null, ResourceFactory.createProperty(baseURI.ontology("recordID"))).getString(), marcRecord);
        }

        return model;
    }

    @Override
    public boolean resourceExists(String resourceUri) {
        return repository.askIfResourceExists(resourceUri);
    }

    @Override
    public Optional<String> retrieveBibliofilPerson(String personId) {
        return repository.getResourceURIByBibliofilId(personId);

    }

    @Override
    public Model retrieveWorksByCreator(String creatorId) {
        return repository.retrieveWorksByCreator(creatorId);
    }
}
