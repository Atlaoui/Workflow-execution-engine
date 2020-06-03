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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class MasterImpl implements TaskMaster {

	/***************************Pannes************************************/
	private Thread demon = new Thread( new Demon());
	private List<Pair<Long, JobValidator>> notFinished = new ArrayList<>();

	/*********************************************************************/

	/*********************Con avec les esclave ***************************/
	private CopyOnWriteArrayList<Pair<String,Integer>> slaves = new CopyOnWriteArrayList<>();

	private ConcurrentHashMap<Long,Pair<Integer,Map<String, Object>>> Retvalues= new ConcurrentHashMap<>();

	/***********************Gestion des acces au map **********************************/

	private ReentrantLock lock = new ReentrantLock();
	private Condition condition = lock.newCondition();

	private ExecutorService pool = Executors.newCachedThreadPool();

    /*********************** Vars *******************************/
	/** Id du job permet ainsi de les stocker dans la map*/
	private AtomicLong ID_JOB_CUR = new AtomicLong(0L);//id du job courant
	/**
	 * utiliser afin de faire tourner la position du 1er esclave choisie ainsi ont s'assure
	 * d'une répartition équitable des tache
	 */
	private AtomicInteger POS_CUR =new AtomicInteger(0);

	/************************************************************/
	public MasterImpl(){
	//	demon.setDaemon(true);
	//	demon.start();
	}
    @Override
    public long executeTask(JobValidator job) throws RemoteException {
    		// attendre le temps que les slaves s'atache
			while(slaves.size() == 0) try { Thread.sleep(500); }
			catch (InterruptedException e) { e.printStackTrace(); }

			long id = ID_JOB_CUR.getAndIncrement();
			Retvalues.put(id,new Pair<>(job.getTaskGraph().size(),new HashMap<>()));
			notFinished.add(new Pair<Long,JobValidator>(id,job));
			int pos = POS_CUR.get();
			POS_CUR.set((pos+1)%slaves.size());
			pool.execute(new JobRunner(job,id,pos,slaves,this));

        return id;
    }


	@Override
    public  void  putResult(long key, String name , Object value){
		System.out.println("Je put result");
		try {
			lock.lock();
			Retvalues.get(key).id--;
			Retvalues.get(key).value.put(name,value);
			condition.signalAll();
		}finally {
			if(Retvalues.get(key).id==0)
				notFinished.removeIf(p -> p.id == key);
			lock.unlock();
		}
		System.out.println("fin Je put result");
    }

	@Override
	public Map<String, Object> getResult(long key ,String nom) throws RemoteException, InterruptedException {
		try {
			System.out.println("Je get result");
			lock.lock();
			while (!Retvalues.get(key).value.containsKey(nom))
				condition.await();
			return Retvalues.get(key).value;
		} finally {
			lock.unlock();
			System.out.println("Fin de get result");
		}
	}

	/**
	 * permet a un esclave de s'attacher au master
	 * @param SlaveName
	 * nom de l'esclave
	 * @param nbMax
	 * nombre max de tache de l'esclave
	 * @throws RemoteException
	 */
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
	ConcurrentHashMap<Long, Pair<Integer, Map<String, Object>>> getRetvalues() {
		return Retvalues;
	}

	/**
	 * Class pour pour faciliter la géstion d'esclave
	 * @param <T> l id supposer unique
	 * @param <V> la valeur associer
	 */
	 static class Pair<T, V> implements Serializable{
		private static final long serialVersionUID = 1L;
		public  T id;
		public  V value;
		public Pair(T name, V nb) { this.id = name; this.value = nb;}
	}

	/**
	 * Thread démon qui va ce charger de checker si un job a était fait ou pas
	 * sinon il le relance
	 */
	private class Demon implements Runnable {
		List<Long> list_ids = new LinkedList<>();

		@Override
		public void run() {
			try {
				List<Long> ids = new ArrayList<>();
				while(!Thread.currentThread().isInterrupted()){
					Thread.sleep(10000);
					for(Pair<Long,JobValidator> p : notFinished)
						if(list_ids.contains(p.id)){
							if(Retvalues.get(p.id).id!=0)
								pool.execute(new JobRunner(p.value, p.id, 0, slaves, MasterImpl.this,Retvalues.get(p.id).value.keySet()));
							ids.add(p.id);
							condition.signalAll();
							Thread.sleep(2000);
						}else {
							list_ids.add(p.id);
						}
					notFinished.removeIf(p -> ids.contains(p.id));
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
