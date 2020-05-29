package srcs.workflow.server.distributed.host;

import srcs.workflow.job.Job;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class MasterImpl implements TaskMaster {

    private Map<Integer,TaskHandler> slaves = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer,Map<String, Object>> Retvalues= new ConcurrentHashMap<>();
    private LinkedBlockingQueue<Map<String, Object>> dataQueue = new LinkedBlockingQueue<>();
    private ReentrantLock lock = new ReentrantLock();
    private Integer id = 0;
    private Integer current=0;
    
    
    
    public MasterImpl() {
    	System.out.println("Master crée");
    }
    
    @Override
    public Integer executeTask(Job job) throws RemoteException {
    	System.out.println("J'execute Task");
      // TaskHandler t = slaves.get(current);
        return slaves.size();
    }

//    private Map.Entry<Integer,TaskHandler> getTaskHandler(){}

    @Override
    public  Map<String, Object> getMyResult(int key){   
        return dataQueue.peek();
    }

    @Override
    public void putResult(Integer key, Map<String, Object> value){
        //Retvalues.put(key,value);
    	dataQueue.add(value);
    	
        System.out.println("Je put un result");
        notifyAll();
        current--;
    }

    @Override
    public  void attach(TaskHandler t) throws RemoteException{
        slaves.put(id++,t);
        System.out.println("Je m'attache "+id);
    }
    @Override
	public String toString() {
		return "Master [ slaves = "+ slaves.size()+"]";
	}

	@Override
	public Map<String, Object> getOneJob(Job jobd) throws RemoteException {
		// TODO Auto-generated method stub
		return slaves.get(0).GetOneJob(jobd);
	}
}
