package srcs.workflow.server.distributed.host;

import srcs.workflow.job.Job;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface TaskHandler extends Remote {

    int getNb_max()throws RemoteException;

    String getName()throws RemoteException;

    void executeDist(Job job ,Integer id) throws RemoteException;

    Integer getNbTask()throws RemoteException;

    void attachMaster(TaskMasterImplem t) throws RemoteException;
}
