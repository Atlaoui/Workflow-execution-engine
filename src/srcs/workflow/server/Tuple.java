package srcs.workflow.server;

import java.io.Serializable;

/**
 * Simple class d'encapsulation
 * @param <T>
 * @param <V>
 */
public class Tuple<T, V>  implements Serializable {
    private static final long serialVersionUID = 1L;
    private T name;
    private V value;
    public Tuple(T name, V nb) { this.setName(name); this.setValue(nb);}

    public T getName() { return name; }
    public void setName(T name) { this.name = name; }
    public V getValue() { return value; }
    public void setValue(V value) { this.value = value;}
}