package srcs.workflow.server;

import srcs.workflow.executor.JobExecutorParallel;
import srcs.workflow.job.Job;

import java.rmi.RemoteException;
import java.util.Map;

public class HostImpl implements Host{
    private String name;
    public HostImpl(String name){
        this.name=name;
    }

    @Override
    public Map<String, Object> executeDist(Job job) throws RemoteException {
        try {
            return new JobExecutorParallel(job).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RemoteException("Nop le job a echouer");
    }
}
