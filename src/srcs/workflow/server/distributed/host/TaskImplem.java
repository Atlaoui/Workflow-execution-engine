package srcs.workflow.server.distributed.host;

import srcs.workflow.job.Job;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskImplem implements Task {

    private final int nb_max;
    private final String name;
    private ExecutorService pool;
    public TaskImplem (String name,Integer nb_max){
        this.name=name;
        this.nb_max=nb_max;
        pool = Executors.newFixedThreadPool(nb_max);
    }

    @Override
    public Map<String, Object> executeDist(Job job) throws RemoteException {
        return null;
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

}
