/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package project.experiment.archive;

import java.util.ArrayList;
import project.engine.scheduler.alternativeSolver.v1.AlternativeSolverSettings;
import project.engine.alternativeStats.AlternativesExtremeStats;
import project.engine.alternativeStats.ResourcePerformanceStats;
import project.engine.alternativeStats.SchedulingResultsStats;
import project.engine.scheduler.batchSlicer.BatchSlicer;
import project.engine.scheduler.batchSlicer.BatchSlicerSettings;
import project.engine.data.Alternative;
import project.engine.data.ComputingNode;
import project.engine.data.UserJob;
import project.engine.data.VOEHelper;
import project.engine.data.VOEnvironment;
import project.engine.data.environmentGenerator.EnvironmentGenerator;
import project.engine.data.environmentGenerator.EnvironmentGeneratorSettings;
import project.engine.data.environmentGenerator.EnvironmentPricingSettings;
import project.engine.data.jobGenerator.JobGenerator;
import project.engine.data.jobGenerator.RequestGenerator;
import project.engine.data.jobGenerator.JobGeneratorSettings;
import project.engine.slot.slotProcessor.criteriaHelpers.MinFinishTimeCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinRunTimeCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinSumCostCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinSumTimeCriteria;

/**
 *
 * @author Emelyanov
 */
public class SingleExtremeInBSExperiment {

    public AlternativesExtremeStats normalSearch;
    public SchedulingResultsStats normalResults;

    public AlternativesExtremeStats minCostSearch;
    public SchedulingResultsStats minCostResults;

    public AlternativesExtremeStats minRuntimeSearch;
    public SchedulingResultsStats minRuntimeResults;

    public AlternativesExtremeStats minFinishTimeSearch;
    public SchedulingResultsStats minFinishTimeResults;

    public AlternativesExtremeStats minSumTimeSearch;
    public SchedulingResultsStats minSumTimeResults;

    public ResourcePerformanceStats perfStats;

    //Batch Slicer entity
    private BatchSlicer bs;
    private BatchSlicerSettings bss;

    public ArrayList<UserJob> batchToShow;
    public VOEnvironment envToShow;

    private int cycleLength = 600;

