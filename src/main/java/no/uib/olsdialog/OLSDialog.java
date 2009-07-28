package no.uib.olsdialog;

import java.awt.Component;
import no.uib.olsdialog.util.MyComboBoxRenderer;
import no.uib.olsdialog.util.HelpDialog;
import no.uib.olsdialog.util.Util;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.SkyKrupp;
import java.rmi.RemoteException;
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
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

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

    private String field;
    private String selectedOntology;
    private int modifiedRow = -1;
    private OLSInputable olsInputable;
    private String mappedTerm;

    /**
     * Opens a dialog that lets you search for terms using the OLS.
     * 
     * @param parent - the parent JFrame
     * @param olsInputable - a reference to the frame using the OLS Dialog
     * @param modal
     * @param field - the name of the field to insert the results into
     * @param selectedOntology - the name of the ontology to search in
     * @param term - the term to search for
     */
    public OLSDialog(JFrame parent, OLSInputable olsInputable, boolean modal, String field,
            String selectedOntology, String term) {
        this(parent, olsInputable, modal, field, selectedOntology, -1, term);
    }

    /**
     * Opens a dialog that lets you search for terms using the OLS.
     *
     * @param parent - the parent JDialog
     * @param olsInputable - a reference to the frame using the OLS Dialog
     * @param modal
     * @param field - the name of the field to insert the results into
     * @param selectedOntology - the name of the ontology to search in
     * @param term - the term to search for
     */
    public OLSDialog(JDialog parent, OLSInputable olsInputable, boolean modal, String field,
            String selectedOntology, String term) {
        this(parent, olsInputable, modal, field, selectedOntology, -1, term);
    }

    /**
     * Opens a dialog that lets you search for terms using the OLS.
     * 
     * @param parent - the parent JFrame
     * @param olsInputable - a reference to the frame using the OLS Dialog
     * @param modal
     * @param field - the name of the field to insert the results into
     * @param selectedOntology - the name of the ontology to search in
     * @param modifiedRow - the row to modify, use -1 if adding a new row
     * @param term - the term to search for
     */
    public OLSDialog(JFrame parent, OLSInputable olsInputable, boolean modal, String field,
            String selectedOntology, int modifiedRow, String term) {
        super(parent, modal);

        this.olsInputable = olsInputable;
        this.field = field;
        this.selectedOntology = selectedOntology;
        this.modifiedRow = modifiedRow;
        this.mappedTerm = term;

        setUpFrame();

        boolean error = insertOntologyNames();

        if (error) {
            this.dispose();
        } else {
            olsSearchTextField.requestFocus();

            if (mappedTerm != null) {
                olsSearchTextField.setText(mappedTerm);
                olsSearchTextFieldKeyReleased(null);
            }

            this.setLocationRelativeTo(parent);
            this.setVisible(true);
        }
    }

    /**
     * Opens a dialog that lets you search for terms using the OLS.
     *
     * @param parent - the parent JDialog
     * @param olsInputable - a reference to the frame using the OLS Dialog
     * @param modal
     * @param field - the name of the field to insert the results into
     * @param selectedOntology - the name of the ontology to search in
     * @param modifiedRow - the row to modify, use -1 if adding a new row
     * @param term - the term to search for
     */
    public OLSDialog(JDialog parent, OLSInputable olsInputable, boolean modal, String field,
            String selectedOntology, int modifiedRow, String term) {
        super(parent, modal);

        this.olsInputable = olsInputable;
        this.field = field;
        this.selectedOntology = selectedOntology;
        this.modifiedRow = modifiedRow;
        this.mappedTerm = term;

        setUpFrame();

        boolean error = insertOntologyNames();

        if (error) {
            this.dispose();
        } else {
            olsSearchTextField.requestFocus();

            if (mappedTerm != null) {
                olsSearchTextField.setText(mappedTerm);
                olsSearchTextFieldKeyReleased(null);
            }

            this.setLocationRelativeTo(parent);
            this.setVisible(true);
        }
    }

    /**
     * Includes code used by all constructors to set up the frame, e.g., handling column tooltips etc.
     */
    private void setUpFrame() {

        initComponents();

        // disables the tabs for the not yet implemented search options
        searchTypeJTabbedPane.setEnabledAt(1, false);
        searchTypeJTabbedPane.setEnabledAt(2, false);

        // use combobox renderer that centers the text
        ontologyJComboBox.setRenderer(new MyComboBoxRenderer(null, SwingConstants.CENTER));

        // disable reordring of the columns
        olsResultsTextSearchJTable.getTableHeader().setReorderingAllowed(false);
        olsResultsMassSearchJTable.getTableHeader().setReorderingAllowed(false);
        termDetailsTextSearchJXTable.getTableHeader().setReorderingAllowed(false);
        termDetailsMassSearchJXTable.getTableHeader().setReorderingAllowed(false);
        termDetailsGraphSearchJXTable.getTableHeader().setReorderingAllowed(false);

        // show tooltip if content in the value column is longer than 50 characters
        termDetailsTextSearchJXTable.getColumn(1).setCellRenderer(new DefaultTableRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

                if(column == 1 && table.getValueAt(row, column).toString().length() > 50){
                    table.setToolTipText("" + table.getValueAt(row, column));
                } else{
                    table.setToolTipText(null);
                }
                
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        termDetailsMassSearchJXTable.getColumn(1).setCellRenderer(new DefaultTableRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

                if(column == 1 && table.getValueAt(row, column).toString().length() > 50){
                    table.setToolTipText("" + table.getValueAt(row, column));
                } else{
                    table.setToolTipText(null);
                }

                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        termDetailsGraphSearchJXTable.getColumn(1).setCellRenderer(new DefaultTableRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

                if(column == 1 && table.getValueAt(row, column).toString().length() > 50){
                    table.setToolTipText("" + table.getValueAt(row, column));
                } else{
                    table.setToolTipText(null);
                }

                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        // only works for Java 1.6 and newer
//        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
//                getResource("/no/uib/olsdialog/icons/ols_transparent_small.GIF")));

    }

    /**
     * Sets the look and feel of the OLS Dialog.
     * Note that the OLS Dialog has been created with the following look and feel
     * in mind. If using a different look and feel you might need to tweak the GUI
     * to get the best appearance.
     */
    private static void setLookAndFeel() {
        try {
            PlasticLookAndFeel.setPlasticTheme(new SkyKrupp());
            UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            // ignore exception, i.e. use default look and feel
        }
    }

    /**
     * Retrieves and inserts the ontology names into the ontology combo box.
     * 
     * @return false if an error occured, true otherwise
     */
    public boolean insertOntologyNames() {

        boolean error = false;

        Vector ontologyNamesAndKeys = new Vector();

        try {
            QueryService locator = new QueryServiceLocator();
            Query qs = locator.getOntologyQuery();
            Map map = qs.getOntologyNames();

            String temp = "";

            for (Iterator i = map.keySet().iterator(); i.hasNext();) {
                String key = (String) i.next();

                temp = map.get(key) + " [" + key + "]";
                ontologyNamesAndKeys.add(temp);
            }

            java.util.Collections.sort(ontologyNamesAndKeys);

            ontologyJComboBox.setModel(new DefaultComboBoxModel(ontologyNamesAndKeys));
            ontologyJComboBox.setSelectedItem(selectedOntology);
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Not able to contact the OLS. Make sure that you are online and try again.\n" +
                    "You may also want to check your firewall (and proxy) settings.\n\n" +
                    "See the Troubleshooting section at the OLS Dialog home page for details:\n" +
                    "http://code.google.com/p/ols-dialog.",
                    "Failed To Contact OLS", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to access OLS: ");
            ex.printStackTrace();
            error = true;
        } catch (ServiceException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Not able to contact the OLS. Make sure that you are online and try again.\n" +
                    "You may also want to check your firewall (and proxy) settings.\n\n" +
                    "See the Troubleshooting section at the OLS Dialog home page for details:\n" +
                    "http://code.google.com/p/ols-dialog.",
                    "Failed To Contact OLS", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to access OLS: ");
            ex.printStackTrace();
            error = true;
        }

        return error;
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
        jPanel5 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        olsSearchTextField = new javax.swing.JTextField();
        numberOfTermsJTextField = new javax.swing.JTextField();
        jScrollPane = new javax.swing.JScrollPane();
        olsResultsTextSearchJTable = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        definitionTextSearchJTextPane = new javax.swing.JTextPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        termDetailsTextSearchJXTable = new org.jdesktop.swingx.JXTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        ontologyJTree = new javax.swing.JTree();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        definitionGraphSearchJTextPane = new javax.swing.JTextPane();
        jScrollPane8 = new javax.swing.JScrollPane();
        termDetailsGraphSearchJXTable = new org.jdesktop.swingx.JXTable();
        jPanel7 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        modificationMassJTextField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        massAccuracyJTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        olsResultsMassSearchJTable = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        definitionMassSearchJTextPane = new javax.swing.JTextPane();
        jScrollPane6 = new javax.swing.JScrollPane();
        termDetailsMassSearchJXTable = new org.jdesktop.swingx.JXTable();
        jLabel9 = new javax.swing.JLabel();
        ontologyJComboBox = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(" Ontology Lookup Service (OLS)");
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

        jLabel3.setText("Term:");

        olsSearchTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        olsSearchTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                olsSearchTextFieldKeyReleased(evt);
            }
        });

        numberOfTermsJTextField.setEditable(false);
        numberOfTermsJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        numberOfTermsJTextField.setToolTipText("Number of Matching Terms");

        olsResultsTextSearchJTable.setModel(new javax.swing.table.DefaultTableModel(
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
        olsResultsTextSearchJTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                olsResultsTextSearchJTableKeyReleased(evt);
            }
        });
        olsResultsTextSearchJTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                olsResultsTextSearchJTableMouseClicked(evt);
            }
        });
        jScrollPane.setViewportView(olsResultsTextSearchJTable);

        definitionTextSearchJTextPane.setEditable(false);
        jScrollPane4.setViewportView(definitionTextSearchJTextPane);

        termDetailsTextSearchJXTable.setModel(new javax.swing.table.DefaultTableModel(
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
        termDetailsTextSearchJXTable.setOpaque(false);
        jScrollPane3.setViewportView(termDetailsTextSearchJXTable);

        jLabel1.setText("Search Results:");

        jLabel2.setText("Selected Term:");

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
                    .add(jLabel1)
                    .add(jLabel2)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
                    .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(jLabel3)
                        .add(12, 12, 12)
                        .add(olsSearchTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 336, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(numberOfTermsJTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .add(28, 28, 28)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(olsSearchTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3)
                    .add(numberOfTermsJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(32, 32, 32)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 91, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 77, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        searchTypeJTabbedPane.addTab("Text", jPanel5);

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("GO");
        javax.swing.tree.DefaultMutableTreeNode treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("biological_process");
        javax.swing.tree.DefaultMutableTreeNode treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("reproduction");
        javax.swing.tree.DefaultMutableTreeNode treeNode4 = new javax.swing.tree.DefaultMutableTreeNode("sexual reproduction");
        treeNode3.add(treeNode4);
        treeNode4 = new javax.swing.tree.DefaultMutableTreeNode("...");
        javax.swing.tree.DefaultMutableTreeNode treeNode5 = new javax.swing.tree.DefaultMutableTreeNode("...");
        treeNode4.add(treeNode5);
        treeNode3.add(treeNode4);
        treeNode2.add(treeNode3);
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("cell killing");
        treeNode4 = new javax.swing.tree.DefaultMutableTreeNode("leukocyte mediated cytotoxicity");
        treeNode3.add(treeNode4);
        treeNode4 = new javax.swing.tree.DefaultMutableTreeNode("...");
        treeNode5 = new javax.swing.tree.DefaultMutableTreeNode("...");
        treeNode4.add(treeNode5);
        treeNode3.add(treeNode4);
        treeNode2.add(treeNode3);
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("...");
        treeNode4 = new javax.swing.tree.DefaultMutableTreeNode("...");
        treeNode3.add(treeNode4);
        treeNode2.add(treeNode3);
        treeNode1.add(treeNode2);
        treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("molecular_function");
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("...");
        treeNode2.add(treeNode3);
        treeNode1.add(treeNode2);
        treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("cellular_component");
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("...");
        treeNode2.add(treeNode3);
        treeNode1.add(treeNode2);
        ontologyJTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        ontologyJTree.setToggleClickCount(1);
        jScrollPane2.setViewportView(ontologyJTree);

        jLabel8.setText("Selected Term:");

        definitionGraphSearchJTextPane.setEditable(false);
        jScrollPane7.setViewportView(definitionGraphSearchJTextPane);

        termDetailsGraphSearchJXTable.setModel(new javax.swing.table.DefaultTableModel(
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
        termDetailsGraphSearchJXTable.setOpaque(false);
        jScrollPane8.setViewportView(termDetailsGraphSearchJXTable);

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
                    .add(jLabel8)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                .add(18, 18, 18)
                .add(jLabel8)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 77, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        searchTypeJTabbedPane.addTab("Graph", jPanel6);

        jLabel4.setText("Modification Mass:");

        modificationMassJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel5.setText("Accuracy:");

        massAccuracyJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel6.setText("Search Results:");

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
        olsResultsMassSearchJTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                olsResultsMassSearchJTableKeyReleased(evt);
            }
        });
        olsResultsMassSearchJTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                olsResultsMassSearchJTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(olsResultsMassSearchJTable);

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
        jScrollPane6.setViewportView(termDetailsMassSearchJXTable);

        org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
                    .add(jLabel6)
                    .add(jLabel7)
                    .add(jScrollPane5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
                    .add(jPanel7Layout.createSequentialGroup()
                        .add(jLabel4)
                        .add(18, 18, 18)
                        .add(modificationMassJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 49, Short.MAX_VALUE)
                        .add(jLabel5)
                        .add(18, 18, 18)
                        .add(massAccuracyJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 116, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jPanel7Layout.linkSize(new java.awt.Component[] {massAccuracyJTextField, modificationMassJTextField}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .add(28, 28, 28)
                .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(massAccuracyJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(modificationMassJTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5))
                .add(32, 32, 32)
                .add(jLabel6)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 91, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jLabel7)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jScrollPane6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 77, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        searchTypeJTabbedPane.addTab("Mass", jPanel7);

        jLabel9.setText("Ontology:");

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
                    .add(searchTypeJTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(jLabel9)
                        .add(18, 18, 18)
                        .add(ontologyJComboBox, 0, 412, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(11, 11, 11)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(ontologyJComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel9))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(searchTypeJTabbedPane)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(helpJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(aboutJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 203, Short.MAX_VALUE)
                        .add(insertSelectedJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 514, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
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
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Updates the search results if the ontology is changed.
     * 
     * @param evt
     */
    private void ontologyJComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_ontologyJComboBoxItemStateChanged

        if (searchTypeJTabbedPane.getSelectedIndex() == 0) {
            olsSearchTextFieldKeyReleased(null);
        } else if (searchTypeJTabbedPane.getSelectedIndex() == 1) {
            // update the graph view
        } else if (searchTypeJTabbedPane.getSelectedIndex() == 2) {
            // update the mass search
            // note that all ontologies except PSI-MOD will disable this search option!
        }

    }//GEN-LAST:event_ontologyJComboBoxItemStateChanged

    /**
     * Searches the selected ontology for terms matching the inserted string. 
     * The search finds all terms having the current string as a substring. 
     * (But seems to be limited somehow, seeing as using two letters, can 
     * result in more hits, than using just one of the letters...)
     * 
     * @param evt
     */
    private void olsSearchTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_olsSearchTextFieldKeyReleased

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        olsSearchTextField.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        insertSelectedJButton.setEnabled(false);

        try {

            // clear the two result tables
            while (olsResultsTextSearchJTable.getRowCount() > 0) {
                ((DefaultTableModel) olsResultsTextSearchJTable.getModel()).removeRow(0);
            }

            while (termDetailsTextSearchJXTable.getRowCount() > 0) {
                ((DefaultTableModel) termDetailsTextSearchJXTable.getModel()).removeRow(0);
            }

            // clear the definition
            definitionTextSearchJTextPane.setText("");

            // search the selected ontology and find all matching terms
            QueryService locator = new QueryServiceLocator();
            Query qs = locator.getOntologyQuery();

            String ontology = ((String) ontologyJComboBox.getSelectedItem());
            ontology = ontology.substring(ontology.lastIndexOf("[") + 1, ontology.length() - 1);

            Map map = qs.getTermsByName(olsSearchTextField.getText(), ontology + "", false);

            for (Iterator i = map.keySet().iterator(); i.hasNext();) {
                String key = (String) i.next();
                ((DefaultTableModel) olsResultsTextSearchJTable.getModel()).addRow(new Object[]{key, map.get(key)});
            }

            olsSearchTextField.requestFocus();

            this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            olsSearchTextField.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
            numberOfTermsJTextField.setText("" + map.size());

            // make the first row visible
            if (olsResultsTextSearchJTable.getRowCount() > 0) {
                olsResultsTextSearchJTable.scrollRectToVisible(olsResultsTextSearchJTable.getCellRect(0, 0, false));
            }

            //No matching terms found
            if (map.size() == 0) {
                //JOptionPane.showMessageDialog(this, "No mathcing terms found.");
                //this.olsSearchTextField.requestFocus();
            }
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Not able to contact the OLS. Make sure that you are online and try again.\n" +
                    "You may also want to check your firewall (and proxy) settings.\n\n" +
                    "See the Troubleshooting section at the OLS Dialog home page for details:\n" +
                    "http://code.google.com/p/ols-dialog.",
                    "Failed To Contact OLS", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to access OLS: ");
            ex.printStackTrace();
        } catch (ServiceException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Not able to contact the OLS. Make sure that you are online and try again.\n" +
                    "You may also want to check your firewall (and proxy) settings.\n\n" +
                    "See the Troubleshooting section at the OLS Dialog home page for details:\n" +
                    "http://code.google.com/p/ols-dialog.",
                    "Failed To Contact OLS", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to access OLS: ");
            ex.printStackTrace();
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        olsSearchTextField.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));

    }//GEN-LAST:event_olsSearchTextFieldKeyReleased

    /**
     * Inserts the selected ontology into the parents text field or table and 
     * then closes the dialog.
     * 
     * @param evt
     */
    private void insertSelectedJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertSelectedJButtonActionPerformed

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        String ontologyLong = ((String) ontologyJComboBox.getSelectedItem());
        String ontologyShort = ontologyLong.substring(ontologyLong.lastIndexOf("[") + 1, ontologyLong.length() - 1);
        String selectedValue = "";
        String accession = "" + olsResultsTextSearchJTable.getValueAt(olsResultsTextSearchJTable.getSelectedRow(), 0);

        try {
            QueryService locator = new QueryServiceLocator();
            Query qs = locator.getOntologyQuery();
            selectedValue = qs.getTermById(accession, ontologyShort);

            //insert the value into the correct text field or table
            if (olsInputable != null) {
                olsInputable.insertOLSResult(field, selectedValue, accession, ontologyShort, ontologyLong, modifiedRow, mappedTerm);

                this.setVisible(false);
                this.dispose();
            }

        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Not able to contact the OLS. Make sure that you are online and try again.\n" +
                    "You may also want to check your firewall (and proxy) settings.\n\n" +
                    "See the Troubleshooting section at the OLS Dialog home page for details:\n" +
                    "http://code.google.com/p/ols-dialog.",
                    "Failed To Contact OLS", JOptionPane.ERROR_MESSAGE);
            Util.writeToErrorLog("Error when trying to access OLS: ");
            ex.printStackTrace();
        } catch (ServiceException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Not able to contact the OLS. Make sure that you are online and try again.\n" +
                    "You may also want to check your firewall (and proxy) settings.\n\n" +
                    "See the Troubleshooting section at the OLS Dialog home page for details:\n" +
                    "http://code.google.com/p/ols-dialog.",
                    "Failed To Contact OLS", JOptionPane.ERROR_MESSAGE);
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
    private void insertTermDetails(java.awt.event.MouseEvent evt, JTable searchResultTable, JXTable termDetailsTable, JTextPane definitionTextPane) {

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

                // empty the results table
                while (termDetailsTable.getRowCount() > 0) {
                    ((DefaultTableModel) termDetailsTable.getModel()).removeRow(0);
                }

                String termID = (String) searchResultTable.getValueAt(row, 0);

                try {
                    String ontology = ((String) ontologyJComboBox.getSelectedItem());
                    ontology = ontology.substring(ontology.lastIndexOf("[") + 1, ontology.length() - 1);

                    if (ontology.equalsIgnoreCase("NEWT")) {
                        definitionTextPane.setText("Retreiving 'Term Details' is disabled for NEWT.");
                        definitionTextPane.setCaretPosition(0);
                        termDetailsTable.setEnabled(false);
                    } else {

                        termDetailsTable.setEnabled(true);

                        QueryService locator = new QueryServiceLocator();
                        Query qs = locator.getOntologyQuery();

                        // retrieve the terms meta data and insert into the table
                        // note that "definition" is handled separatly
                        Map map = qs.getTermMetadata(termID, ontology);

                        for (Iterator i = map.keySet().iterator(); i.hasNext();) {
                            String key = (String) i.next();

                            if (key.equalsIgnoreCase("definition")) {
                                definitionTextPane.setText("" + map.get(key));
                                definitionTextPane.setCaretPosition(0);
                            } else {
                                ((DefaultTableModel) termDetailsTable.getModel()).addRow(new Object[]{
                                            key, map.get(key)});
                            }
                        }

                        // retrieve the terms xrefs and insert into the table
                        map = qs.getTermXrefs(termID, ontology);

                        for (Iterator i = map.keySet().iterator(); i.hasNext();) {
                            String key = (String) i.next();

                            ((DefaultTableModel) termDetailsTable.getModel()).addRow(new Object[]{
                                        key, map.get(key)});
                        }
                    }
                } catch (RemoteException ex) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Not able to contact the OLS. Make sure that you are online and try again.\n" +
                            "You may also want to check your firewall (and proxy) settings.\n\n" +
                            "See the Troubleshooting section at the OLS Dialog home page for details:\n" +
                            "http://code.google.com/p/ols-dialog.",
                            "Failed To Contact OLS", JOptionPane.ERROR_MESSAGE);
                    Util.writeToErrorLog("Error when trying to access OLS: ");
                    ex.printStackTrace();
                } catch (ServiceException ex) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Not able to contact the OLS. Make sure that you are online and try again.\n" +
                            "You may also want to check your firewall (and proxy) settings.\n\n" +
                            "See the Troubleshooting section at the OLS Dialog home page for details:\n" +
                            "http://code.google.com/p/ols-dialog.",
                            "Failed To Contact OLS", JOptionPane.ERROR_MESSAGE);
                    Util.writeToErrorLog("Error when trying to access OLS: ");
                    ex.printStackTrace();
                }
            }
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }

    /**
     * If the user double clicks the selected row is inserted into the parent 
     * frame and closes the dialog. A single click retrieves the additional
     * information known about the term and displays it in the "Term Details" 
     * frame.
     * 
     * @param evt
     */
    private void olsResultsTextSearchJTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_olsResultsTextSearchJTableMouseClicked
        insertTermDetails(evt, olsResultsTextSearchJTable, termDetailsTextSearchJXTable, definitionTextSearchJTextPane);
}//GEN-LAST:event_olsResultsTextSearchJTableMouseClicked

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
     * See olsResultsJTableMouseClicked
     * 
     * @param evt
     */
    private void olsResultsTextSearchJTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_olsResultsTextSearchJTableKeyReleased
        olsResultsTextSearchJTableMouseClicked(null);
}//GEN-LAST:event_olsResultsTextSearchJTableKeyReleased

    /**
     * See cancelJButtonActionPerformed
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancelJButtonActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

    /**
     * If the user double clicks the selected row is inserted into the parent
     * frame and closes the dialog. A single click retrieves the additional
     * information known about the term and displays it in the "Term Details"
     * frame.
     *
     * @param evt
     */
    private void olsResultsMassSearchJTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_olsResultsMassSearchJTableMouseClicked
        insertTermDetails(evt, olsResultsMassSearchJTable, termDetailsMassSearchJXTable, definitionMassSearchJTextPane);
}//GEN-LAST:event_olsResultsMassSearchJTableMouseClicked

    /**
     * See olsResultsMassSearchJTableMouseClicked
     *
     * @param evt
     */
    private void olsResultsMassSearchJTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_olsResultsMassSearchJTableKeyReleased
        olsResultsMassSearchJTableMouseClicked(null);
}//GEN-LAST:event_olsResultsMassSearchJTableKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutJButton;
    private javax.swing.JButton cancelJButton;
    private javax.swing.JTextPane definitionGraphSearchJTextPane;
    private javax.swing.JTextPane definitionMassSearchJTextPane;
    private javax.swing.JTextPane definitionTextSearchJTextPane;
    private javax.swing.JButton helpJButton;
    private javax.swing.JButton insertSelectedJButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField massAccuracyJTextField;
    private javax.swing.JTextField modificationMassJTextField;
    private javax.swing.JTextField numberOfTermsJTextField;
    private javax.swing.JTable olsResultsMassSearchJTable;
    private javax.swing.JTable olsResultsTextSearchJTable;
    private javax.swing.JTextField olsSearchTextField;
    private javax.swing.JComboBox ontologyJComboBox;
    private javax.swing.JTree ontologyJTree;
    private javax.swing.JTabbedPane searchTypeJTabbedPane;
    private org.jdesktop.swingx.JXTable termDetailsGraphSearchJXTable;
    private org.jdesktop.swingx.JXTable termDetailsMassSearchJXTable;
    private org.jdesktop.swingx.JXTable termDetailsTextSearchJXTable;
    // End of variables declaration//GEN-END:variables
}
