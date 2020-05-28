package srcs.workflow.server.distributed.host;

import srcs.workflow.job.Job;

import java.rmi.RemoteException;

public class TaskMasterImplem implements TaskMaster {
    @Override
    public Integer executeTask(Job job) throws RemoteException {
        return null;
    }
}
