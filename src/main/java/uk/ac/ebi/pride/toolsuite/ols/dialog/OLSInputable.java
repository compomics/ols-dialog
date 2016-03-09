package uk.ac.ebi.pride.toolsuite.ols.dialog;

import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

import java.awt.Window;
import java.util.List;

/**
 * An interface for easy interaction with the OLSDialog
 *
 * @author Harald Barsnes
 */
public interface OLSInputable {

    /**
     * Inserts the selected cv term into the parent frame or dialog. If the
     * frame (or dialog) contains more than one OLS term, the field label can be
     * used to separate between the two. Modified row is used if the cv terms
     * are in a table and one of them are altered.
     *
     * @param field the name of the field where the CV term will be inserted
     * @param selectedValue the value to search for
     * @param accession the accession number to search for
     * @param ontologyShort short name of the ontology to search in, e.g., GO or
     * MOD
     * @param ontologyLong long ontology name, e.g., Gene Ontology [GO]
     * @param modifiedRow if the CV terms is going to be inserted into a table,
     * the row number can be provided here, use -1 if inserting a new row
     * @param mappedTerm the name of the previously mapped term, can be null
     * @param metadata the metadata associated with the current term (can be
     * null or empty)
     */
    public void insertOLSResult(String field, Term selectedValue, Term accession,
            String ontologyShort, String ontologyLong, int modifiedRow, String mappedTerm, List<String> metadata);

    /**
     * Returns a reference to the frame or dialog to where the information is
     * inserted. Used by the OLS dialog to set the location relative to its
     * parent.
     *
     * @return a reference to the frame or dialog
     */
    public Window getWindow();
}
