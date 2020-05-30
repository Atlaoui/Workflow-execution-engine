package srcs.workflow.server.distributed.host;

import srcs.workflow.job.Job;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface TaskMaster extends Remote{
	// prend le job renvois un id 
    Integer executeTask(Job job)throws RemoteException;
    // pour que le clien check si son job est Ok
    Boolean isJobReady(Integer id)throws RemoteException;
    //pour que l'esclave puisse metre le resultat d'un job
    void putResult(Integer key, Map<String, Object> value) throws RemoteException;
    
    //fonction d'attache d'esclave vers le maitre
    void attach(String SlaveName,Integer nbMax) throws RemoteException;
    
    //getOneJobDonne teste pour 1 job 
    Map<String, Object> getOneJob(Job job) throws RemoteException;
    
    Map<String, Object> getJob(Integer id) throws RemoteException;
}
