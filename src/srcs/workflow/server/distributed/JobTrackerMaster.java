package srcs.workflow.server.distributed;

import srcs.workflow.job.Job;
import srcs.workflow.server.central.host.Host;
import srcs.workflow.server.central.host.HostImpl;
import srcs.workflow.server.distributed.host.TaskMaster;
import srcs.workflow.server.distributed.host.TaskMasterImplem;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class JobTrackerMaster {
    public static void main(String[] args){
        Registry registry ;
        try {

            //	new  ProcessBuilder("killall", "-q",  "rmiregistry").start();
            //	Thread.sleep(2000);

            String name = "TrackerMaster";
            registry = LocateRegistry.createRegistry(1099);
            TaskMaster master = new TaskMasterImplem();
            UnicastRemoteObject.exportObject(master,0);
            registry.rebind(name,master);

         /*   Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try {

                //    Runtime.getRuntime().exec("killall -q rmiregistry");
                System.out.println("Host Destroyed");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));*/
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


}
