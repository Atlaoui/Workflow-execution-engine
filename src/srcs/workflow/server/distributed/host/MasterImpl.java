package srcs.workflow.server.distributed.host;

import srcs.workflow.job.Job;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


public class MasterImpl implements TaskMaster {

    private ConcurrentLinkedQueue<Pair<String,Integer>> slaves = new ConcurrentLinkedQueue<>();
    
    private ConcurrentHashMap<Integer,Map<String, Object>> Retvalues= new ConcurrentHashMap<>();

    private List<Pair<Integer,Job>>  awaitList = new ArrayList<>();
    
    private AtomicInteger id = new AtomicInteger(0);
    private AtomicInteger current=new AtomicInteger(0);
    
    private final Registry registry;
     
    public MasterImpl() throws RemoteException {
    	System.out.println("Master Constructeur");
    	registry = LocateRegistry.getRegistry("localhost");
    	System.out.println("Master Constructeur Fini");
    }
    
    @Override
    public  Integer executeTask(Job job) throws RemoteException {
    		System.out.println("Master : Je demande l'execution d'un job");
		
			int tmp = current.get();
			while(slaves.size() == 0)
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			new Thread(new JobRunner(registry,job,id.get(),Retvalues,slaves.element())).start();
					
			
			current.addAndGet((tmp+1)%slaves.size());
			
			 tmp =id.get();
			id.addAndGet(1);
        return tmp;
    }

    @Override
    public synchronized void  putResult(Integer key, Map<String, Object> value){
        Retvalues.put(key,value);
        System.out.println("Je put un result a la pose "+key);
    }

    @Override
    public  void attach(String SlaveName,Integer nbMax) throws RemoteException{
    	System.out.println("Je m'attache name= "+SlaveName +" mon nb max de tache = "+nbMax);
    	slaves.add(new Pair(SlaveName,nbMax));

    }
	@Override
	public synchronized Boolean isJobReady(Integer id) throws RemoteException {
		return Retvalues.containsKey(id);
	}

	@Override
	public synchronized Map<String, Object> getOneJob(Job job) throws RemoteException {
		TaskHandler t;
		try {
			t = connectToSlave();
		//	t.GetOneJobFromSlave(id,job);
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
			throw new RemoteException("Get one job is not OK");
		}
		return t.GetOneJobFromSlave(id.get(), job);
	} 
	

	private TaskHandler connectToSlave() throws RemoteException, NotBoundException{
		System.out.println("Master : commence connect to Slave");
		//metre on place une stratégie
		Pair<String,Integer> p = null;
		for(Pair<String,Integer> t : slaves)
			p=t;
		
		System.out.println("Master : je me connecte a l'esclave de nom "+p.id);
		
		TaskHandler slave =  (TaskHandler) registry.lookup(p.id);
		return slave;
	}
	
	private class Pair<T, V> implements Serializable{ 
		private static final long serialVersionUID = 1L;
		public  T id;
    	public  V value; 
        public Pair(T name, V nb) { this.id = name; this.value = nb;} 
    }


	@Override
	public Map<String, Object> getJob(Integer id) throws RemoteException {
		System.out.println("Je recuper mon job d'id ="+id);
		return Retvalues.get(id);
	}
	
	private class JobRunner implements Runnable {
		Registry r;
		Job job;
		Integer idJob;
		Pair<String,Integer> slave;
		ConcurrentHashMap<Integer,Map<String, Object>> depo;
		JobRunner(Registry r,Job job,Integer id ,
				ConcurrentHashMap<Integer,Map<String, Object>> depo,
				Pair<String,Integer> slave){
			this.r=r;this.job=job;idJob=id;this.depo=depo;this.slave=slave;
		}
		@Override
		public void run() {
			System.out.println("Le thread a commencer de demander a et met la valeur dans la map est de "+Retvalues.size());
			try {
				TaskHandler t = (TaskHandler) r.lookup(slave.id);	
				//depo.put(idJob,t.GetOneJobFromSlave(idJob,job));
				t.executeDist(job,idJob);
			} catch (RemoteException  e) {
				e.printStackTrace();
				System.err.println("Le thrad a eu des soucis Romote");
			} catch (NotBoundException e) {
				System.err.println("Le thrad a eu des soucis");
				e.printStackTrace();
			}
			System.out.println("Le thread a fini de demander a et met la valeur dans la map est de "+Retvalues.size());
		}
		
	}

}
