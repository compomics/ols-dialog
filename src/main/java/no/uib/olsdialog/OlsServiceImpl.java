/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uib.olsdialog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Niels Hulstaert
 */
public class OlsServiceImpl implements OlsService {

    private static final String GET_ALL_ONTOLOGIES_FIRST_PAGE = "http://www.ebi.ac.uk/ols/beta/api/ontologies?page=0&size=20";
    private static final String LINKS = "_links";
    private static final String EMBEDDED = "_embedded";
    private static final String NEXT_LINK = "next";
    private static final String LAST_LINK = "last";
    private static final String HREF = "href";
    private static final String ONTOLOGIES = "ontologies";
    private static final String CONFIG = "config";
    private static final String CONFIG_TITLE = "title";
    private static final String CONFIG_PREFIX = "preferredPrefix";
    private static final String CONFIG_IRI = "baseUris";
    private static final String OLS_BASE_URL = "http://www.ebi.ac.uk/ols/beta/api/ontologies/";

    /**
     * The Spring RestTemplate instance for accessing the OLS rest API.
     */
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Map<String, Ontology> getAllOntologies() throws IOException {
        Map<String, Ontology> ontologies = new HashMap<>();

        String currentPage = GET_ALL_ONTOLOGIES_FIRST_PAGE;
        String lastPage = "";
        while (true) {
            //get the current page
            ResponseEntity<String> response = restTemplate.getForEntity(currentPage, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode responseBody = mapper.readTree(response.getBody());

            //get the ontologies of the page
            JsonNode ontologiesNode = responseBody.get(EMBEDDED).get(ONTOLOGIES);
            Iterator<JsonNode> ontologyIterator = ontologiesNode.iterator();
            while (ontologyIterator.hasNext()) {
                JsonNode ontologyConfigNode = ontologyIterator.next().get(CONFIG);
                if (ontologyConfigNode.has(CONFIG_PREFIX) && ontologyConfigNode.has(CONFIG_TITLE) && ontologyConfigNode.has(CONFIG_IRI)) {
                    Iterator<JsonNode> uriIterator = ontologyConfigNode.get(CONFIG_IRI).elements();
                    if (uriIterator.hasNext()) {
                        String label = ontologyConfigNode.get(CONFIG_PREFIX).asText();
                        ontologies.put(label, new Ontology(label, ontologyConfigNode.get(CONFIG_TITLE).asText(), uriIterator.next().asText()));
                    }
                }
            }

            //get the links to the next and the last page
            if (responseBody.get(LINKS).has(NEXT_LINK)) {
                currentPage = responseBody.get(LINKS).get(NEXT_LINK).get(HREF).asText();
            } else {
                break;
            }
            if (lastPage.isEmpty()) {
                lastPage = responseBody.get(LINKS).get(LAST_LINK).get(HREF).asText();
            }
        }

        return ontologies;
    }

    @Override
    public OntologyTerm getTermById(Ontology ontology, String termId) {
        //get the current page
        ResponseEntity<String> response = restTemplate.getForEntity(OLS_BASE_URL + "{ontology}/terms/{iri}", String.class, ontology.getShortName(), ontology.getBaseUri() + termId);

        return null;
    }

    public static void main(String[] args) throws IOException {
        OlsServiceImpl olsServiceImpl = new OlsServiceImpl();
        olsServiceImpl.getAllOntologies();
    }

}
