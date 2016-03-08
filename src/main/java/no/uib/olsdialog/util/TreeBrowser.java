package no.uib.olsdialog.util;

import no.uib.olsdialog.OLSDialog;
import uk.ac.pride.ols.web.service.model.Identifier;
import uk.ac.pride.ols.web.service.model.Term;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.Enumeration;

/**
 * A simple tree browser for browsing a given ontology in the OLS. <br><br> The
 * code is based on an example provided by Richard Stanford, a tutorial reader.
 *
 * @author Richard Cote
 * @author Harald Barsnes
 */
public class TreeBrowser extends JPanel implements TreeSelectionListener, TreeModelListener, TreeExpansionListener {

    /**
     * The root node.
     */
    protected DefaultMutableTreeNode rootNode;
    /**
     * The tree model.
     */
    protected DefaultTreeModel treeModel;
    /**
     * The JTree.
     */
    protected JTree tree;
    /**
     * The OLS dialog.
     */
    private OLSDialog olsDialog;
    /**
     * The scroll pane to put the tree browser in.
     */
    private static JScrollPane scrollPane;

    /**
     * Creates a new TreeBrowser with an OLSDialog as the parent.
     * <p>
     * The OLSDialog has methods that are required to update the interface by
     * communicating with the OLS web service.
     *
     * @param parent a reference to the OLSDialog
     */
    public TreeBrowser(OLSDialog parent) {
        super(new GridLayout(1, 0));

        this.olsDialog = parent;
        Term term = new Term(null, null, null, null, null, "Load Ontology to Browse");
        tree = new JTree(new DefaultTreeModel(new DefaultMutableTreeNode(new TermNode(term, null))));
        tree.setEditable(false);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setShowsRootHandles(true);
        tree.addTreeSelectionListener(this);

        // add the tree to a scroll pane
        scrollPane = new JScrollPane(tree);
        add(scrollPane);
    }

    /**
     * Scroll both the vertical and the horizontal scroll panes to the minimum
     * values, i.e., moves the view to the upper left corner.
     */
    public void scrollToTop() {
        scrollPane.getVerticalScrollBar().setValue(0);
        scrollPane.getHorizontalScrollBar().setValue(0);
    }

    /**
     * Set the root node to the ontology label.
     *
     * @param ontologyName the ontology label
     */
    public void initialize(String ontologyName) {
        Term term = new Term(null, null, null, null, null, ontologyName);
        rootNode = new DefaultMutableTreeNode(new TermNode(term, null));
        treeModel = new DefaultTreeModel(rootNode);
        treeModel.addTreeModelListener(this);
        tree.addTreeExpansionListener(this);
        tree.setModel(treeModel);
    }

    /**
     * Remove all nodes except the root node.
     */
    public void clear() {
        rootNode.removeAllChildren();
        treeModel.reload();
    }

