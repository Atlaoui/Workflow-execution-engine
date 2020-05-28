package srcs.workflow.server.distributed.host;

import srcs.workflow.job.Job;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Task extends Remote {

    int getNb_max()throws RemoteException;

    String getName()throws RemoteException;

    Map<String, Object> executeDist(Job job) throws RemoteException;

    Integer getNbTask()throws RemoteException;
}
