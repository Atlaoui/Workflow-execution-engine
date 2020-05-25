package srcs.workflow.executor;

import srcs.workflow.graph.Graph;
import srcs.workflow.job.Job;
import srcs.workflow.job.JobValidator;
import srcs.workflow.job.ValidationException;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class JobExecutor {
    protected JobValidator jobV;
    private ReentrantReadWriteLock lock = new  ReentrantReadWriteLock();
    public JobExecutor(Job job){
        try {
            jobV = new JobValidator(job);
            System.out.println(jobV.getTaskGraph().toString());
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    public abstract Map<String,Object> execute() throws Exception;

    public Graph<String> getConcurrentGraph(){
        try {
            lock.readLock().lock();
            return jobV.getTaskGraph();
        }finally {
            lock.readLock().unlock();
        }

    }

}
