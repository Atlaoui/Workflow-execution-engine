package srcs.workflow.server.central.host;


import srcs.workflow.job.Job;
import srcs.workflow.job.JobValidator;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Host extends Remote {

	long executeDist(JobValidator job) throws RemoteException;

    /**
     * renvois le nombre de tache terminer
     * @param id
     * @return
     * @throws RemoteException
     */
    Integer is_finished(long id) throws RemoteException;


    Map<String, Object> getMyResult(long id_job) throws RemoteException;



}
