package srcs.workflow.server.distributed;

import srcs.workflow.server.distributed.host.TaskHandler;
import srcs.workflow.server.distributed.host.SlaveImpl;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

public class TaskTracker {
    public static void main(String[] args){
        try {
        	System.out.println("Main créateur d'esclave");
        	System.out.println(Arrays.toString(args));
        	String name = args[0];
        	int nb_max = Integer.parseInt(args[1]);
            Registry registry = LocateRegistry.getRegistry("localhost");
            TaskHandler t= new SlaveImpl(name,nb_max);
            UnicastRemoteObject.exportObject(t,0);
            registry.rebind(name,t);
            System.out.println("Main créateur d'esclave Ok");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
