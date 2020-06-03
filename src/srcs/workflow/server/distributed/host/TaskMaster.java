package srcs.workflow.server.distributed.host;

import srcs.workflow.job.JobValidator;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface TaskMaster extends Remote{
    /**
     *
     * @param job
     * Job valider
     * @return
     * id de la tache demander
     * @throws RemoteException
     *
     */
    long executeTask(JobValidator job)throws RemoteException;

    /**
     * Permet au client de connaitre l'état d'avancement de sont job
     * @param id
     * @return
     * true pour pret
     * @throws RemoteException
     */
    boolean isJobReady(long id)throws RemoteException;

    /**
     *
     * @param key
     * id du job en cour et position dans la map de retour
     * @param name
     * nom de la tache réussi
     * @param value
     * valeur de retour de cette tache
     * @throws RemoteException
     */
    void putResult(long key, String name, Object value) throws RemoteException;

    /**
     * permet au esclave de récuper leur resultat
     * @param key
     * id du job
     * @param nom
     * non de la valeur de retour de la tache que l'ont veux recuperer
     * @return
     * la totaliter de la map pour eviter les demande trop fréquente
     * @throws RemoteException
     * @throws InterruptedException
     */
    Map<String, Object> getResult(long key, String nom) throws RemoteException, InterruptedException;

    /**
     * permet a l'esclave de s(attacher au maitre
     * @param SlaveName
     * le nom de l'esclave
     * @param nbMax
     * le nombre max de tache possible sur cette VM
     * @throws RemoteException
     */
    void attach(String SlaveName,Integer nbMax) throws RemoteException;

    /**
     * permet au client de récuperer son job
     * @param id_job
     * id du job
     * @return
     * @throws RemoteException
     */
    Map<String, Object> getJob(long id_job) throws RemoteException;
}
