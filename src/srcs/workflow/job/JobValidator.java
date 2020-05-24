package srcs.workflow.job;

import srcs.workflow.graph.Graph;
import srcs.workflow.graph.GraphImpl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

public class JobValidator {
    private Job job ;
    private Graph<String> taskGraph;
    public JobValidator(Job job) throws ValidationException{

        taskGraph = new GraphImpl<>();
        Constraints(job);

        this.job=job;

    }
    private Class<?> getObjectClass(Object obj) throws ValidationException {
        if(obj instanceof Integer)
            return Integer.class;

        if (obj instanceof Number) {
            return Number.class;
        }

        if (obj instanceof String)
        {
            System.out.println(obj);
            return String.class;
        }
        if(obj instanceof Double)
            return Double.class;
        if (obj instanceof Boolean)
            return Boolean.class;


        throw new ValidationException();
    }

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
                //nb_tasks++;
                //si la method return void
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
    private void Constraints(Job job)throws ValidationException{
        Map<String,Class<?>> tasks_Id = new HashMap<>();
        Map<String,Class<?>> cont = new HashMap<>();
        Class<?>[] type_params;
        String value,value_arg;
        int pos_arg;

        if(!TasksConstraint(tasks_Id,job))
            throw new ValidationException();

        for(Map.Entry<String, Object> entry : job.getContext().entrySet())
            cont.put(entry.getKey(),getObjectClass(entry.getValue()));
       // System.out.println(tasks_Id);
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
                                    // if(!job.getContext().get(value_arg).getClass().equals(type_params[pos_arg]))
                                  /*  System.out.println(type_params[pos_arg].isAssignableFrom(cont.get(value_arg)));
                                    System.out.println(int.class.isAssignableFrom(Integer.class));
                                    System.out.println("Typ pos = "+type_params[pos_arg]);
                                    System.out.println("cont type = "+ cont.get(value_arg));*/
                                    throw new ValidationException();
                                }
                            } else if (a instanceof LinkFrom) {
                                //- toute annotation LinkFrom doit référencer une tâche existante
                                value_arg = ((LinkFrom) a).value();
                                if (!tasks_Id.containsKey(value_arg))
                                    throw new ValidationException();
                                if (!type_params[pos_arg].isAssignableFrom(tasks_Id.get(value_arg))) {

                                   /* System.out.println("Params = "+Arrays.toString(type_params));
                                    System.out.println("pos = "+pos_arg);
                                    System.out.println("Value " + value_arg);
                                    System.out.println("Typ pos = "+type_params[pos_arg]);
                                    System.out.println("tasks type = "+ tasks_Id.get(value_arg));*/
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
       //System.out.println("le mien ="+taskGraph);
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
