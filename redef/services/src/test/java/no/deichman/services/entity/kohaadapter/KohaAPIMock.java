package no.deichman.services.entity.kohaadapter;

import com.github.restdriver.clientdriver.ClientDriverRequest;
import com.github.restdriver.clientdriver.ClientDriverRule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import no.deichman.services.circulation.ExpandedRecord;
import no.deichman.services.circulation.Item;
import no.deichman.services.circulation.Record;
import no.deichman.services.testutil.PortSelector;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Rule;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.DELETE;
import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.GET;
import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.POST;
import static com.github.restdriver.clientdriver.RestClientDriver.giveEmptyResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.OK;
import static org.joda.time.LocalDateTime.now;

/**
 * Responsibility: Encapsulate Koha API endpoint mocking.
 */
public final class KohaAPIMock {

    private static final int TWENTY_EIGHT = 28;
    private static final int MINUS_TWENTY_SEVEN = -27;
    private static final int ORIGIN = 2000;
    private static final int BOUND = 9999;
    private final int clientdriverPort = PortSelector.randomFree();
    private static final DateFormat KOHA_DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateFormat KOHA_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final Gson GSON = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

    @Rule
    private final ClientDriverRule clientDriver = new ClientDriverRule(clientdriverPort);

    public int getPort() {
        return clientdriverPort;
    }

    public void addLoginExpectation() {
        clientDriver.addExpectation(
                onRequestTo("/api/v1/auth/session")
                        .withMethod(POST),
                giveResponse("{}", "application/json").withStatus(CREATED.getStatusCode())
                        .withHeader(HttpHeaders.SET_COOKIE, KohaAdapterImpl.SESSION_COOKIE_KEY + "=huh"));
    }

    public void addGetBiblioExpandedExpectation(String biblioId, String responseJSON) throws IOException {
        clientDriver.addExpectation(
                onRequestTo("/api/v1/biblios/" + biblioId + "/expanded")
                        .withMethod(ClientDriverRequest.Method.GET)
                        .withHeader(HttpHeaders.COOKIE, Pattern.compile(".*CGISESSID=huh.*")),
                giveResponse(
                        responseJSON,
                        "application/json; charset=utf8"));
    }

    public void addGetBiblioExpectation(String biblioId, String responseJSON) throws IOException {
        clientDriver.addExpectation(
                onRequestTo("/api/v1/biblios/" + biblioId)
                        .withMethod(ClientDriverRequest.Method.GET)
                        .withHeader(HttpHeaders.COOKIE, Pattern.compile(".*CGISESSID=huh.*")),
                giveResponse(
                        responseJSON,
                        "application/json; charset=utf8"));
    }

    public void addCreateNewBiblioExpectation(String returnedBiblioId) {
        String responseJSON = ""
                + "{\n"
                + "    \"biblionumber\": \"" + returnedBiblioId + "\", \n"
                + "    \"items\": \"\"\n"
                + "}\n";

        clientDriver.addExpectation(
                onRequestTo("/api/v1/biblios")
                        .withMethod(POST)
                        .withHeader(HttpHeaders.COOKIE, Pattern.compile(".*CGISESSID=huh.*")),
                giveResponse(responseJSON, "application/json; charset=utf8")
                        .withHeader("Location", "http://localhost:" + clientdriverPort + "/api/v1/biblios/" + returnedBiblioId)
                        .withStatus(CREATED.getStatusCode()));
    }

    public void addCreateNewBiblioFromMarcXmlExpectation(String biblioId, String expectedPayload) {
        String responseJSON = ""
                + "{\n"
                + "    \"biblionumber\": \"" + biblioId + "\", \n"
                + "    \"items\": \"\"\n"
                + "}\n";
        clientDriver.addExpectation(
                onRequestTo("/api/v1/biblios")
                        .withMethod(POST)
                        .withHeader(HttpHeaders.COOKIE, Pattern.compile(".*CGISESSID=huh.*")),
                giveResponse(responseJSON, "application/json; charset=utf8")
                        .withHeader("Location", "http://localhost:" + clientdriverPort + "/api/v1/biblios/" + biblioId)
                        .withStatus(CREATED.getStatusCode()));
    }

