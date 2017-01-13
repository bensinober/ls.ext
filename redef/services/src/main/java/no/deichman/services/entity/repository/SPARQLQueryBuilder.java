package no.deichman.services.entity.repository;

import no.deichman.services.entity.EntityType;
import no.deichman.services.entity.patch.Patch;
import no.deichman.services.rdf.RDFModelUtil;
import no.deichman.services.uridefaults.BaseURI;
import no.deichman.services.uridefaults.XURI;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.jena.rdf.model.ResourceFactory.createTypedLiteral;

/**
 * Responsibility: TODO.
 */
public final class SPARQLQueryBuilder {
    public static final String INSERT = "INSERT";
    public static final String DELETE = "DELETE";
    public static final String NEWLINE = "\n";
    private static final String INDENT = "    ";
    public static final boolean KEEP_BLANK_NODES = true;
    public static final boolean SKIP_BLANK_NODES = false;

    public SPARQLQueryBuilder() {
    }

    public Query getGetResourceByIdQuery(String id) {
        String queryString = "DESCRIBE <" + id + ">";
        return QueryFactory.create(queryString);
    }

    public Query describeWorkAndLinkedResources(XURI xuri) {
        String queryString = "#\n"
                + "PREFIX deichman: <http://data.deichman.no/ontology#>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "DESCRIBE <__WORKURI__> ?publication ?workContributor ?compType ?format ?mediaType ?subject ?genre ?instrument ?publicationPart"
                + "  ?litform ?hasWorkType ?serial ?nation ?pubContrib ?publicationContributor ?place ?publishedBy ?publicationPartValues\n"
                + "WHERE {\n"
                + "        { <__WORKURI__> a deichman:Work }\n"
                + "  UNION {  <__WORKURI__> deichman:contributor ?workContrib .\n"
                + "           ?workContrib a deichman:Contribution ;\n"
                + "             deichman:agent ?workContributor . \n"
                + "           OPTIONAL { ?workContributor deichman:nationality ?nation }"
                + "    }\n"
                + "  UNION {  ?publication deichman:publicationOf <__WORKURI__> ; \n"
                + "            a deichman:Publication . \n"
                + "          OPTIONAL { ?publication deichman:format ?format .\n"
                + "                  ?format rdfs:label ?label . }\n"
                + "          OPTIONAL { ?publication deichman:hasMediaType ?mediaType .\n"
                + "                  ?mediaType rdfs:label ?label . }\n"
                + "          OPTIONAL { ?publication deichman:contributor ?pubContrib. \n"
                + "                  ?pubContrib a deichman:Contribution ;\n"
                + "                    deichman:agent ?publicationContributor . }\n"
                + "          OPTIONAL { ?publication deichman:inSerial ?serialIssue . \n"
                + "                   ?serialIssue deichman:serial ?serial . }\n"
                + "          OPTIONAL { ?publication deichman:hasPlaceOfPublication ?place }\n"
                + "          OPTIONAL { ?publication deichman:publishedBy ?publishedBy }\n"
                + "          OPTIONAL { ?publication deichman:hasPublicationPart ?hasPublicationPart ."
                + "                     ?hasPublicationPart a deichman:PublicationPart;"
                + "                                  ?publicationPartProperties ?publicationPartValues ."
                + " }\n"
                + "    }\n"
                + "  UNION { <__WORKURI__> deichman:subject ?subject }\n"
                + "  UNION { <__WORKURI__> deichman:hasInstrumentation ?instrumentation .\n"
                + "       ?instrumentation deichman:hasInstrument ?instrument     }\n"
                + "  UNION { <__WORKURI__> deichman:genre ?genre }\n"
                + "  UNION { <__WORKURI__> deichman:literaryForm ?litform }\n"
                + "  UNION { <__WORKURI__> deichman:hasWorkType ?hasWorkType }\n"
                + "  UNION { <__WORKURI__> deichman:hasCompositionType ?compType }\n"
                + "}";
        queryString = queryString.replaceAll("__WORKURI__", xuri.getUri());
        return QueryFactory.create(queryString);
    }

