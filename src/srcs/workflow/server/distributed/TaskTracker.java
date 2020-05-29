package srcs.workflow.server.distributed;

import srcs.workflow.server.distributed.host.TaskHandler;
import srcs.workflow.server.distributed.host.SlaveImpl;
import srcs.workflow.server.distributed.host.TaskMaster;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Random;

public class TaskTracker {
    public static void main(String[] args){
        try {
        	System.out.println("Maine créateur d'esclave");
        	System.out.println(Arrays.toString(args));
        	String name = args[1];
        	int nb_max = Integer.parseInt(args[2]);
            Registry registry = LocateRegistry.getRegistry("localhost");
            TaskHandler t= new SlaveImpl(name,nb_max);
            UnicastRemoteObject.exportObject(t,0);
            registry.rebind(name,t);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
