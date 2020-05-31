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

public class SlaveImpl implements TaskHandler{

	private TaskMaster master;
	private final int nb_max;
	private final String name;
	private ExecutorService pool;
	
	
	
	public SlaveImpl(String name, Integer nb_max){
		System.out.println("Slave Constructeur");
		this.name=name;
		this.nb_max=nb_max;
		pool = Executors.newFixedThreadPool(nb_max);
		String nameMaster = "Master";
		try {
			Registry registry = LocateRegistry.getRegistry("localhost");
			master= (TaskMaster) registry.lookup(nameMaster);
			master.attach(name,nb_max);
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
			System.err.println("Nop Slave ce c'est pas attacher  !!!");
		}
		System.out.println("Slave Constructeur OK");
		
	}

	@Override
	public void executeDist(Job job,Integer id) throws RemoteException {
	//	job_exec = new JobExecutorParallel(job);
		try {
	//		master.putResult(id,job_exec.execute());
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	public Map<String, Object> GetOneJobFromSlave(Integer id , Job job) throws RemoteException {
		try {
			System.out.println("Get One job from the Slave "+this.toString());
			 Map<String, Object> value = new JobExecutorParallel(job).execute();
			//master.putResult(id, value);
			 return value;
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new RemoteException("One job not Ok") ;
	}


}
