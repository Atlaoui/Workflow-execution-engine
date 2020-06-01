package srcs.workflow.server.distributed.host;

import srcs.workflow.job.Job;
import srcs.workflow.job.JobValidator;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class MasterImpl implements TaskMaster {

	/*********************** Pannes *****************************/
	/************************************************************/

	/*********************Concurrence ***************************/
	private CopyOnWriteArrayList<Pair<String,Integer>> slaves = new CopyOnWriteArrayList<>();
    
    private ConcurrentHashMap<Long,Pair<Integer,Map<String, Object>>> Retvalues= new ConcurrentHashMap<>();

	/************************************************************/

	private ReentrantLock lock = new ReentrantLock();
	private Condition condition = lock.newCondition();

    /*********************** Vars *******************************/
    private AtomicLong ID_JOB_CUR = new AtomicLong(0L);//id du job courant
	/**
	 * utiliser afin de faire tourner la position du 1er esclave choisie ainsi ont s'assure
	 * d'une répartition équitable des tache
	 */
	private AtomicInteger POS_CUR =new AtomicInteger(0);

	/************************************************************/
	public MasterImpl() throws RemoteException {

    }
    
    @Override
    public long executeTask(JobValidator job) throws RemoteException {
    		System.out.println("Master : Je demande l'execution d'un job");
    		// attendre le temps que les slaves s'atache
			while(slaves.size() == 0) try { Thread.sleep(500); }
			catch (InterruptedException e) { e.printStackTrace(); }

			long id = ID_JOB_CUR.getAndIncrement();
			Retvalues.put(id,new Pair<>(job.getTaskGraph().size(),new HashMap<>()));
			int pos = POS_CUR.get();
			POS_CUR.set((pos+1)%slaves.size());

			new Thread(new JobRunner(job,id,pos)).start();

        return id;
    }


	@Override
    public  void  putResult(long key, String name , Object value){
		System.out.println("Je put result");
		try {
			lock.lock();
			Retvalues.get(key).id--;
			Retvalues.get(key).value.put(name,value);
		///	condition.signalAll();
		}finally {
			lock.unlock();
		}
		System.out.println("fin Je put result");
    }

	@Override
	public Map<String, Object> getResult(long key ,String nom) throws RemoteException {
		try {
			System.out.println("Je get result");
			lock.lock();
			//while (!Retvalues.get(key).value.containsKey(nom))
			//	condition.await();
			return Retvalues.get(key).value;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
			System.out.println("Fin de get result");
		}
		throw new RemoteException("Get result sur master depuis le slave ");
	}

	@Override
	public void attach(String SlaveName,Integer nbMax) throws RemoteException{
    	System.out.println("Je m'attache name= "+SlaveName +" mon nb max de tache = "+nbMax);
    	slaves.add(new Pair<>(SlaveName, nbMax));
    }


	@Override
	public boolean isJobReady(long id) throws RemoteException {
		return (Retvalues.get(id).id==0);
	}

	@Override
	public  Map<String, Object> getJob(long id) throws RemoteException {
		return Retvalues.get(id).value;
	}

	/**
	 * ce charge de run un job pour le Master
	 */
	private class JobRunner implements Runnable {
		JobValidator job;
		long idJob;
		int indexSlave;
		Registry reg;
		public JobRunner( JobValidator job, long id_job, int pos_slave) {
			try {
				this.reg=LocateRegistry.getRegistry("localhost");
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			;
			this.job=job;
			idJob=id_job;
			indexSlave=pos_slave;
		}

		@Override
		public void run() {
			System.out.println("Le thread a commencer de demander a et met la valeur dans la map est de "+Retvalues.size());
			try {
				Queue<String> tasks = new ArrayDeque<>(job.getTaskGraph().getAllNodes());
				int nb_task =0;
				TaskHandler t;
				int i = 0;
				List<String> args;
				while(tasks.isEmpty() ){
					t = connectToSlave();
					System.out.println(i +"  "+tasks.size());
					if(t==null)
						continue;
					if((nb_task=t.getNbCurTasks())!=0) {
						args = new ArrayList<>();
						//ont dispatche au slave max task possible sur chaque serveur
						for(int j=0;i<tasks.size() && j<nb_task ; j++) {
							args.add(tasks.remove());
						}
						t.executeDist(idJob, args,job.getJob());
					}
				}

			} catch (RemoteException  e) {
				e.printStackTrace();
			}
			System.out.println("Le thread a fini de demander a et met la valeur dans la map est de "+Retvalues.size());
		}


		private TaskHandler connectToSlave() throws RemoteException{
			System.out.println("Petit Thread : commence connect to Slave");
			Pair<String,Integer> p = null;
			boolean isfound=false;
			TaskHandler slave = null;
			while(!isfound){
				Pair<String,Integer> s = slaves.get((indexSlave+1)%slaves.size());
				try {
					slave = (TaskHandler) reg.lookup(s.id);
				}catch(NotBoundException e){
					e.printStackTrace();
					System.out.println("Je suis la ");
					continue;
				}
				if(slave.getNbCurTasks()!=0)
					isfound=true;
			}
			return slave;
		}

	}

	/**
	 * Class pour pour faciliter la géstion d'esclave
	 * @param <T> l id supposer unique
	 * @param <V> la valeur associer
	 */
	private static class Pair<T, V> implements Serializable{
		private static final long serialVersionUID = 1L;
		public  T id;
		public  V value;
		public Pair(T name, V nb) { this.id = name; this.value = nb;}
	}

}
