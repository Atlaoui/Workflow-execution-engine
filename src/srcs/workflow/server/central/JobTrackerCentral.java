package srcs.workflow.server.central;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class JobTrackerCentral {
    public static void main(String[] args){
        Registry registry ;
        try {
            registry = LocateRegistry.createRegistry(1099);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
