package srcs.workflow.executor;

import srcs.workflow.job.Job;

import java.util.HashMap;
import java.util.Map;

public class JobExecutorSequential extends JobExecutor{
    public JobExecutorSequential(Job job) {
        super(job);
    }

    @Override
    public Map<String, Object> execute() throws Exception {
        Map<String, Object> retValues = new HashMap<>();
        for(String funcName : jobV.getTaskGraph()){
            
        }

        return retValues;
    }
}
/*
 Node A
Vertex in : []
Vertex out : [E, C]
Node B
Vertex in : []
Vertex out : [C]
Node C
Vertex in : [A, B]
Vertex out : [E, D]
Node D
Vertex in : [C]
Vertex out : []
Node E
Vertex in : [A, C]
Vertex out : []
*/

/*
Node A
Vertex in : []
Vertex out : [E]
Node B
Vertex in : []
Vertex out : [E]
Node C
Vertex in : []
Vertex out : [F]
Node D
Vertex in : []
Vertex out : [F]
Node E
Vertex in : [A, B]
Vertex out : [G]
Node F
Vertex in : [C, D]
Vertex out : [G]
Node G
Vertex in : [E, F]
Vertex out : []

 */