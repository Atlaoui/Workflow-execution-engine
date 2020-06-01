package srcs.workflow.server.distributed.host;

import srcs.workflow.job.Job;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;


public interface TaskHandler extends Remote {

    int getNbCurTasks()throws RemoteException;

    String getName()throws RemoteException;

    void executeDist(long idJob, List<String> nodes ,Job job) throws RemoteException;
}
