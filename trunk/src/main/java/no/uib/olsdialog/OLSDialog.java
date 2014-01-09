package no.uib.olsdialog;

import no.uib.olsdialog.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.xml.rpc.ServiceException;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.*;
import java.util.List;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import uk.ac.ebi.ols.soap.Query;
import uk.ac.ebi.ols.soap.QueryService;
import uk.ac.ebi.ols.soap.QueryServiceLocator;
import uk.ac.ebi.ols.soap.model.DataHolder;
import no.uib.jsparklines.extra.HtmlLinksRenderer;

/**
 * A dialog for interacting with the Ontology Lookup Service OLS
 * (http://www.ebi.ac.uk/ontology-lookup).
 *
 * @author Harald Barsnes
 *
 * Created: March 2008 Revised: July 2009
 */
public class OLSDialog extends javax.swing.JDialog {

    /**
     * Set to true of debug output is wanted.
     */
    public static final boolean debug = false;
    /**
     * The name of the field to insert the results into.
     */
    private String field;
    /**
     * The currently selected ontology.
     */
    private String selectedOntology;
    /**
     * The index of the modified row in the table.
     */
    private int modifiedRow = -1;
    /**
     * The OLSInputable parent.
     */
    private OLSInputable olsInputable;
    /**
     * The mapped term.
     */
    private String mappedTerm;
    /**
     * List of preselected ontologies.
     */
    private Map<String, List<String>> preselectedOntologies;
    /**
     * List of preselected names to ids.
     */
    private Map<String, String> preselectedNames2Ids;
    /**
     * Counts the number of times the users has pressed a key on the keyboard in
     * the search field.
     */
    private int keyPressedCounter = 0;
    /**
     * The time to wait between keys typed before updating the search.
     */
    private int waitingTime = 1000;
    /**
     * The search is only performed if a certain amount of characters are
     * inserted.
     */
    private final int MINIMUM_WORD_LENGTH = 3;
    /**
     * The default error message used when connecting to OLS fails.
     */
    private String defaultOlsConnectionFailureErrorMessage
            = "An error occured when trying to contact the OLS. Make sure that\n"
            + "you are online. Also check your firewall (and proxy) settings.\n\n"
            + "See the Troubleshooting section at the OLS Dialog home page\n"
            + "for details: http://ols-dialog.googlecode.com.";
    /**
     * Used for term name searches.
     */
    public static final Integer OLS_DIALOG_TERM_NAME_SEARCH = 0;
    /**
     * Used for term id searches.
     */
    public static final Integer OLS_DIALOG_TERM_ID_SEARCH = 1;
    /**
     * Used for PSI modification mass searches.
     */
    public static final Integer OLS_DIALOG_PSI_MOD_MASS_SEARCH = 2;
    /**
     * Used for ontology browsing.
     */
    public static final Integer OLS_DIALOG_BROWSE_ONTOLOGY = 3;
    /**
     * The OLS connection.
     */
    private static Query olsConnection;
    /**
     * The OLS tree browser.
     */
    private TreeBrowser treeBrowser;
    /**
     * The currently selected ontology accession number in the browse tab.
     */
    private String currentlySelectedBrowseOntologyAccessionNumber = null;
    /**
     * The currently selected term name inthe accession number tab.
     */
    private String currentlySelectedTermNameSearchAccessionNumber = null;
    /**
     * The currently selected accession number in the term id tab.
     */
    private String currentlySelectedTermIdSearchAccessionNumber = null;
    /**
     * The currently selected accession number in the mass search tab.
     */
    private String currentlySelectedMassSearchAccessionNumber = null;
    /**
     * The last selected ontololgy.
     */
    private String lastSelectedOntology = null;
    /**
     * The maximum tool tip length before splitting over multiple lines.
     */
    private final int MAX_TOOL_TIP_LENGTH = 40;
    /**
     * The metadata map.
     */
    private Map<String, String> metadata;
    /**
     * The color to use for the HTML tags for the selected rows, in HTML color
     * code.
     */
    private String selectedRowHtmlTagFontColor = "#FFFFFF";
    /**
     * The color to use for the HTML tags for the rows that are not selected, in
     * HTML color code.
     */
    private String notSelectedRowHtmlTagFontColor = "#0101DF";

    /**
     * Opens a dialog that lets you search for terms using the OLS.
     *
     * @param parent the parent JFrame
     * @param olsInputable a reference to the frame using the OLS Dialog
     * @param modal
     * @param field the name of the field to insert the results into
     * @param selectedOntology the name of the ontology to search in, e.g., "GO"
     * or "MOD".
     * @param term the term to search for
     */
    public OLSDialog(JFrame parent, OLSInputable olsInputable, boolean modal, String field,
            String selectedOntology, String term) {
        this(parent, olsInputable, modal, field, selectedOntology, -1, term, null, null, OLS_DIALOG_TERM_NAME_SEARCH, null);
    }

    /**
     * Opens a dialog that lets you search for terms using the OLS.
     *
     * @param parent the parent JFrame
     * @param olsInputable a reference to the frame using the OLS Dialog
     * @param modal
     * @param field the name of the field to insert the results into
     * @param selectedOntology the name of the ontology to search in, e.g., "GO"
     * or "MOD". It also accepts the ontology title, e.g. "PSI Mass Spectrometry
     * Ontology [MS]" or ""PSI Mass Spectrometry Ontology [MS] / source"
     * @param term the term to search for
     * @param preselectedOntologies Default ontologies to display. Key: ontology
     * name, e.g. "MS" or "GO". Value: parent ontologies, e.g. "MS:1000458",
     * "null" (no parent ontology preselected)
     */
    public OLSDialog(JFrame parent, OLSInputable olsInputable, boolean modal, String field,
            String selectedOntology, String term, Map<String, List<String>> preselectedOntologies) {
        this(parent, olsInputable, modal, field, selectedOntology, -1, term, null, null, OLS_DIALOG_TERM_NAME_SEARCH, preselectedOntologies);
    }

    /**
     * Opens a dialog that lets you search for terms using the OLS.
     *
     * @param parent the parent JDialog
     * @param olsInputable a reference to the frame using the OLS Dialog
     * @param modal
     * @param field the name of the field to insert the results into
     * @param selectedOntology the name of the ontology to search in, e.g., "GO"
     * or "MOD".
     * @param term the term to search for
     */
    public OLSDialog(JDialog parent, OLSInputable olsInputable, boolean modal, String field,
            String selectedOntology, String term) {
        this(parent, olsInputable, modal, field, selectedOntology, -1, term, null, null, OLS_DIALOG_TERM_NAME_SEARCH, null);
    }

    /**
     * Opens a dialog that lets you search for terms using the OLS.
     *
     * @param parent the parent JDialog
     * @param olsInputable a reference to the frame using the OLS Dialog
     * @param modal
     * @param field the name of the field to insert the results into
     * @param selectedOntology the name of the ontology to search in, e.g., "GO"
     * or "MOD".
     * @param term the term to search for
     * @param preselectedOntologies Default ontologies to display. Key: ontology
     * name, e.g. "MS" or "GO". Value: parent ontologies, e.g. "MS:1000458",
     * "null" (no parent ontology preselected)
     */
    public OLSDialog(JDialog parent, OLSInputable olsInputable, boolean modal, String field,
            String selectedOntology, String term, Map<String, List<String>> preselectedOntologies) {
        this(parent, olsInputable, modal, field, selectedOntology, -1, term, null, null, OLS_DIALOG_TERM_NAME_SEARCH, preselectedOntologies);
    }

    /**
     * Opens a dialog that lets you search for terms using the OLS.
     *
     * @param parent the parent JFrame
     * @param olsInputable a reference to the frame using the OLS Dialog
     * @param modal
     * @param field the name of the field to insert the results into
     * @param selectedOntology the name of the ontology to search in, e.g., "GO"
     * or "MOD".
     * @param modifiedRow the row to modify, use -1 if adding a new row
     * @param term the term to search for
     */
    public OLSDialog(JFrame parent, OLSInputable olsInputable, boolean modal, String field,
            String selectedOntology, int modifiedRow, String term) {
        this(parent, olsInputable, modal, field, selectedOntology, modifiedRow, term, null, null, OLS_DIALOG_TERM_NAME_SEARCH, null);
    }

    /**
     * Opens a dialog that lets you search for terms using the OLS.
     *
     * @param parent the parent JFrame
     * @param olsInputable a reference to the frame using the OLS Dialog
     * @param modal
     * @param field the name of the field to insert the results into
     * @param selectedOntology the name of the ontology to search in, e.g., "GO"
     * or "MOD".
     * @param modifiedRow the row to modify, use -1 if adding a new row
     * @param term the term to search for
     * @param preselectedOntologies Default ontologies to display. Key: ontology
     * name, e.g. "MS" or "GO". Value: parent ontologies, e.g. "MS:1000458",
     * "null" (no parent ontology preselected)
     */
    public OLSDialog(JFrame parent, OLSInputable olsInputable, boolean modal, String field,
            String selectedOntology, int modifiedRow, String term, Map<String, List<String>> preselectedOntologies) {
        this(parent, olsInputable, modal, field, selectedOntology, modifiedRow, term, null, null, OLS_DIALOG_TERM_NAME_SEARCH, preselectedOntologies);
    }

    /**
     * Opens a dialog that lets you search for terms using the OLS.
     *
     * @param parent the parent JDialog
     * @param olsInputable a reference to the frame using the OLS Dialog
     * @param modal
     * @param field the name of the field to insert the results into
     * @param selectedOntology the name of the ontology to search in, e.g., "GO"
     * or "MOD".
     * @param modifiedRow the row to modify, use -1 if adding a new row
     * @param term the term to search for
     */
    public OLSDialog(JDialog parent, OLSInputable olsInputable, boolean modal, String field,
            String selectedOntology, int modifiedRow, String term) {
        this(parent, olsInputable, modal, field, selectedOntology, modifiedRow, term, null, null, OLS_DIALOG_TERM_NAME_SEARCH, null);
    }

    /**
     * Opens a dialog that lets you search for terms using the OLS.
     *
     * @param parent the parent JDialog
     * @param olsInputable a reference to the frame using the OLS Dialog
     * @param modal
     * @param field the name of the field to insert the results into
     * @param selectedOntology the name of the ontology to search in, e.g., "GO"
     * or "MOD".
     * @param modifiedRow the row to modify, use -1 if adding a new row
     * @param term the term to search for
     * @param preselectedOntologies Default ontologies to display. Key: ontology
     * name, e.g. "MS" or "GO". Value: parent ontologies, e.g. "MS:1000458",
     * "null" (no parent ontology preselected)
     */
    public OLSDialog(JDialog parent, OLSInputable olsInputable, boolean modal, String field,
            String selectedOntology, int modifiedRow, String term, Map<String, List<String>> preselectedOntologies) {
        this(parent, olsInputable, modal, field, selectedOntology, modifiedRow, term, null, null, OLS_DIALOG_TERM_NAME_SEARCH, preselectedOntologies);
    }

    /**
     * Opens a dialog that lets you search for terms using the OLS.
     *
     * @param parent the parent JFrame
     * @param olsInputable a reference to the frame using the OLS Dialog
     * @param modal
     * @param field the name of the field to insert the results into
     * @param selectedOntology the name of the ontology to search in, e.g., "GO"
     * or "MOD".
     * @param modifiedRow the row to modify, use -1 if adding a new row
     * @param term the term to search for
     * @param modificationMass the mass of the modification
     * @param modificationAccuracy the mass accuracy
     * @param searchType one of the following: OLS_DIALOG_TERM_NAME_SEARCH,
     * OLS_DIALOG_TERM_ID_SEARCH, OLS_DIALOG_BROWSE_ONTOLOGY or
     * OLS_DIALOG_PSI_MOD_MASS_SEARCH
     * @param preselectedOntologies
     */
    public OLSDialog(JFrame parent, OLSInputable olsInputable, boolean modal, String field,
            String selectedOntology, int modifiedRow, String term,
            Double modificationMass, Double modificationAccuracy, Integer searchType,
            Map<String, List<String>> preselectedOntologies) {
        super(parent, modal);

        this.olsInputable = olsInputable;
        this.field = field;
        this.selectedOntology = selectedOntology;
        this.modifiedRow = modifiedRow;
        this.mappedTerm = term;

        if (preselectedOntologies == null) {
            this.preselectedOntologies = new HashMap<String, List<String>>();
        } else {
            this.preselectedOntologies = preselectedOntologies;
        }

        setUpFrame(searchType);

        boolean error = openOlsConnectionAndInsertOntologyNames();

        if (error) {
            this.dispose();
        } else {
            insertValues(modificationMass, modificationAccuracy, searchType);
            this.setLocationRelativeTo(parent);
            this.setVisible(true);
        }
    }

    /**
     * Opens a dialog that lets you search for terms using the OLS.
     *
     * @param parent the parent JFrame
     * @param olsInputable a reference to the frame using the OLS Dialog
     * @param modal
     * @param field the name of the field to insert the results into
     * @param selectedOntology the name of the ontology to search in, e.g., "GO"
     * or "MOD".
     * @param modifiedRow the row to modify, use -1 if adding a new row
     * @param term the term to search for
     * @param modificationMass the mass of the modification
     * @param modificationAccuracy the mass accuracy
     * @param searchType one of the following: OLS_DIALOG_TERM_NAME_SEARCH,
     * OLS_DIALOG_TERM_ID_SEARCH, OLS_DIALOG_BROWSE_ONTOLOGY or
     * OLS_DIALOG_PSI_MOD_MASS_SEARCH
     */
    public OLSDialog(JFrame parent, OLSInputable olsInputable, boolean modal, String field,
            String selectedOntology, int modifiedRow, String term,
            Double modificationMass, Double modificationAccuracy, Integer searchType) {
        this(parent, olsInputable, modal, field, selectedOntology, modifiedRow, term, modificationMass, modificationAccuracy, searchType, null);
    }

    /**
     * Opens a dialog that lets you search for terms using the OLS.
     *
     * @param parent the parent JDialog
     * @param olsInputable a reference to the frame using the OLS Dialog
     * @param modal
     * @param field the name of the field to insert the results into
     * @param selectedOntology the name of the ontology to search in, e.g., "GO"
     * or "MOD".
     * @param modifiedRow the row to modify, use -1 if adding a new row
     * @param term the term to search for
     * @param modificationMass the mass of the modification
     * @param modificationAccuracy the mass accuracy
     * @param searchType one of the following: OLS_DIALOG_TERM_NAME_SEARCH,
     * OLS_DIALOG_TERM_ID_SEARCH, OLS_DIALOG_BROWSE_ONTOLOGY or
     * OLS_DIALOG_PSI_MOD_MASS_SEARCH
     * @param preselectedOntologies Default ontologies to display. Key: ontology
     * name, e.g. "MS" or "GO". Value: parent ontologies, e.g. "MS:1000458",
     * "null" (no parent ontology preselected)
     */
    public OLSDialog(JDialog parent, OLSInputable olsInputable, boolean modal, String field,
            String selectedOntology, int modifiedRow, String term,
            Double modificationMass, Double modificationAccuracy, Integer searchType,
            Map<String, List<String>> preselectedOntologies) {
        super(parent, modal);

        this.olsInputable = olsInputable;
        this.field = field;
        this.selectedOntology = selectedOntology;
        this.modifiedRow = modifiedRow;
        this.mappedTerm = term;

        if (preselectedOntologies == null) {
            this.preselectedOntologies = new HashMap<String, List<String>>();
        } else {
            this.preselectedOntologies = preselectedOntologies;
        }

        setUpFrame(searchType);

        boolean error = openOlsConnectionAndInsertOntologyNames();

        if (error) {
            this.dispose();
        } else {
            insertValues(modificationMass, modificationAccuracy, searchType);
            this.setLocationRelativeTo(parent);
            this.setVisible(true);
        }
    }

