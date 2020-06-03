package srcs.workflow.server.distributed;

import srcs.workflow.executor.JobExecutor;
import srcs.workflow.job.Job;
import srcs.workflow.server.distributed.host.TaskMaster;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
public class JobExecutorRemoteDistributed extends JobExecutor{

	public JobExecutorRemoteDistributed(Job job) {
        super(job);
    }

    @Override
    public Map<String, Object> execute() throws Exception {
    	System.out.println("Je commence le execute");

    	Registry registry = LocateRegistry.getRegistry("localhost");
        
        TaskMaster master = (TaskMaster) registry.lookup("Master");
        long id = master.executeTask(jobV);
        System.out.println("l'id du travaille que je demande est : "+id);
        //attendre que la réponse soit positive
        try {
			while(!master.isJobReady(id)) 
				Thread.sleep(400);
		} catch (InterruptedException | RemoteException e) {
			e.printStackTrace();
		}
        System.out.println("my job is ready");
        return master.getJob(id);
    }
    
 
}
