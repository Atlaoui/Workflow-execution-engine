package srcs.workflow.executor;

import srcs.workflow.job.Context;
import srcs.workflow.job.Job;
import srcs.workflow.job.LinkFrom;
import srcs.workflow.job.Task;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class JobExecutorParallel extends JobExecutor{

    private Map<String, Object> retValues = new ConcurrentHashMap<>();
    public JobExecutorParallel(Job job) {
        super(job);
    }

    @Override
    public Map<String, Object> execute() throws Exception {
        int index=0 ;
        Thread [] tasks = new Thread[jobV.getTaskGraph().size()];

        for(String funcName : jobV.getTaskGraph()){
            tasks[index]=new Thread(new ThreadJob(funcName));
            tasks[index].start();
            index++;
        }

        for(Thread t : tasks)
            t.join();

        return retValues;
    }

    //a factoriser avec JobExecParallele
    private class ThreadJob implements Runnable {
        private String node;
        private ThreadJob(String node){
            this.node=node;
        }

        @Override
        public void run() {
            try {
                Method m = getMethodByName(node);
                Object[] args= new Object[m.getParameterCount()];
                int index = 0;

                for(Parameter p : m.getParameters()){
                    if(p.isAnnotationPresent(Context.class)){
                         args[index]=jobV.getJob().getContext().get(p.getAnnotation(Context.class).value());//a thread safer
                    }else if(p.isAnnotationPresent(LinkFrom.class)){
                        args[index]=getArg(p.getAnnotation(LinkFrom.class).value());
                    }
                    index++;
                }
                retValues.put(node,m.invoke(jobV.getJob(),args));
                relaseAll();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }




    public synchronized Object getArg(String Name){
        Object val ;
        while(!retValues.containsKey(Name)){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        val = retValues.get(Name);
        return val;
    }

    public synchronized void relaseAll(){
        notifyAll();
    }
    

    private Method getMethodByName(String name) throws Exception {
        for (Method m : aquireJob().getClass().getMethods())
            if(m.isAnnotationPresent(Task.class) && m.getAnnotation(Task.class).value().equals(name))
                return m;
        throw new Exception("Method not fund");
    }
    
   

}