    /**
     * Colapses and expands the root node to make sure that all second level non
     * visible nodes are added.
     */
    public void updateTree() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        TreePath path = new TreePath(root.getPath());
        tree.collapsePath(path);
        tree.expandPath(path);
    }

    /**
     * Add child to the currently selected node, or the root node if no
     * selection.
     */
    public DefaultMutableTreeNode addNode(Term term) {
        DefaultMutableTreeNode parentNode;
        TreePath parentPath = tree.getSelectionPath();

        if (parentPath == null) {
            parentNode = rootNode;
        } else {
            parentNode = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
        }

        return addNode(parentNode, term, true);
    }

    /**
     * Add child to a specified node, or the root node if no node specified.
     *
     * @param parent
     * @param shouldBeVisible
     * @return the added node
     */
    public DefaultMutableTreeNode addNode(
            DefaultMutableTreeNode parent, Term term, boolean shouldBeVisible) {

        DefaultMutableTreeNode childNode =
                new DefaultMutableTreeNode(new TermNode(term));

        if (parent == null) {
            parent = rootNode;
        }

        treeModel.insertNodeInto(childNode, parent, parent.getChildCount());

        // make sure the user can see the new node
        if (shouldBeVisible) {
            tree.scrollPathToVisible(new TreePath(childNode.getPath()));
        }

        return childNode;
    }

    // required by TreeModelListener interface
    public void treeNodesChanged(TreeModelEvent e) {
    }

    // required by TreeModelListener interface
    public void treeNodesInserted(TreeModelEvent e) {
    }

    // required by TreeModelListener interface
    public void treeNodesRemoved(TreeModelEvent e) {
    }

    // required by TreeModelListener interface
    public void treeStructureChanged(TreeModelEvent e) {
    }

    /**
     * This method will be called when a user selects a node in the tree.
     * Selecting a node will:<br> 1: load the children of that term<br> 2: load
     * the metadata for that term
     */
    public void valueChanged(TreeSelectionEvent e) {

        olsDialog.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        // get selected node
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();

        if (node == null) {
            return;
        }

        // store the current scroll bar values, used to keep the scroll bars
        // from moving ones the new list of nodes is opened in order to make
        // sure that the opened node remains visible
        int verticalScrollBarValue = scrollPane.getVerticalScrollBar().getValue();
        int horizontalScrollBarValue = scrollPane.getHorizontalScrollBar().getValue();

        // get node data object
        TermNode nodeInfo = (TermNode) node.getUserObject();

        // load the children and the meta data, unless the term is the 'no roots defined' dummy term
        if (nodeInfo.getTerm() != null && nodeInfo.getTerm().getLabel() != null && !nodeInfo.getTerm().getLabel().equalsIgnoreCase("No Root Terms Defined!")) {

            // load children only for leaf nodes and those that have not been marked as processed
            if (node.isLeaf() && node.getAllowsChildren()) {

                if (OLSDialog.debug) {
                    System.out.println("will load children for: " + nodeInfo);
                }

                // load children. if no children, set allowsChildren to false
                if (!olsDialog.loadChildren(node, nodeInfo.getTerm())) {
                    node.setAllowsChildren(false);
                }
            }

            // reset the scroll bars, to make sure the node clicked in the first place is still visible
            scrollPane.getVerticalScrollBar().setValue(verticalScrollBarValue);
            scrollPane.getHorizontalScrollBar().setValue(horizontalScrollBarValue);

            // load metadata
            if (OLSDialog.debug) {
                System.out.println("will load metadata for: " + nodeInfo.getTerm().getGlobalId());
            }

            olsDialog.loadMetaData(nodeInfo.getTerm(), OLSDialog.OLS_DIALOG_BROWSE_ONTOLOGY);
        } else if(nodeInfo.getTerm() != null && nodeInfo.getTerm().getLabel() == null && !nodeInfo.getTerm().getOntologyName().equalsIgnoreCase("No Root Terms Defined!")){

            olsDialog.loadMetaOntologyData(nodeInfo.getTerm().getOntologyName(), OLSDialog.OLS_DIALOG_BROWSE_ONTOLOGY);

        }else {
            olsDialog.clearData(OLSDialog.OLS_DIALOG_BROWSE_ONTOLOGY, true, true);
        }

        olsDialog.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }

    /**
     * Detects when a part of the tree has been expanded and if required load a
     * second level of non visible nodes to make it possible to show folder
     * icons for nodes containing children.
     *
     * @param event
     */
    public void treeExpanded(TreeExpansionEvent event) {

        olsDialog.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        // get selected node
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();

        if (node == null) {
            return;
        }

        // add second level of nodes if not already added
        Enumeration<DefaultMutableTreeNode> enumeration = node.children();

        while (enumeration.hasMoreElements()) {

            DefaultMutableTreeNode currentNode = enumeration.nextElement();

            if (currentNode.getChildCount() == 0) {

                // get node data object
                TermNode nodeInfo = (TermNode) currentNode.getUserObject();

                // add the layer of non visible nodes
                olsDialog.addSecondLevelOfNodes(nodeInfo.getTerm(), olsDialog.getCurrentOntologyLabel(), currentNode);
            }
        }

        olsDialog.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }

    /**
     * Detects when a part of the tree has been collapsed.
     *
     * @param event
     */
    public void treeCollapsed(TreeExpansionEvent event) {
        // required by TreeExpansionListener interface
    }

    /**
     * Inner class that represents a node in the tree. It contains a term name
     * and term id as fields.
     */
    private class TermNode {

        private Term term;
        private Term parentTerm;
        private String type;

        public void setTerm(Term term) {
            this.term = term;
        }

        public TermNode(Term term) {
            this.term = term;
        }

        public TermNode(Term term, Term parentTerm){
            this.term = term;
            this.parentTerm = parentTerm;

        }

        /**
         * Returns the term name.
         *
         * @return the term name
         */
        public String getTermName() {
            return term.getLabel();
        }

        /**
         * Set the term name.
         *
         * @param termName
         */
        public void setTermName(String termName) {
            this.term.setLabel(termName);
        }

        /**
         * Returns the term accession number.
         *
         * @return the term accession number
         */
        public Identifier getOBOTermId() {
            return this.term.getTermOBOId();
        }

        @Override
        public String toString() {
            String nodeString = "";
            if(term != null && term.getGlobalId() != null)
                nodeString += term.getGlobalId().getIdentifier().toUpperCase();
            else if(term != null && term.getGlobalId() == null && term.getLabel() != null)
                nodeString += term.getLabel();
            else if(term != null && term.getOntologyName() != null)
                nodeString += term.getOntologyName().toString().toUpperCase();

            return nodeString;
        }

        public Term getTerm() {
            return term;
        }

        public Term getParentTerm() {
            return parentTerm;
        }

        public void setParentTerm(Term parentTerm) {
            this.parentTerm = parentTerm;
        }
    }
}
