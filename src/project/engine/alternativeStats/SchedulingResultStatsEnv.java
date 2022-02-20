package project.engine.alternativeStats;

import java.util.ArrayList;
import project.engine.data.Alternative;
import project.engine.data.DistributedTask;
import project.engine.data.Slot;
import project.engine.data.UserJob;
import project.engine.data.VOEHelper;
import project.engine.data.VOEnvironment;

/**
 *
 * @author dmieter
 */
@Deprecated
public class SchedulingResultStatsEnv extends SchedulingResultsStats {
    public double averageDistance;
    
    public SchedulingResultStatsEnv(){
        super();
        averageDistance = 0;
    }
    
    public void applyAndProcessResults(ArrayList<UserJob> jobs, VOEnvironment env, long workingTimeNs){
        processResults(jobs, workingTimeNs);
        
        VOEHelper.applyBestAlternativesToVOE(jobs, env);
        
        Double sumDistance = 0d;
        
        for(UserJob job : jobs){
            Alternative a = job.getBestAlternative();
            for(Slot s : a.window.slots){
                DistributedTask prevTask = VOEHelper.getPreviousTask(s.resourceLine.id, s.start, env);
                DistributedTask nextTask = VOEHelper.getNextTask(s.resourceLine.id, s.end, env);
            }
        }
        
    }
}