    public void addDeleteBibloExpectation(String biblioId) {
        clientDriver.addExpectation(
                onRequestTo("/api/v1/biblios/" + biblioId)
                        .withMethod(DELETE)
                        .withHeader(HttpHeaders.COOKIE, Pattern.compile(".*CGISESSID=huh.*")),
                giveEmptyResponse().withStatus(OK.getStatusCode()));
    }

    public void addGetBiblioFromItemNumberExpectation(String itemNumber) {
        clientDriver.addExpectation(onRequestTo("/api/v1/items/" + itemNumber + "/biblio")
                        .withMethod(GET)
                        .withHeader(HttpHeaders.COOKIE, Pattern.compile(".*CGISESSID=huh.*")),
                giveResponse(generateBiblioResponseJson(itemNumber), "application/json; charset=utf8")
        );
    }

    public void addGetBiblioFromItemNumberExpectation(String itemNumber, String recordNumber, String title) {
        String[] items = {itemNumber};
        String responseJson = generateBiblioResponseJson(recordNumber, title, false);
        clientDriver.addExpectation(onRequestTo("/api/v1/items/" + itemNumber + "/biblio")
                        .withMethod(GET)
                        .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                        .withHeader(HttpHeaders.COOKIE, Pattern.compile(".*CGISESSID=huh.*")),
                giveResponse(responseJson, "application/json; charset=utf8")
        );
    }

