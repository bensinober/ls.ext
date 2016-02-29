package no.deichman.services.search;

import no.deichman.services.rdf.RDFModelUtil;
import no.deichman.services.uridefaults.BaseURI;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Responsibility: unit test ModelToIndexMapper. See *ModelToIndexMapperTest files for tests of specific types.
 */
public class ModelToIndexMapperTest {
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void should_throw_runtime_exception_if_unable_to_load_query() {
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("dummy");
        new ModelToIndexMapper("dummy");
    }

    @Test
    public void should_throw_runtime_exception_if_empty_massaged_model() {
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Dummy_URI_1");
        new ModelToIndexMapper("work").createIndexDocument(ModelFactory.createDefaultModel(), "Dummy_URI_1");
    }

    @Test
    public void should_throw_runtime_exception_if_framing_fails() throws IllegalAccessException, NoSuchFieldException {
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Dummy_URI_2");
        Model model = RDFModelUtil.modelFrom("{"
                + "  \"@id\" : \"http://deichman.no/work_1\","
                + "  \"@type\" : \"deichman:Work\","
                + "  \"deichman:mainTitle\" : \"Test\","
                + "  \"@context\" : {"
                + "    \"deichman\" : \"http://deichman.no/ontology#\","
                + "    \"rdfs\" : \"http://www.w3.org/2000/01/rdf-schema#\""
                + "  }"
                + "}", Lang.JSONLD);
        ModelToIndexMapper modelToIndexMapper = new ModelToIndexMapper("work", BaseURI.local());
        Class<?> clazz = modelToIndexMapper.getClass();
        Field field = clazz.getDeclaredField("context");
        field.setAccessible(true);
        field.set(modelToIndexMapper, new HashMap());
        modelToIndexMapper.createIndexDocument(model, "Dummy_URI_2");
    }
}
