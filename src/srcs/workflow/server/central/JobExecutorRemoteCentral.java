package srcs.workflow.server.central;

import srcs.workflow.executor.JobExecutor;
import srcs.workflow.job.Job;

import java.util.Map;

public class JobExecutorRemoteCentral extends JobExecutor {
    public JobExecutorRemoteCentral(Job job) {
        super(job);
    }

    @Override
    public Map<String, Object> execute() throws Exception {
        return null;
    }
}
