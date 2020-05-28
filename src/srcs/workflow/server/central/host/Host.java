package srcs.workflow.server.central.host;


import srcs.workflow.job.Job;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Host extends Remote {

    Map<String, Object> executeDist(Job job) throws RemoteException;
    
    Integer getNbTask()throws RemoteException;

}