    public Query describePersonAndLinkedResources(String personId) {
        String queryString = format("#\n"
                + "PREFIX deichman: <%1$s>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "DESCRIBE <%2$s> ?name ?birth ?death ?personTitle ?nationality ?nationalityLabel \n"
                + "WHERE {\n"
                + "    <%2$s> a deichman:Person .\n"
                + "    optional { <%2$s> deichman:name ?name . }\n"
                + "    optional { <%2$s> deichman:birthYear ?birth . }\n"
                + "    optional { <%2$s> deichman:deathYear ?death . }\n"
                + "    optional { <%2$s> deichman:personTitle ?personTitle . }\n"
                + "    optional { <%2$s> deichman:nationality ?nationality . \n"
                + "                 ?nationality rdfs:label ?nationalityLabel .\n"
                + "             }\n"
                + "}", BaseURI.ontology(), personId);
        return QueryFactory.create(queryString);
    }

    public Query describeCorporationAndLinkedResources(String corporationId) {
        String queryString = format("#\n"
                + "PREFIX deichman: <%1$s>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "DESCRIBE <%2$s> ?nationality ?nationalityLabel ?place\n"
                + "WHERE {\n"
                + "     {\n"
                + "      <%2$s> a deichman:Corporation .\n"
                + "      optional { <%2$s> deichman:nationality ?nationality . \n"
                + "                  ?nationality rdfs:label ?nationalityLabel .\n"
                + "     }\n"
                + "  } \n"
                + "   UNION {"
                + "      <%2$s> deichman:place ?place . \n"
                + "      ?place a deichman:Place .\n"
                + "      <%2$s> deichman:place ?place . \n"
                + "  }\n"
                + "}", BaseURI.ontology(), corporationId);
        return QueryFactory.create(queryString);
    }

    public String getUpdateWorkQueryString(Model work) {
        return getCreateQueryString(work);
    }

    public String getReplaceSubjectQueryString(String newURI) {
        return "DELETE {\n"
                + " ?s ?p ?o .\n"
                + "}\n"
                + "INSERT {\n"
                + " <" + newURI + "> ?p ?o .\n"
                + "}\n"
                + "WHERE {\n"
                + " ?s ?p ?o .\n"
                + " FILTER (!isBlank(?s))\n"
                + "}\n";
    }

    public String getCreateQueryString(Model work) {
        StringWriter sw = new StringWriter();
        RDFDataMgr.write(sw, work, Lang.NTRIPLES);
        String data = sw.toString();

        return "INSERT DATA {\n"
                + "\n"
                + data
                + "\n"
                + "}";
    }

    public Query getItemsFromModelQuery(String id) {
        String q = "PREFIX deichman: <" + BaseURI.ontology() + ">\n"
                + "PREFIX duo: <http://data.deichman.no/utility#>\n"
                + "CONSTRUCT {\n"
                + "  ?uri deichman:editionOf  <" + id + "> ;"
                + "    a deichman:Item ;"
                + "    deichman:branch ?branch ;"
                + "    deichman:location ?location ;"
                + "    deichman:status ?status ;"
                + "    deichman:barcode ?barcode ;"
                + "    duo:shelfmark ?shelfmark ."
                + "} WHERE { \n"
                + "  ?uri a deichman:Item ;\n"
                + "    deichman:branch ?branch ;\n"
                + "    deichman:status ?status ;"
                + "    deichman:barcode ?barcode .\n"
                + " OPTIONAL { ?uri duo:shelfmark ?shelfmark }\n"
                + " OPTIONAL { ?uri deichman:location ?location }\n"
                + "}";
        return QueryFactory.create(q);
    }

    public Query checkIfResourceExists(XURI xuri) {
        String q = "ASK {<" + xuri.getUri() + "> ?p ?o}";
        return QueryFactory.create(q);
    }

