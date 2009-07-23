package no.uib.olsdialog;

import java.awt.Window;

/**
 * An interface for easy interaction with the OLSDialog
 * 
 * @author Harald Barsnes
 * 
 * Created March 2008
 */
public interface OLSInputable {

    /**
     * Inserts the selected cv term into the parent frame or dialog. If the 
     * frame (or dialog) contains more than one OLS term, the field label 
     * can be used to seperate between the two. Modified row is used if 
     * the cv terms are in a table and one of them are altered.
     * 
     * @param field
     * @param selectedValue
     * @param accession
     * @param ontologyShort
     * @param ontologyLong
     * @param modifiedRow
     * @param mappedTerm 
     */
    public void insertOLSResult(String field, String selectedValue, String accession,
            String ontologyShort, String ontologyLong, int modifiedRow, String mappedTerm);

    /**
     * Returns a reference to the frame or dialog to where the information 
     * is inserted. Used by the OLS dialog to set the location relative to 
     * its parent.
     * 
     * @return a reference to the frame or dialog
     */
    public Window getWindow();
}
