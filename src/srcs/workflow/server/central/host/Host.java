package srcs.workflow.server.central.host;


import srcs.workflow.job.Job;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Host extends Remote {

	Tuple<Integer, Map<String, Object>> executeDist(Job job) throws RemoteException;
    
    
    Boolean is_finished() throws RemoteException;
    
    public class Tuple<X, Y> implements Serializable{ 
		private static final long serialVersionUID = 1L;
		public final X x; 
    	public final Y y; 
        public Tuple(X x, Y y) { this.x = x; this.y = y;} 
    } 

}
