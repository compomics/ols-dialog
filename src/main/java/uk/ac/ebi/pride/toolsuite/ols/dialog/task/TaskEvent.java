package uk.ac.ebi.pride.toolsuite.ols.dialog.task;

import java.util.EventObject;

public class TaskEvent<V> extends EventObject {

    private final V value;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public TaskEvent(AbstractTask source, V value) {
        super(source);
        this.value = value;
    }

    public V getValue() {
        return value;
    }
}