    /**
     * Opens a dialog that lets you search for terms using the OLS.
     *
     * @param parent the parent JDialog
     * @param olsInputable a reference to the frame using the OLS Dialog
     * @param modal
     * @param field the name of the field to insert the results into
     * @param selectedOntology the name of the ontology to search in, e.g., "GO"
     * or "MOD".
     * @param modifiedRow the row to modify, use -1 if adding a new row
     * @param term the term to search for
     * @param modificationMass the mass of the modification
     * @param modificationAccuracy the mass accuracy
     * @param searchType one of the following: OLS_DIALOG_TERM_NAME_SEARCH,
     * OLS_DIALOG_TERM_ID_SEARCH, OLS_DIALOG_BROWSE_ONTOLOGY or
     * OLS_DIALOG_PSI_MOD_MASS_SEARCH
     */
    public OLSDialog(JDialog parent, OLSInputable olsInputable, boolean modal, String field,
            String selectedOntology, int modifiedRow, String term,
            Double modificationMass, Double modificationAccuracy, Integer searchType) {
        this(parent, olsInputable, modal, field, selectedOntology, modifiedRow, term, modificationMass, modificationAccuracy, searchType, null);
    }

    /**
     * Inserts the provided values into the corresponding fields.
     */
    private void insertValues(Double modificationMass, Double modificationAccuracy, Integer searchType) {

        if (mappedTerm != null) {
            termNameSearchJTextField.setText(mappedTerm);
            termNameSearchJTextFieldKeyReleased(null);
        }

        if (modificationAccuracy != null) {
            precisionJTextField.setText(modificationAccuracy.toString());
        }

        if (modificationMass != null) {
            modificationMassJTextField.setText(modificationMass.toString());
            modificationMassSearchJButtonActionPerformed(null);
        }

        updateBrowseOntologyView();

        if (searchType == OLS_DIALOG_TERM_NAME_SEARCH) {
            termNameSearchJTextField.requestFocus();
        } else if (searchType == OLS_DIALOG_BROWSE_ONTOLOGY) {
            //updateBrowseOntologyView();
        } else if (searchType == OLS_DIALOG_PSI_MOD_MASS_SEARCH) {
            modificationMassJTextField.requestFocus();
        }
    }

    /**
     * Includes code used by all constructors to set up the frame, e.g.,
     * handling column tooltips etc.
     */
    private void setUpFrame(Integer searchType) {
        // set up of the default font size and type
//        java.util.Enumeration keys = UIManager.getDefaults().keys();
//        while (keys.hasMoreElements()) {
//            Object key = keys.nextElement();
//            Object value = UIManager.get(key);
//            if (value instanceof javax.swing.plaf.FontUIResource) {
//                UIManager.put(key, new java.awt.Font("Tahoma", 0, 11));
//            }
//        }

        initComponents();

        olsResultsTermNameSearchJTable.getColumn("Accession").setCellRenderer(new HtmlLinksRenderer(
                selectedRowHtmlTagFontColor, notSelectedRowHtmlTagFontColor));
        olsResultsTermIdSearchJTable.getColumn("Accession").setCellRenderer(new HtmlLinksRenderer(
                selectedRowHtmlTagFontColor, notSelectedRowHtmlTagFontColor));
        olsResultsMassSearchJTable.getColumn("Accession").setCellRenderer(new HtmlLinksRenderer(
                selectedRowHtmlTagFontColor, notSelectedRowHtmlTagFontColor));

        olsResultsTermNameSearchJScrollPane.getViewport().setOpaque(false);
        termDetailsTermNameSearchJScrollPane.getViewport().setOpaque(false);
        olsResultsTermIdSearchJScrollPane.getViewport().setOpaque(false);
        termDetailsTermIdSearchJScrollPane.getViewport().setOpaque(false);
        termDetailsMassSearchJScrollPane.getViewport().setOpaque(false);
        olsResultsMassSearchJScrollPane.getViewport().setOpaque(false);
        termDetailsBrowseOntologyJScrollPane.getViewport().setOpaque(false);

        setTitle("Ontology Lookup Service - (ols-dialog v" + getVersion() + ")");

        // hide the mass search dummy label
        dummyLabelJLabel.setForeground(massSearchJPanel.getBackground());

        // initialize the tree browser
        treeBrowser = new TreeBrowser(this);
        browseJPanel.add(treeBrowser);

        // open the requested search type pane
        searchTypeJTabbedPane.setSelectedIndex(searchType);

        // use combobox renderer that centers the text
        ontologyJComboBox.setRenderer(new MyComboBoxRenderer(null, SwingConstants.CENTER));
        massTypeJComboBox.setRenderer(new MyComboBoxRenderer(null, SwingConstants.CENTER));

        // disable reordring of the columns
        olsResultsTermNameSearchJTable.getTableHeader().setReorderingAllowed(false);
        olsResultsMassSearchJTable.getTableHeader().setReorderingAllowed(false);
        olsResultsTermIdSearchJTable.getTableHeader().setReorderingAllowed(false);
        termDetailsTermNameSearchJTable.getTableHeader().setReorderingAllowed(false);
        termDetailsMassSearchJTable.getTableHeader().setReorderingAllowed(false);
        termDetailsBrowseOntologyJTable.getTableHeader().setReorderingAllowed(false);
        termDetailsTermIdSearchJTable.getTableHeader().setReorderingAllowed(false);

        // make sure that only one row can be selected at ones
        olsResultsTermNameSearchJTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        olsResultsMassSearchJTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        olsResultsTermIdSearchJTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    }

    /**
     * Retrieves the version number set in the pom file.
     *
     * @return the version number of the ols-dialog
     */
    public String getVersion() {

        java.util.Properties p = new java.util.Properties();

        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("ols-dialog.properties");
            p.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return p.getProperty("ols-dialog.version");
    }

    /**
     * Calls OLS webserver and gets root terms of an ontology.
     *
     * @param ontology
     * @return Map of root terms - key is termId, value is termName. Map should
     * not be null.
     */
    public Map<String, String> getOntologyRoots(String ontology) {
        return getOntologyRoots(ontology, null);
    }

    /**
     * Calls OLS webserver and gets root terms of an ontology from a parent
     * term.
     *
     * @param ontology
     * @param parentTerm
     * @return
     */
    public Map<String, String> getOntologyRoots(String ontology, String parentTerm) {

        Map<String, String> retrievedValues = new HashMap<String, String>();

        try {
            Map roots;
            if (parentTerm == null) {
                roots = olsConnection.getRootTerms(ontology);
            } else {
                roots = olsConnection.getTermChildren(parentTerm, ontology, 1, null);
            }

            if (roots != null) {
                retrievedValues.putAll(roots);
            }

        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(
                    this,
                    defaultOlsConnectionFailureErrorMessage,
                    "OLS Connection Error", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to access OLS: ");
            e.printStackTrace();
        }

        return retrievedValues;
    }

    /**
     * Clears the meta data section for the selected search type.
     *
     * @param searchType the search type to clear the meta data for
     * @param clearSearchResults if true the search results table is cleared
     * @param clearMetaData if true the meta data is cleared
     */
    public void clearData(Integer searchType, boolean clearSearchResults, boolean clearMetaData) {

        JTextPane currentDefinitionsJTextPane = null;
        JTable currentTermDetailsJTable = null;
        JScrollPane currentTermDetailsJScrollPane = null;
        JTable currentSearchResultsJTable = null;
        JScrollPane currentSearchResultsJScrollPane = null;

        if (searchType == OLS_DIALOG_TERM_NAME_SEARCH) {
            currentlySelectedTermNameSearchAccessionNumber = null;
            currentSearchResultsJTable = olsResultsTermNameSearchJTable;
            currentSearchResultsJScrollPane = olsResultsTermNameSearchJScrollPane;
            currentDefinitionsJTextPane = definitionTermNameSearchJTextPane;
            currentTermDetailsJTable = termDetailsTermNameSearchJTable;
            currentTermDetailsJScrollPane = termDetailsTermNameSearchJScrollPane;
            viewTermHierarchyTermNameSearchJLabel.setEnabled(false);
        } else if (searchType == OLS_DIALOG_PSI_MOD_MASS_SEARCH) {
            currentlySelectedMassSearchAccessionNumber = null;
            currentSearchResultsJTable = olsResultsMassSearchJTable;
            currentSearchResultsJScrollPane = olsResultsMassSearchJScrollPane;
            currentDefinitionsJTextPane = definitionMassSearchJTextPane;
            currentTermDetailsJTable = termDetailsMassSearchJTable;
            currentTermDetailsJScrollPane = termDetailsMassSearchJScrollPane;
            viewTermHierarchyMassSearchJLabel.setEnabled(false);
        } else if (searchType == OLS_DIALOG_BROWSE_ONTOLOGY) {
            currentlySelectedBrowseOntologyAccessionNumber = null;
            currentDefinitionsJTextPane = definitionBrowseOntologyJTextPane;
            currentTermDetailsJTable = termDetailsBrowseOntologyJTable;
            currentTermDetailsJScrollPane = termDetailsBrowseOntologyJScrollPane;
            viewTermHierarchyBrowseOntologyJLabel.setEnabled(false);
        } else if (searchType == OLS_DIALOG_TERM_ID_SEARCH) {
            currentlySelectedTermIdSearchAccessionNumber = null;
            currentSearchResultsJTable = olsResultsTermIdSearchJTable;
            currentSearchResultsJScrollPane = olsResultsTermIdSearchJScrollPane;
            currentDefinitionsJTextPane = definitionTermIdSearchJTextPane;
            currentTermDetailsJTable = termDetailsTermIdSearchJTable;
            currentTermDetailsJScrollPane = termDetailsTermIdSearchJScrollPane;
            viewTermHierarchyTermIdSearchJLabel.setEnabled(false);
        }

        if (clearMetaData) {

            currentDefinitionsJTextPane.setText("");

            while (currentTermDetailsJTable.getRowCount() > 0) {
                ((DefaultTableModel) currentTermDetailsJTable.getModel()).removeRow(0);
            }

            currentTermDetailsJScrollPane.getVerticalScrollBar().setValue(0);
        }

        if (clearSearchResults) {
            if (searchType != OLS_DIALOG_BROWSE_ONTOLOGY) {

                while (currentSearchResultsJTable.getRowCount() > 0) {
                    ((DefaultTableModel) currentSearchResultsJTable.getModel()).removeRow(0);
                }

                currentSearchResultsJScrollPane.getVerticalScrollBar().setValue(0);
            }
        }
    }

    /**
     * Tries to load the children of a given term.
     *
     * @param parent the tree node where to load the terms
     * @param termId the term id to query on
     * @return true if the terms was loaded successfully, false otherwise
     */
    public boolean loadChildren(TreeNode parent, String termId) {

        if (termId == null) {
            return false;
        }

        boolean error = false;

        String ontology = getCurrentOntologyLabel();

        //get children from OLS
        Map<String, String> childTerms = null;

        try {
            childTerms = olsConnection.getTermChildren(termId, ontology, 1, null);
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    defaultOlsConnectionFailureErrorMessage,
                    "OLS Connection Error", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to access OLS: ");
            ex.printStackTrace();
            error = true;
        }

        if (!error && childTerms != null && !childTerms.isEmpty()) {
            // add the nodes to the tree
            for (String tId : childTerms.keySet()) {
                treeBrowser.addNode(tId, childTerms.get(tId));
            }
            return true;
        } else {
            if (debug) {
                System.out.println("no children returned for " + termId);
            }
            return false;
        }
    }

    /**
     * Returns the ontology label extracted from the term id.
     *
     * @param termId the term id to extract the ontology label from
     * @return the ontology label extracted from the term id, or null if no
     * ontology is found
     */
    private String getOntologyLabelFromTermId(String termId) {

        String ontologyLabel;

        if (termId.lastIndexOf(":") != -1) {
            ontologyLabel = termId.substring(0, termId.lastIndexOf(":"));
        } else if (termId.lastIndexOf("_") != -1) { // needed for EFO
            ontologyLabel = termId.substring(0, termId.lastIndexOf("_"));
        } else if (termId.equalsIgnoreCase("No Root Terms Defined!")) {
            ontologyLabel = null;
        } else {
            ontologyLabel = "NEWT";
        }

        return ontologyLabel;
    }

    /**
     * Load metadata for a given termId.
     *
     * @param termId the term to load meta data for
     * @param searchType the search type where the meta data will be inserted
     */
    public void loadMetaData(String termId, Integer searchType) {

        JTextPane currentDefinitionsJTextPane = null;
        JTable currentTermDetailsJTable = null;
        JScrollPane currentTermDetailsJScrollPane = null;

        if (searchType == OLS_DIALOG_TERM_NAME_SEARCH) {
            currentDefinitionsJTextPane = definitionTermNameSearchJTextPane;
            currentTermDetailsJTable = termDetailsTermNameSearchJTable;
            currentTermDetailsJScrollPane = termDetailsTermNameSearchJScrollPane;
        } else if (searchType == OLS_DIALOG_PSI_MOD_MASS_SEARCH) {
            currentDefinitionsJTextPane = definitionMassSearchJTextPane;
            currentTermDetailsJTable = termDetailsMassSearchJTable;
            currentTermDetailsJScrollPane = termDetailsMassSearchJScrollPane;
        } else if (searchType == OLS_DIALOG_BROWSE_ONTOLOGY) {
            currentDefinitionsJTextPane = definitionBrowseOntologyJTextPane;
            currentTermDetailsJTable = termDetailsBrowseOntologyJTable;
            currentTermDetailsJScrollPane = termDetailsBrowseOntologyJScrollPane;
        } else if (searchType == OLS_DIALOG_TERM_ID_SEARCH) {
            currentDefinitionsJTextPane = definitionTermIdSearchJTextPane;
            currentTermDetailsJTable = termDetailsTermIdSearchJTable;
            currentTermDetailsJScrollPane = termDetailsTermIdSearchJScrollPane;
        }

        //clear meta data
        clearData(searchType, false, true);

        if (termId == null) {
            return;
        }

        String ontology = getOntologyLabelFromTermId(termId);

        if (searchType == OLS_DIALOG_TERM_NAME_SEARCH) {
            currentlySelectedTermNameSearchAccessionNumber = termId;
            viewTermHierarchyTermNameSearchJLabel.setEnabled(true);
        } else if (searchType == OLS_DIALOG_PSI_MOD_MASS_SEARCH) {
            currentlySelectedMassSearchAccessionNumber = termId;
            viewTermHierarchyMassSearchJLabel.setEnabled(true);
        } else if (searchType == OLS_DIALOG_BROWSE_ONTOLOGY) {
            currentlySelectedBrowseOntologyAccessionNumber = termId;
            viewTermHierarchyBrowseOntologyJLabel.setEnabled(true);
        } else if (searchType == OLS_DIALOG_TERM_ID_SEARCH) {
            currentlySelectedTermIdSearchAccessionNumber = termId;
            viewTermHierarchyTermIdSearchJLabel.setEnabled(true);
        }

        boolean error = false;

        if (ontology != null && ontology.equalsIgnoreCase("NEWT")) {
            currentDefinitionsJTextPane.setText("Retreiving 'Term Details' is disabled for NEWT.");
            currentDefinitionsJTextPane.setCaretPosition(0);
            currentTermDetailsJTable.setEnabled(false);
            error = true;
        } else {
            currentTermDetailsJTable.setEnabled(true);
        }

        if (!error) {

            metadata = null;
            Map<String, String> xRefs = null;

            //query OLS
            try {
                metadata = olsConnection.getTermMetadata(termId, ontology);
                xRefs = olsConnection.getTermXrefs(termId, ontology);
            } catch (RemoteException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        defaultOlsConnectionFailureErrorMessage,
                        "OLS Connection Error", JOptionPane.ERROR_MESSAGE);
                Util.writeToErrorLog("Error when trying to access OLS: ");
                ex.printStackTrace();

                if (searchType == OLS_DIALOG_TERM_NAME_SEARCH) {
                    currentlySelectedTermNameSearchAccessionNumber = null;
                } else if (searchType == OLS_DIALOG_PSI_MOD_MASS_SEARCH) {
                    currentlySelectedMassSearchAccessionNumber = null;
                } else if (searchType == OLS_DIALOG_BROWSE_ONTOLOGY) {
                    currentlySelectedBrowseOntologyAccessionNumber = null;
                } else if (searchType == OLS_DIALOG_TERM_ID_SEARCH) {
                    currentlySelectedTermIdSearchAccessionNumber = termId;
                }

                error = true;
            }

            if (!error && !metadata.isEmpty()) {

                // retrieve the terms meta data and insert into the table
                // note that "definition" is handled separatly
                for (Iterator i = metadata.keySet().iterator(); i.hasNext();) {
                    String key = (String) i.next();

                    if (key != null && key.equalsIgnoreCase("definition")) {
                        currentDefinitionsJTextPane.setText("" + metadata.get(key));
                        currentDefinitionsJTextPane.setCaretPosition(0);
                    } else {
                        ((DefaultTableModel) currentTermDetailsJTable.getModel()).addRow(
                                new Object[]{key, metadata.get(key)});
                    }
                }

                if (currentDefinitionsJTextPane.getText().equalsIgnoreCase("null")) {
                    currentDefinitionsJTextPane.setText("(no definition provided in CV term)");
                }

                // iterate the xrefs and insert them into the table
                for (Iterator i = xRefs.keySet().iterator(); i.hasNext();) {
                    String key = (String) i.next();

                    ((DefaultTableModel) currentTermDetailsJTable.getModel()).addRow(
                            new Object[]{key, xRefs.get(key)});
                }

                // set the horizontal scroll bar to the top
                currentTermDetailsJScrollPane.getVerticalScrollBar().setValue(0);
            } else {
                if (searchType == OLS_DIALOG_TERM_NAME_SEARCH) {
                    viewTermHierarchyTermNameSearchJLabel.setEnabled(false);
                } else if (searchType == OLS_DIALOG_PSI_MOD_MASS_SEARCH) {
                    viewTermHierarchyMassSearchJLabel.setEnabled(false);
                } else if (searchType == OLS_DIALOG_BROWSE_ONTOLOGY) {
                    viewTermHierarchyBrowseOntologyJLabel.setEnabled(false);
                } else if (searchType == OLS_DIALOG_TERM_ID_SEARCH) {
                    viewTermHierarchyTermIdSearchJLabel.setEnabled(false);
                }
            }
        } else {
            if (ontology != null && ontology.equalsIgnoreCase("NEWT")) {
                if (searchType == OLS_DIALOG_TERM_NAME_SEARCH) {
                    viewTermHierarchyTermNameSearchJLabel.setEnabled(true);
                } else if (searchType == OLS_DIALOG_PSI_MOD_MASS_SEARCH) {
                    viewTermHierarchyMassSearchJLabel.setEnabled(true);
                } else if (searchType == OLS_DIALOG_BROWSE_ONTOLOGY) {
                    viewTermHierarchyBrowseOntologyJLabel.setEnabled(true);
                } else if (searchType == OLS_DIALOG_TERM_ID_SEARCH) {
                    viewTermHierarchyTermIdSearchJLabel.setEnabled(true);
                }
            } else {
                if (searchType == OLS_DIALOG_TERM_NAME_SEARCH) {
                    viewTermHierarchyTermNameSearchJLabel.setEnabled(false);
                } else if (searchType == OLS_DIALOG_PSI_MOD_MASS_SEARCH) {
                    viewTermHierarchyMassSearchJLabel.setEnabled(false);
                } else if (searchType == OLS_DIALOG_BROWSE_ONTOLOGY) {
                    viewTermHierarchyBrowseOntologyJLabel.setEnabled(false);
                } else if (searchType == OLS_DIALOG_TERM_ID_SEARCH) {
                    viewTermHierarchyTermIdSearchJLabel.setEnabled(false);
                }
            }
        }

        if (searchType == OLS_DIALOG_TERM_NAME_SEARCH) {
            insertSelectedJButton.setEnabled(currentlySelectedTermNameSearchAccessionNumber != null);
        } else if (searchType == OLS_DIALOG_PSI_MOD_MASS_SEARCH) {
            insertSelectedJButton.setEnabled(currentlySelectedMassSearchAccessionNumber != null);
        } else if (searchType == OLS_DIALOG_BROWSE_ONTOLOGY) {
            insertSelectedJButton.setEnabled(currentlySelectedBrowseOntologyAccessionNumber != null);
        } else if (searchType == OLS_DIALOG_TERM_ID_SEARCH) {
            insertSelectedJButton.setEnabled(currentlySelectedTermIdSearchAccessionNumber != null);
        }
    }

