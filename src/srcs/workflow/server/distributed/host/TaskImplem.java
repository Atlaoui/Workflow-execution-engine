package srcs.workflow.server.distributed.host;

import srcs.workflow.executor.JobExecutorParallel;
import srcs.workflow.job.Job;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskImplem implements TaskHandler {
    private TaskMasterImplem master;
    private final int nb_max;
    private final String name;
    private ExecutorService pool;
    private JobExecutorParallel job_exec ;
    public TaskImplem (String name,Integer nb_max){
        this.name=name;
        this.nb_max=nb_max;
        pool = Executors.newFixedThreadPool(nb_max);
    }

    @Override
    public void executeDist(Job job,Integer id) throws RemoteException {
        job_exec = new JobExecutorParallel(job);
        try {
            master.putResult(id,job_exec.execute());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Integer getNbTask() throws RemoteException {
        return null;
    }
    @Override
    public int getNb_max() {
        return nb_max;
    }
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void attachMaster(TaskMasterImplem t){
        master = t;
    }

}
