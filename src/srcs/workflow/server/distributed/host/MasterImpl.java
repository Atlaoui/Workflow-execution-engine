package srcs.workflow.server.distributed.host;

import srcs.workflow.job.Job;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MasterImpl implements TaskMaster {

    private List<Pair<String,Integer>> slaves = new ArrayList<>();
    
    private HashMap<Integer,Map<String, Object>> Retvalues= new HashMap<>();

    private List<Pair<Integer,Job>>  awaitList = new ArrayList<>();
    
    private Integer id = 0;
    private Integer current=0;
    
    
    
    public MasterImpl() {
    	System.out.println("Master crée");
    }
    
    @Override
    public  Integer executeTask(Job job) throws RemoteException {
    	TaskHandler t;
		try {
			t = connectToSlave(slaves.get(0).id);
			t.GetOneJobFromSlave(0,job);
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
			throw new RemoteException("Get one job is not OK");
		}
		current++;
        return current;
    }

//    private Map.Entry<Integer,TaskHandler> getTaskHandler(){}

    @Override
    public synchronized void  putResult(Integer key, Map<String, Object> value){
        Retvalues.put(key,value);
        System.out.println("Je put un result");
    }

    @Override
    public  void attach(String SlaveName,Integer nbMax) throws RemoteException{
        slaves.add(new Pair(SlaveName,nbMax));
        System.out.println("Je m'attache "+id);
    }
	@Override
	public synchronized Boolean isJobReady(Integer id) throws RemoteException {
		return Retvalues.containsKey(id);
	}

	@Override
	public synchronized Map<String, Object> getOneJob(Job job) throws RemoteException {
		TaskHandler t;
		try {
			t = connectToSlave(slaves.get(0).id);
		//	t.GetOneJobFromSlave(id,job);
		} catch (RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RemoteException("Get one job is not OK");
		}
		return t.GetOneJobFromSlave(id, job);
	} 
	

	private TaskHandler connectToSlave(String name) throws RemoteException, NotBoundException{
		System.out.println("Master : je me connecte a l'esclave de nom "+name);
		Registry registry = LocateRegistry.getRegistry("localhost");
		TaskHandler slave =  (TaskHandler) registry.lookup(name);
		return slave;
	}
	
	private class Pair<T, V> implements Serializable{ 
		private static final long serialVersionUID = 1L;
		public final T id;
    	public  V value; 
        public Pair(T name, V nb) { this.id = name; this.value = nb;} 
    }
    @Override
	public String toString() {
		return "Master [ slaves = "+ slaves.size()+"]";
	}

	@Override
	public Map<String, Object> getJob(Integer id) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
    

}
