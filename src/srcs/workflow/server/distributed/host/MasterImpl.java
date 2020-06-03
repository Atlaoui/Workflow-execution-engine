package srcs.workflow.server.distributed.host;

import srcs.workflow.job.JobValidator;
import srcs.workflow.server.Tuple;

import java.rmi.RemoteException;
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
	private List<Tuple<Long, JobValidator>> notFinished = new ArrayList<>();
	/*********************************************************************/

	/*********************Con avec les esclave ***************************/
	private CopyOnWriteArrayList<Tuple<String,Integer>> slaves = new CopyOnWriteArrayList<>();

	private ConcurrentHashMap<Long,Tuple<Integer,Map<String, Object>>> Retvalues= new ConcurrentHashMap<>();

	/***********************Gestion des acces au map **********************************/

	private ReentrantLock lock = new ReentrantLock(true);
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
		demon.setDaemon(true);
		demon.start();
	}
    @Override
    public long executeTask(JobValidator job) throws RemoteException {
    		// attendre le temps que les slaves s'atache
			while(slaves.size() == 0) try { Thread.sleep(500); }
			catch (InterruptedException e) { e.printStackTrace(); }

			long id = ID_JOB_CUR.getAndIncrement();
			Retvalues.put(id,new Tuple<>(job.getTaskGraph().size(),new HashMap<>()));
			notFinished.add(new Tuple<>(id,job));
			int pos = POS_CUR.get();
			POS_CUR.set((pos+1)%slaves.size());
			pool.execute(new JobRunner(job,id,pos,slaves,this));
        return id;
    }


	@Override
    public  void  putResult(long key, String name , Object value){
		try {
			lock.lock();
			if(!Retvalues.get(key).getValue().containsKey(name)) {
				Retvalues.get(key).setName(Retvalues.get(key).getName() - 1);
				Retvalues.get(key).getValue().put(name, value);
			}
			condition.signalAll();
		}finally {
			if(Retvalues.get(key).getName() ==0)
				notFinished.removeIf(p -> p.getName() == key);
			lock.unlock();
		}
	}

	@Override
	public Map<String, Object> getResult(long key ,String nom) throws RemoteException, InterruptedException {
		try {
			lock.lock();
			while (!Retvalues.get(key).getValue().containsKey(nom))
				condition.await();
			return Retvalues.get(key).getValue();
		} finally {
			lock.unlock();
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
    	slaves.add(new Tuple<>(SlaveName, nbMax));
    }


	@Override
	public boolean isJobReady(long id) throws RemoteException {
		return (Retvalues.get(id).getName() ==0);
	}

	@Override
	public  Map<String, Object> getJob(long id) throws RemoteException {
		return Retvalues.get(id).getValue();
	}


	/**
	 * ce charge de run un job pour le Master
	 */
	ConcurrentHashMap<Long, Tuple<Integer, Map<String, Object>>> getRetvalues() {
		return Retvalues;
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
				boolean clean = false;
				while(!Thread.currentThread().isInterrupted()){
					Thread.sleep(10000);
					clean = false;
					for(Tuple<Long,JobValidator> p : notFinished)
						if(list_ids.contains(p.getName())){
							if(Retvalues.get(p.getName()).getName() !=0)
								pool.execute(new JobRunner(p.getValue(), p.getName(), 0, slaves, MasterImpl.this, Retvalues.get(p.getName()).getValue().keySet()));
							ids.add(p.getName());
							condition.signalAll();
							clean = true;
							Thread.sleep(1000);
						}else {
							list_ids.add(p.getName());
						}
					if(clean)
						notFinished.removeIf(p -> ids.contains(p.getName()));
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
