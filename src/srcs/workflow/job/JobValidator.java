package srcs.workflow.job;

import srcs.workflow.graph.Graph;
import srcs.workflow.graph.GraphImpl;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class JobValidator implements Serializable {
    private Job job ;
    private Graph<String> taskGraph;
    public JobValidator(Job job) throws ValidationException{

        taskGraph = new GraphImpl<>();
        Constraints(job);

        this.job=job;

    }

    /**
     * ce charge des contrainte sur Task.class
     * @param map
     * @param job
     * @return
     * @throws ValidationException
     */
    private boolean TasksConstraint(Map<String,Class<?>> map,Job job) throws ValidationException{
        boolean is_one = false;
        String value;
        for (Method m : job.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(Task.class)) {
                is_one = true;
                value = m.getAnnotation(Task.class).value();
                if (map.containsKey(value))
                    return false;
                else
                    map.put(value, m.getReturnType());
                if (m.getReturnType() == Void.TYPE)
                    return false;
                //si la methode est statik
                if (Modifier.isStatic(m.getModifiers()))
                    return false;
                try{
                    taskGraph.addNode(value);
                }catch(IllegalArgumentException e){
                    throw new ValidationException();
                }
            }
        }
        return is_one;
    }

    /**
     * assure le reste contrainte
     * @param job
     * @throws ValidationException
     */
    private void Constraints(Job job)throws ValidationException{
        Map<String,Class<?>> tasks_Id = new HashMap<>();
        Map<String,Class<?>> cont = new HashMap<>();
        Class<?>[] type_params;
        String value,value_arg;
        int pos_arg;

        if(!TasksConstraint(tasks_Id,job))
            throw new ValidationException();

        for(Map.Entry<String, Object> entry : job.getContext().entrySet())
            cont.put(entry.getKey(),  entry.getValue().getClass());
        for (Method m : job.getClass().getMethods()) {//voir si c pas mieux le declared
            if (m.isAnnotationPresent(Task.class)) {
                Annotation[][] ano = m.getParameterAnnotations();
                value=m.getAnnotation(Task.class).value();
                type_params = m.getParameterTypes();
                for (Annotation[] annotations : ano) {
                    if (annotations.length == 0)
                        throw new ValidationException();
                    pos_arg = 0;
                    for (Annotation[] ano_param : ano) {
                        for (Annotation a : ano_param) {
                            if (a instanceof Context) {
                                //- toute annotation Context doit référencer un objet existant dans le contexte du job
                                value_arg = ((Context) a).value();
                                if (!cont.containsKey(value_arg))
                                    throw new ValidationException();
                                //- il doit y avoir une compatibilité de type entre un paramètre annoté Context et l’objet
                                if (!type_params[pos_arg].isAssignableFrom(cont.get(value_arg))) {
                                    throw new ValidationException();
                                }
                            } else if (a instanceof LinkFrom) {
                                //- toute annotation LinkFrom doit référencer une tâche existante
                                value_arg = ((LinkFrom) a).value();
                                if (!tasks_Id.containsKey(value_arg))
                                    throw new ValidationException();
                                if (!type_params[pos_arg].isAssignableFrom(tasks_Id.get(value_arg))) {
                                    throw new ValidationException();
                                }
                                if(!taskGraph.existEdge(value_arg,value))
                                    taskGraph.addEdge(value_arg,value);

                            } else//un param sans annotation
                                throw new ValidationException();

                        }
                        pos_arg++;
                    }
                }
            }
        }

        if(!taskGraph.isDAG())
            throw new ValidationException();
    }



    public Job getJob() {
        return job;
    }

    public Graph<String> getTaskGraph() {
        return taskGraph;
    }

    public Method getMethod(String d) throws IllegalArgumentException {
        for(Method m : job.getClass().getDeclaredMethods())
            if(m.getAnnotation(Task.class).value().equals(d))
                return m;
        throw new IllegalArgumentException();
    }
}
