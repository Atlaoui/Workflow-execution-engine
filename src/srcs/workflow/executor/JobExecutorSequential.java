package srcs.workflow.executor;

import srcs.workflow.job.Context;
import srcs.workflow.job.Job;
import srcs.workflow.job.LinkFrom;
import srcs.workflow.job.Task;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
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
        for(String funcName : jobV.getTaskGraph()) {

            for (Method m : jobV.getJob().getClass().getMethods()) {
                if(!m.isAnnotationPresent(Task.class) )
                    continue;
                //System.out.println(funcName+" Nom "+hasOnlyContexteParam(m.getParameters()));
                //  if(m.isAnnotationPresent(Task.class)){ System.out.println("Param "+m.getParameterCount());
                //    System.out.println(m.getAnnotation(Task.class).value()); }
                if (m.getParameterCount() == 0 && m.getAnnotation(Task.class).value().equals(funcName) && hasOnlyContexteParam(m.getParameters()) && jobV.getTaskGraph().getNeighborsIn(m.getAnnotation(Task.class).value()).isEmpty()) {
                    retValues.put(m.getAnnotation(Task.class).value(), m.invoke(jobV.getJob()));
                } else if (m.getAnnotation(Task.class).value().equals(funcName) && hasOnlyContexteParam(m.getParameters()) && jobV.getTaskGraph().getNeighborsIn(m.getAnnotation(Task.class).value()).isEmpty()) {

                    index = 0;
                    args = new Object[m.getParameterCount()];
                    for (Parameter p : m.getParameters()) {
                        //  Sy  System.out.println(jobV.getTaskGraph().getNeighborsIn(m.getAnnotation(Task.class).value()).isEmpty());stem.out.println("Cont = "+p.getAnnotation(Context.class).value());
                        args[index] = jobV.getJob().getContext().get(p.getAnnotation(Context.class).value());
                        //System.out.println("argument cont = "+args[index]);
                        index++;
                    }
                    retValues.put(funcName, m.invoke(jobV.getJob(), args));
                }
            }
        }

        //System.out.println("Depuis le graph "+ retValues);
        //System.out.println("Context = "+jobV.getJob().getContext());

        for(String funcName : jobV.getTaskGraph()) {
            if(retValues.containsKey(funcName))
                continue;
            for (Method m : jobV.getJob().getClass().getMethods()) {
                if(!m.isAnnotationPresent(Task.class) || !m.getAnnotation(Task.class).value().equals(funcName))
                    continue;

                param = m.getParameters();
                args = new Object[m.getParameterCount()];
                index = 0;
                for(Parameter p : param) {
                    if (p.isAnnotationPresent(Context.class)) {
                        //System.out.println("Cont = "+p.getAnnotation(Context.class).value());
                        args[index] = jobV.getJob().getContext().get(p.getAnnotation(Context.class).value());
                      //  System.out.println("argument cont = "+args[index]);
                    } else {
                      //  System.out.println("Link = "+p.getAnnotation(LinkFrom.class).value());
                        args[index] = retValues.get(p.getAnnotation(LinkFrom.class).value());
                    //    System.out.println("argument from = "+args[index]);
                    }
                    index++;
                }
                retValues.put(funcName,m.invoke(jobV.getJob(),args));
            }
        }
            return retValues;
    }

    private boolean hasOnlyContexteParam(Parameter[] parameters) {
        for(Parameter p : parameters) {
            if (p.isAnnotationPresent(LinkFrom.class)){
             //   System.out.println("has link = "+p.getAnnotation(LinkFrom.class).value());
                return false;
            }

         //   else
          //      System.out.println("has only = "+p.getAnnotation(Context.class).value());
        }
        return true;
    }
}
