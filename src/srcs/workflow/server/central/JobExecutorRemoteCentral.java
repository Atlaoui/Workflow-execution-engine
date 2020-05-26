package srcs.workflow.server.central;

import srcs.workflow.executor.JobExecutor;
import srcs.workflow.job.Job;

import java.io.Serializable;
import java.rmi.Remote;
import java.util.Map;

public class JobExecutorRemoteCentral extends JobExecutor implements Remote , Serializable {
    /**
     * Version
     */
    private static final long serialVersionUID = 1L;

    private String serviceName="host_job";

    public JobExecutorRemoteCentral(Job job) {
        super(job);
    }

    @Override
    public Map<String, Object> execute() throws Exception {
        return null;
    }
}
