package srcs.workflow.server.distributed.host;

import srcs.workflow.job.Job;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface TaskMaster extends Remote {
    Integer executeTask(Job job)throws RemoteException;

    Map<String, Object> getMyResult(int id) throws RemoteException;

    void putResult(Integer key, Map<String, Object> value) throws RemoteException;

    void attach(TaskHandler t) throws RemoteException;
}
