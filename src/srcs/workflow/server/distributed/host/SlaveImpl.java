package srcs.workflow.server.distributed.host;

import srcs.workflow.executor.JobExecutorParallel;
import srcs.workflow.job.Context;
import srcs.workflow.job.Job;
import srcs.workflow.job.LinkFrom;
import srcs.workflow.job.Task;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SlaveImpl implements TaskHandler{

	private TaskMaster master;
	private final int nb_max;
	private AtomicInteger nb_task_cur ;
	private final String name;
	
	
	
	public SlaveImpl(String name, Integer nb_max){
		System.out.println("Slave Constructeur");
		this.name=name;
		this.nb_max=nb_max;
		nb_task_cur = new AtomicInteger(nb_max);
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
	public void executeDist(long idJob, List<String> nodes , Job job) throws RemoteException{
		System.out.println("Execut dist a était appeler");
		try {
			for(String node : nodes)
				new Thread (
					new JobRunnerSlave(idJob,node,job)
				).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class JobRunnerSlave implements Runnable{
		private String n;
		private Job j;
		private long id;
		private JobRunnerSlave(long idJob,String n, Job job){
			System.out.println("Job runner Slave");
			this.j=job;this.n=n;id=idJob;
			nb_task_cur.getAndDecrement();

		}
		@Override
		public void run() {
			try {
				System.out.println("le thread de l'esclave commence sont job");
				Method m = getMethodByName(n);
				assert m != null;
				int index = 0;
				Map<String,Object> link_from = null;
				Object[] args= new Object[m.getParameterCount()];
				for(Parameter p : m.getParameters()){
					if(p.isAnnotationPresent(Context.class)){
						System.out.println("contexte Slave");
						args[index]=j.getContext().get(p.getAnnotation(Context.class).value());//a thread safer
					}else if(p.isAnnotationPresent(LinkFrom.class)){
						System.out.println("Linfrom Slave");
						String func_name = p.getAnnotation(LinkFrom.class).value();
						if(link_from == null || !link_from.containsKey(func_name))
							link_from=master.getResult(id,func_name);
						System.out.println("LinkFrom "+link_from);
						args[index]=link_from.get(func_name);
						System.out.println("Fin de ling from");
					}
					index++;
				}
				master.putResult(id,n,m.invoke(j,args));
				nb_task_cur.getAndIncrement();
				System.out.println("le thread de l'esclave termine sont job");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private Method getMethodByName(String name){
			for (Method m : j.getClass().getMethods())
				if(m.isAnnotationPresent(Task.class) && m.getAnnotation(Task.class).value().equals(name))
					return m;
			return null;
		}
	}

	@Override
	public int getNbCurTasks() {
		return nb_task_cur.get();
	}
	

	@Override
	public String getName() {
		return name;
	}



}
