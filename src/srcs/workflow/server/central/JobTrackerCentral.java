package srcs.workflow.server.central;

import srcs.workflow.server.central.host.Host;
import srcs.workflow.server.central.host.HostImpl;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class JobTrackerCentral {
    public static void main(String[] args){
        Registry registry ;
        try {
            String name = "JobRemote";
            registry = LocateRegistry.createRegistry(1099);
            Host h = new HostImpl(name);
            UnicastRemoteObject.exportObject(h,0);
            registry.rebind(name,h);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
     // 	 System.out.println(Arrays.toString(args));
//	new  ProcessBuilder("killall", "-q",  "rmiregistry").start();
//	Thread.sleep(2000);

        /*   Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try {

                //    Runtime.getRuntime().exec("killall -q rmiregistry");
                System.out.println("Host Destroyed");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                }));*/