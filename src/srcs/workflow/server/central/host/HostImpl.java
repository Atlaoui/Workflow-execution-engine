package srcs.workflow.server.central.host;

import srcs.workflow.executor.JobExecutorParallel;
import srcs.workflow.job.Job;
import srcs.workflow.server.central.host.Host.Tuple;

import java.rmi.RemoteException;
import java.util.Map;

public class HostImpl implements Host{
    private String name;
    private Integer nb_task;
    private Boolean is_finished = false;

    public HostImpl(String name){
        this.name=name;
    }

    @Override
    public Tuple<Integer, Map<String, Object>> executeDist(Job job) throws RemoteException {
        try {
        	is_finished=false;
        	JobExecutorParallel exec = new JobExecutorParallel(job);
        	 Map<String, Object> map = exec.execute();
        	 is_finished=true;
        	 nb_task=map.size();
        	 System.err.println("Thread "+Thread.activeCount());
            return new Tuple<Integer, Map<String, Object>>(exec.nbTask(),map);
        
        
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RemoteException("Nop le job a echouer");
    }


	@Override
	public Boolean is_finished() throws RemoteException {
		if(is_finished) {
			is_finished=false;
			return true;
		}
		return is_finished;
	}
}