    public SingleExtremeInBSExperiment(){
        normalSearch = new AlternativesExtremeStats();
        minCostSearch = new AlternativesExtremeStats();
        minFinishTimeSearch = new AlternativesExtremeStats();
        minRuntimeSearch = new AlternativesExtremeStats();
        minSumTimeSearch = new AlternativesExtremeStats();

        normalResults = new SchedulingResultsStats();
        minCostResults = new SchedulingResultsStats();
        minFinishTimeResults = new SchedulingResultsStats();
        minRuntimeResults = new SchedulingResultsStats();
        minSumTimeResults = new SchedulingResultsStats();

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
        ArrayList<UserJob> batch0 = VOEHelper.copyJobBatchList(batch);
        UserJob job0 = batch0.get(10);
        bs.solve(bss, env0, batch0);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch0)){
            success = false;
            System.out.println("Normal failed to find alternatives");
        }

        //MinCost
        bs = new BatchSlicer();
        VOEnvironment env1 = VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch1 = VOEHelper.copyJobBatchList(batch);
        UserJob job1 = batch1.get(10);
        job1.resourceRequest.criteria = new MinSumCostCriteria();
        bs.solve(bss, env1, batch1);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch1)){
            success = false;
            System.out.println("MinCost failed to find alternatives");
        }

        //MinRuntime
        bs = new BatchSlicer();
        VOEnvironment env2 = VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch2 = VOEHelper.copyJobBatchList(batch);
        UserJob job2 = batch2.get(10);
        job2.resourceRequest.criteria = new MinRunTimeCriteria();
        bs.solve(bss, env2, batch2);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch2)){
            success = false;
            System.out.println("MinRuntime failed to find alternatives");
        }

        //MinFinishTime
        bs = new BatchSlicer();
        VOEnvironment env3 = VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch3 = VOEHelper.copyJobBatchList(batch);
        UserJob job3 = batch3.get(10);
        job3.resourceRequest.criteria = new MinFinishTimeCriteria();
        bs.solve(bss, env3, batch3);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch3)){
            success = false;
            System.out.println("MinFinishTime failed to find alternatives");
        }

        //MinSumTimeTime
        bs = new BatchSlicer();
        VOEnvironment env4 = VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch4 = VOEHelper.copyJobBatchList(batch);
        UserJob job4 = batch4.get(10);
        job4.resourceRequest.criteria = new MinSumTimeCriteria();
        bs.solve(bss, env4, batch4);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch4)){
            success = false;
            System.out.println("MinSumTime failed to find alternatives");
        }
        

        if(success){
            ArrayList<Alternative> bestAlternative0 = new ArrayList<Alternative>();
            bestAlternative0.add(job0.getBestAlternative());
            normalSearch.processAlternatives(bestAlternative0);
            normalResults.processResults(batch0);

            ArrayList<Alternative> bestAlternative1 = new ArrayList<Alternative>();
            bestAlternative1.add(job1.getBestAlternative());
            minCostSearch.processAlternatives(bestAlternative1);
            minCostResults.processResults(batch1);

            ArrayList<Alternative> bestAlternative2 = new ArrayList<Alternative>();
            bestAlternative2.add(job2.getBestAlternative());
            minRuntimeSearch.processAlternatives(bestAlternative2);
            minRuntimeResults.processResults(batch2);

            ArrayList<Alternative> bestAlternative3 = new ArrayList<Alternative>();
            bestAlternative3.add(job3.getBestAlternative());
            minFinishTimeSearch.processAlternatives(bestAlternative3);
            minFinishTimeResults.processResults(batch3);

            ArrayList<Alternative> bestAlternative4 = new ArrayList<Alternative>();
            bestAlternative4.add(job4.getBestAlternative());
            minSumTimeSearch.processAlternatives(bestAlternative4);
            minSumTimeResults.processResults(batch4);

            perfStats.processEnvironment(env);

        }

        envToShow = env;
        batchToShow = batch1;
    }

     private VOEnvironment generateNewEnvironment() {
        //Creating resources
        ArrayList<ComputingNode> lines = new ArrayList<ComputingNode>();

        EnvironmentGeneratorSettings envSet = new EnvironmentGeneratorSettings();
        envSet.minResourceSpeed = 1;
        envSet.maxResourceSpeed = 11;
        envSet.resourceLineNum = 24;
        envSet.maxTaskLength = 90;
        envSet.minTaskLength = 10;
        envSet.occupancyLevel = 1;
        envSet.timeInterval = cycleLength;
        //envSet.hgPerfSet = new HyperGeometricSettings(1000, 60, 100, 1);   //mean = 6.0 e = 2.254125347242491
        EnvironmentGenerator envGen = new EnvironmentGenerator();
        EnvironmentPricingSettings epc = new EnvironmentPricingSettings();
        epc.priceQuotient = 1.01;
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
        String data = "SINGLE EXTREME EXPERIMENT\n"
                +"Min Cost Batch Stats\n"
                +this.minCostResults.getData()+"\n"
                +"Min Finish Batch Stats\n"
                +this.minFinishTimeResults.getData()+"\n"
                +"Min Runtime Batch Stats\n"
                +this.minRuntimeResults.getData()+"\n"
                +"Min Proctime Batch Stats\n"
                +this.minSumTimeResults.getData()+"\n"
                +"Normal Batch Stats\n"
                +this.normalResults.getData()+"\n"
                +"Min Cost App Stats\n"
                +this.minCostSearch.getData()+"\n"
                +"Min Finish App Stats\n"
                +this.minFinishTimeSearch.getData()+"\n"
                +"Min Runtime App Stats\n"
                +this.minRuntimeSearch.getData()+"\n"
                +"Min Proctime App Stats\n"
                +this.minSumTimeSearch.getData()+"\n"
                +"Normal App Stats\n"
                +this.normalSearch.getData()+"\n"
                +"Performance Stats\n"
                +this.perfStats.getData();

        return data;
    }

}
