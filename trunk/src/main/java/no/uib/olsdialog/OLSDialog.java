package no.uib.olsdialog;

import java.awt.Component;
import no.uib.olsdialog.util.MyComboBoxRenderer;
import no.uib.olsdialog.util.HelpDialog;
import no.uib.olsdialog.util.Util;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.SkyKrupp;
import java.awt.event.KeyEvent;
import java.rmi.RemoteException;
import java.util.HashMap;
import javax.xml.rpc.ServiceException;
import uk.ac.ebi.ook.web.services.Query;
import uk.ac.ebi.ook.web.services.QueryService;
import uk.ac.ebi.ook.web.services.QueryServiceLocator;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import no.uib.olsdialog.util.TermHierarchyGraphViewer;
import no.uib.olsdialog.util.TreeBrowser;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import uk.ac.ebi.ook.web.model.DataHolder;

/**
 * A dialog for interacting with the Ontology Lookup Service OLS 
 * (http://www.ebi.ac.uk/ontology-lookup).
 *
 * @author  Harald Barsnes
 * 
 * Created: March 2008
 * Revised: July 2009
 */
public class OLSDialog extends javax.swing.JDialog {

    public static final boolean debug = false;
    private final String olsDialogVersionNumber = "3.2";
    private String field;
    private String selectedOntology;
    private int modifiedRow = -1;
    private OLSInputable olsInputable;
    private String mappedTerm;
    private String defaultOlsConnectionFailureErrorMessage =
            "An error occured when trying to contact the OLS. Make sure that\n" +
            "you are online. Also check your firewall (and proxy) settings.\n\n" +
            "See the Troubleshooting section at the OLS Dialog home page\n" +
            "for details: http://ols-dialog.googlecode.com.";
    public static final Integer OLS_DIALOG_TERM_NAME_SEARCH = 0;
    public static final Integer OLS_DIALOG_TERM_ID_SEARCH = 1;
    public static final Integer OLS_DIALOG_PSI_MOD_MASS_SEARCH = 2;
    public static final Integer OLS_DIALOG_BROWSE_ONTOLOGY = 3;
    private static Query olsConnection;
    private TreeBrowser treeBrowser;
    private String currentlySelectedBrowseOntologyAccessionNumber = null;
    private String currentlySelectedTermNameSearchAccessionNumber = null;
    private String currentlySelectedTermIdSearchAccessionNumber = null;
    private String currentlySelectedMassSearchAccessionNumber = null;
    private String lastSelectedOntology = null;
    private final int MAX_TOOL_TIP_LENGTH = 40;

    /**
     * Opens a dialog that lets you search for terms using the OLS.
     * 
     * @param parent the parent JFrame
     * @param olsInputable a reference to the frame using the OLS Dialog
     * @param modal
     * @param field the name of the field to insert the results into
     * @param selectedOntology the name of the ontology to search in
     * @param term the term to search for
     */
    public OLSDialog(JFrame parent, OLSInputable olsInputable, boolean modal, String field,
            String selectedOntology, String term) {
        this(parent, olsInputable, modal, field, selectedOntology, -1, term, null, null, 0);
    }

    /**
     * Opens a dialog that lets you search for terms using the OLS.
     *
     * @param parent the parent JDialog
     * @param olsInputable a reference to the frame using the OLS Dialog
     * @param modal
     * @param field the name of the field to insert the results into
     * @param selectedOntology the name of the ontology to search in
     * @param term the term to search for
     */
    public OLSDialog(JDialog parent, OLSInputable olsInputable, boolean modal, String field,
            String selectedOntology, String term) {
        this(parent, olsInputable, modal, field, selectedOntology, -1, term, null, null, 0);
    }

    /**
     * Opens a dialog that lets you search for terms using the OLS.
     *
     * @param parent the parent JFrame
     * @param olsInputable a reference to the frame using the OLS Dialog
     * @param modal
     * @param field the name of the field to insert the results into
     * @param selectedOntology the name of the ontology to search in
     * @param modifiedRow the row to modify, use -1 if adding a new row
     * @param term the term to search for
     */
    public OLSDialog(JFrame parent, OLSInputable olsInputable, boolean modal, String field,
            String selectedOntology, int modifiedRow, String term) {
        this(parent, olsInputable, modal, field, selectedOntology, modifiedRow, term, null, null, 0);
    }

    /**
     * Opens a dialog that lets you search for terms using the OLS.
     *
     * @param parent the parent JDialog
     * @param olsInputable a reference to the frame using the OLS Dialog
     * @param modal
     * @param field the name of the field to insert the results into
     * @param selectedOntology the name of the ontology to search in
     * @param modifiedRow the row to modify, use -1 if adding a new row
     * @param term the term to search for
     */
    public OLSDialog(JDialog parent, OLSInputable olsInputable, boolean modal, String field,
            String selectedOntology, int modifiedRow, String term) {
        this(parent, olsInputable, modal, field, selectedOntology, modifiedRow, term, null, null, 0);
    }

