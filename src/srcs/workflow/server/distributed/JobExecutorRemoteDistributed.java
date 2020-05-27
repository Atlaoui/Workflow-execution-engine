package srcs.workflow.server.distributed;

import srcs.workflow.executor.JobExecutor;
import srcs.workflow.job.Job;

import java.util.Map;

public class JobExecutorRemoteDistributed extends JobExecutor {
    public JobExecutorRemoteDistributed(Job job) {
        super(job);
    }

    @Override
    public Map<String, Object> execute() throws Exception {
        return null;
    }
}