    private String generateRandomID() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(ORIGIN, BOUND));
    }

    public String generateBiblioResponseJson(String recordNumber, String title, boolean expiditedUser) {
        Record record = new Record();
        record.setTitle(title);
        record.setId(recordNumber);
        record.setBehindExpiditedUser(expiditedUser);

        return GSON.toJson(record, Record.class);

    }

    public String generateBiblioResponseJson(String recordNumber) {
        return String.format("{\n"
                        + "  \"itemnotes_nonpublic\": null,\n"
                        + "  \"replacementprice\": \"450.00\",\n"
                        + "  \"isbn\": \"978-00-00-00000-0 | 00-00-00000-0\",\n"
                        + "  \"unititle\": null,\n"
                        + "  \"datecreated\": \"2016-10-07\",\n"
                        + "  \"ccode\": \"voksen\",\n"
                        + "  \"publicationyear\": null,\n"
                        + "  \"coded_location_qualifier\": \"vo\",\n"
                        + "  \"more_subfields_xml\": null,\n"
                        + "  \"volumedesc\": null,\n"
                        + "  \"permanent_location\": \"m\",\n"
                        + "  \"itemtype\": null,\n"
                        + "  \"new_status\": null,\n"
                        + "  \"volumedate\": null,\n"
                        + "  \"serial\": null,\n"
                        + "  \"collectiontitle\": null,\n"
                        + "  \"collectionissn\": null,\n"
                        + "  \"agerestriction\": null,\n"
                        + "  \"enumchron\": null,\n"
                        + "  \"illus\": null,\n"
                        + "  \"dateaccessioned\": \"2016-10-07\",\n"
                        + "  \"place\": \"Oslo\",\n"
                        + "  \"booksellerid\": null,\n"
                        + "  \"issn\": null,\n"
                        + "  \"materials\": null,\n"
                        + "  \"uri\": null,\n"
                        + "  \"frameworkcode\": \"DCHM\",\n"
                        + "  \"publishercode\": null,\n"
                        + "  \"withdrawn\": \"0\",\n"
                        + "  \"cn_sort\": \"\",\n"
                        + "  \"cn_source\": null,\n"
                        + "  \"volume\": null,\n"
                        + "  \"editionresponsibility\": null,\n"
                        + "  \"holdingbranch\": \"hutl\",\n"
                        + "  \"marcxml\": \"<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\"?>\\n<record><tag>Words fail me<\\/tag><\\/record>\\n\",\n"
                        + "  \"damaged\": \"0\",\n"
                        + "  \"author\": \"Exampleson, Gøril\",\n"
                        + "  \"title\": \"Tre mannfolk i en ballongkurv med gyngehest og parasoll\",\n"
                        + "  \"itype\": \"BOK\",\n"
                        + "  \"cn_suffix\": null,\n"
                        + "  \"copynumber\": \"11\",\n"
                        + "  \"totalissues\": null,\n"
                        + "  \"abstract\": null,\n"
                        + "  \"cn_item\": null,\n"
                        + "  \"itemnumber\": \"%1$s\",\n"
                        + "  \"onloan\": \"%2$s\",\n"
                        + "  \"copyrightdate\": \"2005\",\n"
                        + "  \"url\": null,\n"
                        + "  \"datelastborrowed\": \"%2$s\",\n"
                        + "  \"itemnotes\": null,\n"
                        + "  \"biblionumber\": \"%3$s\",\n"
                        + "  \"size\": null,\n"
                        + "  \"notes\": null,\n"
                        + "  \"restricted\": null,\n"
                        + "  \"stocknumber\": null,\n"
                        + "  \"itemlost\": \"12\",\n"
                        + "  \"stack\": null,\n"
                        + "  \"withdrawn_on\": null,\n"
                        + "  \"datelastseen\": \"%4$s\",\n"
                        + "  \"replacementpricedate\": \"2016-10-07\",\n"
                        + "  \"issues\": \"5\",\n"
                        + "  \"renewals\": \"2\",\n"
                        + "  \"seriestitle\": null,\n"
                        + "  \"collectionvolume\": null,\n"
                        + "  \"reserves\": null,\n"
                        + "  \"cn_class\": null,\n"
                        + "  \"number\": null,\n"
                        + "  \"paidfor\": null,\n"
                        + "  \"location\": \"m\",\n"
                        + "  \"pages\": null,\n"
                        + "  \"itemcallnumber\": \"D Pla\",\n"
                        + "  \"itemlost_on\": null,\n"
                        + "  \"lccn\": null,\n"
                        + "  \"ean\": null,\n"
                        + "  \"notforloan\": \"0\",\n"
                        + "  \"timestamp\": \"2016-10-08 23:09:14\",\n"
                        + "  \"biblioitemnumber\": \"%3$s\",\n"
                        + "  \"editionstatement\": null,\n"
                        + "  \"price\": null,\n"
                        + "  \"barcode\": \"03010000000000\",\n"
                        + "  \"homebranch\": \"hutl\"\n"
                        + "}", generateRandomID(),
                KOHA_DATE_FORMAT.format(getDate(MINUS_TWENTY_SEVEN)),
                recordNumber,
                KOHA_DATE_FORMAT.format(getDate(0)));
    }

    private Date getDate(int addDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now().toDate());
        calendar.add(Calendar.DAY_OF_YEAR, addDays);

        return calendar.getTime();
    }

    public String generateBiblio(String recordNumber, String type) {
        return GSON.toJson(RandomRecord.populateRandom(recordNumber, false, 1));
    }

    public String generateBiblioExpanded(String recordNumber, String type, boolean zeroUser, int numberOfItems, int numberOfHolds, boolean oneLoanedToMe) throws Exception {
        Record record = RandomRecord.populateRandom(recordNumber, zeroUser, numberOfHolds);
        ExpandedRecord apiResponse = new ExpandedRecord();
        apiResponse.setLoanRecord(record);
        List<Item> items = new ArrayList<>();
        items.addAll(createItemsList(numberOfHolds, recordNumber, true, type));
        int extraItems = numberOfItems - numberOfHolds;
        if (extraItems > 0) {
            items.addAll(createItemsList(extraItems, recordNumber, false, type));
        } else if (extraItems < 0) {
            throw new Exception("You have specified more holds than there are items");
        }
        apiResponse.setItems(items);
        return GSON.toJson(apiResponse);
    }

    public String generateBiblioExpanded(String recordNumber, String type, boolean zeroUser, int numberOfItems, int numberOfHolds) throws Exception {
        return generateBiblioExpanded(recordNumber, type, zeroUser, numberOfItems, numberOfHolds, false);
    }

    public String generateBiblioExpandedWithOneItemLoanedToMe(String recordNumber, String type, boolean zeroUser, int numberOfItems, int numberOfHolds) throws Exception {
        return generateBiblioExpanded(recordNumber, type, zeroUser, numberOfItems, numberOfHolds, true);
    }

    private List<Item> createItemsList(int numberOfItems, String recordNumber, boolean onLoan, String type) {

        String returnDate = null;
        if (onLoan) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
            returnDate = dateTimeFormatter.print(new DateTime().plusDays(28));
        }
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < numberOfItems; i++) {
            Item item = ItemStubber.generateRandom(recordNumber, type, returnDate, 1);
            items.add(item);
        }
        return items;
    }

}