    /**
     * A helper method for setting the cell tool tips. Included in order to not
     * have to duplicate the code for each table.
     *
     * @param table
     * @param value
     * @param row
     * @param column
     */
    private void setTableToolTip(JTable table, Object value, int row, int column) {
        if (table != null) {
            if (table.getValueAt(row, column) != null) {
                if (column == 1 && table.getValueAt(row, column).toString().length() > MAX_TOOL_TIP_LENGTH) {
                    table.setToolTipText(buildToolTipText("" + value.toString(), MAX_TOOL_TIP_LENGTH));
                } else {
                    table.setToolTipText(null);
                }
            } else {
                table.setToolTipText(null);
            }
        }
    }

    /**
     * Creates a multiple lines tooltip based on the provided text.
     *
     * @param aToolTip the original one line tool tip
     * @return the multiple line tooltip as HTML
     */
    private String buildToolTipText(String aToolTip, int maxToolTipLength) {

        String currentToolTip = "<html>";

        int indexOfLastSpace = 0;
        String currentToolTipLine = "";
        int currentStartIndex = 0;

        for (int i = 0; i < aToolTip.length(); i++) {

            currentToolTipLine += aToolTip.substring(i, i + 1);

            if (aToolTip.substring(i, i + 1).equalsIgnoreCase(" ")) {
                indexOfLastSpace = i;
            }

            if (currentToolTipLine.length() > maxToolTipLength) {
                if (indexOfLastSpace == currentStartIndex) {
                    currentToolTip += aToolTip.substring(currentStartIndex, i + 1) + "-<br>";
                    currentStartIndex = i + 1;
                    indexOfLastSpace = i + 1;
                    currentToolTipLine = "";
                } else {
                    currentToolTip += aToolTip.substring(currentStartIndex, indexOfLastSpace) + "<br>";
                    currentStartIndex = indexOfLastSpace;
                    currentToolTipLine = "";
                    i = currentStartIndex;
                }
            }
        }

        if (currentToolTipLine.length() > 0) {
            currentToolTip += aToolTip.substring(currentStartIndex);
        }

        currentToolTip += "</html>";

        return currentToolTip;
    }

    /**
     * Opens the OLS connection and retrieves and inserts the ontology names
     * into the ontology combo box.
     *
     * @return false if an error occurred, true otherwise
     */
    private boolean openOlsConnectionAndInsertOntologyNames() {

        boolean error = false;

        Vector ontologyNamesAndKeys = new Vector();

        preselectedNames2Ids = new HashMap<String, String>();

        try {
            QueryService locator = new QueryServiceLocator();
            olsConnection = locator.getOntologyQuery();
            Map map = olsConnection.getOntologyNames();

            String ontologyToSelect = "";

            for (Iterator i = map.keySet().iterator(); i.hasNext();) {
                String key = (String) i.next();
                String temp = map.get(key) + " [" + key + "]";
                if (preselectedOntologies.isEmpty()) {
                    ontologyNamesAndKeys.add(temp);
                } else {
                    if (preselectedOntologies.keySet().contains(key.toUpperCase())) {
                        if (preselectedOntologies.get(key.toUpperCase()) == null) {
                            ontologyNamesAndKeys.add(temp);
                        } else {
                            for (String ontologyTermId : preselectedOntologies.get(key.toUpperCase())) {
                                String ontologyTermName = olsConnection.getTermById(ontologyTermId, key);
                                String suffix = ontologyTermName;
                                if (ontologyTermName == null) {
                                    suffix = ontologyTermId;
                                } else if (ontologyTermName.length() == 0) {
                                    suffix = ontologyTermId;
                                }
                                String ontologyName = temp + " / " + suffix;
                                if (selectedOntology.equalsIgnoreCase(ontologyName)) {
                                    ontologyToSelect = ontologyName;
                                }
                                ontologyNamesAndKeys.add(ontologyName);
                                preselectedNames2Ids.put(suffix, ontologyTermId);
                            }
                        }
                    }
                }

                if (selectedOntology.equalsIgnoreCase(temp) || selectedOntology.equalsIgnoreCase(key)) {
                    ontologyToSelect = temp;
                }
            }
            //check all preselected ontologies have been found in OLS
            if (!preselectedOntologies.isEmpty()) {
                if (preselectedOntologies.size() != ontologyNamesAndKeys.size()) {
                    String msg = "Warning: One or more of your preselected ontologies have not been found in OLS";
                    Util.writeToErrorLog(msg);
                }
            }

            // sort the ontologies into alphabetic ascending order
            java.util.Collections.sort(ontologyNamesAndKeys);

            ontologyNamesAndKeys.add(0, "-- Search in All Ontologies available in the OLS registry --");
            if (preselectedOntologies.size() > 1) {
                ontologyNamesAndKeys.add(1, "-- Search in these preselected Ontologies --");
            }

            ontologyJComboBox.setModel(new DefaultComboBoxModel(ontologyNamesAndKeys));
            //default selected ontology. Has to be the same name shown in the menu
            ontologyJComboBox.setSelectedItem(ontologyToSelect);

            hideOrShowNewtLinks();

            lastSelectedOntology = (String) ontologyJComboBox.getSelectedItem();
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    defaultOlsConnectionFailureErrorMessage,
                    "Failed to Contact the OLS", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to access OLS: ");
            ex.printStackTrace();
            error = true;
        } catch (ServiceException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    defaultOlsConnectionFailureErrorMessage,
                    "Failed to Contact the OLS", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to access OLS: ");
            ex.printStackTrace();
            error = true;
        }

        return error;
    }

    /**
     * Makes the 'newt species tip' links visible or not visible.
     */
    private void hideOrShowNewtLinks() {

        // note: has to be done like this and not simply by disabling or
        // making invisible, as both of those options have unwanted side effects
        if (getCurrentOntologyLabel().equalsIgnoreCase("NEWT")) {
            newtSpeciesTipsTermNameSearchJLabel.setForeground(Color.BLUE);
            newtSpeciesTipsTermIdSearchJLabel.setForeground(Color.BLUE);
            newtSpeciesTipsTermNameSearchJLabel.setText("NEWT Species Tips");
            newtSpeciesTipsTermIdSearchJLabel.setText("NEWT Species Tips");
        } else {
            newtSpeciesTipsTermNameSearchJLabel.setText(" ");
            newtSpeciesTipsTermIdSearchJLabel.setText(" ");
        }
    }

