package uk.ac.ebi.pride.toolsuite.ols.dialog.message;


import uk.ac.ebi.pride.toolsuite.ols.dialog.prop.PropertyChangeHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ThrowableHandler extends PropertyChangeHelper {
    /** This property is triggered when adding a new throwable entry */
    public static final String ADD_THROWABLE_PROP = "ADD_THROWABLE_PROP";
    
    /** This property is triggered when removing a new throwable entry */
    public static final String REMOVE_THROWABLE_PROP = "REMOVE_THROWABLE_PROP";
    /**
     * A list of current exceptions
     */
    private final List<ThrowableEntry> throwables;

    public ThrowableHandler() {
        throwables = Collections.synchronizedList(new ArrayList<ThrowableEntry>());
    }

    /**
     * Return a list of all throwables.
     *
     * @return  List<ThrowableEntry> a list of throwables.
     */
    public List<ThrowableEntry> getAllThrowables() {
        return new ArrayList<>(throwables);
    }

    /**
     * Add a new ThrowableEntry
     *
     * @param entry new ThrowableEntry
     */
    public void addThrowableEntry(ThrowableEntry entry) {
        List<ThrowableEntry> oldThrowables, newThrowables;
        synchronized (throwables) {
            oldThrowables = new ArrayList<>(throwables);
            throwables.add(entry);
            newThrowables = new ArrayList<>(throwables);
        }
        // notify
        firePropertyChange(ADD_THROWABLE_PROP, oldThrowables, newThrowables);
    }

    /**
     * Remove an Throwable entry
     *
     * @param entry throwable entyr
     */
    public void removeThrowableEntry(ThrowableEntry entry) {
        List<ThrowableEntry> oldThrowables, newThrowables;
        synchronized (throwables) {
            oldThrowables = new ArrayList<>(throwables);
            throwables.remove(entry);
            newThrowables = new ArrayList<>(throwables);
        }

        // notify
        firePropertyChange(REMOVE_THROWABLE_PROP, oldThrowables, newThrowables);
    }

    /**
     * Remove all the throwable enties.
     */
    public void removeAllThrowableEntries() {
        List<ThrowableEntry> oldThrowables, newThrowables;
        synchronized (throwables) {
            oldThrowables = new ArrayList<>(throwables);
            throwables.clear();
            newThrowables = new ArrayList<>(throwables);
        }

        // notify
        firePropertyChange(REMOVE_THROWABLE_PROP, oldThrowables, newThrowables);
    }
}
