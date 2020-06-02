package srcs.workflow.server.distributed.host;

import srcs.workflow.job.Job;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface TaskHandler extends Remote {

    int getNbCurTasks()throws RemoteException;

    String getName()throws RemoteException;

    void executeDist(long idJob, String node ,Job job) throws RemoteException;

    void cancelJob(long idJob) throws RemoteException;
}
