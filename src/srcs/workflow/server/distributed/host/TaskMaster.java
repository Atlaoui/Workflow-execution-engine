package srcs.workflow.server.distributed.host;

import srcs.workflow.job.Job;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TaskMaster extends Remote {
    Integer executeTask(Job job)throws RemoteException;
}
