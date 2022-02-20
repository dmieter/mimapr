
package project.engine.scheduler.backFill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import project.engine.data.Alternative;
import project.engine.data.UserJob;
import project.engine.data.VOEHelper;
import project.engine.data.VOEnvironment;
import project.engine.slot.slotProcessor.SimpleSquareWindowFinder;
import project.engine.slot.slotProcessor.SlotProcessorSettings;

/**
 *
 * @author emelyanov
 */
public class BackfillingProcedure {
    protected SimpleSquareWindowFinder sqwFinder;
    protected SlotProcessorSettings sps;
    
    private int t;
    private final int intervalEnd;
    
    protected VOEnvironment env;
    protected ArrayList<UserJob> pendingJobs;
    protected ArrayList<UserJob> scheduledJobs;
    protected ArrayList<UserJob> unfulfillableJobs;
    protected ArrayList<UserJob> schBatch;
    
    public BackfillingProcedure(SimpleSquareWindowFinder sqwFinder, int intervalStart, int intervalEnd){
        this.sqwFinder = sqwFinder;
        sps = new SlotProcessorSettings();
        sps.cycleStart = intervalStart;
        sps.cycleLength = intervalEnd - intervalStart;
        
        t = intervalStart;
        this.intervalEnd = intervalEnd;
        
        schBatch = new ArrayList<>();
        scheduledJobs = new ArrayList<>();
        unfulfillableJobs = new ArrayList<>();
        pendingJobs = new ArrayList<>();
    }
    
    public boolean schedule(List<UserJob> jobBatch, VOEnvironment env){
        this.env = VOEHelper.copyEnvironment(env);
        pendingJobs.addAll(jobBatch);
        Collections.sort(pendingJobs, (j1, j2)->(j1.timestamp.compareTo(j2.timestamp)));
        
        int cntSteps = 0;
        while(!pendingJobs.isEmpty()){
            performSchedulingStep();
            cntSteps++;
            if(cntSteps > 10000){
                throw new IllegalStateException("Too many backfilling cycles");
            }
        }
        
        return true;
    }
    
    private void performSchedulingStep(){
        if(pendingJobs.isEmpty()){
            return;
        }
        
        /* go further in time and get next jobs */
        t = pendingJobs.iterator().next().timestamp;
        //System.out.println("next bf step at " + t);
        for(Iterator<UserJob> it = pendingJobs.iterator(); it.hasNext();){
            UserJob job = it.next();
            if(job.timestamp <= t){
                schBatch.add(job);
                it.remove();
            } else{
                break;
            }
        }

        // we might want to save initial priorities, so cooment the line below
        //applyPriorirties(schBatch);  // i.e. small jobs first or smth like that
        
        if(schBatch.isEmpty()){
            return;
        }
        
        /* perform scheduling */
        sps.cycleStart = t;
        sps.cycleLength = intervalEnd - t;
        sqwFinder.findAlternatives(schBatch, env, sps, 1);
        
        /* processing scheduling results */
        for(Iterator<UserJob> it = schBatch.iterator(); it.hasNext();){
            UserJob job = it.next();
            if(!job.alternatives.isEmpty()){
                job.bestAlternative = 0;
                
                Alternative a = job.getBestAlternative();
                if(a.getStart() < t || (a.getStart() + a.getRuntime()) > intervalEnd){
                    throw new IllegalStateException("Job scheduled incorrectly");
                }else{
                    //System.out.println(job.name + " starts at " + a.getStart());
                }
                
                VOEHelper.applyBestAlternativeToVOE(job, env);
                scheduledJobs.add(job);
                it.remove();
            }else{
                // without changes in interval, env or resource request this job can't be scheduled
                unfulfillableJobs.add(job);
                it.remove();
            }
        }
    }
    
    private void applyPriorirties(List<UserJob> queue){
        /* small jobs firts */
        Collections.sort(queue, (j1, j2)->(((Double)j1.resourceRequest.volume).compareTo(j2.resourceRequest.volume)));
    }

    /**
     * @param sqwFinder the sqwFinder to set
     */
    public void setFinder(SimpleSquareWindowFinder sqwFinder) {
        this.sqwFinder = sqwFinder;
    }
}
