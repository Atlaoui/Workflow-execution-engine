package srcs.workflow.server.central;

import srcs.workflow.executor.JobExecutor;
import srcs.workflow.job.Job;
import srcs.workflow.server.central.host.Host;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

public class JobExecutorRemoteCentral extends JobExecutor implements Remote , Serializable {
    /**
     * Version
     */
    private static final long serialVersionUID = 1L;

    public JobExecutorRemoteCentral(Job job) {
        super(job);
    }

    @Override
    public Map<String, Object> execute() throws Exception {
        String name = "JobRemote";
        Registry registry = LocateRegistry.getRegistry("localhost");
        Host s1 = (Host)registry.lookup(name);
       long id =s1.executeDist(jobV);
       while (jobV.getTaskGraph().size()!= s1.is_finished(id)) {
           Thread.sleep(100);
       }
       System.out.println(s1.is_finished(id));

	    return  s1.getMyResult(id);
    }
}