    public Query checkIfStatementExists(Statement statement) throws UnsupportedEncodingException {
        String triple = statementToN3(statement);
        String q = "ASK {" + triple + "}";
        return QueryFactory.create(q);
    }

    private String statementToN3(Statement statement) throws UnsupportedEncodingException {
        Model tempExists = ModelFactory.createDefaultModel();
        tempExists.add(statement);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        RDFDataMgr.write(baos, tempExists, Lang.NTRIPLES);
        return baos.toString("UTF-8");
    }

    public String patch(List<Patch> patches, Resource subject) {
        StringBuilder q = new StringBuilder();
        if (subject != null) {
            q.append(String.format(""
                            + "DELETE { <%s> <%smodified> ?modified }"
                            + "WHERE { <%s> <%smodified> ?modified };",
                    subject.getURI(), BaseURI.ontology(), subject.getURI(), BaseURI.ontology()));
        }
        String del = getStringOfStatments(patches, "DEL", SKIP_BLANK_NODES);
        String delSelect = getStringOfStatementsWithVariables(patches, "DEL");
        String add = getStringOfStatments(patches, "ADD", KEEP_BLANK_NODES);

        if (del.length() > 0) {
            q.append("DELETE DATA {" + NEWLINE + del + "};" + NEWLINE);
        }
        if (delSelect.length() > 0) {
            q.append("DELETE {" + NEWLINE + delSelect + "}" + NEWLINE + "WHERE {" + NEWLINE + delSelect + "};" + NEWLINE);
        }
        if (add.length() > 0) {
            q.append(INSERT + " DATA {" + NEWLINE + add + "};" + NEWLINE);
        }

        return q.toString();
    }

    private String getStringOfStatementsWithVariables(List<Patch> patches, String operation) {

        String retVal = "";

        String bnodeSubjectCheck = patches.stream()
                .filter(s -> s.getStatement().getSubject().isAnon())
                .map(s -> {
                    return "a";
                })
                .collect(joining(""));

        String bnodeObjectCheck = patches.stream()
                .filter(s -> s.getStatement().getObject().isAnon())
                .map(s -> {
                    return "a";
                })
                .collect(joining());

        if (bnodeSubjectCheck.contains("a") && bnodeObjectCheck.contains("a")) {
            retVal = patches.stream()
                    .filter(patch -> patch.getOperation().toUpperCase().equals(operation.toUpperCase())
                            && (patch.getStatement().getSubject().isAnon() || patch.getStatement().getObject().isAnon()))
                    .map(patch2 -> {
                                Model model = ModelFactory.createDefaultModel();
                                model.add(patch2.getStatement());
                                String withoutBnodes = RDFModelUtil.stringFrom(model, Lang.NTRIPLES).replaceAll("_:", "?");
                                return INDENT + withoutBnodes;
                            }
                    ).filter(s -> !s.isEmpty()).collect(joining());

        }

        return retVal;
    }

    private String getStringOfStatments(List<Patch> patches, String operation, boolean keepBlankNodes) {
        return patches.stream()
                .filter(patch -> patch.getOperation().toUpperCase().equals(operation.toUpperCase())
                        && (keepBlankNodes || !patch.getStatement().getObject().isAnon() && !patch.getStatement().getSubject().isAnon()))
                .map(patch2 -> {
                            Model model = ModelFactory.createDefaultModel();
                            model.add(patch2.getStatement());
                            return INDENT + RDFModelUtil.stringFrom(model, Lang.NTRIPLES);
                        }
                ).filter(s -> !s.isEmpty()).collect(joining());

    }

    public Query getImportedResourceById(String id, String type) {
        String q = "SELECT ?uri "
                + "WHERE "
                + "  { ?uri <http://data.deichman.no/duo#bibliofil" + type + "Id> \"" + id + "\" }";
        return QueryFactory.create(q);
    }

    public Query getBibliofilPersonResource(String personId) {
        String q = "SELECT ?uri "
                + "WHERE "
                + "  { ?uri <http://data.deichman.no/duo#bibliofilPersonId> \"" + personId + "\" }";
        return QueryFactory.create(q);
    }

