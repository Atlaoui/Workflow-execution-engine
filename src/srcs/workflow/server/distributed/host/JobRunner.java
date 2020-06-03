package srcs.workflow.server.distributed.host;

import srcs.workflow.job.JobValidator;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

/**
 * CLass qui ce charge de run et dispatche les tache
 */
public class JobRunner implements Runnable {
    private final MasterImpl m;
    private  List<MasterImpl.Pair<String, Integer>> slaves;
    private JobValidator job;
    private long idJob;
    private int indexSlave;
    private Registry reg;
    private List<String> down = new ArrayList<>();
    public JobRunner(JobValidator job, long id_job, int pos_slave , List<MasterImpl.Pair<String,Integer>> slaves ,MasterImpl m) {
        this.m=m;
        this.slaves=slaves;
        try {
            this.reg= LocateRegistry.getRegistry("localhost");
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
        try {
            Queue<String> tasks = new ArrayDeque<>(job.getTaskGraph().getAllNodes());
            TaskHandler t;
            String nodeCur;
            //1er boucle on envois a tlm
            boolean is_error=false;
            while(!tasks.isEmpty()) {
                if(is_error)
                    break;
                nodeCur=tasks.element();
                try {
                    t = connectToSlave();
                    if (t == null)
                        continue;
                    if (t.getNbCurTasks() != 0) {
                        //ont dispatche au slave max task possible sur chaque serveur
                        t.executeDist(idJob, nodeCur, job.getJob());
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                    is_error=true;
                }
                if(!is_error)
                    tasks.remove(nodeCur);
            }
            //verification que toutes les tache sont terminer
            if(is_error && m.getRetvalues().get(idJob).id!=0){
                //list des tache no terminer
                List<String> toCheck = new ArrayList<>(job.getTaskGraph().getAllNodes());
                toCheck.removeAll(m.getRetvalues().get(idJob).value.keySet());

                toCheck.sort((a, b) -> Integer.compare(job.getTaskGraph().getNeighborsIn((String) b).size(), job.getTaskGraph().getNeighborsIn((String) a).size()));

                CanncelAllJobs(idJob);

                while (!toCheck.isEmpty()){
                    System.out.println("Dans check");
                    System.out.println(toCheck);
                    t = connectToSlave();
                    if(t==null)
                        continue;
                    if(t.getNbCurTasks()!=0) {
                        //ont dispatche au slave max task possible sur chaque serveur
                        t.executeDist(idJob, toCheck.remove(0),job.getJob());
                    }
                }
            }
        } catch (RemoteException  e) {
            e.printStackTrace();
        }
        System.out.println("Le thread a fini de demander a et met la valeur dans la map est de taille "+m.getRetvalues().size());
    }

    private void CanncelAllJobs(long idJob) {
        for(MasterImpl.Pair<String,Integer> p : slaves){
            TaskHandler slave = null;
            if(!down.contains(p.id))
                try {
                    slave = (TaskHandler) reg.lookup(p.id);
                    slave.cancelJob(idJob);
                } catch (RemoteException | NotBoundException e) {
                    e.printStackTrace();
                }
        }
    }

    private TaskHandler connectToSlave()  {
        System.out.println("Petit Thread : commence connect to Slave");
        MasterImpl.Pair<String,Integer> p;
        boolean isfound=false;
        TaskHandler slave = null;
        while(!isfound){
            do //on tourne tant que le node n'est pas marquer comme en panne
                p = slaves.get((indexSlave++) % slaves.size());
            while(down.contains(p.id));
            try {
                slave = (TaskHandler) reg.lookup(p.id);
                if(slave.getNbCurTasks()!=0)
                    isfound=true;
            }catch(NotBoundException e){
                e.printStackTrace();// a cause du décalage au début
            }catch (RemoteException e){
                down.add(p.id);
            }
        }
        return slave;
    }

}