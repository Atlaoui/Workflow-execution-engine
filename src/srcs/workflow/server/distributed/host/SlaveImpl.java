package srcs.workflow.server.distributed.host;

import srcs.workflow.executor.JobExecutorParallel;
import srcs.workflow.job.Job;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SlaveImpl implements TaskHandler {
	private MasterImpl master;
	private final int nb_max;
	private final String name;
	private ExecutorService pool;
	private JobExecutorParallel job_exec ;
	public SlaveImpl(String name, Integer nb_max){

		this.name=name;
		this.nb_max=nb_max;
		pool = Executors.newFixedThreadPool(nb_max);
		String nameMaster = "Master";
		try {
			Registry registry = LocateRegistry.getRegistry("localhost");
			master= (MasterImpl) registry.lookup(nameMaster);
			master.attach(this);
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void executeDist(Job job,Integer id) throws RemoteException {
		job_exec = new JobExecutorParallel(job);
		try {
			master.putResult(id,job_exec.execute());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Integer getNbTask() throws RemoteException {
		return null;
	}
	@Override
	public int getNb_max() {
		return nb_max;
	}
	

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return "Slave [master=" + master + ", Max Tasks=" + nb_max + ", name=" + name+ "]";
	}

	@Override
	public Map<String, Object> GetOneJob(Job job) throws RemoteException {
		// TODO Auto-generated method stub
		try {
			return new JobExecutorParallel(job).execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new RemoteException("One job not Ok") ;
	}


}
