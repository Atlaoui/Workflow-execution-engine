package srcs.workflow.executor;

import srcs.workflow.job.Job;
import srcs.workflow.job.JobValidator;
import srcs.workflow.job.ValidationException;

import java.util.Map;

public abstract class JobExecutor {
    protected JobValidator jobV;

    public JobExecutor(Job job){
        try {
            jobV = new JobValidator(job);
            System.out.println(jobV.getTaskGraph().toString());
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    public abstract Map<String,Object> execute() throws Exception;
}