    public Query getBibliofilPlaceResource(String id) {
        String q = "SELECT ?uri "
                + "WHERE "
                + "  { ?uri <http://data.deichman.no/duo#bibliofilPlaceId> \"" + id + "\" }";
        return QueryFactory.create(q);
    }

    public Query describeWorksByCreator(XURI xuri) {
        String q = format(""
                + "PREFIX deichman: <%1$s>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX dcterms: <http://purl.org/dc/terms/>\n"
                + "DESCRIBE ?work <%2$s>\n"
                + "WHERE {\n"
                + "      ?work a deichman:Work ;\n"
                + "            deichman:contributor ?contrib .\n"
                + "      ?contrib deichman:agent <%2$s> .\n"
                + "}", BaseURI.ontology(), xuri.getUri());
        return QueryFactory.create(q);
    }

    public Query describeLinkedPublications(XURI xuri) {
        String q = format(""
                + "PREFIX deichman: <%s>\n"
                + "DESCRIBE ?publication WHERE \n"
                + "    {\n"
                + "        ?publication deichman:publicationOf <%s>\n"
                + "    }", BaseURI.ontology(), xuri.getUri());
        return QueryFactory.create(q);
    }

    public Query selectAllUrisOfType(String type) {
        String q = format(""
                + "PREFIX deichman: <%s>\n"
                + "SELECT ?uri WHERE \n"
                + "    {\n"
                + "        ?uri a deichman:%s .\n"
                + "    }", BaseURI.ontology(), capitalize(type));
        return QueryFactory.create(q);
    }

    public String updateHoldingBranches(String recordId, String branches) {
        String q = format(""
                        + "PREFIX : <%s>\n"
                        + "DELETE { ?pub :hasHoldingBranch ?branch }\n"
                        + "INSERT { ?pub :hasHoldingBranch \"%s\" . }\n"
                        + "WHERE { ?pub :recordId \"%s\" OPTIONAL { ?pub :hasHoldingBranch ?branch } }\n",
                BaseURI.ontology(), StringUtils.join(branches.split(","), "\",\""), recordId);
        return q;
    }

    public Query getWorkByRecordId(String recordId) {
        String q = format(""
                        + "PREFIX : <%s>\n"
                        + "SELECT ?work\n"
                        + "WHERE { ?pub :recordId \"%s\" .\n"
                        + "        ?pub :publicationOf ?work }\n",
                BaseURI.ontology(), recordId);
        return QueryFactory.create(q);
    }


    public Query selectWorksByAgent(XURI agent) {
        String q = format(""
                + "PREFIX deichman: <%1$s>\n"
                + "SELECT ?work\n"
                + "WHERE {\n"
                + "  ?work a deichman:Work ;\n"
                + "    deichman:contributor ?contrib .\n"
                + "  ?contrib  deichman:agent <%2$s> .\n"
                + "}", BaseURI.ontology(), agent.getUri());
        return QueryFactory.create(q);
    }

