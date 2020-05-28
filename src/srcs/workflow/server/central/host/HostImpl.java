package srcs.workflow.server.central.host;

import srcs.workflow.executor.JobExecutorParallel;
import srcs.workflow.job.Job;

import java.rmi.RemoteException;
import java.util.Map;

public class HostImpl implements Host{
    private String name;
    private Integer nb_task;
    public HostImpl(String name){
        this.name=name;
    }

    @Override
    public Map<String, Object> executeDist(Job job) throws RemoteException {
        try {
        	 Map<String, Object> map = new JobExecutorParallel(job).execute();
        	 nb_task=map.size();
            return map;
        
        
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RemoteException("Nop le job a echouer");
    }

	@Override
	public Integer getNbTask() throws RemoteException {
		// TODO Auto-generated method stub
		return nb_task;
	}
}
