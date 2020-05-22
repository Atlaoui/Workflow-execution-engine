package srcs.workflow.job;

import java.util.Map;

public abstract class Job {
    private final String name;
    private final Map<String,Object> context;
    public Job(String name,Map context){
        this.name=name;
        this.context=context;
    }

    public String getName() { return name; }

    public Map<String, Object> getContext() { return context; }
}