    public Query constructInformationForMARC(XURI publication) {
        String q = ""
                + "PREFIX deichman: <http://data.deichman.no/ontology#>\n"
                + "PREFIX raw: <http://data.deichman.no/raw#>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "CONSTRUCT {\n"
                + " <__PUBLICATIONURI__> deichman:mainTitle ?mainTitle ;\n"
                + "       deichman:partTitle ?partTitle ;\n"
                + "       deichman:subtitle ?subtitle ;\n"
                + "       deichman:partNumber ?partNumber ;\n"
                + "       deichman:isbn ?isbn ;\n"
                + "       deichman:publicationYear ?publicationYear ;\n"
                + "       deichman:publicationPlace ?placeLabel ;\n"
                + "       deichman:ageLimit ?ageLimit ;\n"
                + "       deichman:mainEntryPerson ?mainEntryPersonName ;\n"
                + "       deichman:mainEntryCorporation ?mainEntryCorporationName ;\n"
                + "       deichman:subject ?subjectLabelAndSpecification ;\n"
                + "       deichman:formatLabel ?formatLabel ;\n"
                + "       deichman:language ?language ;\n"
                + "       deichman:workType ?workType ;\n"
                + "       deichman:mediaType ?mediaTypeLabel ;\n"
                + "       deichman:literaryForm ?literaryForm ;\n"
                + "       deichman:literaryFormLabel ?literaryFormLabel ;\n"
                + "       deichman:adaptationLabel ?formatAdaptationLabel ;\n"
                + "       deichman:adaptationLabel ?contentAdaptationLabel ;\n"
                + "       deichman:genre ?genreLabel ;\n"
                + "       deichman:fictionNonfiction ?fictionNonfiction ;\n"
                + "       deichman:summary ?summary ;"
                + "       deichman:audience ?audienceLabel ;\n"
                + "       deichman:locationFormat ?locationFormat ;\n"
                + "       deichman:locationDewey ?locationDewey ;\n"
                + "       deichman:locationSignature ?locationSignature ."
                + "}\n"
                + "WHERE {\n"
                + "  <__PUBLICATIONURI__> deichman:mainTitle ?mainTitleUntranscribed .\n"
                + "  OPTIONAL { <__PUBLICATIONURI__> deichman:subtitle ?subtitleUntranscribed }\n"
                + "  OPTIONAL { <__PUBLICATIONURI__> raw:transcribedTitle ?mainTitleTranscribed }\n"
                + "  OPTIONAL { <__PUBLICATIONURI__> raw:transcribedSubtitle ?subtitleTranscribed }\n"
                + "  { <__PUBLICATIONURI__> deichman:partTitle ?partTitle }\n"
                + "  UNION { <__PUBLICATIONURI__> deichman:hasSummary ?summary }\n"
                + "  UNION { <__PUBLICATIONURI__> deichman:partNumber ?partNumber }\n"
                + "  UNION { <__PUBLICATIONURI__> deichman:isbn ?isbn }\n"
                + "  UNION { <__PUBLICATIONURI__> deichman:publicationYear ?publicationYear }\n"
                + "  UNION { <__PUBLICATIONURI__> deichman:language ?language }\n"
                + "  UNION { <__PUBLICATIONURI__> deichman:ageLimit ?ageLimit }\n"
                + "  UNION { <__PUBLICATIONURI__> deichman:hasPlaceOfPublication ?place . ?place deichman:prefLabel ?placeLabel }\n"
                + "  UNION { <__PUBLICATIONURI__> deichman:locationFormat ?locationFormat }\n"
                + "  UNION { <__PUBLICATIONURI__> deichman:locationClassNumber ?locationDewey }\n"
                + "  UNION { <__PUBLICATIONURI__> deichman:locationSignature ?locationSignature } \n"
                + "  UNION { <__PUBLICATIONURI__> deichman:hasMediaType ?mediaType .\n"
                + "     ?mediaType rdfs:label ?mediaTypeLabel .\n"
                + "     FILTER(lang(?mediaTypeLabel) = \"no\")\n"
                + "  }"
                + "\n"
                + "  UNION { <__PUBLICATIONURI__> deichman:format ?format .\n"
                + "     ?format rdfs:label ?formatLabel .\n"
                + "     FILTER(lang(?formatLabel) = \"no\")\n"
                + "  }"
                + "  UNION { <__PUBLICATIONURI__> deichman:publicationOf ?work ."
                + "     ?work deichman:audience ?audience .\n"
                + "     ?audience rdfs:label ?audienceLabel .\n"
                + "     FILTER(lang(?audienceLabel) = \"no\")\n"
                + "  }"
                + "  UNION { <__PUBLICATIONURI__> deichman:hasFormatAdaptation ?formatAdaptation .\n"
                + "     ?formatAdaptation rdfs:label ?formatAdaptationLabel .\n"
                + "     FILTER(lang(?formatAdaptationLabel) = \"no\")\n"
                + "  }"
                + "\n"
                + "  UNION { "
                + "    <__PUBLICATIONURI__>  deichman:publicationOf ?work .\n"
                + "     ?work a deichman:Work ;\n"
                + "     deichman:hasContentAdaptation ?contentAdaptation .\n"
                + "     ?contentAdaptation rdfs:label ?contentAdaptationLabel .\n"
                + "     FILTER(lang(?contentAdaptationLabel) = \"no\")\n"
                + "  }\n"
                + "  UNION { "
                + "    <__PUBLICATIONURI__>  deichman:publicationOf ?work .\n"
                + "     ?work a deichman:Work .\n"
                + "     OPTIONAL { ?work deichman:hasWorkType ?type .\n"
                + "       ?type rdfs:label ?workType .\n"
                + "       FILTER(lang(?workType) = \"no\")\n"
                + "     }\n"
                + "  }\n"
                + "  UNION {\n"
                + "    <__PUBLICATIONURI__>  deichman:publicationOf ?work .\n"
                + "    ?work deichman:contributor ?contrib .\n"
                + "    ?contrib a deichman:MainEntry ;\n"
                + "      deichman:agent ?agent .\n"
                + "    OPTIONAL { ?agent a deichman:Person ; deichman:name ?mainEntryPersonName . }\n"
                + "    OPTIONAL { ?agent a deichman:Corporation ; deichman:name ?mainEntryCorporationName . }\n"
                + "  }\n"
                + "  UNION {\n"
                + "    <__PUBLICATIONURI__>  deichman:publicationOf ?work .\n"
                + "    ?work deichman:subject ?subject .\n"
                + "    ?subject deichman:prefLabel ?subjectLabel .\n"
                + "    OPTIONAL { ?subject deichman:specification ?subjectSpecification .} \n"
                + "    BIND(IF(BOUND(?subjectSpecification), CONCAT(?subjectLabel, \"__\", ?subjectSpecification), ?subjectLabel) AS ?subjectLabelAndSpecification)"
                + "  }\n"
                + "  UNION {\n"
                + "    <__PUBLICATIONURI__>  deichman:publicationOf ?work .\n"
                + "    ?work deichman:subject ?subject .\n"
                + "    ?subject deichman:name ?subjectLabel .\n"
                + "    OPTIONAL { ?subject deichman:specification ?subjectSpecification .} \n"
                + "    BIND(IF(BOUND(?subjectSpecification), CONCAT(?subjectLabel, \"__\", ?subjectSpecification), ?subjectLabel) AS ?subjectLabelAndSpecification)"
                + "  }\n"
                + "  UNION {\n"
                + "    <__PUBLICATIONURI__>  deichman:publicationOf ?work .\n"
                + "    ?work deichman:genre ?genre .\n"
                + "    ?genre deichman:prefLabel ?genreLabel .\n"
                + "  }\n"
                + "  UNION { "
                + "    <__PUBLICATIONURI__>  deichman:publicationOf ?work .\n"
                + "    ?work deichman:literaryForm ?literaryForm .\n"
                + "    ?literaryForm rdfs:label ?literaryFormLabel .\n"
                + "    FILTER(lang(?literaryFormLabel) = \"no\")\n"
                + "  }\n"
                + "  UNION { <__PUBLICATIONURI__>  deichman:publicationOf ?work . ?work  deichman:fictionNonfiction ?fictionNonfiction }"
                + "  BIND(IF(BOUND(?mainTitleTranscribed), ?mainTitleTranscribed, ?mainTitleUntranscribed) AS ?mainTitle)\n"
                + "  BIND(IF(BOUND(?subtitleTranscribed), ?subtitleTranscribed, ?subtitleUntranscribed) AS ?subtitle)\n"
                + "}";
        q = q.replaceAll("__PUBLICATIONURI__", publication.getUri());

        return QueryFactory.create(q);
    }

    public Query getRecordIdsByWork(XURI xuri) {
        String queryString = "SELECT ?recordId\n"
                + "WHERE {\n"
                + "  ?p <http://data.deichman.no/ontology#publicationOf> <" + xuri.getUri() + "> ;\n"
                + "     <http://data.deichman.no/ontology#recordId> ?recordId\n"
                + "}\n";
        return QueryFactory.create(queryString);
    }

    public Query constructInversePublicationRelations(XURI workUri) {
        String query = "PREFIX deichman: <http://data.deichman.no/ontology#>\n"
                + "CONSTRUCT {<" + workUri.getUri() + "> deichman:hasPublication ?publication} WHERE {?publication deichman:publicationOf <" + workUri.getUri() + ">}";
        return QueryFactory.create(query);
    }

    public Query getNumberOfRelationsForResource(XURI xuri) {
        String queryString = format("PREFIX deich: <http://data.deichman.no/ontology#>\n"
                + "SELECT ?type (COUNT(?a) as ?references)\n"
                + "WHERE {\n"
                + "  ?a ?b <%s> ;\n"
                + "     a ?type .\n"
                + "}\n"
                + "GROUP BY ?type ", xuri.getUri());
        return QueryFactory.create(queryString);
    }

    public Query constructFromQueryAndProjection(EntityType entityType, Map<String, String> queryParameters, Collection<String> projection) {
        Stream<String> start = of("prefix deichman:<http://data.deichman.no/ontology#>\n"
                + "construct {"
                + "     ?uri a ?type .\n");
        Stream<String> conditionals = queryParameters
                .entrySet()
                .stream()
                .map(entry -> format(" ?uri deichman:%s %s .", entry.getKey(), createTypedLiteral(entry.getValue()).asNode().toString()));

        Stream<String> type = of(format(" ?uri a deichman:%s .", org.apache.commons.lang3.StringUtils.capitalize(entityType.getPath())));
        Stream<String> selects = projection.stream().map(property -> format(" OPTIONAL { ?uri deichman:%s ?%s } ", property, property));
        Stream<String> projections = projection.stream().map(property -> format(" ?uri deichman:%s ?%s .", property, property));

        Stream<String> where = of("}\n"
                + "where {"
                + "     ?uri a ?type .\n ");

        Stream<String> end = newArrayList("}\n").stream();
        String query = concat(start, concat(projections, concat(where, concat(type, concat(conditionals, concat(selects, end)))))).collect(joining("\n"));
        return QueryFactory.create(query);
    }

    public String patch(List<Patch> deleteModelAsPatches) {
        return patch(deleteModelAsPatches, null);
    }

    public Query describeEventAndLinkedResources(XURI eventUri) {
        String queryString = format("#\n"
                + "PREFIX deichman: <%1$s>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "DESCRIBE <%2$s> ?place\n"
                + "WHERE {\n"
                + "   { <%2$s> a deichman:Event . }\n"
                + "  UNION {"
                + "      <%2$s> deichman:place ?place . \n"
                + "      ?place a deichman:Place .\n"
                + "  }\n"
                + "}", BaseURI.ontology(), eventUri);
        return QueryFactory.create(queryString);
    }

    public Query describeSerialAndLinkedResources(XURI serialUri) {
        String queryString = format("#\n"
                + "PREFIX deichman: <%1$s>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "DESCRIBE <%2$s> ?publisher ?publisherPlace\n"
                + "WHERE {\n"
                + "   { <%2$s> a deichman:Serial . }\n"
                + "  UNION {"
                + "      <%2$s> deichman:publishedBy ?publisher . \n"
                + "  }\n"
                + "  UNION {"
                + "      <%2$s> deichman:publishedBy ?publisher . \n"
                + "      ?publisher deichman:place ?publisherPlace . \n"
                + "      ?publisherPlace a deichman:Place .\n"
                + "  }\n"
                + "}", BaseURI.ontology(), serialUri);
        return QueryFactory.create(queryString);
    }
}
