package srcs.workflow.graph;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public interface Graph<T> extends Iterable<T> , Serializable {

    void addNode(T n) throws IllegalArgumentException;

    void addEdge(T from, T to) throws IllegalArgumentException;


    boolean existEdge(T from, T to);


    boolean isEmpty() throws IllegalArgumentException;

    int size();



    boolean existNode(T n);

    List<T> getNeighborsIn(T to)throws IllegalArgumentException;

    List<T> getNeighborsOut(T from) throws IllegalArgumentException;

    Set<T> accessible(T from) throws IllegalArgumentException;

    // je me suis permis d'étandre l'interface
    // pour ne pas bricoler la récuperation de touts les noeud
    Set<T> getAllNodes ();

    boolean isDAG();
}
