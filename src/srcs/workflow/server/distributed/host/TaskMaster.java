package srcs.workflow.server.distributed.host;

import srcs.workflow.job.Job;
import srcs.workflow.job.JobValidator;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface TaskMaster extends Remote{
	// prend le job renvois un id 
    long executeTask(JobValidator job)throws RemoteException;
    // pour que le clien check si son job est Ok
    boolean isJobReady(long id)throws RemoteException;
    //pour que l'esclave puisse metre le resultat d'un job
    void putResult(long key, String name, Object value) throws RemoteException;

    //pour que les slave puisse recupérer leur resultat
    Map<String, Object> getResult(long key, String nom) throws RemoteException;

    //fonction d'attache d'esclave vers le maitre
    void attach(String SlaveName,Integer nbMax) throws RemoteException;
    
    //get Job par le client pour recupérer ça task
    Map<String, Object> getJob(long id_job) throws RemoteException;
}
