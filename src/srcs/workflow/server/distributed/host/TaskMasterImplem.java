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

public class TaskMasterImplem implements TaskMaster {

    private Map<Integer,TaskHandler> slaves = new HashMap<>();
    private ConcurrentHashMap<Integer,Map<String, Object>> Retvalues= new ConcurrentHashMap<>();
    private Integer id = 0;
    private Integer current=0;
    private Registry registry ;
    public  TaskMasterImplem() {
    	try {
			registry = LocateRegistry.getRegistry("localhost");
	
    	String name = "Monpetit";
        TaskHandler task = new TaskImplem("cool",10);
        UnicastRemoteObject.exportObject(task,0);
        current++;
        registry.rebind(name,task);
        attach((TaskHandler) registry.lookup(name));
    	} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("1111111111111111111111111111111111111111111111111");
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
	}
    

    @Override
    public Integer executeTask(Job job) throws RemoteException {
       // if(current>=slaves.size())
        //    throw new RemoteException("Pas assez de Jvm");
        TaskHandler t = slaves.get(current);
       // current++;
        return current-1;
    }

//    private Map.Entry<Integer,TaskHandler> getTaskHandler(){}

    @Override
    public synchronized Map<String, Object> getMyResult(int key){
        while(!Retvalues.contains(key))
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        return Retvalues.remove(key);
    }

    @Override
    public synchronized void putResult(Integer key, Map<String, Object> value){
        Retvalues.put(key,value);
        notifyAll();
        current--;
    }

    @Override
    public void attach(TaskHandler t) throws RemoteException{
        slaves.put(id++,t);
        t.attachMaster(this);
    }
}
