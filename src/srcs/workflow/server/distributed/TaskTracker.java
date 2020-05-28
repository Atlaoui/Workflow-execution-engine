package srcs.workflow.server.distributed;

import srcs.workflow.server.central.host.Host;
import srcs.workflow.server.central.host.HostImpl;
import srcs.workflow.server.distributed.host.TaskHandler;
import srcs.workflow.server.distributed.host.TaskImplem;
import srcs.workflow.server.distributed.host.TaskMaster;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

public class TaskTracker {
    public static void main(String[] args){
        try {

            //	new  ProcessBuilder("killall", "-q",  "rmiregistry").start();
            //	Thread.sleep(2000);
            Random r = new Random();
            Integer i = r.nextInt();
            String name = "Task"+i;
            Registry registry = LocateRegistry.getRegistry("localhost");
            TaskHandler t= new TaskImplem(name,i);
            UnicastRemoteObject.exportObject(t,0);
            registry.rebind(name,t);
            TaskMaster master = (TaskMaster) registry.lookup("TrackerMaster");
            master.attach(t);

         /*   Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try {

                //    Runtime.getRuntime().exec("killall -q rmiregistry");
                System.out.println("Host Destroyed");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));*/
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

}