    /**
     * Update the ontology tree browser with the roots of the selected ontology.
     */
    private void updateBrowseOntologyView() {

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        // get selected ontology
        String ontology = getCurrentOntologyLabel();

        // get selected predefined ontology term for selected ontology
        String parentTermName = getCurrentOntologyTermLabel();
        String parentTermId = preselectedNames2Ids.get(parentTermName);

        // set the root to the ontology label
        if (parentTermName != null && parentTermId != null) {
            treeBrowser.initialize("[" + parentTermId + "] " + parentTermName);
        } else {
            treeBrowser.initialize(ontology);
        }

        // load root terms
        Map<String, String> rootTerms = getOntologyRoots(ontology, parentTermId);

        // update the tree
        for (String termId : rootTerms.keySet()) {
            treeBrowser.addNode(termId, rootTerms.get(termId));
        }

        // not root terms found
        if (rootTerms.isEmpty()) {
            treeBrowser.addNode("No Root Terms Defined!", "");
        }

        // makes sure that all second level non visible nodes are added
        treeBrowser.updateTree();

        // move the horizontal scroll bar value to the top
        treeBrowser.scrollToTop();

        currentlySelectedBrowseOntologyAccessionNumber = null;

        clearData(OLS_DIALOG_BROWSE_ONTOLOGY, true, true);

        if (debug) {
            System.out.println("updated roots");
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }

    /**
     * Adds a second level of non visible nodes. Needed to be able to show
     * folder icons for the current level of nodes.
     *
     * @param termId the term id for the term to add the second level for
     * @param ontology the ontology to get the terms from
     * @param parentNode the node to add the new nodes to
     * @return true if an error occurred, false otherwise
     */
    public boolean addSecondLevelOfNodes(String termId, String ontology, DefaultMutableTreeNode parentNode) {

        boolean error = false;

        try {
            // get the next level of nodes
            Map<String, String> secondLevelChildTerms = olsConnection.getTermChildren(termId, ontology, 1, null);

            // add the level of non visible nodes
            for (String tId2 : secondLevelChildTerms.keySet()) {
                treeBrowser.addNode(parentNode, tId2, secondLevelChildTerms.get(tId2), false);
            }

        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    defaultOlsConnectionFailureErrorMessage,
                    "OLS Connection Error", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to access OLS: ");
            ex.printStackTrace();
            error = true;
        }

        return error;
    }

    /**
     * Returns the currently selected ontology label.
     *
     * @return the currently selected ontology label
     */
    public String getCurrentOntologyLabel() {

        String ontology = ((String) ontologyJComboBox.getSelectedItem());
        //ontology = ontology.substring(ontology.lastIndexOf("[") + 1, ontology.length() - 1);
        if (ontology.lastIndexOf("[") != -1) {
            ontology = ontology.substring(ontology.lastIndexOf("[") + 1, ontology.length());
        }
        if (ontology.lastIndexOf("]") != -1) {
            ontology = ontology.substring(0, ontology.lastIndexOf("]"));
        }

        return ontology;
    }

    public String getCurrentOntologyTermLabel() {

        String ontologyTerm = ((String) ontologyJComboBox.getSelectedItem());
        //ontology = ontology.substring(ontology.lastIndexOf("[") + 1, ontology.length() - 1);
        if (ontologyTerm.lastIndexOf("/ ") != -1) {
            ontologyTerm = ontologyTerm.substring(ontologyTerm.lastIndexOf("/ ") + 2, ontologyTerm.length());
        } else {
            ontologyTerm = null;
        }

        return ontologyTerm;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        backgroundPanel = new javax.swing.JPanel();
        insertSelectedJButton = new javax.swing.JButton();
        cancelJButton = new javax.swing.JButton();
        helpJButton = new javax.swing.JButton();
        aboutJButton = new javax.swing.JButton();
        searchParametersJPanel = new javax.swing.JPanel();
        searchTypeJTabbedPane = new javax.swing.JTabbedPane();
        termNameSearchJPanel = new javax.swing.JPanel();
        definitionTermNameScrollPane = new javax.swing.JScrollPane();
        definitionTermNameSearchJTextPane = new javax.swing.JTextPane();
        termDetailsTermNameSearchJScrollPane = new javax.swing.JScrollPane();
        termDetailsTermNameSearchJTable = new javax.swing.JTable();
        searchResultsTermNameJLabel = new javax.swing.JLabel();
        selectedTermTermNameJLabel = new javax.swing.JLabel();
        olsResultsTermNameSearchJScrollPane = new javax.swing.JScrollPane();
        olsResultsTermNameSearchJTable = new javax.swing.JTable();
        viewTermHierarchyTermNameSearchJLabel = new javax.swing.JLabel();
        termNameJPanel = new javax.swing.JPanel();
        termNameJLabel = new javax.swing.JLabel();
        termNameSearchJTextField = new javax.swing.JTextField();
        newtSpeciesTipsTermNameSearchJLabel = new javax.swing.JLabel();
        numberOfTermsTermNameSearchJTextField = new javax.swing.JTextField();
        termIdSearchJPanel = new javax.swing.JPanel();
        definitionTermNameIdSearchScrollPane = new javax.swing.JScrollPane();
        definitionTermIdSearchJTextPane = new javax.swing.JTextPane();
        termDetailsTermIdSearchJScrollPane = new javax.swing.JScrollPane();
        termDetailsTermIdSearchJTable = new javax.swing.JTable();
        searchResultsTermIdLabel = new javax.swing.JLabel();
        searchTermTermIdLabel = new javax.swing.JLabel();
        olsResultsTermIdSearchJScrollPane = new javax.swing.JScrollPane();
        olsResultsTermIdSearchJTable = new javax.swing.JTable();
        viewTermHierarchyTermIdSearchJLabel = new javax.swing.JLabel();
        termIdPanel = new javax.swing.JPanel();
        termIdLabel = new javax.swing.JLabel();
        termIdSearchJTextField = new javax.swing.JTextField();
        newtSpeciesTipsTermIdSearchJLabel = new javax.swing.JLabel();
        termIdSearchJButton = new javax.swing.JButton();
        massSearchJPanel = new javax.swing.JPanel();
        searchResultsMassSearchLabel = new javax.swing.JLabel();
        searchTermMassSearchLabel = new javax.swing.JLabel();
        definitionSelectedTermMassSearchScrollPane = new javax.swing.JScrollPane();
        definitionMassSearchJTextPane = new javax.swing.JTextPane();
        termDetailsMassSearchJScrollPane = new javax.swing.JScrollPane();
        termDetailsMassSearchJTable = new javax.swing.JTable();
        olsResultsMassSearchJScrollPane = new javax.swing.JScrollPane();
        olsResultsMassSearchJTable = new javax.swing.JTable();
        viewTermHierarchyMassSearchJLabel = new javax.swing.JLabel();
        massPanel = new javax.swing.JPanel();
        massLabel = new javax.swing.JLabel();
        modificationMassJTextField = new javax.swing.JTextField();
        plussMinusLabel = new javax.swing.JLabel();
        precisionJTextField = new javax.swing.JTextField();
        typeLabel = new javax.swing.JLabel();
        massTypeJComboBox = new javax.swing.JComboBox();
        dummyLabelJLabel = new javax.swing.JLabel();
        modificationMassSearchJButton = new javax.swing.JButton();
        browseOntologyJPanel = new javax.swing.JPanel();
        selectedTermBrowseLabel = new javax.swing.JLabel();
        treeScrollPane = new javax.swing.JScrollPane();
        definitionBrowseOntologyJTextPane = new javax.swing.JTextPane();
        termDetailsBrowseOntologyJScrollPane = new javax.swing.JScrollPane();
        termDetailsBrowseOntologyJTable = new javax.swing.JTable();
        browseJPanel = new javax.swing.JPanel();
        viewTermHierarchyBrowseOntologyJLabel = new javax.swing.JLabel();
        ontologyJLabel = new javax.swing.JLabel();
        ontologyJComboBox = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(" Ontology Lookup Service - (ols-dialog v3.0)");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        insertSelectedJButton.setText("Use Selected Term");
        insertSelectedJButton.setEnabled(false);
        insertSelectedJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertSelectedJButtonActionPerformed(evt);
            }
        });

        cancelJButton.setText("Cancel");
        cancelJButton.setMaximumSize(new java.awt.Dimension(121, 23));
        cancelJButton.setMinimumSize(new java.awt.Dimension(121, 23));
        cancelJButton.setPreferredSize(new java.awt.Dimension(121, 23));
        cancelJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelJButtonActionPerformed(evt);
            }
        });

        helpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/olsdialog/icons/help.GIF"))); // NOI18N
        helpJButton.setToolTipText("Help");
        helpJButton.setBorderPainted(false);
        helpJButton.setContentAreaFilled(false);
        helpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                helpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                helpJButtonMouseExited(evt);
            }
        });
        helpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpJButtonActionPerformed(evt);
            }
        });

        aboutJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/olsdialog/icons/ols_transparent_small.GIF"))); // NOI18N
        aboutJButton.setToolTipText("About");
        aboutJButton.setBorderPainted(false);
        aboutJButton.setContentAreaFilled(false);
        aboutJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                aboutJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                aboutJButtonMouseExited(evt);
            }
        });
        aboutJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutJButtonActionPerformed(evt);
            }
        });

        searchParametersJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Search Parameters"));
        searchParametersJPanel.setOpaque(false);

        searchTypeJTabbedPane.setBackground(new java.awt.Color(230, 230, 230));
        searchTypeJTabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                searchTypeJTabbedPaneStateChanged(evt);
            }
        });

        termNameSearchJPanel.setBackground(new java.awt.Color(230, 230, 230));

        definitionTermNameSearchJTextPane.setEditable(false);
        definitionTermNameScrollPane.setViewportView(definitionTermNameSearchJTextPane);

        termDetailsTermNameSearchJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        termDetailsTermNameSearchJTable.setOpaque(false);
        termDetailsTermNameSearchJScrollPane.setViewportView(termDetailsTermNameSearchJTable);

        searchResultsTermNameJLabel.setText("Search Results");

        selectedTermTermNameJLabel.setText("Selected Term");

        olsResultsTermNameSearchJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Accession", "CV Term"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        olsResultsTermNameSearchJTable.setOpaque(false);
        olsResultsTermNameSearchJTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                olsResultsTermNameSearchJTableMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                olsResultsTermNameSearchJTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                olsResultsTermNameSearchJTableMouseReleased(evt);
            }
        });
        olsResultsTermNameSearchJTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                olsResultsTermNameSearchJTableMouseMoved(evt);
            }
        });
        olsResultsTermNameSearchJTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                olsResultsTermNameSearchJTableKeyReleased(evt);
            }
        });
        olsResultsTermNameSearchJScrollPane.setViewportView(olsResultsTermNameSearchJTable);

        viewTermHierarchyTermNameSearchJLabel.setForeground(new java.awt.Color(0, 0, 255));
        viewTermHierarchyTermNameSearchJLabel.setText("View Term Hierarchy");
        viewTermHierarchyTermNameSearchJLabel.setEnabled(false);
        viewTermHierarchyTermNameSearchJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                viewTermHierarchyTermNameSearchJLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                viewTermHierarchyTermNameSearchJLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                viewTermHierarchyTermNameSearchJLabelMouseExited(evt);
            }
        });

        termNameJPanel.setOpaque(false);

        termNameJLabel.setText("Term Name");

        termNameSearchJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        termNameSearchJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                termNameSearchJTextFieldKeyReleased(evt);
            }
        });

        newtSpeciesTipsTermNameSearchJLabel.setFont(newtSpeciesTipsTermNameSearchJLabel.getFont().deriveFont(newtSpeciesTipsTermNameSearchJLabel.getFont().getSize()-1f));
        newtSpeciesTipsTermNameSearchJLabel.setForeground(new java.awt.Color(0, 0, 255));
        newtSpeciesTipsTermNameSearchJLabel.setText("NEWT Species Tips");
        newtSpeciesTipsTermNameSearchJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                newtSpeciesTipsTermNameSearchJLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                newtSpeciesTipsTermNameSearchJLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                newtSpeciesTipsTermNameSearchJLabelMouseExited(evt);
            }
        });

        numberOfTermsTermNameSearchJTextField.setEditable(false);
        numberOfTermsTermNameSearchJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        numberOfTermsTermNameSearchJTextField.setToolTipText("Number of Matching Terms");

        org.jdesktop.layout.GroupLayout termNameJPanelLayout = new org.jdesktop.layout.GroupLayout(termNameJPanel);
        termNameJPanel.setLayout(termNameJPanelLayout);
        termNameJPanelLayout.setHorizontalGroup(
            termNameJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(termNameJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(termNameJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(newtSpeciesTipsTermNameSearchJLabel)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, termNameJPanelLayout.createSequentialGroup()
                        .add(termNameJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(termNameSearchJTextField)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(numberOfTermsTermNameSearchJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 79, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        termNameJPanelLayout.setVerticalGroup(
            termNameJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(termNameJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(termNameJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(termNameJLabel)
                    .add(numberOfTermsTermNameSearchJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(termNameSearchJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(newtSpeciesTipsTermNameSearchJLabel))
        );

        termNameJPanelLayout.linkSize(new java.awt.Component[] {numberOfTermsTermNameSearchJTextField, termNameSearchJTextField}, org.jdesktop.layout.GroupLayout.VERTICAL);

        org.jdesktop.layout.GroupLayout termNameSearchJPanelLayout = new org.jdesktop.layout.GroupLayout(termNameSearchJPanel);
        termNameSearchJPanel.setLayout(termNameSearchJPanelLayout);
        termNameSearchJPanelLayout.setHorizontalGroup(
            termNameSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, termNameJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(termNameSearchJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(termNameSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(termNameSearchJPanelLayout.createSequentialGroup()
                        .add(searchResultsTermNameJLabel)
                        .add(0, 0, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, olsResultsTermNameSearchJScrollPane)
                    .add(termDetailsTermNameSearchJScrollPane)
                    .add(termNameSearchJPanelLayout.createSequentialGroup()
                        .add(selectedTermTermNameJLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(viewTermHierarchyTermNameSearchJLabel))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, definitionTermNameScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE))
                .addContainerGap())
        );
        termNameSearchJPanelLayout.setVerticalGroup(
            termNameSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, termNameSearchJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(termNameJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(1, 1, 1)
                .add(searchResultsTermNameJLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(olsResultsTermNameSearchJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(termNameSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(selectedTermTermNameJLabel)
                    .add(viewTermHierarchyTermNameSearchJLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(definitionTermNameScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(termDetailsTermNameSearchJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                .addContainerGap())
        );

        searchTypeJTabbedPane.addTab("Term Name Search", termNameSearchJPanel);

        termIdSearchJPanel.setBackground(new java.awt.Color(230, 230, 230));

        definitionTermIdSearchJTextPane.setEditable(false);
        definitionTermNameIdSearchScrollPane.setViewportView(definitionTermIdSearchJTextPane);

        termDetailsTermIdSearchJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        termDetailsTermIdSearchJTable.setOpaque(false);
        termDetailsTermIdSearchJScrollPane.setViewportView(termDetailsTermIdSearchJTable);

        searchResultsTermIdLabel.setText("Search Results");

        searchTermTermIdLabel.setText("Selected Term");

        olsResultsTermIdSearchJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Accession", "CV Term"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        olsResultsTermIdSearchJTable.setOpaque(false);
        olsResultsTermIdSearchJTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                olsResultsTermIdSearchJTableMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                olsResultsTermIdSearchJTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                olsResultsTermIdSearchJTableMouseReleased(evt);
            }
        });
        olsResultsTermIdSearchJTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                olsResultsTermIdSearchJTableMouseMoved(evt);
            }
        });
        olsResultsTermIdSearchJTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                olsResultsTermIdSearchJTableKeyReleased(evt);
            }
        });
        olsResultsTermIdSearchJScrollPane.setViewportView(olsResultsTermIdSearchJTable);

        viewTermHierarchyTermIdSearchJLabel.setForeground(new java.awt.Color(0, 0, 255));
        viewTermHierarchyTermIdSearchJLabel.setText("View Term Hierarchy");
        viewTermHierarchyTermIdSearchJLabel.setEnabled(false);
        viewTermHierarchyTermIdSearchJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                viewTermHierarchyTermIdSearchJLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                viewTermHierarchyTermIdSearchJLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                viewTermHierarchyTermIdSearchJLabelMouseExited(evt);
            }
        });

        termIdPanel.setOpaque(false);
        termIdPanel.setPreferredSize(new java.awt.Dimension(600, 50));

        termIdLabel.setText("Term ID");
        termIdLabel.setPreferredSize(new java.awt.Dimension(58, 14));

        termIdSearchJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        termIdSearchJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                termIdSearchJTextFieldKeyPressed(evt);
            }
        });

        newtSpeciesTipsTermIdSearchJLabel.setFont(newtSpeciesTipsTermIdSearchJLabel.getFont().deriveFont(newtSpeciesTipsTermIdSearchJLabel.getFont().getSize()-1f));
        newtSpeciesTipsTermIdSearchJLabel.setForeground(new java.awt.Color(0, 0, 255));
        newtSpeciesTipsTermIdSearchJLabel.setText("NEWT Species Tips");
        newtSpeciesTipsTermIdSearchJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                newtSpeciesTipsTermIdSearchJLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                newtSpeciesTipsTermIdSearchJLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                newtSpeciesTipsTermIdSearchJLabelMouseExited(evt);
            }
        });

        termIdSearchJButton.setText("Search");
        termIdSearchJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                termIdSearchJButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout termIdPanelLayout = new org.jdesktop.layout.GroupLayout(termIdPanel);
        termIdPanel.setLayout(termIdPanelLayout);
        termIdPanelLayout.setHorizontalGroup(
            termIdPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, termIdPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(termIdPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(newtSpeciesTipsTermIdSearchJLabel)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, termIdPanelLayout.createSequentialGroup()
                        .add(termIdLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(termIdSearchJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(termIdSearchJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 80, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        termIdPanelLayout.setVerticalGroup(
            termIdPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(termIdPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(termIdPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(termIdSearchJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(termIdLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(termIdSearchJButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(newtSpeciesTipsTermIdSearchJLabel))
        );

        org.jdesktop.layout.GroupLayout termIdSearchJPanelLayout = new org.jdesktop.layout.GroupLayout(termIdSearchJPanel);
        termIdSearchJPanel.setLayout(termIdSearchJPanelLayout);
        termIdSearchJPanelLayout.setHorizontalGroup(
            termIdSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, termIdPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(termIdSearchJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(termIdSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(termIdSearchJPanelLayout.createSequentialGroup()
                        .add(searchResultsTermIdLabel)
                        .add(0, 509, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, olsResultsTermIdSearchJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)
                    .add(termDetailsTermIdSearchJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)
                    .add(definitionTermNameIdSearchScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)
                    .add(termIdSearchJPanelLayout.createSequentialGroup()
                        .add(searchTermTermIdLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 414, Short.MAX_VALUE)
                        .add(viewTermHierarchyTermIdSearchJLabel)))
                .addContainerGap())
        );
        termIdSearchJPanelLayout.setVerticalGroup(
            termIdSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, termIdSearchJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(termIdPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(searchResultsTermIdLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(olsResultsTermIdSearchJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(termIdSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(searchTermTermIdLabel)
                    .add(viewTermHierarchyTermIdSearchJLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(definitionTermNameIdSearchScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(termDetailsTermIdSearchJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                .addContainerGap())
        );

        searchTypeJTabbedPane.addTab("Term ID Search", termIdSearchJPanel);

        massSearchJPanel.setBackground(new java.awt.Color(230, 230, 230));

        searchResultsMassSearchLabel.setText("Search Results");

        searchTermMassSearchLabel.setText("Selected Term");

        definitionMassSearchJTextPane.setEditable(false);
        definitionSelectedTermMassSearchScrollPane.setViewportView(definitionMassSearchJTextPane);

        termDetailsMassSearchJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        termDetailsMassSearchJTable.setOpaque(false);
        termDetailsMassSearchJScrollPane.setViewportView(termDetailsMassSearchJTable);

        olsResultsMassSearchJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Accession", "CV Term"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        olsResultsMassSearchJTable.setOpaque(false);
        olsResultsMassSearchJTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                olsResultsMassSearchJTableMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                olsResultsMassSearchJTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                olsResultsMassSearchJTableMouseReleased(evt);
            }
        });
        olsResultsMassSearchJTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                olsResultsMassSearchJTableMouseMoved(evt);
            }
        });
        olsResultsMassSearchJTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                olsResultsMassSearchJTableKeyReleased(evt);
            }
        });
        olsResultsMassSearchJScrollPane.setViewportView(olsResultsMassSearchJTable);

        viewTermHierarchyMassSearchJLabel.setForeground(new java.awt.Color(0, 0, 255));
        viewTermHierarchyMassSearchJLabel.setText("View Term Hierarchy");
        viewTermHierarchyMassSearchJLabel.setEnabled(false);
        viewTermHierarchyMassSearchJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                viewTermHierarchyMassSearchJLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                viewTermHierarchyMassSearchJLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                viewTermHierarchyMassSearchJLabelMouseExited(evt);
            }
        });

        massPanel.setOpaque(false);
        massPanel.setPreferredSize(new java.awt.Dimension(444, 50));

        massLabel.setText("Mass");

        modificationMassJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        modificationMassJTextField.setText("0.0");
        modificationMassJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                modificationMassJTextFieldKeyPressed(evt);
            }
        });

        plussMinusLabel.setText("+-");
        plussMinusLabel.setToolTipText("Mass Accuracy");

        precisionJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        precisionJTextField.setText("0.1");
        precisionJTextField.setToolTipText("Mass Accuracy");
        precisionJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                precisionJTextFieldKeyPressed(evt);
            }
        });

        typeLabel.setText("Type");

        massTypeJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "- Select -", "DiffAvg", "DiffMono", "MassAvg", "MassMono" }));
        massTypeJComboBox.setSelectedIndex(2);
        massTypeJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                massTypeJComboBoxActionPerformed(evt);
            }
        });

        dummyLabelJLabel.setFont(dummyLabelJLabel.getFont().deriveFont(dummyLabelJLabel.getFont().getSize()-1f));
        dummyLabelJLabel.setText(" ");

        modificationMassSearchJButton.setText("Search");
        modificationMassSearchJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modificationMassSearchJButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout massPanelLayout = new org.jdesktop.layout.GroupLayout(massPanel);
        massPanel.setLayout(massPanelLayout);
        massPanelLayout.setHorizontalGroup(
            massPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, massPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(massPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(massPanelLayout.createSequentialGroup()
                        .add(dummyLabelJLabel)
                        .add(200, 200, 200))
                    .add(massPanelLayout.createSequentialGroup()
                        .add(massLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(modificationMassJTextField)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(plussMinusLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(precisionJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 81, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(typeLabel)
                        .add(18, 18, 18)
                        .add(massTypeJComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .add(modificationMassSearchJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 80, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        massPanelLayout.setVerticalGroup(
            massPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, massPanelLayout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(massPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(modificationMassSearchJButton)
                    .add(massTypeJComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(typeLabel)
                    .add(precisionJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(plussMinusLabel)
                    .add(massLabel)
                    .add(modificationMassJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(dummyLabelJLabel))
        );

        org.jdesktop.layout.GroupLayout massSearchJPanelLayout = new org.jdesktop.layout.GroupLayout(massSearchJPanel);
        massSearchJPanel.setLayout(massSearchJPanelLayout);
        massSearchJPanelLayout.setHorizontalGroup(
            massSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, massSearchJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(massSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, termDetailsMassSearchJScrollPane)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, definitionSelectedTermMassSearchScrollPane)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, olsResultsMassSearchJScrollPane)
                    .add(massSearchJPanelLayout.createSequentialGroup()
                        .add(massSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(searchResultsMassSearchLabel)
                            .add(searchTermMassSearchLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 411, Short.MAX_VALUE)
                        .add(viewTermHierarchyMassSearchJLabel)))
                .addContainerGap())
            .add(massPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
        );
        massSearchJPanelLayout.setVerticalGroup(
            massSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, massSearchJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(massPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(searchResultsMassSearchLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(olsResultsMassSearchJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(massSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(searchTermMassSearchLabel)
                    .add(viewTermHierarchyMassSearchJLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(definitionSelectedTermMassSearchScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(termDetailsMassSearchJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                .addContainerGap())
        );

        searchTypeJTabbedPane.addTab("PSI-MOD Mass Search", massSearchJPanel);

        browseOntologyJPanel.setBackground(new java.awt.Color(230, 230, 230));

        selectedTermBrowseLabel.setText("Selected Term");

        definitionBrowseOntologyJTextPane.setEditable(false);
        treeScrollPane.setViewportView(definitionBrowseOntologyJTextPane);

        termDetailsBrowseOntologyJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        termDetailsBrowseOntologyJTable.setOpaque(false);
        termDetailsBrowseOntologyJScrollPane.setViewportView(termDetailsBrowseOntologyJTable);

        browseJPanel.setOpaque(false);
        browseJPanel.setLayout(new javax.swing.BoxLayout(browseJPanel, javax.swing.BoxLayout.LINE_AXIS));

        viewTermHierarchyBrowseOntologyJLabel.setForeground(new java.awt.Color(0, 0, 255));
        viewTermHierarchyBrowseOntologyJLabel.setText("View Term Hierarchy");
        viewTermHierarchyBrowseOntologyJLabel.setEnabled(false);
        viewTermHierarchyBrowseOntologyJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                viewTermHierarchyBrowseOntologyJLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                viewTermHierachyJLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                viewTermHierachyJLabelMouseExited(evt);
            }
        });

        org.jdesktop.layout.GroupLayout browseOntologyJPanelLayout = new org.jdesktop.layout.GroupLayout(browseOntologyJPanel);
        browseOntologyJPanel.setLayout(browseOntologyJPanelLayout);
        browseOntologyJPanelLayout.setHorizontalGroup(
            browseOntologyJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, browseOntologyJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(browseOntologyJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, browseJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, browseOntologyJPanelLayout.createSequentialGroup()
                        .add(selectedTermBrowseLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 414, Short.MAX_VALUE)
                        .add(viewTermHierarchyBrowseOntologyJLabel))
                    .add(treeScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)
                    .add(termDetailsBrowseOntologyJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE))
                .addContainerGap())
        );
        browseOntologyJPanelLayout.setVerticalGroup(
            browseOntologyJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, browseOntologyJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(browseJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(browseOntologyJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(selectedTermBrowseLabel)
                    .add(viewTermHierarchyBrowseOntologyJLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(treeScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(termDetailsBrowseOntologyJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                .addContainerGap())
        );

        searchTypeJTabbedPane.addTab("Browse Ontology", browseOntologyJPanel);

        ontologyJLabel.setText("Ontology");

        ontologyJComboBox.setMaximumRowCount(30);
        ontologyJComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                ontologyJComboBoxItemStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout searchParametersJPanelLayout = new org.jdesktop.layout.GroupLayout(searchParametersJPanel);
        searchParametersJPanel.setLayout(searchParametersJPanelLayout);
        searchParametersJPanelLayout.setHorizontalGroup(
            searchParametersJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(searchParametersJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(searchParametersJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(searchTypeJTabbedPane)
                    .add(searchParametersJPanelLayout.createSequentialGroup()
                        .add(ontologyJLabel)
                        .add(18, 18, 18)
                        .add(ontologyJComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        searchParametersJPanelLayout.setVerticalGroup(
            searchParametersJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(searchParametersJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(searchParametersJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(ontologyJLabel)
                    .add(ontologyJComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(32, 32, 32)
                .add(searchTypeJTabbedPane)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout backgroundPanelLayout = new org.jdesktop.layout.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(backgroundPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(searchParametersJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(backgroundPanelLayout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(helpJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(aboutJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(insertSelectedJButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        backgroundPanelLayout.linkSize(new java.awt.Component[] {cancelJButton, insertSelectedJButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(searchParametersJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(backgroundPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(backgroundPanelLayout.createSequentialGroup()
                        .add(1, 1, 1)
                        .add(backgroundPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(cancelJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(insertSelectedJButton)))
                    .add(aboutJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(helpJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        backgroundPanelLayout.linkSize(new java.awt.Component[] {cancelJButton, insertSelectedJButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(backgroundPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(backgroundPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Updates the search results if the ontology is changed.
     *
     * @param evt
     */
    private void ontologyJComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_ontologyJComboBoxItemStateChanged

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        // insert the ontology label into the term id search field
        if (ontologyJComboBox.getSelectedIndex() != 0 && isPreselectedOption() == false) {
            if (getCurrentOntologyLabel().equalsIgnoreCase("EFO")) {
                termIdSearchJTextField.setText(getCurrentOntologyLabel() + "_");
            } else if (getCurrentOntologyLabel().equalsIgnoreCase("NEWT")) {
                termIdSearchJTextField.setText("");
            } else {
                termIdSearchJTextField.setText(getCurrentOntologyLabel() + ":");
            }
        } else {
            termIdSearchJTextField.setText("");
        }

        if (searchTypeJTabbedPane.getSelectedIndex() != OLS_DIALOG_PSI_MOD_MASS_SEARCH) {

            String currentOntology = (String) ontologyJComboBox.getSelectedItem();

            if (!currentOntology.equalsIgnoreCase(lastSelectedOntology)) {

                lastSelectedOntology = (String) ontologyJComboBox.getSelectedItem();

                currentlySelectedBrowseOntologyAccessionNumber = null;
                currentlySelectedTermNameSearchAccessionNumber = null;
                currentlySelectedTermIdSearchAccessionNumber = null;

                insertSelectedJButton.setEnabled(false);

                // disable the 'browse ontology' tab when 'search in all ontologies' or 'NEWT' is selected or 'search in preselected ontologies'
                if (ontologyJComboBox.getSelectedIndex() == 0 || getCurrentOntologyLabel().equalsIgnoreCase("NEWT") || isPreselectedOption() == true) {
                    searchTypeJTabbedPane.setEnabledAt(OLS_DIALOG_BROWSE_ONTOLOGY, false);

                    // move away from the 'browse ontology' tab if it is disabled and selected
                    if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_BROWSE_ONTOLOGY) {
                        searchTypeJTabbedPane.setSelectedIndex(OLS_DIALOG_TERM_NAME_SEARCH);

                        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

                        if (getCurrentOntologyLabel().equalsIgnoreCase("NEWT")) {
                            JOptionPane.showMessageDialog(this, "Browse Ontology is not available for NEWT.",
                                    "Browse Ontology Disabled", JOptionPane.INFORMATION_MESSAGE);
                        } else if (ontologyJComboBox.getSelectedIndex() == 0) {
                            JOptionPane.showMessageDialog(this, "Browse Ontology is not available when searching several ontologies.",
                                    "Browse Ontology Disabled", JOptionPane.INFORMATION_MESSAGE);
                        }

                        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
                    }

                } else {
                    searchTypeJTabbedPane.setEnabledAt(OLS_DIALOG_BROWSE_ONTOLOGY, true);
                }

                // set the focus
                if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_TERM_NAME_SEARCH) {
                    termNameSearchJTextField.requestFocus();
                } else if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_TERM_ID_SEARCH) {
                    termIdSearchJTextField.requestFocus();
                }

                // make the 'newt species tip' link visible or not visible
                hideOrShowNewtLinks();

                // update the searches
                termNameSearchJTextFieldKeyReleased(null);
                updateBrowseOntologyView();
                clearData(OLS_DIALOG_TERM_ID_SEARCH, true, true);

                viewTermHierarchyTermNameSearchJLabel.setEnabled(false);
                viewTermHierarchyTermIdSearchJLabel.setEnabled(false);
                viewTermHierarchyBrowseOntologyJLabel.setEnabled(false);
            }
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

    }//GEN-LAST:event_ontologyJComboBoxItemStateChanged

    /**
     * Searches the selected ontology for terms matching the inserted string.
     * The search finds all terms having the current string as a substring. (But
     * seems to be limited somehow, seeing as using two letters, can result in
     * more hits, than using just one of the letters...)
     *
     * @param evt
     */
    private void termNameSearchJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_termNameSearchJTextFieldKeyReleased

        keyPressedCounter++;

        new Thread("SearchThread") {

            @Override
            public synchronized void run() {

                try {
                    wait(waitingTime);
                } catch (InterruptedException e) {
                }

                // see if the gui is to be updated or not
                if (keyPressedCounter == 1) {

                    setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
                    termNameSearchJTextField.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

                    insertSelectedJButton.setEnabled(false);
                    currentlySelectedTermNameSearchAccessionNumber = null;

                    try {
                        // clear the old meta data
                        clearData(OLS_DIALOG_TERM_NAME_SEARCH, true, true);

                        // the search is only performed if a certain amount of characters are inserted
                        if (termNameSearchJTextField.getText().length() >= MINIMUM_WORD_LENGTH) {

                            // search the selected ontology and find all matching terms
                            String ontology = getCurrentOntologyLabel();

                            // if 'search in all ontologies' is selected, set the ontology to null
                            if (ontologyJComboBox.getSelectedIndex() == 0) {
                                ontology = null;
                            }

                            Map map = new HashMap();
                            if (isPreselectedOption() == true) {
                                // Ontology terms for preselected Ontologies
                                for (String preselectedOntology : preselectedOntologies.keySet()) {
                                    map.putAll(olsConnection.getTermsByName(termNameSearchJTextField.getText(), preselectedOntology.toUpperCase(), false));
                                }
                            } else {
                                map = olsConnection.getTermsByName(termNameSearchJTextField.getText(), ontology, false);
                            }

                            for (Iterator i = map.keySet().iterator(); i.hasNext();) {
                                String key = (String) i.next();
                                ((DefaultTableModel) olsResultsTermNameSearchJTable.getModel()).addRow(new Object[]{
                                    getOlsAccessionLink(key), map.get(key)});
                            }

                            // set the preferred size of the accession column
                            Integer width = getPreferredColumnWidth(olsResultsTermNameSearchJTable, olsResultsTermNameSearchJTable.getColumn("Accession").getModelIndex(), 6);

                            if (width != null) {
                                olsResultsTermNameSearchJTable.getColumn("Accession").setMinWidth(width);
                                olsResultsTermNameSearchJTable.getColumn("Accession").setMaxWidth(width);
                            } else {
                                olsResultsTermNameSearchJTable.getColumn("Accession").setMinWidth(15);
                                olsResultsTermNameSearchJTable.getColumn("Accession").setMaxWidth(Integer.MAX_VALUE);
                            }

                            termNameSearchJTextField.requestFocus();

                            setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                            termNameSearchJTextField.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
                            numberOfTermsTermNameSearchJTextField.setText("" + map.size());

                            // make the first row visible
                            if (olsResultsTermNameSearchJTable.getRowCount() > 0) {
                                olsResultsTermNameSearchJTable.scrollRectToVisible(olsResultsTermNameSearchJTable.getCellRect(0, 0, false));
                            }

                            //No matching terms found
                            if (map.isEmpty()) {
                                //JOptionPane.showMessageDialog(this, "No mathcing terms found.");
                            }
                        } else {
                            numberOfTermsTermNameSearchJTextField.setText("-");
                        }
                    } catch (RemoteException ex) {
                        JOptionPane.showMessageDialog(
                                null,
                                defaultOlsConnectionFailureErrorMessage,
                                "OLS Connection Error", JOptionPane.ERROR_MESSAGE);
                        Util.writeToErrorLog("Error when trying to access OLS: ");
                        ex.printStackTrace();
                    }

                    setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                    termNameSearchJTextField.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));

                    // gui updated, reset the counter
                    keyPressedCounter = 0;
                } else {
                    // gui not updated, decrease the counter
                    keyPressedCounter--;
                }
            }
        }.start();
    }//GEN-LAST:event_termNameSearchJTextFieldKeyReleased

    /**
     * Inserts the selected ontology into the parents text field or table and
     * then closes the dialog.
     *
     * @param evt
     */
    private void insertSelectedJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertSelectedJButtonActionPerformed

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        String termId = null;

        if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_TERM_NAME_SEARCH) {
            termId = "" + olsResultsTermNameSearchJTable.getValueAt(olsResultsTermNameSearchJTable.getSelectedRow(), 0);
        } else if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_BROWSE_ONTOLOGY) {
            termId = currentlySelectedBrowseOntologyAccessionNumber;
        } else if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_PSI_MOD_MASS_SEARCH) {
            termId = "" + olsResultsMassSearchJTable.getValueAt(olsResultsMassSearchJTable.getSelectedRow(), 0);
        } else if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_TERM_ID_SEARCH) {
            termId = "" + olsResultsTermIdSearchJTable.getValueAt(olsResultsTermIdSearchJTable.getSelectedRow(), 0);
        }

        if (termId != null) {

            // remove the link details
            if (searchTypeJTabbedPane.getSelectedIndex() != OLS_DIALOG_BROWSE_ONTOLOGY) {
                termId = termId.substring(termId.indexOf("termId=") + "termId=".length());
                termId = termId.substring(0, termId.indexOf("\""));
            }

            String ontologyLong = ((String) ontologyJComboBox.getSelectedItem());
            String ontologyShort;

            if (ontologyJComboBox.getSelectedIndex() == 0 || isPreselectedOption() == true) {

                ontologyShort = getOntologyLabelFromTermId(termId);

                if (ontologyShort == null) {
                    ontologyShort = "NEWT";
                    ontologyLong = "NEWT UniProt Taxonomy Database [NEWT]";
                } else {

                    try {
                        Map ontologies = olsConnection.getOntologyNames();
                        ontologyLong = ontologies.get(ontologyShort).toString() + "[" + ontologyShort + "]";
                    } catch (RemoteException ex) {
                        JOptionPane.showMessageDialog(
                                this,
                                defaultOlsConnectionFailureErrorMessage,
                                "OLS Connection Error", JOptionPane.ERROR_MESSAGE);
                        Util.writeToErrorLog("Error when trying to access OLS: ");
                        ex.printStackTrace();
                        ontologyLong = "unknown";
                    }
                }
            } else {
                ontologyShort = ontologyLong.substring(ontologyLong.lastIndexOf("[") + 1, ontologyLong.length() - 1);
            }

            try {
                String selectedValue = olsConnection.getTermById(termId, ontologyShort);

                //insert the value into the correct text field or table
                if (olsInputable != null) {
                    olsInputable.insertOLSResult(field, selectedValue, termId, ontologyShort, ontologyLong, modifiedRow, mappedTerm, metadata);
                    this.setVisible(false);
                    this.dispose();
                }
            } catch (RemoteException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        defaultOlsConnectionFailureErrorMessage,
                        "OLS Connection Error", JOptionPane.ERROR_MESSAGE);
                Util.writeToErrorLog("Error when trying to access OLS: ");
                ex.printStackTrace();
            }
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_insertSelectedJButtonActionPerformed

    /**
     * Updates the information about the selected CV term.
     *
     * @param evt
     * @param searchResultTable
     */
    private void insertTermDetails(java.awt.event.MouseEvent evt, JTable searchResultTable) {

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        int row = searchResultTable.getSelectedRow();

        if (row != -1) {
            insertSelectedJButton.setEnabled(true);
        } else {
            insertSelectedJButton.setEnabled(false);
        }

        boolean doSearch = true;

        if (evt != null) {
            if (evt.getClickCount() == 2 && evt.getButton() == java.awt.event.MouseEvent.BUTTON1) {
                insertSelectedJButtonActionPerformed(null);
                doSearch = false;
            }
        }

        // This does not seem to work... The search is always performed. It
        // seems as the first click in the double click results in one event
        // and the second in another. This results in the term details always
        // beeing retrieved...
        if (doSearch) {
            if (row != -1) {

                Integer searchType = null;

                if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_TERM_NAME_SEARCH) {
                    currentlySelectedTermNameSearchAccessionNumber = (String) searchResultTable.getValueAt(row, 0);
                    searchType = OLS_DIALOG_TERM_NAME_SEARCH;
                } else if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_PSI_MOD_MASS_SEARCH) {
                    currentlySelectedMassSearchAccessionNumber = (String) searchResultTable.getValueAt(row, 0);
                    searchType = OLS_DIALOG_PSI_MOD_MASS_SEARCH;
                } else if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_TERM_ID_SEARCH) {
                    currentlySelectedTermIdSearchAccessionNumber = (String) searchResultTable.getValueAt(row, 0);
                    searchType = OLS_DIALOG_TERM_ID_SEARCH;
                }

                String termID = (String) searchResultTable.getValueAt(row, 0);
                termID = termID.substring(termID.indexOf("termId=") + "termId=".length());
                termID = termID.substring(0, termID.indexOf("\""));
                loadMetaData(termID, searchType);
            }
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }

    /**
     * Closes the dialog.
     *
     * @param evt
     */
    private void cancelJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelJButtonActionPerformed
        if (olsInputable != null) {
            this.setVisible(false);
            this.dispose();
        } else {
            System.exit(0);
        }
    }//GEN-LAST:event_cancelJButtonActionPerformed

    /**
     * Opens a help frame.
     *
     * @param evt
     */
    private void helpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, true, getClass().getResource("/no/uib/olsdialog/helpfiles/OLSDialog.html"));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonActionPerformed

    /**
     * Opens an About frame.
     *
     * @param evt
     */
    private void aboutJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, true, getClass().getResource("/no/uib/olsdialog/helpfiles/AboutOLS.html"));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_aboutJButtonActionPerformed

    /**
     * @see #cancelJButtonActionPerformed(java.awt.event.ActionEvent)
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancelJButtonActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

    /**
     * Returns an array of DataHolder objects that contain data on MOD entries
     * (termId, termName, massDelta) given a massDeltaType and a range of
     * masses.
     *
     * @param massDeltaType the type of massDelta to query (can be null)
     * @param fromMass the lower mass limit (inclusive, mandatory)
     * @param toMass the higher mass limit (inclusive, mandatory)
     * @return
     */
    public DataHolder[] getModificationsByMassDelta(String massDeltaType, double fromMass, double toMass) {

        DataHolder[] retval = null;

        try {
            QueryService locator = new QueryServiceLocator();
            Query service = locator.getOntologyQuery();

            retval = service.getTermsByAnnotationData("MOD", massDeltaType, null, fromMass, toMass);

        } catch (ServiceException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    defaultOlsConnectionFailureErrorMessage,
                    "OLS Connection Error", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to access OLS: ");
            ex.printStackTrace();
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    defaultOlsConnectionFailureErrorMessage,
                    "OLS Connection Error", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to access OLS: ");
            ex.printStackTrace();
        }

        return retval;
    }

    /**
     * Tries to find all terms in the selected ontology that include the
     * selected mass term and has a value within the selected boundaries.
     *
     * @param evt
     */
    private void modificationMassSearchJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modificationMassSearchJButtonActionPerformed
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        insertSelectedJButton.setEnabled(false);
        viewTermHierarchyMassSearchJLabel.setEnabled(false);

        // clear the old data
        clearData(OLS_DIALOG_PSI_MOD_MASS_SEARCH, true, true);

        boolean error = false;
        double currentModificationMass = 0.0;
        double currentAccuracy = 0.1;

        try {
            currentModificationMass = new Double(modificationMassJTextField.getText()).doubleValue();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "The mass is not a number!", "Modification Mass", JOptionPane.INFORMATION_MESSAGE);
            modificationMassJTextField.requestFocus();
            error = true;
        }

        if (!error) {
            try {
                currentAccuracy = new Double(precisionJTextField.getText()).doubleValue();

                if (currentAccuracy < 0) {
                    JOptionPane.showMessageDialog(null,
                            "The precision has to be a positive value.", "Mass Accuracy", JOptionPane.INFORMATION_MESSAGE);
                    precisionJTextField.requestFocus();
                    error = true;
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null,
                        "The precision is not a number!", "Mass Accuracy", JOptionPane.INFORMATION_MESSAGE);
                precisionJTextField.requestFocus();
                error = true;
            }
        }

        if (!error) {

            String massType = massTypeJComboBox.getSelectedItem().toString();

            DataHolder[] results = getModificationsByMassDelta(massType,
                    currentModificationMass - currentAccuracy,
                    currentModificationMass + currentAccuracy);

            if (results != null) {
                for (int i = 0; i < results.length; i++) {
                    ((DefaultTableModel) olsResultsMassSearchJTable.getModel()).addRow(
                            new Object[]{getOlsAccessionLink(results[i].getTermId()),
                                results[i].getTermName()});
                }

                // set the preferred size of the accession column
                Integer width = getPreferredColumnWidth(olsResultsMassSearchJTable, olsResultsMassSearchJTable.getColumn("Accession").getModelIndex(), 6);

                if (width != null) {
                    olsResultsMassSearchJTable.getColumn("Accession").setMinWidth(width);
                    olsResultsMassSearchJTable.getColumn("Accession").setMaxWidth(width);
                } else {
                    olsResultsMassSearchJTable.getColumn("Accession").setMinWidth(15);
                    olsResultsMassSearchJTable.getColumn("Accession").setMaxWidth(Integer.MAX_VALUE);
                }
            }

            this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

            // make the first row visible
            if (olsResultsMassSearchJTable.getRowCount() > 0) {
                olsResultsMassSearchJTable.scrollRectToVisible(olsResultsTermNameSearchJTable.getCellRect(0, 0, false));
            }
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_modificationMassSearchJButtonActionPerformed

    /**
     * Enables or disables the search button based on the selection in the combo
     * box.
     *
     * @param evt
     */
    private void massTypeJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_massTypeJComboBoxActionPerformed
        modificationMassSearchJButton.setEnabled(massTypeJComboBox.getSelectedIndex() != 0);
    }//GEN-LAST:event_massTypeJComboBoxActionPerformed

    /**
     * Makes sure that the PSI-MOD ontology is selected when the modification
     * mass search tab is selected.
     *
     * @param evt
     */
    private void searchTypeJTabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_searchTypeJTabbedPaneStateChanged
        if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_PSI_MOD_MASS_SEARCH) {
            insertSelectedJButton.setEnabled(currentlySelectedMassSearchAccessionNumber != null);
            lastSelectedOntology = (String) ontologyJComboBox.getSelectedItem();
            ontologyJComboBox.setSelectedItem("Protein Modifications (PSI-MOD) [MOD]");
            ontologyJComboBox.setEnabled(false);
        } else if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_BROWSE_ONTOLOGY) {
            insertSelectedJButton.setEnabled(currentlySelectedBrowseOntologyAccessionNumber != null);
            ontologyJComboBox.setSelectedItem(lastSelectedOntology);
            ontologyJComboBox.setEnabled(true);
        } else if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_TERM_NAME_SEARCH) {
            insertSelectedJButton.setEnabled(currentlySelectedTermNameSearchAccessionNumber != null);
            ontologyJComboBox.setSelectedItem(lastSelectedOntology);
            ontologyJComboBox.setEnabled(true);
        } else if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_TERM_ID_SEARCH) {
            insertSelectedJButton.setEnabled(currentlySelectedTermIdSearchAccessionNumber != null);
            ontologyJComboBox.setSelectedItem(lastSelectedOntology);
            ontologyJComboBox.setEnabled(true);
        }
    }//GEN-LAST:event_searchTypeJTabbedPaneStateChanged

    /**
     * @see
     * #olsResultsTermNameSearchJTableMouseClicked(java.awt.event.MouseEvent)
     */
    private void olsResultsTermNameSearchJTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_olsResultsTermNameSearchJTableKeyReleased
        olsResultsTermNameSearchJTableMouseClicked(null);
    }//GEN-LAST:event_olsResultsTermNameSearchJTableKeyReleased

    /**
     * If the user double clicks the selected row is inserted into the parent
     * frame and closes the dialog. A single click retrieves the additional
     * information known about the term and displays it in the "Term Details"
     * frame.
     *
     * @param evt
     */
    private void olsResultsTermNameSearchJTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_olsResultsTermNameSearchJTableMouseClicked
        insertTermDetails(evt, olsResultsTermNameSearchJTable);
    }//GEN-LAST:event_olsResultsTermNameSearchJTableMouseClicked

    /**
     * @see #olsResultsMassSearchJTableMouseClicked(java.awt.event.MouseEvent)
     */
    private void olsResultsMassSearchJTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_olsResultsMassSearchJTableKeyReleased
        olsResultsMassSearchJTableMouseClicked(null);
    }//GEN-LAST:event_olsResultsMassSearchJTableKeyReleased

    /**
     * If the user double clicks the selected row is inserted into the parent
     * frame and closes the dialog. A single click retrieves the additional
     * information known about the term and displays it in the "Term Details"
     * frame.
     *
     * @param evt
     */
    private void olsResultsMassSearchJTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_olsResultsMassSearchJTableMouseClicked
        insertTermDetails(evt, olsResultsMassSearchJTable);
    }//GEN-LAST:event_olsResultsMassSearchJTableMouseClicked

    /**
     * Changes the cursor to the hand cursor when over the term hierarchy link.
     *
     * @param evt
     */
    private void viewTermHierachyJLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewTermHierachyJLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_viewTermHierachyJLabelMouseEntered

    /**
     * Changes the cursor back to the default cursor when leaving the term
     * hierarchy link.
     *
     * @param evt
     */
    private void viewTermHierachyJLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewTermHierachyJLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_viewTermHierachyJLabelMouseExited

    /**
     * Opens a new dialog showing the term hierarchy as a graph.
     *
     * @param evt
     */
    private void viewTermHierarchyBrowseOntologyJLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewTermHierarchyBrowseOntologyJLabelMouseClicked
        viewTermHierarchy();
    }//GEN-LAST:event_viewTermHierarchyBrowseOntologyJLabelMouseClicked

    /**
     * Opens a new dialog showing the term hierarchy as a graph.
     *
     * @param evt
     */
    private void viewTermHierarchyMassSearchJLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewTermHierarchyMassSearchJLabelMouseClicked
        viewTermHierarchy();
    }//GEN-LAST:event_viewTermHierarchyMassSearchJLabelMouseClicked

    /**
     * Changes the cursor to the hand cursor when over the term hierarchy link.
     *
     * @param evt
     */
    private void viewTermHierarchyMassSearchJLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewTermHierarchyMassSearchJLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_viewTermHierarchyMassSearchJLabelMouseEntered

    /**
     * Changes the cursor back to the default cursor when leaving the term
     * hierarchy link.
     *
     * @param evt
     */
    private void viewTermHierarchyMassSearchJLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewTermHierarchyMassSearchJLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_viewTermHierarchyMassSearchJLabelMouseExited

    /**
     * Opens a new dialog showing the term hierarchy as a graph.
     *
     * @param evt
     */
    private void viewTermHierarchyTermNameSearchJLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewTermHierarchyTermNameSearchJLabelMouseClicked
        viewTermHierarchy();
    }//GEN-LAST:event_viewTermHierarchyTermNameSearchJLabelMouseClicked

    /**
     * Changes the cursor to the hand cursor when over the term hierarchy link.
     *
     * @param evt
     */
    private void viewTermHierarchyTermNameSearchJLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewTermHierarchyTermNameSearchJLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_viewTermHierarchyTermNameSearchJLabelMouseEntered

    /**
     * Changes the cursor back to the default cursor when leaving the term
     * hierarchy link.
     *
     * @param evt
     */
    private void viewTermHierarchyTermNameSearchJLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewTermHierarchyTermNameSearchJLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_viewTermHierarchyTermNameSearchJLabelMouseExited

    /**
     * If the user double clicks the selected row is inserted into the parent
     * frame and closes the dialog. A single click retrieves the additional
     * information known about the term and displays it in the "Term Details"
     * frame.
     *
     * @param evt
     */
    private void olsResultsTermIdSearchJTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_olsResultsTermIdSearchJTableMouseClicked
        insertTermDetails(evt, olsResultsTermIdSearchJTable);
    }//GEN-LAST:event_olsResultsTermIdSearchJTableMouseClicked

    /**
     * @see #olsResultsTermIdSearchJTableMouseClicked(java.awt.event.MouseEvent)
     */
    private void olsResultsTermIdSearchJTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_olsResultsTermIdSearchJTableKeyReleased
        olsResultsTermIdSearchJTableMouseClicked(null);
    }//GEN-LAST:event_olsResultsTermIdSearchJTableKeyReleased

    /**
     * Opens a new dialog showing the term hierarchy as a graph.
     *
     * @param evt
     */
    private void viewTermHierarchyTermIdSearchJLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewTermHierarchyTermIdSearchJLabelMouseClicked
        viewTermHierarchy();
    }//GEN-LAST:event_viewTermHierarchyTermIdSearchJLabelMouseClicked

    /**
     * Changes the cursor to the hand cursor when over the term hierarchy link.
     *
     * @param evt
     */
    private void viewTermHierarchyTermIdSearchJLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewTermHierarchyTermIdSearchJLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_viewTermHierarchyTermIdSearchJLabelMouseEntered

    /**
     * Changes the cursor back to the default cursor when leaving the term
     * hierarchy link.
     *
     * @param evt
     */
    private void viewTermHierarchyTermIdSearchJLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewTermHierarchyTermIdSearchJLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_viewTermHierarchyTermIdSearchJLabelMouseExited

    /**
     * Searches for the term matching the inserted accession number and inserts
     * the result into the table.
     *
     * @param evt
     */
    private void termIdSearchJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_termIdSearchJButtonActionPerformed

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        termIdSearchJTextField.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        insertSelectedJButton.setEnabled(false);
        currentlySelectedTermIdSearchAccessionNumber = null;

        try {

            // clear the old data
            clearData(OLS_DIALOG_TERM_ID_SEARCH, true, true);

            // search the selected ontology and find all matching terms
            String ontology = getCurrentOntologyLabel();

            if (ontologyJComboBox.getSelectedIndex() == 0) {
                ontology = null;
            }

            String currentTermName = "";
            if (this.isPreselectedOption() == true) {
                // Ontology term for preselected Ontologies
                preselectedOntologiesLoop:
                for (String preselectedOntology : preselectedOntologies.keySet()) {
                    currentTermName = olsConnection.getTermById(termIdSearchJTextField.getText().trim(), preselectedOntology.toUpperCase());
                    if (currentTermName.length() > 0) {
                        break preselectedOntologiesLoop;
                    }
                }
            } else {
                // Ontology term for one ontology or for all OLS (ontology = null).
                currentTermName = olsConnection.getTermById(termIdSearchJTextField.getText().trim(), ontology);
            }

            if (currentTermName == null) {
                JOptionPane.showMessageDialog(this, "No matching terms found.", "No Matching Terms", JOptionPane.INFORMATION_MESSAGE);
                termIdSearchJTextField.requestFocus();
            } else {

                if (currentTermName.equalsIgnoreCase(termIdSearchJTextField.getText().trim())) {
                    JOptionPane.showMessageDialog(this, "No matching terms found.", "No Matching Terms", JOptionPane.INFORMATION_MESSAGE);
                    termIdSearchJTextField.requestFocus();
                } else {
                    ((DefaultTableModel) olsResultsTermIdSearchJTable.getModel()).addRow(new Object[]{
                        getOlsAccessionLink(termIdSearchJTextField.getText().trim()), currentTermName});

                    // set the preferred size of the accession column
                    Integer width = getPreferredColumnWidth(olsResultsTermIdSearchJTable, olsResultsTermIdSearchJTable.getColumn("Accession").getModelIndex(), 6);

                    if (width != null) {
                        olsResultsTermIdSearchJTable.getColumn("Accession").setMinWidth(width);
                        olsResultsTermIdSearchJTable.getColumn("Accession").setMaxWidth(width);
                    } else {
                        olsResultsTermIdSearchJTable.getColumn("Accession").setMinWidth(15);
                        olsResultsTermIdSearchJTable.getColumn("Accession").setMaxWidth(Integer.MAX_VALUE);
                    }
                }
            }

            this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            termIdSearchJTextField.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));

            // make the first row visible
            if (olsResultsTermIdSearchJTable.getRowCount() > 0) {
                olsResultsTermIdSearchJTable.scrollRectToVisible(olsResultsTermIdSearchJTable.getCellRect(0, 0, false));
            }

        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    defaultOlsConnectionFailureErrorMessage,
                    "OLS Connection Error", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to access OLS: ");
            ex.printStackTrace();
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        termIdSearchJTextField.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));

    }//GEN-LAST:event_termIdSearchJButtonActionPerformed

    /**
     * If 'Enter' is pressed and the 'Search' button is enabled, the search is
     * performed.
     *
     * @param evt
     */
    private void modificationMassJTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_modificationMassJTextFieldKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (modificationMassSearchJButton.isEnabled()) {
                modificationMassSearchJButtonActionPerformed(null);
            }
        }
    }//GEN-LAST:event_modificationMassJTextFieldKeyPressed

    /**
     * @see #modificationMassJTextFieldKeyPressed(java.awt.event.KeyEvent)
     */
    private void precisionJTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_precisionJTextFieldKeyPressed
        modificationMassJTextFieldKeyPressed(evt);
    }//GEN-LAST:event_precisionJTextFieldKeyPressed

    /**
     * If 'Enter' is pressed and the 'Search' button is enabled the search is
     * performed. Also enables or disables the search button when the field
     * contains text or not.
     *
     * @param evt
     */
    private void termIdSearchJTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_termIdSearchJTextFieldKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (termIdSearchJButton.isEnabled()) {
                termIdSearchJButtonActionPerformed(null);
            }
        } else {
            termIdSearchJButton.setEnabled(termIdSearchJTextField.getText().length() > 0);
        }
    }//GEN-LAST:event_termIdSearchJTextFieldKeyPressed

    /**
     * Opens a dialog displaying the most common species for easy selection.
     *
     * @param evt
     */
    private void newtSpeciesTipsTermNameSearchJLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_newtSpeciesTipsTermNameSearchJLabelMouseClicked
        if (newtSpeciesTipsTermNameSearchJLabel.getForeground() == Color.BLUE) {
            new SimpleNewtSelection(this, true);
        }
    }//GEN-LAST:event_newtSpeciesTipsTermNameSearchJLabelMouseClicked

    /**
     * Changes the cursor to the hand cursor when over the term hierarchy link.
     *
     * @param evt
     */
    private void newtSpeciesTipsTermNameSearchJLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_newtSpeciesTipsTermNameSearchJLabelMouseEntered
        if (newtSpeciesTipsTermNameSearchJLabel.getForeground() == Color.BLUE) {
            this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    }//GEN-LAST:event_newtSpeciesTipsTermNameSearchJLabelMouseEntered

    /**
     * Changes the cursor back to the default cursor when leaving the term
     * hierarchy link.
     *
     * @param evt
     */
    private void newtSpeciesTipsTermNameSearchJLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_newtSpeciesTipsTermNameSearchJLabelMouseExited
        if (newtSpeciesTipsTermNameSearchJLabel.getForeground() == Color.BLUE) {
            this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }
    }//GEN-LAST:event_newtSpeciesTipsTermNameSearchJLabelMouseExited

    /**
     * Opens a dialog displaying the most common species for easy selection.
     *
     * @param evt
     */
    private void newtSpeciesTipsTermIdSearchJLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_newtSpeciesTipsTermIdSearchJLabelMouseClicked
        if (newtSpeciesTipsTermIdSearchJLabel.getForeground() == Color.BLUE) {
            new SimpleNewtSelection(this, true);
        }
    }//GEN-LAST:event_newtSpeciesTipsTermIdSearchJLabelMouseClicked

    /**
     * Changes the cursor to the hand cursor when over the term hierarchy link.
     *
     * @param evt
     */
    private void newtSpeciesTipsTermIdSearchJLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_newtSpeciesTipsTermIdSearchJLabelMouseEntered
        if (newtSpeciesTipsTermIdSearchJLabel.getForeground() == Color.BLUE) {
            this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    }//GEN-LAST:event_newtSpeciesTipsTermIdSearchJLabelMouseEntered

    /**
     * Changes the cursor back to the default cursor when leaving the term
     * hierarchy link.
     *
     * @param evt
     */
    private void newtSpeciesTipsTermIdSearchJLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_newtSpeciesTipsTermIdSearchJLabelMouseExited
        if (newtSpeciesTipsTermIdSearchJLabel.getForeground() == Color.BLUE) {
            this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }
    }//GEN-LAST:event_newtSpeciesTipsTermIdSearchJLabelMouseExited

    /**
     * Changes the cursor to the hand cursor when over the help icon.
     *
     * @param evt
     */
    private void helpJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_helpJButtonMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_helpJButtonMouseEntered

    /**
     * Changes the cursor back to the default cursor.
     *
     * @param evt
     */
    private void helpJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_helpJButtonMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpJButtonMouseExited

    /**
     * Changes the cursor to the hand cursor when over the about icon.
     *
     * @param evt
     */
    private void aboutJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_aboutJButtonMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_aboutJButtonMouseEntered

    /**
     * Changes the cursor back to the default cursor.
     *
     * @param evt
     */
    private void aboutJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_aboutJButtonMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_aboutJButtonMouseExited

    /**
     * Changes the cursor to a hand cursor if over the accession column.
     *
     * @param evt
     */
    private void olsResultsTermNameSearchJTableMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_olsResultsTermNameSearchJTableMouseMoved
        int row = olsResultsTermNameSearchJTable.rowAtPoint(evt.getPoint());
        int column = olsResultsTermNameSearchJTable.columnAtPoint(evt.getPoint());

        if (row != -1) {
            if (column == olsResultsTermNameSearchJTable.getColumn("Accession").getModelIndex() && olsResultsTermNameSearchJTable.getValueAt(row, column) != null) {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            } else {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        }
    }//GEN-LAST:event_olsResultsTermNameSearchJTableMouseMoved

    /**
     * Changes the cursor to a hand cursor if over the accession column.
     *
     * @param evt
     */
    private void olsResultsTermIdSearchJTableMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_olsResultsTermIdSearchJTableMouseMoved
        int row = olsResultsTermIdSearchJTable.rowAtPoint(evt.getPoint());
        int column = olsResultsTermIdSearchJTable.columnAtPoint(evt.getPoint());

        if (row != -1) {
            if (column == olsResultsTermIdSearchJTable.getColumn("Accession").getModelIndex() && olsResultsTermIdSearchJTable.getValueAt(row, column) != null) {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            } else {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        }
    }//GEN-LAST:event_olsResultsTermIdSearchJTableMouseMoved

    /**
     * Changes the cursor to a hand cursor if over the accession column.
     *
     * @param evt
     */
    private void olsResultsMassSearchJTableMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_olsResultsMassSearchJTableMouseMoved
        int row = olsResultsMassSearchJTable.rowAtPoint(evt.getPoint());
        int column = olsResultsMassSearchJTable.columnAtPoint(evt.getPoint());

        if (row != -1) {
            if (column == olsResultsMassSearchJTable.getColumn("Accession").getModelIndex() && olsResultsMassSearchJTable.getValueAt(row, column) != null) {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            } else {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        }
    }//GEN-LAST:event_olsResultsMassSearchJTableMouseMoved

    /**
     * Changes the cursor back to the default cursor.
     *
     * @param evt
     */
    private void olsResultsTermNameSearchJTableMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_olsResultsTermNameSearchJTableMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_olsResultsTermNameSearchJTableMouseExited

    /**
     * Changes the cursor back to the default cursor.
     *
     * @param evt
     */
    private void olsResultsTermIdSearchJTableMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_olsResultsTermIdSearchJTableMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_olsResultsTermIdSearchJTableMouseExited

    /**
     * Changes the cursor back to the default cursor.
     *
     * @param evt
     */
    private void olsResultsMassSearchJTableMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_olsResultsMassSearchJTableMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_olsResultsMassSearchJTableMouseExited

    /**
     * Open the accession number link in the web browser.
     *
     * @param evt
     */
    private void olsResultsTermNameSearchJTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_olsResultsTermNameSearchJTableMouseReleased
        int row = olsResultsTermNameSearchJTable.rowAtPoint(evt.getPoint());
        int column = olsResultsTermNameSearchJTable.columnAtPoint(evt.getPoint());

        if (row != -1) {
            if (column == olsResultsTermNameSearchJTable.getColumn("Accession").getModelIndex()) {
                // open protein link in web browser
                if (column == olsResultsTermNameSearchJTable.getColumn("Accession").getModelIndex() && evt != null && evt.getButton() == MouseEvent.BUTTON1) {
                    if (((String) olsResultsTermNameSearchJTable.getValueAt(row, column)).lastIndexOf("<html>") != -1) {
                        String link = (String) olsResultsTermNameSearchJTable.getValueAt(row, column);
                        link = link.substring(link.indexOf("\"") + 1);
                        link = link.substring(0, link.indexOf("\""));

                        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
                        BareBonesBrowserLaunch.openURL(link);
                        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                    }
                }
            }
        }
    }//GEN-LAST:event_olsResultsTermNameSearchJTableMouseReleased

    /**
     * Open the accession number link in the web browser.
     *
     * @param evt
     */
    private void olsResultsTermIdSearchJTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_olsResultsTermIdSearchJTableMouseReleased
        int row = olsResultsTermIdSearchJTable.rowAtPoint(evt.getPoint());
        int column = olsResultsTermIdSearchJTable.columnAtPoint(evt.getPoint());

        if (row != -1) {
            if (column == olsResultsTermIdSearchJTable.getColumn("Accession").getModelIndex()) {
                // open protein link in web browser
                if (column == olsResultsTermIdSearchJTable.getColumn("Accession").getModelIndex() && evt != null && evt.getButton() == MouseEvent.BUTTON1) {
                    if (((String) olsResultsTermIdSearchJTable.getValueAt(row, column)).lastIndexOf("<html>") != -1) {
                        String link = (String) olsResultsTermIdSearchJTable.getValueAt(row, column);
                        link = link.substring(link.indexOf("\"") + 1);
                        link = link.substring(0, link.indexOf("\""));

                        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
                        BareBonesBrowserLaunch.openURL(link);
                        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                    }
                }
            }
        }
    }//GEN-LAST:event_olsResultsTermIdSearchJTableMouseReleased

    /**
     * Open the accession number link in the web browser.
     *
     * @param evt
     */
    private void olsResultsMassSearchJTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_olsResultsMassSearchJTableMouseReleased
        int row = olsResultsMassSearchJTable.rowAtPoint(evt.getPoint());
        int column = olsResultsMassSearchJTable.columnAtPoint(evt.getPoint());

        if (row != -1) {
            if (column == olsResultsMassSearchJTable.getColumn("Accession").getModelIndex()) {
                // open protein link in web browser
                if (column == olsResultsMassSearchJTable.getColumn("Accession").getModelIndex() && evt != null && evt.getButton() == MouseEvent.BUTTON1) {
                    if (((String) olsResultsMassSearchJTable.getValueAt(row, column)).lastIndexOf("<html>") != -1) {
                        String link = (String) olsResultsMassSearchJTable.getValueAt(row, column);
                        link = link.substring(link.indexOf("\"") + 1);
                        link = link.substring(0, link.indexOf("\""));

                        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
                        BareBonesBrowserLaunch.openURL(link);
                        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                    }
                }
            }
        }
    }//GEN-LAST:event_olsResultsMassSearchJTableMouseReleased

    /**
     * Opens a new dialog showing the term hierarchy as a graph.
     */
    private void viewTermHierarchy() {

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        String accession = null;

        if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_TERM_NAME_SEARCH) {
            accession = "" + olsResultsTermNameSearchJTable.getValueAt(olsResultsTermNameSearchJTable.getSelectedRow(), 0);
        } else if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_BROWSE_ONTOLOGY) {
            accession = currentlySelectedBrowseOntologyAccessionNumber;
        } else if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_PSI_MOD_MASS_SEARCH) {
            accession = "" + olsResultsMassSearchJTable.getValueAt(olsResultsMassSearchJTable.getSelectedRow(), 0);
        } else if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_TERM_ID_SEARCH) {
            accession = "" + olsResultsTermIdSearchJTable.getValueAt(olsResultsTermIdSearchJTable.getSelectedRow(), 0);
        }

        if (accession != null) {

            // remove the link details
            if (searchTypeJTabbedPane.getSelectedIndex() != OLS_DIALOG_BROWSE_ONTOLOGY) {
                accession = accession.substring(accession.indexOf("termId=") + "termId=".length());
                accession = accession.substring(0, accession.indexOf("\""));
            }

            String selectedValue = "";
            String ontology = getCurrentOntologyLabel();

            try {
                selectedValue = olsConnection.getTermById(accession, ontology);
            } catch (RemoteException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        defaultOlsConnectionFailureErrorMessage,
                        "OLS Connection Error", JOptionPane.ERROR_MESSAGE);
                Util.writeToErrorLog("Error when trying to access OLS: ");
                ex.printStackTrace();
            }

            new TermHierarchyGraphViewer(this, true, accession, selectedValue, ontology);
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }

    /**
     * Returns true if the preselected ontologies index is selected.
     *
     * @return true if the preselected ontologies index is selected
     */
    private boolean isPreselectedOption() {
        return preselectedOntologies.size() > 1 && ontologyJComboBox.getSelectedIndex() == 1;
    }

    /**
     * Inserts a NEWT term into the currently opened search tab.
     *
     * @param termName the term name
     * @param termId the terms id
     */
    public void insertNewtSelection(String termName, String termId) {

        if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_TERM_NAME_SEARCH) {
            termNameSearchJTextField.setText(termName);
            termNameSearchJTextFieldKeyReleased(null);
        } else if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_TERM_ID_SEARCH) {
            termIdSearchJTextField.setText(termId);
            termIdSearchJButtonActionPerformed(null);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutJButton;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JPanel browseJPanel;
    private javax.swing.JPanel browseOntologyJPanel;
    private javax.swing.JButton cancelJButton;
    private javax.swing.JTextPane definitionBrowseOntologyJTextPane;
    private javax.swing.JTextPane definitionMassSearchJTextPane;
    private javax.swing.JScrollPane definitionSelectedTermMassSearchScrollPane;
    private javax.swing.JTextPane definitionTermIdSearchJTextPane;
    private javax.swing.JScrollPane definitionTermNameIdSearchScrollPane;
    private javax.swing.JScrollPane definitionTermNameScrollPane;
    private javax.swing.JTextPane definitionTermNameSearchJTextPane;
    private javax.swing.JLabel dummyLabelJLabel;
    private javax.swing.JButton helpJButton;
    private javax.swing.JButton insertSelectedJButton;
    private javax.swing.JLabel massLabel;
    private javax.swing.JPanel massPanel;
    private javax.swing.JPanel massSearchJPanel;
    private javax.swing.JComboBox massTypeJComboBox;
    private javax.swing.JTextField modificationMassJTextField;
    private javax.swing.JButton modificationMassSearchJButton;
    private javax.swing.JLabel newtSpeciesTipsTermIdSearchJLabel;
    private javax.swing.JLabel newtSpeciesTipsTermNameSearchJLabel;
    private javax.swing.JTextField numberOfTermsTermNameSearchJTextField;
    private javax.swing.JScrollPane olsResultsMassSearchJScrollPane;
    private javax.swing.JTable olsResultsMassSearchJTable;
    private javax.swing.JScrollPane olsResultsTermIdSearchJScrollPane;
    private javax.swing.JTable olsResultsTermIdSearchJTable;
    private javax.swing.JScrollPane olsResultsTermNameSearchJScrollPane;
    private javax.swing.JTable olsResultsTermNameSearchJTable;
    private javax.swing.JComboBox ontologyJComboBox;
    private javax.swing.JLabel ontologyJLabel;
    private javax.swing.JLabel plussMinusLabel;
    private javax.swing.JTextField precisionJTextField;
    private javax.swing.JPanel searchParametersJPanel;
    private javax.swing.JLabel searchResultsMassSearchLabel;
    private javax.swing.JLabel searchResultsTermIdLabel;
    private javax.swing.JLabel searchResultsTermNameJLabel;
    private javax.swing.JLabel searchTermMassSearchLabel;
    private javax.swing.JLabel searchTermTermIdLabel;
    private javax.swing.JTabbedPane searchTypeJTabbedPane;
    private javax.swing.JLabel selectedTermBrowseLabel;
    private javax.swing.JLabel selectedTermTermNameJLabel;
    private javax.swing.JScrollPane termDetailsBrowseOntologyJScrollPane;
    private javax.swing.JTable termDetailsBrowseOntologyJTable;
    private javax.swing.JScrollPane termDetailsMassSearchJScrollPane;
    private javax.swing.JTable termDetailsMassSearchJTable;
    private javax.swing.JScrollPane termDetailsTermIdSearchJScrollPane;
    private javax.swing.JTable termDetailsTermIdSearchJTable;
    private javax.swing.JScrollPane termDetailsTermNameSearchJScrollPane;
    private javax.swing.JTable termDetailsTermNameSearchJTable;
    private javax.swing.JLabel termIdLabel;
    private javax.swing.JPanel termIdPanel;
    private javax.swing.JButton termIdSearchJButton;
    private javax.swing.JPanel termIdSearchJPanel;
    private javax.swing.JTextField termIdSearchJTextField;
    private javax.swing.JLabel termNameJLabel;
    private javax.swing.JPanel termNameJPanel;
    private javax.swing.JPanel termNameSearchJPanel;
    private javax.swing.JTextField termNameSearchJTextField;
    private javax.swing.JScrollPane treeScrollPane;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JLabel viewTermHierarchyBrowseOntologyJLabel;
    private javax.swing.JLabel viewTermHierarchyMassSearchJLabel;
    private javax.swing.JLabel viewTermHierarchyTermIdSearchJLabel;
    private javax.swing.JLabel viewTermHierarchyTermNameSearchJLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Returns the protein accession number as a web link to the given PSI-MOD
     * at http://www.ebi.ac.uk/ontology-lookup.
     *
     * @param modAccession the PSI-MOD accession number
     * @return the OLS web link
     */
    public String getOlsAccessionLink(String modAccession) {
        String accessionNumberWithLink = "<html><a href=\"http://www.ebi.ac.uk/ontology-lookup/?termId=" + modAccession + "\""
                + "\"><font color=\"" + notSelectedRowHtmlTagFontColor + "\">"
                + modAccession + "</font></a></html>";
        return accessionNumberWithLink;
    }

    /**
     * Gets the preferred width of the column specified by colIndex. The column
     * will be just wide enough to show the column head and the widest cell in
     * the column. Margin pixels are added to the left and right (resulting in
     * an additional width of 2*margin pixels. <br> Note that this method
     * iterates all rows in the table to get the perfect width of the column!
     *
     * @param table the table
     * @param colIndex the colum index
     * @param margin the margin to add
     * @return the preferred width of the column
     */
    public int getPreferredColumnWidth(JTable table, int colIndex, int margin) {

        DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
        TableColumn col = colModel.getColumn(colIndex);

        // get width of column header
        TableCellRenderer renderer = col.getHeaderRenderer();
        if (renderer == null) {
            renderer = table.getTableHeader().getDefaultRenderer();
        }

        Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, 0);
        int width = comp.getPreferredSize().width;

        for (int r = 0; r < table.getRowCount(); r++) {
            renderer = table.getCellRenderer(r, colIndex);
            comp = renderer.getTableCellRendererComponent(
                    table, table.getValueAt(r, colIndex), false, false, r, colIndex);
            width = Math.max(width, comp.getPreferredSize().width);
        }

        // add margin
        width += 2 * margin;

        return width;
    }
}
