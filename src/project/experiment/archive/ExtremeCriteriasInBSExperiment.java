/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package project.experiment.archive;

import java.util.ArrayList;
import project.engine.scheduler.alternativeSolver.v1.AlternativeSolverSettings;
import project.engine.alternativeStats.ResourcePerformanceStats;
import project.engine.alternativeStats.SchedulingResultsStats;
import project.engine.scheduler.batchSlicer.BatchSlicer;
import project.engine.scheduler.batchSlicer.BatchSlicerSettings;
import project.engine.data.ComputingNode;
import project.engine.data.UserJob;
import project.engine.data.VOEHelper;
import project.engine.data.VOEnvironment;
import project.math.distributions.HyperGeometricFacade;
import project.engine.data.environmentGenerator.EnvironmentGenerator;
import project.engine.data.environmentGenerator.EnvironmentGeneratorSettings;
import project.engine.data.environmentGenerator.EnvironmentPricingSettings;
import project.engine.data.jobGenerator.JobGenerator;
import project.engine.data.jobGenerator.RequestGenerator;
import project.engine.data.jobGenerator.JobGeneratorSettings;
import project.engine.scheduler.SchedulerOperations;
import project.engine.slot.slotProcessor.criteriaHelpers.MinFinishTimeCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinRunTimeCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinSumCostCriteria;
import project.math.distributions.HyperGeometricSettings;

/**
 *
 * @author Emelyanov
 */
public class ExtremeCriteriasInBSExperiment {

    public SchedulingResultsStats extremeResults;
    public SchedulingResultsStats normalResults;

    public SchedulingResultsStats minCostExtreme;
    public SchedulingResultsStats minCostNormal;

    public SchedulingResultsStats minRuntimeExtreme;
    public SchedulingResultsStats minRuntimeNormal;

    public SchedulingResultsStats minFinishTimeExtreme;
    public SchedulingResultsStats minFinishTimeNormal;

    public SchedulingResultsStats minStartTimeExtreme;
    public SchedulingResultsStats minStartTimeNormal;

    public ResourcePerformanceStats perfStats;


    //Batch Slicer entity
    private BatchSlicer bs;
    private BatchSlicerSettings bss;

    public ArrayList<UserJob> batchToShow;
    public VOEnvironment envToShow;

    private int cycleLength = 600;

    public ExtremeCriteriasInBSExperiment(){
        normalResults = new SchedulingResultsStats();
        minCostNormal = new SchedulingResultsStats();
        minFinishTimeNormal = new SchedulingResultsStats();
        minRuntimeNormal = new SchedulingResultsStats();
        minStartTimeNormal = new SchedulingResultsStats();

        extremeResults = new SchedulingResultsStats();
        minCostExtreme = new SchedulingResultsStats();
        minFinishTimeExtreme = new SchedulingResultsStats();
        minRuntimeExtreme = new SchedulingResultsStats();
        minStartTimeExtreme = new SchedulingResultsStats();

        perfStats = new ResourcePerformanceStats();
    }

    private void clearSchedullingData(){
        bs = new BatchSlicer();
    }

    public void performExperiments(int expNum){

        batchSlicerConfiguration();

        for(int i=0; i<expNum;i++){
            System.out.println("--------------Experiment #"+i+" -------------------");
            performSingleExperiment();
        }
    }

    private void performSingleExperiment() {
        clearSchedullingData();
        VOEnvironment env = generateNewEnvironment();
        ArrayList<UserJob> batch = generateJobBatch();

        boolean success = true;

        //Normal
        VOEnvironment env0 = VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> normalBatch = VOEHelper.copyJobBatchList(batch);
        bs.solve(bss, env0, normalBatch);
        if(!SchedulingResultsStats.checkBatchForSuccess(normalBatch)){
            success = false;
            System.out.println("Normal failed to find alternatives");
            normalResults.addFailExperiment();
        }

        //Extreme
        bs = new BatchSlicer();
        VOEnvironment env1 = VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> extremeBatch = VOEHelper.copyJobBatchList(batch);
        JobGenerator jg = new JobGenerator();
        jg.setRandomBatchCriterias(extremeBatch);
        bs.solve(bss, env1, extremeBatch);
        if(!SchedulingResultsStats.checkBatchForSuccess(extremeBatch)){
            success = false;
            System.out.println("Extreme failed to find alternatives");
            extremeResults.addFailExperiment();
        }

        if(success){
            ArrayList<UserJob> minFinishExtremeList = new ArrayList<UserJob>();
            ArrayList<UserJob> minFinishNormalList = new ArrayList<UserJob>();
            
            ArrayList<UserJob> minCostExtremeList = new ArrayList<UserJob>();
            ArrayList<UserJob> minCostNormalList = new ArrayList<UserJob>();
            
            ArrayList<UserJob> minRuntimeExtremeList = new ArrayList<UserJob>();
            ArrayList<UserJob> minRuntimeNormalList = new ArrayList<UserJob>();
            
            ArrayList<UserJob> minStartTimeExtremeList = new ArrayList<UserJob>();
            ArrayList<UserJob> minStartTimeNormalList = new ArrayList<UserJob>();

            for(UserJob job : extremeBatch){
                if(job.resourceRequest.criteria == null){
                    minStartTimeExtremeList.add(job);
                }else if(job.resourceRequest.criteria instanceof MinSumCostCriteria){
                    minCostExtremeList.add(job);
                }else if(job.resourceRequest.criteria instanceof MinRunTimeCriteria){
                    minRuntimeExtremeList.add(job);
                }else if(job.resourceRequest.criteria instanceof MinFinishTimeCriteria){
                    minFinishExtremeList.add(job);
                }
            }
            
            //Getting the same jobs form normal batch to normal lists
            minFinishNormalList = SchedulerOperations.getSubListById(normalBatch, minFinishExtremeList);
            minCostNormalList = SchedulerOperations.getSubListById(normalBatch, minCostExtremeList);
            minRuntimeNormalList = SchedulerOperations.getSubListById(normalBatch, minRuntimeExtremeList);
            minStartTimeNormalList = SchedulerOperations.getSubListById(normalBatch, minStartTimeExtremeList);

            String distribution = minCostExtremeList.size()+" "+minFinishExtremeList.size()+" "+minRuntimeExtremeList.size()+" "+minStartTimeExtremeList.size();
            
            extremeResults.processResults(extremeBatch);
            normalResults.processResults(normalBatch);

            minCostExtreme.processResults(minCostExtremeList);
            minCostExtreme.addCollectedValue(minCostExtreme.averageJobCost);  
            minCostNormal.processResults(minCostNormalList);
            minCostNormal.addCollectedValue(minCostNormal.averageJobCost); 
                    
            minRuntimeExtreme.processResults(minRuntimeExtremeList);
            minRuntimeExtreme.addCollectedValue(minRuntimeExtreme.averageJobRunTime);
            minRuntimeNormal.processResults(minRuntimeNormalList);
            minRuntimeNormal.addCollectedValue(minRuntimeNormal.averageJobRunTime);
            
            
            minFinishTimeExtreme.processResults(minFinishExtremeList);
            minFinishTimeNormal.processResults(minFinishNormalList);

            minStartTimeExtreme.processResults(minStartTimeExtremeList);
            minStartTimeNormal.processResults(minStartTimeNormalList);

            perfStats.processEnvironment(env);
        }

        envToShow = env;
        batchToShow = extremeBatch;
    }

