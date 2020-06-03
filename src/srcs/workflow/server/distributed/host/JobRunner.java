package srcs.workflow.server.distributed.host;

import srcs.workflow.job.JobValidator;
import srcs.workflow.server.Tuple;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

/**
 * CLass qui ce charge de dispatcher les tache
 */
public class JobRunner implements Runnable {
    private final MasterImpl m;
    private  List<Tuple<String, Integer>> slaves;
    private JobValidator job;
    private long idJob;
    private int indexSlave;
    private Registry reg;
    private List<String> down = new ArrayList<>();
    private Queue<String> tasks;
    public JobRunner(JobValidator job, long id_job, int pos_slave , List<Tuple<String,Integer>> slaves ,MasterImpl m) {
        this.m=m;
        this.slaves=slaves;
        try {
            this.reg= LocateRegistry.getRegistry("localhost");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        this.job=job;
        idJob=id_job;
        indexSlave=pos_slave;
        tasks = new ArrayDeque<>(job.getTaskGraph().getAllNodes());

    }

    public JobRunner(JobValidator job, long id_job, int pos_slave , List<Tuple<String,Integer>> slaves , MasterImpl m , Set<String> set) {
        this.m=m;
        this.slaves=slaves;
        try {
            this.reg= LocateRegistry.getRegistry("localhost");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        this.job=job;
        idJob=id_job;
        indexSlave=pos_slave;
        this.tasks = new ArrayDeque<>(job.getTaskGraph().getAllNodes());
        tasks.removeIf(set::contains);
    }

    @Override
    public void run() {
        try {
            TaskHandler t;
            String nodeCur = null;
            boolean is_error=false;
            // cette boucle n'est pas nécessaire pour les teste sans panne  mais la récupération
            //est plus éfficace comme ça
            List<String> tacheRacine = new ArrayList<>(tasks);
            for(String node : tacheRacine)
                if(job.getTaskGraph().getNeighborsIn(node).size()==0) {
                    t = connectToSlave();
                    t.executeDist(idJob, node, job.getJob());
                    tasks.remove(node);
                }


            while(!tasks.isEmpty()) {
                if(is_error)
                    break;
                try {
                    t = connectToSlave();
                    nodeCur=tasks.element();

                       t.executeDist(idJob, nodeCur, job.getJob());
                    //ont dispatche au slave max task possible sur chaque serveur

                } catch (RemoteException e) {
                    e.printStackTrace();
                    is_error=true;
                }
                if(!is_error)
                    tasks.remove(nodeCur);
            }
            //verification que toutes les tache sont terminer
            if(is_error && m.getRetvalues().get(idJob).getName() !=0){
                //list des tache no terminer
                List<String> toCheck = new ArrayList<>(job.getTaskGraph().getAllNodes());
                toCheck.removeAll(m.getRetvalues().get(idJob).getValue().keySet());

                toCheck.sort((a, b) -> Integer.compare(job.getTaskGraph().getNeighborsIn(b).size(), job.getTaskGraph().getNeighborsIn(a).size()));
                CanncelAllJobs(idJob);
                
                while (!toCheck.isEmpty()){
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
            CanncelAllJobs(idJob);
        }
    }

    /**
     * permet d'intrupt tout les tache  demander et qui sont lier a l'id de ce job
     * @param idJob
     * id du job en cours
     */
    private void CanncelAllJobs(long idJob) {
        for(Tuple<String,Integer> p : slaves){
            TaskHandler slave = null;
            if(!down.contains(p.getName()))
                try {
                    slave = (TaskHandler) reg.lookup(p.getName());
                    slave.cancelJob(idJob);
                } catch (RemoteException | NotBoundException e) {
                    e.printStackTrace();
                }
        }
    }

    /**
     * elle permet la connextion a serveur quoi qu'il arrive et isole les serveur mort
     * et verifie que la jvm distante puisse recevoir des job
     * @return
     * le serveur sélectionner
     */
    private TaskHandler connectToSlave()  {
       Tuple<String,Integer> p;
        boolean isfound=false;
        TaskHandler slave = null;
        while(!isfound){
            do //on tourne tant que le node n'est pas marquer comme en panne
                p = slaves.get((indexSlave++) % slaves.size());
            while(down.contains(p.getName()));
            try {
                slave = (TaskHandler) reg.lookup(p.getName());
                if(slave.getNbCurTasks()!=0)
                    isfound=true;
            }catch(NotBoundException e){
                e.printStackTrace();// a cause du décalage au début
            }catch (RemoteException e){
                down.add(p.getName());
            }
        }
        return slave;
    }

}