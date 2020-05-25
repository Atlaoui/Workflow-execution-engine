package srcs.workflow.executor;

import srcs.workflow.job.Context;
import srcs.workflow.job.Job;
import srcs.workflow.job.LinkFrom;
import srcs.workflow.job.Task;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JobExecutorParallel extends JobExecutor{
    public JobExecutorParallel(Job job) {
        super(job);
    }


    @Override
    public Map<String, Object> execute() throws Exception {
        Map<String, Object> retValues = new ConcurrentHashMap<>();
        Object[] args;
        Parameter[] param;
        int index ;
        ExecutorService pool = Executors.newFixedThreadPool(jobV.getTaskGraph().size());

        pool.shutdown();
        return retValues;
    }

    private class ThreadJob implements Runnable {

        @Override
        public void run() {

        }
    }


}
