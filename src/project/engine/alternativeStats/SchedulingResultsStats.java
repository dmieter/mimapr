/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package project.engine.alternativeStats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import project.engine.data.Alternative;
import project.engine.data.Slot;
import project.engine.data.UserJob;

/**
 *
 * @author Magica
 */
public class SchedulingResultsStats {

    public String name;
    
    public int experimentsNum;
    public double avJobsNum;
    public double averageJobTime;
    public double averageJobRunTime;
    public double averageJobStartTime;
    public double averageJobFinishTime;
    public double averageMakespan;
    public double averageJobCost;
    public double averagePerformance;
    public double averageJobAlternatives;
    public double averageUserRanking;
    public double failsNum;
    public double averageWorkingTime;
    public ArrayList<Double> collectedValue;
    public ArrayList<AverageDouble> averageList;

    public ResourcePerformanceStats failStats;

    public SchedulingResultsStats(){
       clearStats();
    }
    
    public SchedulingResultsStats(String name){
        this.name = name;
       clearStats();
    }

    public void clearStats(){
       experimentsNum = 0;
       avJobsNum = 0;
       averageJobTime = 0;
       averageJobRunTime = 0;
       averageJobCost = 0;
       averageJobStartTime = 0;
       averageJobFinishTime = 0;
       averageMakespan = 0;
       averagePerformance = 0;
       averageJobAlternatives = 0;
       averageUserRanking = 0;
       failsNum = 0;
       averageWorkingTime = 0;
       collectedValue = new ArrayList<>();
       averageList = new ArrayList<>();
       failStats = new ResourcePerformanceStats();
    }

    public void processResults(ArrayList<UserJob> jobs){
        processResults(jobs, 0);
    }
    
    public void processResults(ArrayList<UserJob> jobs, long workingTimeNs){

        if(jobs.isEmpty()){
            return;
        }

        double avCost = 0;
        double avTime = 0;
        double avPerf = 0;
        double avRuntime = 0;
        double avStart = 0;
        double avFinish = 0;
        double avAlternatives = 0;
        double avUserRanking = 0;
        double maxFinishTime = 0;

        for(UserJob job : jobs){
            Alternative a = job.getBestAlternative();
            avCost += a.getCost();
            avTime += a.getLength();
            avRuntime += a.getRuntime();
            avStart += a.getStart();
            double finishTime = a.getStart()+a.getRuntime();
            avFinish += finishTime;
            avAlternatives += job.alternatives.size();
            avUserRanking += a.getUserRating();
            
            for(Slot s : a.window.slots){
                avPerf += s.resourceLine.getPerformance();
            }
            
            if(finishTime > maxFinishTime){
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
    
        averageJobTime = (averageJobTime*experimentsNum + avTime)/(experimentsNum+1);
        averageJobCost = (averageJobCost*experimentsNum + avCost)/(experimentsNum+1);
        averagePerformance = (averagePerformance*experimentsNum + avPerf)/(experimentsNum+1);
        averageJobRunTime = (averageJobRunTime*experimentsNum + avRuntime)/(experimentsNum+1);
        averageJobStartTime = (averageJobStartTime*experimentsNum + avStart)/(experimentsNum+1);
        averageJobFinishTime = (averageJobFinishTime*experimentsNum + avFinish)/(experimentsNum+1);
        averageJobAlternatives = (averageJobAlternatives*experimentsNum + avAlternatives)/(experimentsNum+1);
        averageUserRanking = (averageUserRanking*experimentsNum + avUserRanking)/(experimentsNum+1);
        
        averageWorkingTime = (averageWorkingTime*experimentsNum + workingTimeNs)/(experimentsNum+1);
        avJobsNum = (avJobsNum*experimentsNum + jobs.size())/(experimentsNum+1);
        averageMakespan = (averageMakespan*experimentsNum + maxFinishTime)/(experimentsNum+1);

        experimentsNum++;
        

//        for(UserJob j : jobs){
//            DescriptiveStatistics stats = getJobStat(j);
//            addAverageList(Arrays.asList(
//                stats.getMean(), stats.getStandardDeviation(), stats.getSkewness()));
//        }
   
    }
    
//    public DescriptiveStatistics getJobStat(UserJob job){
//        Alternative a = job.getBestAlternative();
//        DescriptiveStatistics stats = new DescriptiveStatistics();
//
//        a.window.slots.forEach(s -> stats.addValue(s.getPerformance()));
//        
//        return stats;
//    }
    
    public void addFailExperiment(){
        failsNum++;
    }

    public static boolean checkBatchForSuccess(ArrayList<UserJob> jobs){
        boolean succes = true;
        if(jobs.size() < 1)
            return false;

        int g = 0;
        for(UserJob job : jobs){
            if((job.bestAlternative < 0)&&(job.alternatives.size() > 0)){
                g = 1;
                if(job.alternatives.size() == 1){ /// WTF???
                    job.bestAlternative = 0;
                }
            }
            if(job.bestAlternative<0 && job.alternatives.size()>0){
                g = 2;
            }
            if((job.bestAlternative < 0)||(job.alternatives.size() < 1)){
                return false;
            }
        }
        return succes;
    }

    public void addCollectedValue(double value){
        collectedValue.add(value);
    }
    
    public void addAverageList(List<Double> dataList){
        int i = 0;
        for(Double data : dataList){
            if(i < averageList.size()){
                AverageDouble avData = averageList.get(i);
                avData.putValue(data);
            }else{
                /* no previous data for this index i, just adding current value */
                averageList.add(new AverageDouble(data));
            }
            
            i++;
        }
    }
    
    public String getData(){
        String data = "";
        if(name != null){
            data += name + "\n";
        }
        data += "Number Of Experiments: "+this.experimentsNum+"\n"
                +"JobsNum: "+this.avJobsNum+"\n"
                +"AlternativesPerJob: "+this.averageJobAlternatives+"\n"
                +"UserRanking: "+this.averageUserRanking+"\n"
                +"JobStart: "+this.averageJobStartTime+"\n"
                +"JobRunTime: "+this.averageJobRunTime+"\n"
                +"JobFinishTime: "+this.averageJobFinishTime+"\n"
                +"JobSumTime: "+this.averageJobTime+"\n"
                +"JobSumCost: "+this.averageJobCost+"\n"
                +"SumPerformance: "+this.averagePerformance+"\n"
                +"Makespan: "+this.averageMakespan+"\n"
                +"Fails: "+this.failsNum+"\n"
                +"Working Time: "+this.averageWorkingTime/1000000000+" s\n";

        if(collectedValue.size() > 0){
            data += "Collected Value: ";
            for(Double d : collectedValue){
                data += d +"\n";
            }
        }
        
        if(!averageList.isEmpty()){
            data += "Average List Values: \n";
            for(AverageDouble ad : averageList){
                data += ad +"\n";
            }
        }
        return data;
    }
}
