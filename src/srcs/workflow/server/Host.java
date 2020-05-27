package srcs.workflow.server;


import srcs.workflow.job.Job;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Host extends Remote {

    Map<String, Object> executeDist(Job job) throws RemoteException;

}
