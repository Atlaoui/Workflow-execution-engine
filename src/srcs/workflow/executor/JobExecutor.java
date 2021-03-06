package srcs.workflow.executor;

import srcs.workflow.graph.Graph;
import srcs.workflow.job.Job;
import srcs.workflow.job.JobValidator;
import srcs.workflow.job.ValidationException;


import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class JobExecutor  {
    protected JobValidator jobV;
    // pas vraiment nécessaisre au final
    private ReentrantReadWriteLock lock = new  ReentrantReadWriteLock();
    public JobExecutor(Job job){
        try {
            jobV = new JobValidator(job);
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    public abstract Map<String,Object> execute() throws Exception;

    // les lock non pas était necessaire au final
    public Graph<String> aquireGraph(){
            lock.readLock().lock();
            return jobV.getTaskGraph();
    }
    public void relaseGraph(){
        lock.readLock().unlock();
    }
    public Job aquireJob(){
            lock.readLock().lock();
            return jobV.getJob();
    }
    public void relaseJob(){
        lock.readLock().unlock();
    }

}
