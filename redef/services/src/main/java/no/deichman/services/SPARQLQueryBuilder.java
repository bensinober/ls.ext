package no.deichman.services;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.update.UpdateAction;

import no.deichman.services.uridefaults.BaseURI;
import no.deichman.services.uridefaults.BaseURIDefault;

public class SPARQLQueryBuilder {
    private BaseURI baseURI;

    SPARQLQueryBuilder(){
        baseURI = new BaseURIDefault();
    }

    public SPARQLQueryBuilder(BaseURI base){
        baseURI = base;
    }

    public Query dumpModel() {
    	String q = "DESCRIBE * WHERE {?s ?p ?o}";
    	return QueryFactory.create(q);
    }

	public Query getGetWorkByIdQuery(String id) {
        String queryString = "DESCRIBE <" + id + ">";
        return QueryFactory.create(queryString);
    }

    public String getUpdateWorkQueryString(Model work) {
        StringWriter sw = new StringWriter();
        RDFDataMgr.write(sw, work, Lang.NTRIPLES);
        String data = sw.toString();
        return "INSERT DATA {\n"
                + "\n"
                + data
                + "\n"
                + "}";
    }

    private String renameWorkResource(String newURI) {
        return "DELETE {\n"
                + " ?s ?p ?o .\n"
                + "}\n"
                + "INSERT {\n"
                + " <" + newURI + "> ?p ?o .\n"
                + "}\n"
                + "WHERE {\n"
                + " ?s ?p ?o .\n"
                + "}\n";
    }

    public String getCreateWorkQueryString(String id, Model work) {

        UpdateAction.parseExecute(renameWorkResource(id), work);
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
        String q = "PREFIX deichman: <" + baseURI.getOntologyURI() + ">\n"
                + "CONSTRUCT {\n"
                + "  <" + id + "> deichman:hasEdition"
                + "( ["
                + "    a deichman:Item ;"
                + "    deichman:location ?location ;"
                + "    deichman:status ?status"
                + "  ])"
                + "} WHERE { \n"
                + "  ?uri a deichman:Item ;\n"
                + "    deichman:location ?location;\n"
                + "    deichman:status ?status .\n"
                + "}";
       return QueryFactory.create(q);
    }

    public Query checkIfResourceExists(String uri) {
        String q = "ASK {<" + uri + "> ?p ?o}";
        return QueryFactory.create(q);
    }

	public Query checkIfResourceExistsInGraph(String uri, String graph) {
        String q = "ASK {GRAPH <" + graph + "> {<" + uri + "> ?p ?o}}";
        return QueryFactory.create(q);
	}

	public Query checkIfStatementExists(Statement statement) throws UnsupportedEncodingException {
        String triple = statementToN3(statement);
        String q = "ASK {" + triple + "}" ;
		return QueryFactory.create(q);
    }

    public Query checkIfStatementExistsInGraph(Statement statement, String graph) throws UnsupportedEncodingException {
        String triple = statementToN3(statement);
        String q = "ASK {GRAPH <" + graph + "> {" + triple + "}}" ;
		return QueryFactory.create(q);
    }

    private String statementToN3 (Statement statement) throws UnsupportedEncodingException {
        Model tempExists = ModelFactory.createDefaultModel();
        tempExists.add(statement);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        RDFDataMgr.write(baos, tempExists, Lang.NTRIPLES);
        return baos.toString("UTF-8");
    }

	public String updateAdd(Model inputModel) {
        StringWriter sw = new StringWriter();
        RDFDataMgr.write(sw, inputModel, Lang.NTRIPLES);
        String data = sw.toString();

        return "INSERT DATA {\n"
                + "\n"
                + data
                + "\n"
                + "}";
	}

	public String updateAddToGraph(Model inputModel, String graph) {
        StringWriter sw = new StringWriter();
        RDFDataMgr.write(sw, inputModel, Lang.NTRIPLES);
        String data = sw.toString();

        String q = "INSERT DATA {\n"
        		+ "GRAPH <" + graph + "> {\n"
                + "\n"
                + data
                + "\n"
                + "}\n"
                + "}";
       return q;
    }

	public String updateDelete(Model inputModel) {
        StringWriter sw = new StringWriter();
        RDFDataMgr.write(sw, inputModel, Lang.NTRIPLES);
        String data = sw.toString();

        return "DELETE {\n"
        		+ data
        		+ "} WHERE {\n"
                + "\n"
                + data
                + "\n"
                + "}";
	}

	public String updateDeleteFromGraph(Model inputModel, String graph) {
        StringWriter sw = new StringWriter();
        RDFDataMgr.write(sw, inputModel, Lang.NTRIPLES);
        String data = sw.toString();

        return "DELETE DATA { GRAPH <" + graph + "> {\n"
        + "\n"
        + data
        + "\n"
        + "}\n"
        + "}";
	}

	public String askIfGraphExists(String graph) {
        String q = "ASK {GRAPH <" + graph + "> {}}" ;
		return q;
	}

}
