package srcs.workflow.server.distributed;

import srcs.workflow.server.distributed.host.TaskMaster;
import srcs.workflow.server.distributed.host.MasterImpl;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class JobTrackerMaster {
    public static void main(String[] args){
        Registry registry ;
        try {
            String name = "Master";
            registry = LocateRegistry.createRegistry(1099);
            TaskMaster master = new MasterImpl();
            UnicastRemoteObject.exportObject(master,0);
            registry.rebind(name,master);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    /* try {
    new  ProcessBuilder("killall", "-q",  "rmiregistry").start();
    Thread.sleep(2000);
} catch (IOException | InterruptedException e) {
    e.printStackTrace();
}*/
    /*   Runtime.getRuntime().addShutdownHook(new Thread(()->{
    try {

        //    Runtime.getRuntime().exec("killall -q rmiregistry");
        System.out.println("Host Destroyed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }));*/

}
