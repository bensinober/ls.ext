package no.deichman.services.search;

import no.deichman.services.rdf.RDFModelUtil;
import no.deichman.services.uridefaults.BaseURI;
import no.deichman.services.uridefaults.XURI;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.junit.Assert;
import org.junit.Test;

import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

/**
 * Responsibility: unit test WorkModelToIndexMapper.
 */
public class WorkModelToIndexMapperTest {
    private String comparisonJsonDocument = "{\n" +
            "  \"work\": {\n" +
            "    \"uri\": \"%1$s\",\n" +
            "    \"contributor\": {\n" +
            "      \"uri\": \"_:b0\",\n" +
            "      \"author\": {\n" +
            "        \"uri\": \"%2$s\",\n" +
            "        \"birthYear\": \"1957\",\n" +
            "        \"name\": \"Ragde, Anne B.\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"mainTitle\": \"Berlinerpoplene\",\n" +
            "    \"publication\": [\n" +
            "      {\n" +
            "        \"uri\": \"http:\\/\\/192.168.50.12:8005\\/publication\\/p594502562255\",\n" +
            "        \"audience\": \"http:\\/\\/data.deichman.no\\/audience#adult\",\n" +
            "        \"format\": \"http:\\/\\/data.deichman.no\\/format#Book\",\n" +
            "        \"issued\": \"2013\",\n" +
            "        \"language\": \"http:\\/\\/lexvo.org\\/id\\/iso639-3\\/ita\",\n" +
            "        \"mainTitle\": \"La casa delle bugie\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"uri\": \"http:\\/\\/192.168.50.12:8005\\/publication\\/p735933031021\",\n" +
            "        \"audience\": \"http:\\/\\/data.deichman.no\\/audience#adult\",\n" +
            "        \"format\": \"http:\\/\\/data.deichman.no\\/format#Book\",\n" +
            "        \"issued\": \"2004\",\n" +
            "        \"language\": \"http:\\/\\/lexvo.org\\/id\\/iso639-3\\/nob\",\n" +
            "        \"mainTitle\": \"Berlinerpoplene\"\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";

