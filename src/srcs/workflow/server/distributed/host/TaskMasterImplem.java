package srcs.workflow.server.distributed.host;

import srcs.workflow.job.Job;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TaskMasterImplem implements TaskMaster {

    private Map<Integer,TaskHandler> slaves = new HashMap<>();
    private ConcurrentHashMap<Integer,Map<String, Object>> Retvalues= new ConcurrentHashMap<>();
    private Integer id = 0;
    private Integer current=0;

    @Override
    public Integer executeTask(Job job) throws RemoteException {
        if(current>=slaves.size())
            throw new RemoteException("Pas assez");
        TaskHandler t = slaves.get(current);
        current++;
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
    public void attach(TaskHandler t){
        slaves.put(id++,t);
        t.attachMaster(this);
    }
}