     private VOEnvironment generateNewEnvironment() {
        //Creating resources
        ArrayList<ComputingNode> lines = new ArrayList<ComputingNode>();

        EnvironmentGeneratorSettings envSet = new EnvironmentGeneratorSettings();
        envSet.minResourceSpeed = 2;
        envSet.maxResourceSpeed = 11;
        envSet.resourceLineNum = 24;
        envSet.maxTaskLength = 100;
        envSet.minTaskLength = 10;
        //envSet.occupancyLevel = 1;
        HyperGeometricSettings hgSet = new HyperGeometricSettings(1000, 150, 30, 0, 10, 0, 2);
        envSet.occupGenerator = new HyperGeometricFacade(hgSet);
        envSet.timeInterval = cycleLength;
        //envSet.hgPerfSet = new HyperGeometricSettings(1000, 60, 100, 1);   //mean = 6.0 e = 2.254125347242491
        EnvironmentGenerator envGen = new EnvironmentGenerator();
        EnvironmentPricingSettings epc = new EnvironmentPricingSettings();
        epc.priceQuotient = 1;
        epc.priceMutationFactor = 0.6;
        epc.speedExtraCharge = 0.02;

        lines = envGen.generateResourceTypes(envSet);

        //creating environment
        VOEnvironment env = new VOEnvironment();
        env = envGen.generate(envSet, lines);
        env.applyPricing(epc);

        return env;
    }

     private ArrayList<UserJob> generateJobBatch() {
        JobGenerator jg = new JobGenerator();
        JobGeneratorSettings jgs = new JobGeneratorSettings();
        jgs.taskNumber = 20;

        jgs.minPrice = 1.0;
        jgs.maxPrice = 1.6;
        jgs.useSpeedPriceFactor = true;

        jgs.minTime = 100;
        jgs.maxTime = 500;

        jgs.minSpeed = 1;
        jgs.maxSpeed = 1;

        jgs.minCPU = 2;
        jgs.maxCPU = 5;

        return jg.generate(jgs);
    }

     private void batchSlicerConfiguration(){
        bs = new BatchSlicer();
        bss = new BatchSlicerSettings();

        bss.periodStart = 0;
        bss.periodEnd = cycleLength;
        bss.sliceAlgorithm = 0;
        bss.spAlgorithmType = "MODIFIED";
        bss.spConceptType = "EXTREME";
        bss.slicesNum = 5;
        bss.shiftAlternatives = true;
        bss.asSettings = new AlternativeSolverSettings();
        bss.asSettings.usePareto = false;
        bss.asSettings.limitedVar = AlternativeSolverSettings.COST;
        bss.asSettings.optimizedVar = AlternativeSolverSettings.TIME;
        bss.asSettings.optType = "MIN";
        bss.asSettings.optimalOnly = true;
        bss.asSettings.limitCalculationType = 0;    //average
    }

     public String getData(){
        String data = "EXTREME CRITERIAS IN BS EXPERIMENT\n"
                +"Extreme Stats\n"
                +this.extremeResults.getData()+"\n"
                +"Normal Stats\n"
                +this.normalResults.getData()+"\n"
                +"Min Cost Extreme Stats\n"
                +this.minCostExtreme.getData()+"\n"
                +"Min Cost Normal Stats\n"
                +this.minCostNormal.getData()+"\n"
                +"Min Finish Extreme Stats\n"
                +this.minFinishTimeExtreme.getData()+"\n"
                +"Min Finish Normal Stats\n"
                +this.minFinishTimeNormal.getData()+"\n"
                +"Min Runtime Extreme Stats\n"
                +this.minRuntimeExtreme.getData()+"\n"
                +"Min Runtime Normal Stats\n"
                +this.minRuntimeNormal.getData()+"\n"
                +"Min Start Extreme Stats\n"
                +this.minStartTimeExtreme.getData()+"\n"
                +"Min Start Normal Stats\n"
                +this.minStartTimeNormal.getData()+"\n"
                +"Performance Stats\n"
                +this.perfStats.getData();

        return data;
    }
}