    @Test
    public void testModelToIndexDocument() throws Exception {
        XURI workXuri = new XURI("http://192.168.50.12:8005/work/w4e5db3a95caa282e5968f68866774e20");
        XURI personXuri = new XURI("http://192.168.50.12:8005/person/h10834700");
        XURI publicationXuri1 = new XURI("http://192.168.50.12:8005/publication/p594502562255");
        XURI publicationXuri2 = new XURI("http://192.168.50.12:8005/publication/p735933031021");

        String inputGraph = "@prefix ns1: <http://data.deichman.no/duo#> .\n" +
                "@prefix ns2: <http://deichman.no/ontology#> .\n" +
                "@prefix ns4: <http://192.168.50.12:8005/raw#> .\n" +
                "@prefix ns5: <http://data.deichman.no/role#> .\n" +
                "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
                "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
                "@prefix xml: <http://www.w3.org/XML/1998/namespace> .\n" +
                "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
                "\n" +
                "<http://192.168.50.12:8005/publication/p594502562255> rdf:type ns2:Publication ;\n" +
                "    ns2:bibliofilPublicationID \"1549895\" ;\n" +
                "    ns2:contributor [ rdf:type ns2:Contribution ;\n" +
                "            ns2:agent <http://192.168.50.12:8005/person/h10834700> ;\n" +
                "            ns2:role ns5:author ] ;\n" +
                "    ns2:format <http://data.deichman.no/format#Book> ;\n" +
                "    ns2:isbn \"978-88-545-0662-6\" ;\n" +
                "    ns2:language <http://lexvo.org/id/iso639-3/ita> ;\n" +
                "    ns2:mainTitle \"La casa delle bugie\" ;\n" +
                "    ns2:publicationOf <http://192.168.50.12:8005/work/w4e5db3a95caa282e5968f68866774e20> ;\n" +
                "    ns2:publicationYear \"2013\"^^xsd:gYear ;\n" +
                "    ns2:recordID \"3\" ;\n" +
                "    ns4:locationDewey \"ITA\" ;\n" +
                "    ns4:locationSignature \"Rag\" ;\n" +
                "    ns4:statementOfResponsibility \"Anne B. Ragde ; traduzione di Cristina Falcinella\" .\n" +
                "\n" +
                "<http://192.168.50.12:8005/publication/p735933031021> rdf:type ns2:Publication ;\n" +
                "    ns2:bibliofilPublicationID \"0626460\" ;\n" +
                "    ns2:contributor [ rdf:type ns2:Contribution ;\n" +
                "            ns2:agent <http://192.168.50.12:8005/person/h10834700> ;\n" +
                "            ns2:role ns5:author ] ;\n" +
                "    ns2:contributor [ rdf:type ns2:Contribution ;\n" +
                "            ns2:agent <http://192.168.50.12:8005/person/h11234> ;\n" +
                "            ns2:role ns5:translator ] ;\n" +
                "    ns2:format <http://data.deichman.no/format#Book> ;\n" +
                "    ns2:isbn \"82-495-0272-8\" ;\n" +
                "    ns2:language <http://lexvo.org/id/iso639-3/nob> ;\n" +
                "    ns2:mainTitle \"Berlinerpoplene\" ;\n" +
                "    ns2:publicationOf <http://192.168.50.12:8005/work/w4e5db3a95caa282e5968f68866774e20> ;\n" +
                "    ns2:publicationYear \"2004\"^^xsd:gYear ;\n" +
                "    ns2:recordID \"11\" ;\n" +
                "    ns2:subtitle \"roman\" ;\n" +
                "    ns4:locationSignature \"Rag\" ;\n" +
                "    ns4:publicationHistory \"Forts. i: Eremittkrepsene\" ;\n" +
                "    ns4:statementOfResponsibility \"Anne Birkefeldt Ragde\" .\n" +
                "\n" +
                "\n" +
                "<http://192.168.50.12:8005/work/w4e5db3a95caa282e5968f68866774e20> rdf:type ns2:Work ;\n" +
                "    ns2:audience <http://data.deichman.no/audience#adult> ;\n" +
                "    ns2:contributor [ rdf:type ns2:Contribution,\n" +
                "                ns2:MainEntry ;\n" +
                "            ns2:agent <http://192.168.50.12:8005/person/h10834700> ;\n" +
                "            ns2:role ns5:author ] ;\n" +
                "    ns2:language <http://lexvo.org/id/iso639-3/nob> ;\n" +
                "    ns2:literaryForm <http://data.deichman.no/literaryForm#fiction>,\n" +
                "        <http://data.deichman.no/literaryForm#novel> ;\n" +
                "    ns2:mainTitle \"Berlinerpoplene\" .\n" +
                "\n" +
                "<http://192.168.50.12:8005/person/h10834700> rdf:type ns2:Person ;\n" +
                "    ns2:birthYear \"1957\" ;\n" +
                "    ns2:name \"Ragde, Anne B.\" ;\n" +
                "    ns2:nationality <http://data.deichman.no/nationality#n> ;\n" +
                "    ns2:personTitle \"forfatter\" ;\n" +
                "    ns4:lifeSpan \"1957-\" ;\n" +
                "    ns1:bibliofilPersonId \"10834700\" .\n" +
                "\n" +
                "<http://192.168.50.12:8005/person/h11234> rdf:type ns2:Person ;\n" +
                "    ns2:name \"Falcinella, Cristina\" ;\n" +
                "    ns2:nationality <http://data.deichman.no/nationality#ita> .\n";

        Model model = RDFModelUtil.modelFrom(inputGraph, Lang.TURTLE);

        String jsonDocument = new ModelToIndexMapper("work", BaseURI.local()).createIndexDocument(model, workXuri);

        Assert.assertThat(jsonDocument, sameJSONAs(String.format(comparisonJsonDocument, workXuri.getUri(), personXuri.getUri(), publicationXuri1.getUri(), publicationXuri2.getUri())).allowingAnyArrayOrdering());
    }
}
