package srcs.workflow.server.central;

import srcs.workflow.executor.JobExecutor;
import srcs.workflow.job.Job;
import srcs.workflow.server.central.host.Host;
import srcs.workflow.server.central.host.Host.Tuple;

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
        Tuple<Integer, Map<String, Object>> ret =s1.executeDist(jobV.getJob());
	    //ret.forEach((k, v) -> {System.out.println(v);});
        System.out.println(ret.x);
        return ret.y;
    }
}