    /**
     * Opens a dialog that lets you search for terms using the OLS.
     * 
     * @param parent the parent JFrame
     * @param olsInputable a reference to the frame using the OLS Dialog
     * @param modal
     * @param field the name of the field to insert the results into
     * @param selectedOntology the name of the ontology to search in
     * @param modifiedRow the row to modify, use -1 if adding a new row
     * @param term the term to search for
     * @param modificationMass the mass of the modification
     * @param modificationAccuracy the mass accuracy
     * @param searchType one of the following: OLS_DIALOG_TERM_NAME_SEARCH, OLS_DIALOG_TERM_ID_SEARCH,
     *                     OLS_DIALOG_BROWSE_ONTOLOGY or OLS_DIALOG_PSI_MOD_MASS_SEARCH
     */
    public OLSDialog(JFrame parent, OLSInputable olsInputable, boolean modal, String field,
            String selectedOntology, int modifiedRow, String term,
            Double modificationMass, Double modificationAccuracy, Integer searchType) {
        super(parent, modal);

        this.olsInputable = olsInputable;
        this.field = field;
        this.selectedOntology = selectedOntology;
        this.modifiedRow = modifiedRow;
        this.mappedTerm = term;

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
     * @param selectedOntology the name of the ontology to search in
     * @param modifiedRow the row to modify, use -1 if adding a new row
     * @param term the term to search for
     * @param modificationMass the mass of the modification
     * @param modificationAccuracy the mass accuracy
     * @param searchType one of the following: OLS_DIALOG_TERM_NAME_SEARCH, OLS_DIALOG_TERM_ID_SEARCH,
     *                     OLS_DIALOG_BROWSE_ONTOLOGY or OLS_DIALOG_PSI_MOD_MASS_SEARCH
     */
    public OLSDialog(JDialog parent, OLSInputable olsInputable, boolean modal, String field,
            String selectedOntology, int modifiedRow, String term,
            Double modificationMass, Double modificationAccuracy, Integer searchType) {
        super(parent, modal);

        this.olsInputable = olsInputable;
        this.field = field;
        this.selectedOntology = selectedOntology;
        this.modifiedRow = modifiedRow;
        this.mappedTerm = term;

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
     * Includes code used by all constructors to set up the frame, e.g., handling column tooltips etc.
     */
    private void setUpFrame(Integer searchType) {

        initComponents();

        setLookAndFeel();

        setTitle("Ontology Lookup Service - (ols-dialog v" + olsDialogVersionNumber + ")");

        // initialize the tree browser
        treeBrowser = new TreeBrowser(this);
        browseJPanel.add(treeBrowser);

        // open the requested search type pane
        searchTypeJTabbedPane.setSelectedIndex(searchType);

        // use combobox renderer that centers the text
        ontologyJComboBox.setRenderer(new MyComboBoxRenderer(null, SwingConstants.CENTER));
        massTypeJComboBox.setRenderer(new MyComboBoxRenderer(null, SwingConstants.CENTER));

        // disable reordring of the columns
        olsResultsTermNameSearchJXTable.getTableHeader().setReorderingAllowed(false);
        olsResultsMassSearchJXTable.getTableHeader().setReorderingAllowed(false);
        termDetailsTermNameSearchJXTable.getTableHeader().setReorderingAllowed(false);
        termDetailsMassSearchJXTable.getTableHeader().setReorderingAllowed(false);
        termDetailsBrowseOntologyJXTable.getTableHeader().setReorderingAllowed(false);

        // show tooltip if content in the value column is longer than 50 characters
        termDetailsTermNameSearchJXTable.getColumn(1).setCellRenderer(new DefaultTableRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                setTableToolTip(table, value, row, column);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        termDetailsMassSearchJXTable.getColumn(1).setCellRenderer(new DefaultTableRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                setTableToolTip(table, value, row, column);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        termDetailsBrowseOntologyJXTable.getColumn(1).setCellRenderer(new DefaultTableRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                setTableToolTip(table, value, row, column);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        // only works for Java 1.6 and newer
//        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
//                getResource("/no/uib/olsdialog/icons/ols_transparent_small.GIF")));

    }

    /**
     * Calls OLS webserver and gets root terms of an ontology
     * 
     * @return Map of root terms - key is termId, value is termName. Map should not be null.
     */
    public Map<String, String> getOntologyRoots(String ontology) {

        Map<String, String> retrievedValues = new HashMap<String, String>();

        try {
            HashMap roots = olsConnection.getRootTerms(ontology);

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
     * Clears the meta data section for the selected search type
     *
     * @param searchType the search type to clear the meta data for
     * @param clearSearchResults if true the search results table is cleared
     * @param clearMetaData if true the meta data is cleared
     */
    private void clearData(Integer searchType, boolean clearSearchResults, boolean clearMetaData) {

        JTextPane currentDefinitionsJTextPane = null;
        JXTable currentTermDetailsJXTable = null;
        JScrollPane currentTermDetailsJScrollPane = null;
        JXTable currentSearchResultsJXTable = null;
        JScrollPane currentSearchResultsJScrollPane = null;

        if (searchType == OLS_DIALOG_TERM_NAME_SEARCH) {
            currentlySelectedTermNameSearchAccessionNumber = null;
            currentSearchResultsJXTable = olsResultsTermNameSearchJXTable;
            currentSearchResultsJScrollPane = olsResultsTermNameSearchJScrollPane;
            currentDefinitionsJTextPane = definitionTermNameSearchJTextPane;
            currentTermDetailsJXTable = termDetailsTermNameSearchJXTable;
            currentTermDetailsJScrollPane = termDetailsTermNameSearchJScrollPane;
        } else if (searchType == OLS_DIALOG_PSI_MOD_MASS_SEARCH) {
            currentlySelectedMassSearchAccessionNumber = null;
            currentSearchResultsJXTable = olsResultsMassSearchJXTable;
            currentSearchResultsJScrollPane = olsResultsMassSearchJScrollPane;
            currentDefinitionsJTextPane = definitionMassSearchJTextPane;
            currentTermDetailsJXTable = termDetailsMassSearchJXTable;
            currentTermDetailsJScrollPane = termDetailsMassSearchJScrollPane;
        } else if (searchType == OLS_DIALOG_BROWSE_ONTOLOGY) {
            currentlySelectedBrowseOntologyAccessionNumber = null;
            currentDefinitionsJTextPane = definitionBrowseOntologyJTextPane;
            currentTermDetailsJXTable = termDetailsBrowseOntologyJXTable;
            currentTermDetailsJScrollPane = termDetailsBrowseOntologyJScrollPane;
        } else if (searchType == OLS_DIALOG_TERM_ID_SEARCH) {
            currentlySelectedTermIdSearchAccessionNumber = null;
            currentSearchResultsJXTable = olsResultsTermIdSearchJXTable;
            currentSearchResultsJScrollPane = olsResultsTermIdSearchJScrollPane;
            currentDefinitionsJTextPane = definitionTermIdSearchJTextPane;
            currentTermDetailsJXTable = termDetailsTermIdSearchJXTable;
            currentTermDetailsJScrollPane = termDetailsTermIdSearchJScrollPane;
        }

        if (clearMetaData) {
            currentDefinitionsJTextPane.setText("");

            while (currentTermDetailsJXTable.getRowCount() > 0) {
                ((DefaultTableModel) currentTermDetailsJXTable.getModel()).removeRow(0);
            }

            currentTermDetailsJScrollPane.getVerticalScrollBar().setValue(0);
        }

        if (clearSearchResults) {
            if (searchType != OLS_DIALOG_BROWSE_ONTOLOGY) {

                while (currentSearchResultsJXTable.getRowCount() > 0) {
                    ((DefaultTableModel) currentSearchResultsJXTable.getModel()).removeRow(0);
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
     * @return true if the terms was loaded sucessfully, false otherwise
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

        if (!error && !childTerms.isEmpty()) {

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
     * @return the ontology label extracted from the term id, or null if no ontology is found
     */
    private String getOntologyLabelFromTermId(String termId) {

        String ontologyLabel = null;

        if (termId.lastIndexOf(":") != -1) {
            ontologyLabel = termId.substring(0, termId.lastIndexOf(":"));
        } else if (termId.lastIndexOf("_") != -1) { // needed for EFO
            ontologyLabel = termId.substring(0, termId.lastIndexOf("_"));
        } else{
            ontologyLabel = "NEWT"; // TODO: verify that this is correct
        }

        return ontologyLabel;
    }

    /**
     * Load metadata for a given termiId.
     *
     * @param termId the term to load meta data for
     * @param searchType the search type where the meta data will be inserted
     */
    public void loadMetaData(String termId, Integer searchType) {

        JTextPane currentDefinitionsJTextPane = null;
        JXTable currentTermDetailsJXTable = null;
        JScrollPane currentTermDetailsJScrollPane = null;

        if (searchType == OLS_DIALOG_TERM_NAME_SEARCH) {
            currentDefinitionsJTextPane = definitionTermNameSearchJTextPane;
            currentTermDetailsJXTable = termDetailsTermNameSearchJXTable;
            currentTermDetailsJScrollPane = termDetailsTermNameSearchJScrollPane;
        } else if (searchType == OLS_DIALOG_PSI_MOD_MASS_SEARCH) {
            currentDefinitionsJTextPane = definitionMassSearchJTextPane;
            currentTermDetailsJXTable = termDetailsMassSearchJXTable;
            currentTermDetailsJScrollPane = termDetailsMassSearchJScrollPane;
        } else if (searchType == OLS_DIALOG_BROWSE_ONTOLOGY) {
            currentDefinitionsJTextPane = definitionBrowseOntologyJTextPane;
            currentTermDetailsJXTable = termDetailsBrowseOntologyJXTable;
            currentTermDetailsJScrollPane = termDetailsBrowseOntologyJScrollPane;
        } else if (searchType == OLS_DIALOG_TERM_ID_SEARCH) {
            currentDefinitionsJTextPane = definitionTermIdSearchJTextPane;
            currentTermDetailsJXTable = termDetailsTermIdSearchJXTable;
            currentTermDetailsJScrollPane = termDetailsTermIdSearchJScrollPane;
        }

        //clear meta data
        clearData(searchType, false, true);

        if (termId == null) {
            return;
        }

        String ontology = getOntologyLabelFromTermId(termId);

        boolean error = false;

        if (ontology != null && ontology.equalsIgnoreCase("NEWT")) {
            currentDefinitionsJTextPane.setText("Retreiving 'Term Details' is disabled for NEWT.");
            currentDefinitionsJTextPane.setCaretPosition(0);
            currentTermDetailsJXTable.setEnabled(false);
            error = true;
        } else {
            currentTermDetailsJXTable.setEnabled(true);
        }

        if (!error) {

            Map<String, String> metadata = null;
            Map<String, String> xRefs = null;

            //query OLS
            try {
                metadata = olsConnection.getTermMetadata(termId, ontology);
                xRefs = olsConnection.getTermXrefs(termId, ontology);

                if (searchType == OLS_DIALOG_TERM_NAME_SEARCH) {
                    currentlySelectedTermNameSearchAccessionNumber = termId;
                    viewTermHierachyTermNameSearchJLabel.setEnabled(true);
                } else if (searchType == OLS_DIALOG_PSI_MOD_MASS_SEARCH) {
                    currentlySelectedMassSearchAccessionNumber = termId;
                    viewTermHierachyMassSearchJLabel.setEnabled(true);
                } else if (searchType == OLS_DIALOG_BROWSE_ONTOLOGY) {
                    currentlySelectedBrowseOntologyAccessionNumber = termId;
                    viewTermHierachyBrowseOntologyJLabel.setEnabled(true);
                } else if (searchType == OLS_DIALOG_TERM_ID_SEARCH) {
                    currentlySelectedTermIdSearchAccessionNumber = termId;
                    viewTermHierachyTermIdSearchJLabel.setEnabled(true);
                }
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
                }
                if (searchType == OLS_DIALOG_TERM_ID_SEARCH) {
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
                        ((DefaultTableModel) currentTermDetailsJXTable.getModel()).addRow(
                                new Object[]{key, metadata.get(key)});
                    }
                }

                if (currentDefinitionsJTextPane.getText().equalsIgnoreCase("null")) {
                    currentDefinitionsJTextPane.setText("(no definition provided in CV term)");
                }

                // iterate the xrefs and insert them into the table
                for (Iterator i = xRefs.keySet().iterator(); i.hasNext();) {
                    String key = (String) i.next();

                    ((DefaultTableModel) currentTermDetailsJXTable.getModel()).addRow(
                            new Object[]{key, xRefs.get(key)});
                }

                // set the horizontal scroll bar to the top
                currentTermDetailsJScrollPane.getVerticalScrollBar().setValue(0);
            } else {
                if (searchType == OLS_DIALOG_TERM_NAME_SEARCH) {
                    viewTermHierachyTermNameSearchJLabel.setEnabled(false);
                } else if (searchType == OLS_DIALOG_PSI_MOD_MASS_SEARCH) {
                    viewTermHierachyMassSearchJLabel.setEnabled(false);
                } else if (searchType == OLS_DIALOG_BROWSE_ONTOLOGY) {
                    viewTermHierachyBrowseOntologyJLabel.setEnabled(false);
                } else if (searchType == OLS_DIALOG_TERM_ID_SEARCH) {
                    viewTermHierachyTermIdSearchJLabel.setEnabled(false);
                }
            }
        } else {
            if (searchType == OLS_DIALOG_TERM_NAME_SEARCH) {
                viewTermHierachyTermNameSearchJLabel.setEnabled(false);
            } else if (searchType == OLS_DIALOG_PSI_MOD_MASS_SEARCH) {
                viewTermHierachyMassSearchJLabel.setEnabled(false);
            } else if (searchType == OLS_DIALOG_BROWSE_ONTOLOGY) {
                viewTermHierachyBrowseOntologyJLabel.setEnabled(false);
            } else if (searchType == OLS_DIALOG_TERM_ID_SEARCH) {
                viewTermHierachyTermIdSearchJLabel.setEnabled(false);
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
     * A helper method for setting the cell tool tips. Included in order to not have to
     * duplicate the code for each table.
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
        } else {
            table.setToolTipText(null);
        }
    }

    /**
     * Creates a multiple lines tooltip based on the provided text.
     *
     * @param toolTip the orginal one line tool tip
     * @return the multiple line tooltip as html
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
     * Sets the look and feel of the OLS Dialog.
     *
     * Note that the OLS Dialog has been created with the following look and feel
     * in mind. If using a different look and feel you might need to tweak the GUI
     * to get the best appearance.
     */
    private void setLookAndFeel() {
        try {
            PlasticLookAndFeel.setPlasticTheme(new SkyKrupp());
            UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (UnsupportedLookAndFeelException e) {
            // ignore exception, i.e. use default look and feel
        }
    }

    /**
     * Opens the OLS connection and retrieves and inserts the ontology names
     * into the ontology combo box.
     * 
     * @return false if an error occured, true otherwise
     */
    public boolean openOlsConnectionAndInsertOntologyNames() {

        boolean error = false;

        Vector ontologyNamesAndKeys = new Vector();

        try {
            QueryService locator = new QueryServiceLocator();
            olsConnection = locator.getOntologyQuery();
            Map map = olsConnection.getOntologyNames();

            String temp = "";

            for (Iterator i = map.keySet().iterator(); i.hasNext();) {
                String key = (String) i.next();

                temp = map.get(key) + " [" + key + "]";
                ontologyNamesAndKeys.add(temp);
            }

            // sort the ontologies into alphabetic ascending order
            java.util.Collections.sort(ontologyNamesAndKeys);

            ontologyNamesAndKeys.add(0, "-- Search In All Ontologies --");

            ontologyJComboBox.setModel(new DefaultComboBoxModel(ontologyNamesAndKeys));
            ontologyJComboBox.setSelectedItem(selectedOntology);
            lastSelectedOntology = (String) ontologyJComboBox.getSelectedItem();
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    defaultOlsConnectionFailureErrorMessage,
                    "Failed To Contact OLS", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to access OLS: ");
            ex.printStackTrace();
            error = true;
        } catch (ServiceException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    defaultOlsConnectionFailureErrorMessage,
                    "Failed To Contact OLS", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to access OLS: ");
            ex.printStackTrace();
            error = true;
        }

        return error;
    }

    /**
     * Update the ontology tree browser with the roots of the selected ontology.
     */
    private void updateBrowseOntologyView() {

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        // get selected ontology
        String ontology = getCurrentOntologyLabel();

        // set the root to the ontology label
        treeBrowser.initialize(ontology);

        // load root terms
        Map<String, String> rootTerms = getOntologyRoots(ontology);

        // update the tree
        for (String termId : rootTerms.keySet()) {
            treeBrowser.addNode(termId, rootTerms.get(termId));
        }

        // makes sure that all second level non visible nodes are added
        treeBrowser.updateTree();

        // move the horizontal scroll bar value to the top
        treeBrowser.scrollToTop();

        currentlySelectedBrowseOntologyAccessionNumber = null;

        if (debug) {
            System.out.println("updated roots");
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }

    /**
     * Adds a second level of non visible nodes. Needed to be able to show folder
     * icons for the current level of nodes.
     *
     * @param termId the term id for the term to add the second level for
     * @param ontology the ontology to get the terms from
     * @param parentNode the node to add the new nodes to
     * @return true if an error occured, false otherwise
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
        ontology = ontology.substring(ontology.lastIndexOf("[") + 1, ontology.length() - 1);

        return ontology;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        insertSelectedJButton = new javax.swing.JButton();
        cancelJButton = new javax.swing.JButton();
        helpJButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        aboutJButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        searchTypeJTabbedPane = new javax.swing.JTabbedPane();
        termNameSearchJPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        termNameSearchJTextField = new javax.swing.JTextField();
        numberOfTermsTermNameSearchJTextField = new javax.swing.JTextField();
        jScrollPane4 = new javax.swing.JScrollPane();
        definitionTermNameSearchJTextPane = new javax.swing.JTextPane();
        termDetailsTermNameSearchJScrollPane = new javax.swing.JScrollPane();
        termDetailsTermNameSearchJXTable = new org.jdesktop.swingx.JXTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        olsResultsTermNameSearchJScrollPane = new javax.swing.JScrollPane();
        olsResultsTermNameSearchJXTable = new org.jdesktop.swingx.JXTable();
        viewTermHierachyTermNameSearchJLabel = new javax.swing.JLabel();
        termIdSearchJPanel = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        termIdSearchJTextField = new javax.swing.JTextField();
        jScrollPane6 = new javax.swing.JScrollPane();
        definitionTermIdSearchJTextPane = new javax.swing.JTextPane();
        termDetailsTermIdSearchJScrollPane = new javax.swing.JScrollPane();
        termDetailsTermIdSearchJXTable = new org.jdesktop.swingx.JXTable();
        jLabel11 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        olsResultsTermIdSearchJScrollPane = new javax.swing.JScrollPane();
        olsResultsTermIdSearchJXTable = new org.jdesktop.swingx.JXTable();
        viewTermHierachyTermIdSearchJLabel = new javax.swing.JLabel();
        termIdSearchJButton = new javax.swing.JButton();
        massSearchJPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        modificationMassJTextField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        precisionJTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        definitionMassSearchJTextPane = new javax.swing.JTextPane();
        termDetailsMassSearchJScrollPane = new javax.swing.JScrollPane();
        termDetailsMassSearchJXTable = new org.jdesktop.swingx.JXTable();
        modificationMassSearchJButton = new javax.swing.JButton();
        massTypeJComboBox = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        olsResultsMassSearchJScrollPane = new javax.swing.JScrollPane();
        olsResultsMassSearchJXTable = new org.jdesktop.swingx.JXTable();
        viewTermHierachyMassSearchJLabel = new javax.swing.JLabel();
        browseOntologyJPanel = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        definitionBrowseOntologyJTextPane = new javax.swing.JTextPane();
        termDetailsBrowseOntologyJScrollPane = new javax.swing.JScrollPane();
        termDetailsBrowseOntologyJXTable = new org.jdesktop.swingx.JXTable();
        browseJPanel = new javax.swing.JPanel();
        viewTermHierachyBrowseOntologyJLabel = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        ontologyJComboBox = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(" Ontology Lookup Service - (ols-dialog v3.0)");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

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
        helpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpJButtonActionPerformed(evt);
            }
        });

        aboutJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/no/uib/olsdialog/icons/ols_transparent_small.GIF"))); // NOI18N
        aboutJButton.setToolTipText("About");
        aboutJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutJButtonActionPerformed(evt);
            }
        });

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Search Parameters", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0))); // NOI18N

        searchTypeJTabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                searchTypeJTabbedPaneStateChanged(evt);
            }
        });

        jLabel3.setText("Term Name:");

        termNameSearchJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        termNameSearchJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                termNameSearchJTextFieldKeyReleased(evt);
            }
        });

        numberOfTermsTermNameSearchJTextField.setEditable(false);
        numberOfTermsTermNameSearchJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        numberOfTermsTermNameSearchJTextField.setToolTipText("Number of Matching Terms");

        definitionTermNameSearchJTextPane.setEditable(false);
        jScrollPane4.setViewportView(definitionTermNameSearchJTextPane);

        termDetailsTermNameSearchJXTable.setModel(new javax.swing.table.DefaultTableModel(
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
        termDetailsTermNameSearchJXTable.setOpaque(false);
        termDetailsTermNameSearchJScrollPane.setViewportView(termDetailsTermNameSearchJXTable);

        jLabel1.setText("Search Results:");

        jLabel2.setText("Selected Term:");

        olsResultsTermNameSearchJXTable.setModel(new javax.swing.table.DefaultTableModel(
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
        olsResultsTermNameSearchJXTable.setOpaque(false);
        olsResultsTermNameSearchJXTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                olsResultsTermNameSearchJXTableMouseClicked(evt);
            }
        });
        olsResultsTermNameSearchJXTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                olsResultsTermNameSearchJXTableKeyReleased(evt);
            }
        });
        olsResultsTermNameSearchJScrollPane.setViewportView(olsResultsTermNameSearchJXTable);

        viewTermHierachyTermNameSearchJLabel.setForeground(new java.awt.Color(0, 0, 255));
        viewTermHierachyTermNameSearchJLabel.setText("View Term Hierarchy");
        viewTermHierachyTermNameSearchJLabel.setEnabled(false);
        viewTermHierachyTermNameSearchJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                viewTermHierachyTermNameSearchJLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                viewTermHierachyTermNameSearchJLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                viewTermHierachyTermNameSearchJLabelMouseExited(evt);
            }
        });

        org.jdesktop.layout.GroupLayout termNameSearchJPanelLayout = new org.jdesktop.layout.GroupLayout(termNameSearchJPanel);
        termNameSearchJPanel.setLayout(termNameSearchJPanelLayout);
        termNameSearchJPanelLayout.setHorizontalGroup(
            termNameSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(termNameSearchJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(termNameSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, olsResultsTermNameSearchJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
                    .add(termDetailsTermNameSearchJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
                    .add(jLabel1)
                    .add(termNameSearchJPanelLayout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 299, Short.MAX_VALUE)
                        .add(viewTermHierachyTermNameSearchJLabel))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
                    .add(termNameSearchJPanelLayout.createSequentialGroup()
                        .add(jLabel3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(termNameSearchJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(numberOfTermsTermNameSearchJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 80, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        termNameSearchJPanelLayout.setVerticalGroup(
            termNameSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(termNameSearchJPanelLayout.createSequentialGroup()
                .add(23, 23, 23)
                .add(termNameSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(numberOfTermsTermNameSearchJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(termNameSearchJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(29, 29, 29)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(olsResultsTermNameSearchJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                .add(18, 18, 18)
                .add(termNameSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(viewTermHierachyTermNameSearchJLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(termDetailsTermNameSearchJScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 77, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        searchTypeJTabbedPane.addTab("Term Name Search", termNameSearchJPanel);

        jLabel10.setText("Term ID:");

        termIdSearchJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        termIdSearchJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                termIdSearchJTextFieldKeyReleased(evt);
            }
        });

        definitionTermIdSearchJTextPane.setEditable(false);
        jScrollPane6.setViewportView(definitionTermIdSearchJTextPane);

        termDetailsTermIdSearchJXTable.setModel(new javax.swing.table.DefaultTableModel(
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
        termDetailsTermIdSearchJXTable.setOpaque(false);
        termDetailsTermIdSearchJScrollPane.setViewportView(termDetailsTermIdSearchJXTable);

        jLabel11.setText("Search Results:");

        jLabel13.setText("Selected Term:");

        olsResultsTermIdSearchJXTable.setModel(new javax.swing.table.DefaultTableModel(
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
        olsResultsTermIdSearchJXTable.setOpaque(false);
        olsResultsTermIdSearchJXTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                olsResultsTermIdSearchJXTableMouseClicked(evt);
            }
        });
        olsResultsTermIdSearchJXTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                olsResultsTermIdSearchJXTableKeyReleased(evt);
            }
        });
        olsResultsTermIdSearchJScrollPane.setViewportView(olsResultsTermIdSearchJXTable);

        viewTermHierachyTermIdSearchJLabel.setForeground(new java.awt.Color(0, 0, 255));
        viewTermHierachyTermIdSearchJLabel.setText("View Term Hierarchy");
        viewTermHierachyTermIdSearchJLabel.setEnabled(false);
        viewTermHierachyTermIdSearchJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                viewTermHierachyTermIdSearchJLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                viewTermHierachyTermIdSearchJLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                viewTermHierachyTermIdSearchJLabelMouseExited(evt);
            }
        });

        termIdSearchJButton.setText("Search");
        termIdSearchJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                termIdSearchJButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout termIdSearchJPanelLayout = new org.jdesktop.layout.GroupLayout(termIdSearchJPanel);
        termIdSearchJPanel.setLayout(termIdSearchJPanelLayout);
        termIdSearchJPanelLayout.setHorizontalGroup(
            termIdSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(termIdSearchJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(termIdSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, olsResultsTermIdSearchJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
                    .add(termDetailsTermIdSearchJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
                    .add(jLabel11)
                    .add(termIdSearchJPanelLayout.createSequentialGroup()
                        .add(jLabel13)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 299, Short.MAX_VALUE)
                        .add(viewTermHierachyTermIdSearchJLabel))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
                    .add(termIdSearchJPanelLayout.createSequentialGroup()
                        .add(jLabel10)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(termIdSearchJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(termIdSearchJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 80, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        termIdSearchJPanelLayout.setVerticalGroup(
            termIdSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(termIdSearchJPanelLayout.createSequentialGroup()
                .add(23, 23, 23)
                .add(termIdSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel10)
                    .add(termIdSearchJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(termIdSearchJButton))
                .add(29, 29, 29)
                .add(jLabel11)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(olsResultsTermIdSearchJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                .add(18, 18, 18)
                .add(termIdSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel13)
                    .add(viewTermHierachyTermIdSearchJLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(termDetailsTermIdSearchJScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 77, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        searchTypeJTabbedPane.addTab("Term ID Search", termIdSearchJPanel);

        jLabel4.setText("Mass:");

        modificationMassJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        modificationMassJTextField.setText("0.0");
        modificationMassJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                modificationMassJTextFieldKeyReleased(evt);
            }
        });

        jLabel5.setText("+-");
        jLabel5.setToolTipText("Mass Accuracy");

        precisionJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        precisionJTextField.setText("0.1");
        precisionJTextField.setToolTipText("Mass Accuracy");
        precisionJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                precisionJTextFieldKeyReleased(evt);
            }
        });

        jLabel6.setText("Search Results:");

        jLabel7.setText("Selected Term:");

        definitionMassSearchJTextPane.setEditable(false);
        jScrollPane5.setViewportView(definitionMassSearchJTextPane);

        termDetailsMassSearchJXTable.setModel(new javax.swing.table.DefaultTableModel(
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
        termDetailsMassSearchJXTable.setOpaque(false);
        termDetailsMassSearchJScrollPane.setViewportView(termDetailsMassSearchJXTable);

        modificationMassSearchJButton.setText("Search");
        modificationMassSearchJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modificationMassSearchJButtonActionPerformed(evt);
            }
        });

        massTypeJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "- Select -", "DiffAvg", "DiffMono", "MassAvg", "MassMono" }));
        massTypeJComboBox.setSelectedIndex(2);
        massTypeJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                massTypeJComboBoxActionPerformed(evt);
            }
        });

        jLabel12.setText("Type:");

        olsResultsMassSearchJXTable.setModel(new javax.swing.table.DefaultTableModel(
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
        olsResultsMassSearchJXTable.setOpaque(false);
        olsResultsMassSearchJXTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                olsResultsMassSearchJXTableMouseClicked(evt);
            }
        });
        olsResultsMassSearchJXTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                olsResultsMassSearchJXTableKeyReleased(evt);
            }
        });
        olsResultsMassSearchJScrollPane.setViewportView(olsResultsMassSearchJXTable);

        viewTermHierachyMassSearchJLabel.setForeground(new java.awt.Color(0, 0, 255));
        viewTermHierachyMassSearchJLabel.setText("View Term Hierarchy");
        viewTermHierachyMassSearchJLabel.setEnabled(false);
        viewTermHierachyMassSearchJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                viewTermHierachyMassSearchJLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                viewTermHierachyMassSearchJLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                viewTermHierachyMassSearchJLabelMouseExited(evt);
            }
        });

        org.jdesktop.layout.GroupLayout massSearchJPanelLayout = new org.jdesktop.layout.GroupLayout(massSearchJPanel);
        massSearchJPanel.setLayout(massSearchJPanelLayout);
        massSearchJPanelLayout.setHorizontalGroup(
            massSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(massSearchJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(massSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(olsResultsMassSearchJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
                    .add(termDetailsMassSearchJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
                    .add(jLabel6)
                    .add(massSearchJPanelLayout.createSequentialGroup()
                        .add(jLabel7)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 299, Short.MAX_VALUE)
                        .add(viewTermHierachyMassSearchJLabel))
                    .add(jScrollPane5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
                    .add(massSearchJPanelLayout.createSequentialGroup()
                        .add(jLabel4)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(modificationMassJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel5)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(precisionJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 76, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(jLabel12)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(massTypeJComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 76, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 53, Short.MAX_VALUE)
                        .add(modificationMassSearchJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 80, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        massSearchJPanelLayout.linkSize(new java.awt.Component[] {massTypeJComboBox, precisionJTextField}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        massSearchJPanelLayout.setVerticalGroup(
            massSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, massSearchJPanelLayout.createSequentialGroup()
                .add(24, 24, 24)
                .add(massSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(modificationMassSearchJButton)
                    .add(modificationMassJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5)
                    .add(precisionJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel12)
                    .add(massTypeJComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 25, Short.MAX_VALUE)
                .add(jLabel6)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(olsResultsMassSearchJScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 111, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(massSearchJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(viewTermHierachyMassSearchJLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(termDetailsMassSearchJScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 77, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        searchTypeJTabbedPane.addTab("PSI-MOD Mass Search", massSearchJPanel);

        jLabel8.setText("Selected Term:");

        definitionBrowseOntologyJTextPane.setEditable(false);
        jScrollPane7.setViewportView(definitionBrowseOntologyJTextPane);

        termDetailsBrowseOntologyJXTable.setModel(new javax.swing.table.DefaultTableModel(
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
        termDetailsBrowseOntologyJXTable.setOpaque(false);
        termDetailsBrowseOntologyJScrollPane.setViewportView(termDetailsBrowseOntologyJXTable);

        browseJPanel.setLayout(new javax.swing.BoxLayout(browseJPanel, javax.swing.BoxLayout.LINE_AXIS));

        viewTermHierachyBrowseOntologyJLabel.setForeground(new java.awt.Color(0, 0, 255));
        viewTermHierachyBrowseOntologyJLabel.setText("View Term Hierarchy");
        viewTermHierachyBrowseOntologyJLabel.setEnabled(false);
        viewTermHierachyBrowseOntologyJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                viewTermHierachyBrowseOntologyJLabelMouseClicked(evt);
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
                    .add(org.jdesktop.layout.GroupLayout.LEADING, browseJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, browseOntologyJPanelLayout.createSequentialGroup()
                        .add(jLabel8)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 299, Short.MAX_VALUE)
                        .add(viewTermHierachyBrowseOntologyJLabel))
                    .add(jScrollPane7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
                    .add(termDetailsBrowseOntologyJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE))
                .addContainerGap())
        );
        browseOntologyJPanelLayout.setVerticalGroup(
            browseOntologyJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, browseOntologyJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(browseJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                .add(15, 15, 15)
                .add(browseOntologyJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(viewTermHierachyBrowseOntologyJLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(termDetailsBrowseOntologyJScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 77, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        searchTypeJTabbedPane.addTab("Browse Ontology", browseOntologyJPanel);

        jLabel9.setText("Ontology:");

        ontologyJComboBox.setMaximumRowCount(30);
        ontologyJComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                ontologyJComboBoxItemStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                        .add(jLabel9)
                        .add(18, 18, 18)
                        .add(ontologyJComboBox, 0, 428, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, searchTypeJTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(ontologyJComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel9))
                .add(18, 18, 18)
                .add(searchTypeJTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(helpJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(aboutJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 219, Short.MAX_VALUE)
                        .add(insertSelectedJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(18, 18, 18)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(7, 7, 7)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(cancelJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(insertSelectedJButton)))
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(aboutJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(helpJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
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
        if (ontologyJComboBox.getSelectedIndex() != 0) {
            if(getCurrentOntologyLabel().equalsIgnoreCase("EFO")){
                termIdSearchJTextField.setText(getCurrentOntologyLabel() + "_");
            } else{
                termIdSearchJTextField.setText(getCurrentOntologyLabel() + ":");
            }
        } else{
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

                // disable the 'browse ontology' tab when 'search in all ontologies' is selected
                searchTypeJTabbedPane.setEnabledAt(OLS_DIALOG_BROWSE_ONTOLOGY, ontologyJComboBox.getSelectedIndex() != 0);

                if (ontologyJComboBox.getSelectedIndex() == 0
                        && searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_BROWSE_ONTOLOGY) {
                    searchTypeJTabbedPane.setSelectedIndex(OLS_DIALOG_TERM_NAME_SEARCH);
                }

                termNameSearchJTextFieldKeyReleased(null);
                updateBrowseOntologyView();
                clearData(OLS_DIALOG_TERM_ID_SEARCH, true, true);

                viewTermHierachyTermNameSearchJLabel.setEnabled(false);
                viewTermHierachyTermIdSearchJLabel.setEnabled(false);
                viewTermHierachyBrowseOntologyJLabel.setEnabled(false);
            }
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

    }//GEN-LAST:event_ontologyJComboBoxItemStateChanged

    /**
     * Searches the selected ontology for terms matching the inserted string. 
     * The search finds all terms having the current string as a substring. 
     * (But seems to be limited somehow, seeing as using two letters, can 
     * result in more hits, than using just one of the letters...)
     * 
     * @param evt
     */
    private void termNameSearchJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_termNameSearchJTextFieldKeyReleased

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        termNameSearchJTextField.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        insertSelectedJButton.setEnabled(false);
        currentlySelectedTermNameSearchAccessionNumber = null;

        try {
            // clear the old meta data
            clearData(OLS_DIALOG_TERM_NAME_SEARCH, true, true);

            // search the selected ontology and find all matching terms
            String ontology = getCurrentOntologyLabel();

            // if 'search in all ontologies' is selected, set the ontology to null
            if (ontologyJComboBox.getSelectedIndex() == 0) {
                ontology = null;
            }

            Map map = olsConnection.getTermsByName(termNameSearchJTextField.getText(), ontology, false);

            for (Iterator i = map.keySet().iterator(); i.hasNext();) {
                String key = (String) i.next();
                ((DefaultTableModel) olsResultsTermNameSearchJXTable.getModel()).addRow(new Object[]{key, map.get(key)});
            }

            termNameSearchJTextField.requestFocus();

            this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            termNameSearchJTextField.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
            numberOfTermsTermNameSearchJTextField.setText("" + map.size());

            // make the first row visible
            if (olsResultsTermNameSearchJXTable.getRowCount() > 0) {
                olsResultsTermNameSearchJXTable.scrollRectToVisible(olsResultsTermNameSearchJXTable.getCellRect(0, 0, false));
            }

            //No matching terms found
            if (map.size() == 0) {
                //JOptionPane.showMessageDialog(this, "No mathcing terms found.");
                //this.olsSearchTextField.requestFocus();
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
        termNameSearchJTextField.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));

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
            termId = "" + olsResultsTermNameSearchJXTable.getValueAt(olsResultsTermNameSearchJXTable.getSelectedRow(), 0);
        } else if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_BROWSE_ONTOLOGY) {
            termId = currentlySelectedBrowseOntologyAccessionNumber;
        } else if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_PSI_MOD_MASS_SEARCH) {
            termId = "" + olsResultsMassSearchJXTable.getValueAt(olsResultsMassSearchJXTable.getSelectedRow(), 0);
        } else if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_TERM_ID_SEARCH) {
            termId = "" + olsResultsTermIdSearchJXTable.getValueAt(olsResultsTermIdSearchJXTable.getSelectedRow(), 0);
        }

        String ontologyLong = ((String) ontologyJComboBox.getSelectedItem());
        String ontologyShort = "unknown";

        if (ontologyJComboBox.getSelectedIndex() == 0) {

            ontologyShort = getOntologyLabelFromTermId(termId);

            if (ontologyShort == null) {
                ontologyShort = "NEWT"; // TODO: verify that this is correct
                ontologyLong = "NEWT UniProt Taxonomy Database [NEWT]";  // TODO: verify that this is correct
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
                olsInputable.insertOLSResult(field, selectedValue, termId, ontologyShort, ontologyLong, modifiedRow, mappedTerm);
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

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_insertSelectedJButtonActionPerformed

    /**
     * Updates the information about the selected CV term
     *
     * @param evt
     * @param searchResultTable
     * @param termDetailsTable
     * @param definitionTextPane
     */
    private void insertTermDetails(java.awt.event.MouseEvent evt, JXTable searchResultTable) {

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
     * See cancelJButtonActionPerformed
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancelJButtonActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

    /**
     * returns an array of DataHolder objects that contain data on MOD entries (termId, termName, massDelta)
     * given a massDeltaType and a range of masses.
     *
     * @param massDeltaType the type of massDelta to query (can be null)
     * @param fromMass the lower mass limit (inclusive, mandatory)
     * @param toMass the higher mass limit (inclusive, mandatory)
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
        viewTermHierachyMassSearchJLabel.setEnabled(false);

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
                    ((DefaultTableModel) olsResultsMassSearchJXTable.getModel()).addRow(
                            new Object[]{results[i].getTermId(),
                                results[i].getTermName()});
                }
            }

            this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

            // make the first row visible
            if (olsResultsMassSearchJXTable.getRowCount() > 0) {
                olsResultsMassSearchJXTable.scrollRectToVisible(olsResultsTermNameSearchJXTable.getCellRect(0, 0, false));
            }
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_modificationMassSearchJButtonActionPerformed

    /**
     * Enables or disables the search button based on the selection in the combo box.
     *
     * @param evt
     */
    private void massTypeJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_massTypeJComboBoxActionPerformed
        modificationMassSearchJButton.setEnabled(massTypeJComboBox.getSelectedIndex() != 0);
    }//GEN-LAST:event_massTypeJComboBoxActionPerformed

    /**
     * Makes sure that the PSI-MOD ontology is selected when the modification mass
     * search tab is selected.
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
     * See olsResultsTextSearcJXTableMouseClicked
     */
    private void olsResultsTermNameSearchJXTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_olsResultsTermNameSearchJXTableKeyReleased
        olsResultsTermNameSearchJXTableMouseClicked(null);
    }//GEN-LAST:event_olsResultsTermNameSearchJXTableKeyReleased

    /**
     * If the user double clicks the selected row is inserted into the parent
     * frame and closes the dialog. A single click retrieves the additional
     * information known about the term and displays it in the "Term Details"
     * frame.
     *
     * @param evt
     */
    private void olsResultsTermNameSearchJXTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_olsResultsTermNameSearchJXTableMouseClicked
        insertTermDetails(evt, olsResultsTermNameSearchJXTable);
    }//GEN-LAST:event_olsResultsTermNameSearchJXTableMouseClicked

    /**
     * See olsResultsMassSearchJXTableMouseClicked
     */
    private void olsResultsMassSearchJXTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_olsResultsMassSearchJXTableKeyReleased
        olsResultsMassSearchJXTableMouseClicked(null);
    }//GEN-LAST:event_olsResultsMassSearchJXTableKeyReleased

    /**
     * If the user double clicks the selected row is inserted into the parent
     * frame and closes the dialog. A single click retrieves the additional
     * information known about the term and displays it in the "Term Details"
     * frame.
     *
     * @param evt
     */
    private void olsResultsMassSearchJXTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_olsResultsMassSearchJXTableMouseClicked
        insertTermDetails(evt, olsResultsMassSearchJXTable);
    }//GEN-LAST:event_olsResultsMassSearchJXTableMouseClicked

    /**
     * If 'Enter' is pressed and the 'Search' button is enabled, the search is performed.
     * 
     * @param evt
     */
    private void modificationMassJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_modificationMassJTextFieldKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (modificationMassSearchJButton.isEnabled()) {
                modificationMassSearchJButtonActionPerformed(null);
            }
        }
    }//GEN-LAST:event_modificationMassJTextFieldKeyReleased

    /**
     * See modificationMassJTextFieldKeyReleased
     */
    private void precisionJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_precisionJTextFieldKeyReleased
        modificationMassJTextFieldKeyReleased(evt);
    }//GEN-LAST:event_precisionJTextFieldKeyReleased

    /**
     * Changes the cursor to the hand cursor when over the term hierachy link.
     *
     * @param evt
     */
    private void viewTermHierachyJLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewTermHierachyJLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_viewTermHierachyJLabelMouseEntered

    /**
     * Changes the cursor back to the default cursor when leaving the term hierachy link.
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
    private void viewTermHierachyBrowseOntologyJLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewTermHierachyBrowseOntologyJLabelMouseClicked
        viewTermHierachy();
    }//GEN-LAST:event_viewTermHierachyBrowseOntologyJLabelMouseClicked

    /**
     * Opens a new dialog showing the term hierarchy as a graph.
     *
     * @param evt
     */
    private void viewTermHierachyMassSearchJLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewTermHierachyMassSearchJLabelMouseClicked
        viewTermHierachy();
    }//GEN-LAST:event_viewTermHierachyMassSearchJLabelMouseClicked

    /**
     * Changes the cursor to the hand cursor when over the term hierachy link.
     *
     * @param evt
     */
    private void viewTermHierachyMassSearchJLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewTermHierachyMassSearchJLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_viewTermHierachyMassSearchJLabelMouseEntered

    /**
     * Changes the cursor back to the default cursor when leaving the term hierachy link.
     *
     * @param evt
     */
    private void viewTermHierachyMassSearchJLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewTermHierachyMassSearchJLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_viewTermHierachyMassSearchJLabelMouseExited

    /**
     * Opens a new dialog showing the term hierarchy as a graph.
     *
     * @param evt
     */
    private void viewTermHierachyTermNameSearchJLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewTermHierachyTermNameSearchJLabelMouseClicked
        viewTermHierachy();
    }//GEN-LAST:event_viewTermHierachyTermNameSearchJLabelMouseClicked

    /**
     * Changes the cursor to the hand cursor when over the term hierachy link.
     *
     * @param evt
     */
    private void viewTermHierachyTermNameSearchJLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewTermHierachyTermNameSearchJLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_viewTermHierachyTermNameSearchJLabelMouseEntered

    /**
     * Changes the cursor back to the default cursor when leaving the term hierachy link.
     *
     * @param evt
     */
    private void viewTermHierachyTermNameSearchJLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewTermHierachyTermNameSearchJLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_viewTermHierachyTermNameSearchJLabelMouseExited

    /**
     * If the user double clicks the selected row is inserted into the parent
     * frame and closes the dialog. A single click retrieves the additional
     * information known about the term and displays it in the "Term Details"
     * frame.
     *
     * @param evt
     */
    private void olsResultsTermIdSearchJXTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_olsResultsTermIdSearchJXTableMouseClicked
        insertTermDetails(evt, olsResultsTermIdSearchJXTable);
    }//GEN-LAST:event_olsResultsTermIdSearchJXTableMouseClicked

    /**
     * See olsResultsTermIdSearcJXTableMouseClicked.
     */
    private void olsResultsTermIdSearchJXTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_olsResultsTermIdSearchJXTableKeyReleased
        olsResultsTermIdSearchJXTableMouseClicked(null);
    }//GEN-LAST:event_olsResultsTermIdSearchJXTableKeyReleased

