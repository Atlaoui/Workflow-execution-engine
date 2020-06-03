package srcs.workflow.server.central.host;

import srcs.workflow.job.*;
import srcs.workflow.server.Tuple;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HostImpl implements Host{
    private String Hostname;
    private ConcurrentHashMap<Long, Tuple<AtomicInteger, Map<String, Object>>> tasks = new ConcurrentHashMap<>();
    private Long ID_CUR = 0L;

    public HostImpl(String name){
        this.Hostname=name;
    }

    @Override
    public long executeDist(JobValidator job) throws RemoteException {
        try {

        	 final long id_cur=ID_CUR;
             nextId();
                tasks.put(id_cur,new Tuple<>(new AtomicInteger(0),new ConcurrentHashMap<>()));
        	 new Thread(()->{
                for(String node : job.getTaskGraph())
                    new Thread(new ThreadJob(id_cur,node,job.getJob())).start();
        	 }).start();
            return id_cur;
        
        
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RemoteException("Nop le job a echouer");
    }

    @Override
	public Integer is_finished(long id_job) throws RemoteException {
		if(tasks.containsKey(id_job))
             return tasks.get(id_job).getName().get();
		return 0;
	}

    @Override
    public Map<String, Object> getMyResult(long id_job) {
        return tasks.get(id_job).getValue();
    }

    // pour avoir des id qui tourne
	private void nextId(){
        ID_CUR= (ID_CUR+1)%Long.MAX_VALUE;
    }

    private synchronized Object getArg(String Name ,long id_job){
        while (!tasks.get(id_job).getValue().containsKey(Name)) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return tasks.get(id_job).getValue().get(Name);
    }

    private synchronized void relaseAll(){
        notifyAll();
    }

    private class ThreadJob implements Runnable {
        private String node;
        private Job job_t;
        private long id_thread;
        private ThreadJob(long id_T,String node, Job job_t){
            this.node=node; this.job_t=job_t;this.id_thread=id_T;
        }

        @Override
        public void run() {
            try {
                Method m = getMethodByName(node);
                Object[] args= new Object[m.getParameterCount()];
                int index = 0;

                for(Parameter p : m.getParameters()){
                    if(p.isAnnotationPresent(Context.class)){
                        args[index]=job_t.getContext().get(p.getAnnotation(Context.class).value());//a thread safer
                    }else if(p.isAnnotationPresent(LinkFrom.class)){
                        args[index]=getArg(p.getAnnotation(LinkFrom.class).value(),id_thread);
                    }
                    index++;
                }
                tasks.get(id_thread).getName().getAndAdd(1);
                tasks.get(id_thread).getValue().put(node,m.invoke(job_t,args));
                relaseAll();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        private Method getMethodByName(String name) throws Exception {
            for (Method m : job_t.getClass().getMethods())
                if(m.isAnnotationPresent(Task.class) && m.getAnnotation(Task.class).value().equals(name))
                    return m;
            throw new Exception("Method not fund");
        }
    }

}
