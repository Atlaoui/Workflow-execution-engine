package srcs.workflow.executor;

import srcs.workflow.job.Context;
import srcs.workflow.job.Job;
import srcs.workflow.job.LinkFrom;
import srcs.workflow.job.Task;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobExecutorSequential extends JobExecutor{
    public JobExecutorSequential(Job job) {
        super(job);
    }

    @Override
    public Map<String, Object> execute() throws Exception {
        Map<String, Object> retValues = new HashMap<>();
        //ont isole deja les fonction sans param racine
        Object[] args;
        Parameter[] param;
        int index ;
        List<Method> Mlist = new ArrayList<>();

        //ont cherche et invoque les tache racine
        for(String funcName : jobV.getTaskGraph()) {
            for (Method m : jobV.getJob().getClass().getMethods()) {
                if(!m.isAnnotationPresent(Task.class))
                    continue;
                if(!m.getAnnotation(Task.class).value().equals(funcName))
                    continue;
                if(!jobV.getTaskGraph().getNeighborsIn(funcName).isEmpty()){
                    Mlist.add(m);
                    continue;
                }
                if (m.getParameterCount() == 0 )
                    retValues.put(funcName, m.invoke(jobV.getJob()));
                else{
                    index = 0;
                    args = new Object[m.getParameterCount()];
                    for (Parameter p : m.getParameters()) {
                        args[index] = jobV.getJob().getContext().get(p.getAnnotation(Context.class).value());
                        index++;
                    }
                    retValues.put(funcName, m.invoke(jobV.getJob(), args));
                }
            }
        }

    //ont prend le reste dé méthode et on les invoque
        for (Method m : Mlist) {
            param = m.getParameters();
            args = new Object[m.getParameterCount()];
            index = 0;
            for(Parameter p : param) {
                if (p.isAnnotationPresent(Context.class))
                    args[index] = jobV.getJob().getContext().get(p.getAnnotation(Context.class).value());
                else
                    args[index] = retValues.get(p.getAnnotation(LinkFrom.class).value());
                index++;
            }
            retValues.put(m.getAnnotation(Task.class).value(),m.invoke(jobV.getJob(),args));
        }

        return retValues;
    }

}