    /**
     * Opens a new dialog showing the term hierarchy as a graph.
     *
     * @param evt
     */
    private void viewTermHierachyTermIdSearchJLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewTermHierachyTermIdSearchJLabelMouseClicked
        viewTermHierachy();
    }//GEN-LAST:event_viewTermHierachyTermIdSearchJLabelMouseClicked

    /**
     * Changes the cursor to the hand cursor when over the term hierachy link.
     *
     * @param evt
     */
    private void viewTermHierachyTermIdSearchJLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewTermHierachyTermIdSearchJLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_viewTermHierachyTermIdSearchJLabelMouseEntered

    /**
     * Changes the cursor back to the default cursor when leaving the term hierachy link.
     *
     * @param evt
     */
    private void viewTermHierachyTermIdSearchJLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewTermHierachyTermIdSearchJLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_viewTermHierachyTermIdSearchJLabelMouseExited

    /**
     * Searches for the term matching the inserted accession number and
     * inserts the result into the table.
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

            String currentTermName = olsConnection.getTermById(termIdSearchJTextField.getText().trim(), ontology);

            if (currentTermName == null) {
                JOptionPane.showMessageDialog(this, "No matching terms found.", "No Match Terms", JOptionPane.INFORMATION_MESSAGE);
                termIdSearchJTextField.requestFocus();
            } else {

                if (currentTermName.equalsIgnoreCase(termIdSearchJTextField.getText().trim())) {
                    JOptionPane.showMessageDialog(this, "No matching terms found.", "No Match Terms", JOptionPane.INFORMATION_MESSAGE);
                    termIdSearchJTextField.requestFocus();
                } else {
                    ((DefaultTableModel) olsResultsTermIdSearchJXTable.getModel()).addRow(new Object[]{
                                termIdSearchJTextField.getText().trim(), currentTermName});
                }
            }

            this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            termIdSearchJTextField.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));

            // make the first row visible
            if (olsResultsTermIdSearchJXTable.getRowCount() > 0) {
                olsResultsTermIdSearchJXTable.scrollRectToVisible(olsResultsTermIdSearchJXTable.getCellRect(0, 0, false));
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
     * If 'Enter' is pressed and the 'Search' button is enabled the search is performed. 
     * Also enables or disables the search button when the field contains text or not.
     * 
     * @param evt
     */
    private void termIdSearchJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_termIdSearchJTextFieldKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (termIdSearchJButton.isEnabled()) {
                termIdSearchJButtonActionPerformed(null);
            }
        } else {
            termIdSearchJButton.setEnabled(termIdSearchJTextField.getText().length() > 0);
        }
    }//GEN-LAST:event_termIdSearchJTextFieldKeyReleased

    /**
     * Opens a new dialog showing the term hierarchy as a graph.
     */
    private void viewTermHierachy() {

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        String accession = null;

        if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_TERM_NAME_SEARCH) {
            accession = "" + olsResultsTermNameSearchJXTable.getValueAt(olsResultsTermNameSearchJXTable.getSelectedRow(), 0);
        } else if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_BROWSE_ONTOLOGY) {
            accession = currentlySelectedBrowseOntologyAccessionNumber;
        } else if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_PSI_MOD_MASS_SEARCH) {
            accession = "" + olsResultsMassSearchJXTable.getValueAt(olsResultsMassSearchJXTable.getSelectedRow(), 0);
        } else if (searchTypeJTabbedPane.getSelectedIndex() == OLS_DIALOG_TERM_ID_SEARCH) {
            accession = "" + olsResultsTermIdSearchJXTable.getValueAt(olsResultsTermIdSearchJXTable.getSelectedRow(), 0);
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

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutJButton;
    private javax.swing.JPanel browseJPanel;
    private javax.swing.JPanel browseOntologyJPanel;
    private javax.swing.JButton cancelJButton;
    private javax.swing.JTextPane definitionBrowseOntologyJTextPane;
    private javax.swing.JTextPane definitionMassSearchJTextPane;
    private javax.swing.JTextPane definitionTermIdSearchJTextPane;
    private javax.swing.JTextPane definitionTermNameSearchJTextPane;
    private javax.swing.JButton helpJButton;
    private javax.swing.JButton insertSelectedJButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel massSearchJPanel;
    private javax.swing.JComboBox massTypeJComboBox;
    private javax.swing.JTextField modificationMassJTextField;
    private javax.swing.JButton modificationMassSearchJButton;
    private javax.swing.JTextField numberOfTermsTermNameSearchJTextField;
    private javax.swing.JScrollPane olsResultsMassSearchJScrollPane;
    private org.jdesktop.swingx.JXTable olsResultsMassSearchJXTable;
    private javax.swing.JScrollPane olsResultsTermIdSearchJScrollPane;
    private org.jdesktop.swingx.JXTable olsResultsTermIdSearchJXTable;
    private javax.swing.JScrollPane olsResultsTermNameSearchJScrollPane;
    private org.jdesktop.swingx.JXTable olsResultsTermNameSearchJXTable;
    private javax.swing.JComboBox ontologyJComboBox;
    private javax.swing.JTextField precisionJTextField;
    private javax.swing.JTabbedPane searchTypeJTabbedPane;
    private javax.swing.JScrollPane termDetailsBrowseOntologyJScrollPane;
    private org.jdesktop.swingx.JXTable termDetailsBrowseOntologyJXTable;
    private javax.swing.JScrollPane termDetailsMassSearchJScrollPane;
    private org.jdesktop.swingx.JXTable termDetailsMassSearchJXTable;
    private javax.swing.JScrollPane termDetailsTermIdSearchJScrollPane;
    private org.jdesktop.swingx.JXTable termDetailsTermIdSearchJXTable;
    private javax.swing.JScrollPane termDetailsTermNameSearchJScrollPane;
    private org.jdesktop.swingx.JXTable termDetailsTermNameSearchJXTable;
    private javax.swing.JButton termIdSearchJButton;
    private javax.swing.JPanel termIdSearchJPanel;
    private javax.swing.JTextField termIdSearchJTextField;
    private javax.swing.JPanel termNameSearchJPanel;
    private javax.swing.JTextField termNameSearchJTextField;
    private javax.swing.JLabel viewTermHierachyBrowseOntologyJLabel;
    private javax.swing.JLabel viewTermHierachyMassSearchJLabel;
    private javax.swing.JLabel viewTermHierachyTermIdSearchJLabel;
    private javax.swing.JLabel viewTermHierachyTermNameSearchJLabel;
    // End of variables declaration//GEN-END:variables
}
