package no.deichman.services.rest;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import no.deichman.services.kohaadapter.KohaAdapter;
import no.deichman.services.repository.InMemoryRepository;
import no.deichman.services.service.EntityServiceImpl;
import no.deichman.services.uridefaults.BaseURI;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static no.deichman.services.rest.utils.TestJSON.assertValidJSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PublicationResourceTest {

    private static final String A_BIBLIO_ID = "1234";

    private PublicationResource resource;

    private String createTestPublicationJSON(String id) {
        return "{\"@context\": "
                + "{\"dcterms\": \"http://purl.org/dc/terms/\","
                + "\"deichman\": \"http://deichman.no/ontology#\"},"
                + "\"@graph\": "
                + "{\"@id\": \"http://deichman.no/publication/" + id + "\","
                + "\"@type\": \"deichman:Publication\","
                + "\"dcterms:identifier\":\"" + id + "\"}}";
    }

    @Mock
    private KohaAdapter mockKohaAdapter;

    @Before
    public void setUp() throws Exception {
        BaseURI baseURI = BaseURI.local();
        EntityServiceImpl service = new EntityServiceImpl(baseURI, new InMemoryRepository(), mockKohaAdapter);
        resource = new PublicationResource(baseURI, service);
    }

    @Test
    public void should_have_default_constructor() {
        assertNotNull(new PublicationResource());
    }

    @Test
    public void should_return_201_when_publication_created() throws Exception{
        when(mockKohaAdapter.getNewBiblio()).thenReturn(A_BIBLIO_ID);
        Response result = resource.createPublication(createTestPublicationJSON("publication_SHOULD_EXIST"));
        assertNull(result.getEntity());
        assertEquals(CREATED.getStatusCode(), result.getStatus());
    }

    @Test
    public void should_return_the_new_publication() throws Exception{
        when(mockKohaAdapter.getNewBiblio()).thenReturn(A_BIBLIO_ID);
        Response createResponse = resource.createPublication(createTestPublicationJSON("publication_SHOULD_EXIST"));

        String publicationId = createResponse.getHeaderString("Location").replaceAll("http://deichman.no/publication/", "");

        Response result = resource.getPublicationJSON(publicationId);

        assertNotNull(result);
        assertEquals(CREATED.getStatusCode(), createResponse.getStatus());
        assertEquals(OK.getStatusCode(), result.getStatus());
        assertTrue(result.getEntity().toString().contains("\"deichman:recordID\""));
        assertValidJSON(result.getEntity().toString());
    }

    @Test
    public void test_delete_publication() throws Exception{
        when(mockKohaAdapter.getNewBiblio()).thenReturn(A_BIBLIO_ID);
        Response createResponse = resource.createPublication(createTestPublicationJSON("publication_SHOULD_BE_PATCHABLE"));
        String publicationId = createResponse.getHeaderString("Location").replaceAll("http://deichman.no/publication/", "");
        Response response = resource.deletePublication(publicationId);
        assertEquals(NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test(expected = BadRequestException.class)
    public void patch_should_return_status_400() throws Exception {
        when(mockKohaAdapter.getNewBiblio()).thenReturn(A_BIBLIO_ID);
        Response result = resource.createPublication(createTestPublicationJSON("publication_SHOULD_BE_PATCHABLE"));
        String publicationId = result.getLocation().getPath().substring("/publication/".length());
        String patchData = "{}";
        resource.patchPublication(publicationId, patchData);
    }

    @Test
    public void patched_publication_should_persist_changes() throws Exception {
        when(mockKohaAdapter.getNewBiblio()).thenReturn(A_BIBLIO_ID);
        String publication = createTestPublicationJSON("publication_SHOULD_BE_PATCHABLE");
        Response result = resource.createPublication(publication);
        String publicationId = result.getLocation().getPath().substring("/publication/".length());
        String patchData = "{"
                + "\"op\": \"add\","
                + "\"s\": \"" + result.getLocation().toString() + "\","
                + "\"p\": \"http://deichman.no/ontology#color\","
                + "\"o\": {"
                + "\"value\": \"red\""
                + "}"
                + "}";
        Response patchResponse = resource.patchPublication(publicationId,patchData);
        Model testModel = ModelFactory.createDefaultModel();
        String response = patchResponse.getEntity().toString();
        InputStream in = new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8));
        RDFDataMgr.read(testModel, in, Lang.JSONLD);
        Statement s = ResourceFactory.createStatement(
                ResourceFactory.createResource(result.getLocation().toString()),
                ResourceFactory.createProperty("http://deichman.no/ontology#color"),
                ResourceFactory.createPlainLiteral("red"));
        assertTrue(testModel.contains(s));
    }

    @Test(expected = NotFoundException.class)
    public void patching_a_non_existing_resource_should_return_404() throws Exception {
        resource.patchPublication("a_missing_publication1234", "{}");
    }

}
