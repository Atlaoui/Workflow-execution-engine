package srcs.workflow.test;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import srcs.workflow.job.Job;

public abstract class JobForTest extends Job {

	
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String SEP="_";
	
	private final File working_dir;
	
	private  List<String> tasksequence=null;
	private  Map<String,Long> mapping_task_thread=null;
	private  Map<String,Integer> mapping_task_pid=null;
	
	
	public JobForTest(String name, Map<String, Object> context, File working_dir) {
		super(name, context);
		this.working_dir=working_dir;
		
	}
	
	
	public void reset() {
		if(!working_dir.exists()) {
			working_dir.mkdir();
		}else {
			if(!working_dir.isDirectory()) {
				working_dir.delete();
				working_dir.mkdir();
			}
		}
		for(File f : working_dir.listFiles()) {
			f.delete();
		}
		mapping_task_thread=null;
		tasksequence=null;
		mapping_task_pid=null;
	}
		
	public Map<String,Integer> getMappingTaskPid(){
		if(mapping_task_pid == null) {
			computeStat();
		}
		return mapping_task_pid;
	}
	
	public  List<String> getTaskSequence(){
		if(tasksequence == null) {
			computeStat();
		}
		return tasksequence;
	}
	
	public  Map<String,Long> getMappingTaskThread(){
		if(mapping_task_thread == null) {
			computeStat();
		}
		return mapping_task_thread;
	}
	
	private void computeStat() {
		File[] list_files = working_dir.listFiles();
		Arrays.sort(list_files, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				String tmsp1 = o1.getName().split(SEP)[3];
				String tmsp2 = o2.getName().split(SEP)[3];
				
				return Long.valueOf(Long.parseLong(tmsp1)).compareTo(Long.valueOf(Long.parseLong(tmsp2)));
			}			
		});
		tasksequence = Arrays.asList(list_files).stream().map(f-> f.getName().split(SEP)[0]).collect(Collectors.toList());		
		mapping_task_thread = new HashMap<>();
		mapping_task_pid = new HashMap<>();
		for(File f : list_files) {
			String tmp[] = f.getName().split(SEP);
			String id_task = tmp[0];
			String pid = tmp[1];
			String id_thread = tmp[2];
			
			mapping_task_thread.put(id_task, Long.parseLong(id_thread));
			mapping_task_pid.put(id_task, Integer.parseInt(pid));
		}
		
	}
	
	
	protected void begin(String id_task) {
		String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
		
		
		File f = new File(working_dir, id_task+SEP+pid+SEP+Thread.currentThread().getId()+SEP+System.currentTimeMillis());
		try {
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	protected void end(String id_task) {
		
	}
}
