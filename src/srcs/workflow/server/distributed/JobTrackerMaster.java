package srcs.workflow.server.distributed;

import srcs.workflow.server.distributed.host.TaskMaster;
import srcs.workflow.server.distributed.host.MasterImpl;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class JobTrackerMaster {
    public static void main(String[] args){
        Registry registry ;
        try {
        	
        	System.out.println("Main Du Master");
            String name = "Master";
            registry = LocateRegistry.createRegistry(1099);
            TaskMaster master = new MasterImpl();
            UnicastRemoteObject.exportObject(master,0);
            registry.rebind(name,master);
            System.out.println("Main Du Master Ok");
            

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}


//
/*  Thread killALL = new Thread(() -> {
	System.out.println("Normalement je tue tlm");
	try {
		registry.unbind(name);
	new  ProcessBuilder("killall", "-q",  "rmiregistry").start();
		Thread.sleep(500);
	} catch (NotBoundException | InterruptedException | IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
});*/
 
//  Runtime.getRuntime().addShutdownHook(killALL);
