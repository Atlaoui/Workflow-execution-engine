package srcs.workflow.graph;

import java.io.Serializable;
import java.util.*;

public class GraphImpl<T> implements Graph<T> {
    /**
     * Version
     */
    private static final long serialVersionUID = 1L;

    private Map<T, Tuple> map = new HashMap<>();


    @Override
    public void addNode(T n) throws IllegalArgumentException{
        if(map.containsKey(n))
            throw new IllegalArgumentException("Node exists");
        map.put(n,new Tuple());
    }

    @Override
    public void addEdge(T from, T to) throws IllegalArgumentException {
        if(!map.containsKey(from) || !map.containsKey(to))
            throw new IllegalArgumentException("Node dont exists");

        List<T> l = map.get(from).vertex_out;

        if(l.contains(to))
            throw new IllegalArgumentException("Link exists");

        map.get(to).setIn(from);
        map.get(from).setOut(to);
    }

    @Override
    public boolean existEdge(T from, T to) {
        Tuple node = map.get(to);
        if(node == null)
            return false;
        return node.vertex_in.contains(from);
    }

    @Override
    public boolean existNode(T n) {
        return map.containsKey(n);
    }

    @Override
    public List<T> getNeighborsIn(T to) {
        Tuple t = map.get(to);
        if(t==null)
            throw new IllegalArgumentException("No neighbors in with this name");
        return t.vertex_in;
    }

    @Override
    public List<T> getNeighborsOut(T from) throws IllegalArgumentException {
            Tuple t = map.get(from);
            if(t==null)
                throw new IllegalArgumentException("No neighbors out with this name");
        return t.vertex_out;
    }

    //vois si y a pas mieux
    @Override
    public Set<T> accessible(T from) throws IllegalArgumentException {
        Tuple t = map.get(from);
        if(t==null)
            throw new IllegalArgumentException("No neighbors accessible with this name");
        Set<T>  visited = new HashSet<T>();
        Stack<T> S = new Stack<>();
        S.push(from);
        T v;
        boolean is_first=true;
        while(!S.empty()){
            v=S.pop();
            if(!visited.contains(v)){
                if(is_first)
                    is_first=false;
                else
                    visited.add(v);
                for(T w : map.get(v).vertex_out){
                    S.push(w);
                }
            }
        }
        return visited;
    }

    @Override
    public boolean isDAG() {
        Iterator<T> iter = map.keySet().iterator();
        T tmp;
        while (iter.hasNext()){
            tmp=iter.next();
            if(accessible(tmp).contains(tmp))
                return false;
        }
        return true;
    }

    @Override
    public boolean isEmpty() throws IllegalArgumentException {
        return map.isEmpty();
    }

    @Override
    public int size() {
        return map.size();
    }



    @Override
    public Iterator<T> iterator() {
        return map.keySet().iterator();
    }

    private class Tuple implements Serializable {
        /**
         * Version
         */
        private static final long serialVersionUID = 1L;
        /**arc entrant*/
        private List<T> vertex_in=new LinkedList<T>();
        /**arc sortant*/
        private List<T> vertex_out=new LinkedList<>();

        void setIn(T n){
            vertex_in.add(n);
        }
        void setOut(T n){
            vertex_out.add(n);
        }
    }


    @Override
    public String toString() {
        StringBuilder S = new StringBuilder();
        for(Map.Entry<T, Tuple> entry: map.entrySet()){
            S.append("Node ").append(entry.getKey()).append("\n");
            S.append("Vertex in : ").append(entry.getValue().vertex_in).append("\n");
            S.append("Vertex out : ").append(entry.getValue().vertex_out).append("\n");
        }
        return S.toString();
    }
}
