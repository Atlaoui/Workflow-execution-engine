package srcs.workflow.server.distributed.host;

import srcs.workflow.job.Context;
import srcs.workflow.job.Job;
import srcs.workflow.job.LinkFrom;
import srcs.workflow.job.Task;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class SlaveImpl implements TaskHandler{

	private TaskMaster master;
	private AtomicInteger nb_task_cur ;
	private final String name;
	private ExecutorService pool ;
	private List<Pair<Long,Future<?>>> toCancel = new ArrayList<>();

	
	
	
	public SlaveImpl(String name, Integer nb_max){
		System.out.println("Slave Constructeur");
		this.name=name;
		nb_task_cur = new AtomicInteger(nb_max);
		String nameMaster = "Master";
		try {
			pool = Executors.newFixedThreadPool(nb_max);
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
	public void executeDist(long idJob, String node , Job job) throws RemoteException{
		System.out.println("Execut dist a était appeler");
		try {
			pool.submit(new JobRunnerSlave(idJob,node,job),600);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void cancelJob(long idJob) throws RemoteException {
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
						args[index]=j.getContext().get(p.getAnnotation(Context.class).value());//a thread safer
					}else if(p.isAnnotationPresent(LinkFrom.class)){
						String func_name = p.getAnnotation(LinkFrom.class).value();
						if(link_from == null || !link_from.containsKey(func_name))
							link_from=master.getResult(id,func_name);
						args[index]=link_from.get(func_name);
					}
					index++;
				}
				master.putResult(id,n,m.invoke(j,args));

			}catch (InterruptedException | IllegalAccessException | InvocationTargetException | RemoteException e) {
				e.printStackTrace();
			}finally {
				nb_task_cur.getAndIncrement();
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

	/**
	 * Class pour pour faciliter la géstion d'esclave
	 * @param <T> l id supposer unique
	 * @param <V> la valeur associer
	 */
	private static class Pair<T, V>{
		private  T id;
		private   V value;
		public Pair(T name, V nb) { this.id = name; this.value = nb;}
	}
}
