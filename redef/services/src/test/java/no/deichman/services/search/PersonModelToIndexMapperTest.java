package no.deichman.services.search;

import no.deichman.services.uridefaults.BaseURI;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;

import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

/**
 * Responsibility: unit test PersonModelToIndexMapper.
 */
public class PersonModelToIndexMapperTest {
    @Test
    public void testModelToIndexDocument() throws Exception {
        Model model = ModelFactory.createDefaultModel();
        model.add(ResourceFactory.createStatement(
                ResourceFactory.createResource("http://deichman.no/person_1"),
                RDF.type,
                ResourceFactory.createResource("http://deichman.no/ontology#Person")));

        model.add(ResourceFactory.createStatement(
                ResourceFactory.createResource("http://deichman.no/person_1"),
                ResourceFactory.createProperty("http://deichman.no/a_prop"),
                ResourceFactory.createPlainLiteral("a_prop_value")));

        model.add(ResourceFactory.createStatement(
                ResourceFactory.createResource("http://deichman.no/person_1"),
                ResourceFactory.createProperty("http://deichman.no/ontology#name"),
                ResourceFactory.createPlainLiteral("personName_value")));

        model.add(ResourceFactory.createStatement(
                ResourceFactory.createResource("http://deichman.no/person_1"),
                ResourceFactory.createProperty("http://deichman.no/ontology#nationality"),
                ResourceFactory.createResource("http://deichman.no/nationality#n")));

        model.add(ResourceFactory.createStatement(
                ResourceFactory.createResource("http://deichman.no/nationality#n"),
                RDF.type,
                ResourceFactory.createResource("http://data.deichman.no/utility#Nationality")));

        model.add(ResourceFactory.createStatement(
                ResourceFactory.createResource("http://deichman.no/nationality#n"),
                ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label"),
                ResourceFactory.createLangLiteral("Norsk", "no")));

        model.add(ResourceFactory.createStatement(
                ResourceFactory.createResource("http://deichman.no/work_1"),
                RDF.type,
                ResourceFactory.createResource("http://deichman.no/ontology#Work")));

        model.add(ResourceFactory.createStatement(
                ResourceFactory.createResource("http://deichman.no/work_1"),
                ResourceFactory.createProperty("http://deichman.no/ontology#creator"),
                ResourceFactory.createResource("http://deichman.no/person_1")));

        model.add(ResourceFactory.createStatement(
                ResourceFactory.createResource("http://deichman.no/work_1"),
                ResourceFactory.createProperty("http://deichman.no/ontology#mainTitle"),
                ResourceFactory.createPlainLiteral("work_1_title")));

        model.add(ResourceFactory.createStatement(
                ResourceFactory.createResource("http://deichman.no/work_2"),
                RDF.type,
                ResourceFactory.createResource("http://deichman.no/ontology#Work")));

        model.add(ResourceFactory.createStatement(
                ResourceFactory.createResource("http://deichman.no/work_2"),
                ResourceFactory.createProperty("http://deichman.no/ontology#creator"),
                ResourceFactory.createResource("http://deichman.no/person_1")));

        model.add(ResourceFactory.createStatement(
                ResourceFactory.createResource("http://deichman.no/work_2"),
                ResourceFactory.createProperty("http://deichman.no/ontology#mainTitle"),
                ResourceFactory.createPlainLiteral("work_2_title")));

        model.add(ResourceFactory.createStatement(
                ResourceFactory.createResource("http://deichman.no/work_2"),
                ResourceFactory.createProperty("http://deichman.no/ontology#mainTitle"),
                ResourceFactory.createLangLiteral("work_2_english_title", "en")));

        model.add(ResourceFactory.createStatement(
                ResourceFactory.createResource("http://deichman.no/work_2"),
                ResourceFactory.createProperty("http://deichman.no/ontology#partTitle"),
                ResourceFactory.createPlainLiteral("work_2_part_title")));

        model.add(ResourceFactory.createStatement(
                ResourceFactory.createResource("http://deichman.no/work_2"),
                ResourceFactory.createProperty("http://deichman.no/ontology#partTitle"),
                ResourceFactory.createLangLiteral("work_2_english_part_title", "en")));

        String jsonDocument = new ModelToIndexMapper("person", BaseURI.local()).createIndexDocument(model, "http://deichman.no/person_1");

        Assert.assertThat(jsonDocument, sameJSONAs(""
                + "{"
                + "   \"person\":{"
                + "      \"uri\":\"http://deichman.no/person_1\","
                + "      \"name\":\"personName_value\","
                + "      \"nationality\":\"Norsk\","
                + "      \"work\":["
                + "         {"
                + "            \"uri\":\"http://deichman.no/work_1\","
                + "            \"mainTitle\":\"work_1_title\""
                + "         },"
                + "         {"
                + "            \"uri\":\"http://deichman.no/work_2\","
                + "            \"mainTitle\":["
                + "               \"work_2_title\","
                + "               \"work_2_english_title\""
                + "            ],"
                + "            \"partTitle\":["
                + "               \"work_2_part_title\","
                + "               \"work_2_english_part_title\""
                + "            ]"
                + "         }"
                + "      ]"
                + "   }"
                + "}").allowingAnyArrayOrdering());
    }
}
