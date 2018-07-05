package uk.ac.ebi.pride.toolsuite.ols.dialog.taskmanager;

import uk.ac.ebi.pride.toolsuite.ols.dialog.OLSDialog;
import uk.ac.ebi.pride.toolsuite.ols.dialog.message.ThrowableEntry;
import uk.ac.ebi.pride.toolsuite.ols.dialog.message.ThrowableHandler;
import uk.ac.ebi.pride.toolsuite.ols.dialog.util.GUIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.List;

/**
 * NotificationPanel is to display an error icon when an exception has been caught.
 *
 * User: rwang
 * Date: 16-Nov-2010
 * Time: 13:29:54
 */
public class NotificationPanel extends StatusBarPanel {

    /**
     * Throwable dialog
     */
    private JDialog throwableMessageBoard;
    /**
     * Error message label
     */
    private JLabel errorLabel;
    /**
     * Reference to Pride inspector context
     */

    public NotificationPanel(OLSDialog olsDialog) {
        super(24, true);

        this.setLayout(new BorderLayout());

        // Throwable message board
        throwableMessageBoard = new NotificationDialog(olsDialog);
        throwableMessageBoard.setVisible(false);

        // get pride inspector context
        Icon icon = GUIUtils.loadIcon("icons/no_exception_notify.png");
        errorLabel = new JLabel(icon);
        errorLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                throwableMessageBoard.setVisible(true);
            }
        });
        this.add(errorLabel, BorderLayout.CENTER);

        // add itself as a listener to task manager
        ThrowableHandler throwableHandler = olsDialog.getThrowableHandler();
        throwableHandler.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String eventName = evt.getPropertyName();
        if (ThrowableHandler.ADD_THROWABLE_PROP.equals(eventName)) {
            Icon icon = GUIUtils.loadIcon("icons/exception_notify.png");
            errorLabel.setIcon(icon);
        } else if (ThrowableHandler.REMOVE_THROWABLE_PROP.equals(eventName)) {
            List<ThrowableEntry> throwables = (List<ThrowableEntry>)evt.getNewValue();
            if (throwables.isEmpty()) {
                Icon icon = GUIUtils.loadIcon("icons/no_exception_notify.png");
                errorLabel.setIcon(icon);
            }
        }

    }
}
