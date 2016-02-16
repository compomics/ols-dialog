/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uib.olsdialog;

import java.io.IOException;
import java.util.Map;

/**
 *
 * @author Niels Hulstaert
 */
public interface OlsService {

    /**
     * Get the available ontologies as a map (key: the ontology label; value:
     * the ontology instance).
     *
     * @return
     * @throws IOException
     */
    Map<String, Ontology> getAllOntologies() throws IOException;

    OntologyTerm getTermById(Ontology ontology, String termId) throws IOException;

}
