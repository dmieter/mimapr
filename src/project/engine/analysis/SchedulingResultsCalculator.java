package project.engine.analysis;

import java.util.ArrayList;
import project.engine.data.Alternative;
import project.engine.data.Slot;
import project.engine.data.UserJob;

/**
 *
 * @author emelyanov
 */
public class SchedulingResultsCalculator {

    protected double avCost = 0;
    protected double avTime = 0;
    protected double avPerf = 0;
    protected double avRuntime = 0;
    protected double avStart = 0;
    protected double avFinish = 0;
    protected double avAlternatives = 0;
    protected double avUserRanking = 0;
    protected double maxFinishTime = 0;

    protected void clear() {
        avCost = 0;
        avTime = 0;
        avPerf = 0;
        avRuntime = 0;
        avStart = 0;
        avFinish = 0;
        avAlternatives = 0;
        avUserRanking = 0;
        maxFinishTime = 0;
    }

    public void calculateResults(ArrayList<UserJob> jobs) {

        clear();
        
        if (jobs.isEmpty()) {
            return;
        }

        for (UserJob job : jobs) {
            Alternative a = job.getBestAlternative();
            avCost += a.getCost();
            avTime += a.getLength();
            avRuntime += a.getRuntime();
            avStart += a.getStart();
            double finishTime = a.getStart() + a.getRuntime();
            avFinish += finishTime;
            avAlternatives += job.alternatives.size();
            avUserRanking += a.getUserRating();

            for (Slot s : a.window.slots) {
                avPerf += s.resourceLine.getPerformance();
            }

            if (finishTime > getMaxFinishTime()) {
                maxFinishTime = finishTime;
            }
        }
        int jobsNum = jobs.size();

        avCost /= jobsNum;
        avTime /= jobsNum;
        avRuntime /= jobsNum;
        avStart /= jobsNum;
        avFinish /= jobsNum;
        avAlternatives /= jobsNum;
        avUserRanking /= jobsNum;
        avPerf /= jobsNum;
    }

    /**
     * @return the avCost
     */
    public double getAvCost() {
        return avCost;
    }

    /**
     * @return the avTime
     */
    public double getAvTime() {
        return avTime;
    }

    /**
     * @return the avPerf
     */
    public double getAvPerf() {
        return avPerf;
    }

    /**
     * @return the avRuntime
     */
    public double getAvRuntime() {
        return avRuntime;
    }

    /**
     * @return the avStart
     */
    public double getAvStart() {
        return avStart;
    }

    /**
     * @return the avFinish
     */
    public double getAvFinish() {
        return avFinish;
    }

    /**
     * @return the maxFinishTime
     */
    public double getMaxFinishTime() {
        return maxFinishTime;
    }
}
