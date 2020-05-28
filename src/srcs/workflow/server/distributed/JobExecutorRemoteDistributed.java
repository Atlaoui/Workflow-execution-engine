package srcs.workflow.server.distributed;

import srcs.workflow.executor.JobExecutor;
import srcs.workflow.job.Job;
import srcs.workflow.server.distributed.host.TaskMaster;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

public class JobExecutorRemoteDistributed extends JobExecutor {
    public JobExecutorRemoteDistributed(Job job) {
        super(job);
    }

    @Override
    public Map<String, Object> execute() throws Exception {
        Registry registry = LocateRegistry.getRegistry("localhost");
        TaskMaster master = (TaskMaster) registry.lookup("TrackerMaster");
        int id = master.executeTask(jobV.getJob());
        return master.getMyResult(id);
    }
}
