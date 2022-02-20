
package project.experiment.archive;

import java.util.ArrayList;
import project.engine.scheduler.alternativeSolver.v1.AlternativeSolverSettings;
import project.engine.scheduler.alternativeSolver.v1.LimitCountData;
import project.engine.alternativeStats.ResourcePerformanceStats;
import project.engine.alternativeStats.SchedulingResultsStats;
import project.engine.scheduler.batchSlicer.BatchSlicer;
import project.engine.scheduler.batchSlicer.BatchSlicerSettings;
import project.engine.data.ComputingNode;
import project.engine.data.UserJob;
import project.engine.data.VOEHelper;
import project.engine.data.VOEnvironment;
import project.math.distributions.HyperGeometricFacade;
import project.math.distributions.HyperGeometricSettings;
import project.engine.data.environmentGenerator.EnvironmentGenerator;
import project.engine.data.environmentGenerator.EnvironmentGeneratorSettings;
import project.engine.data.environmentGenerator.EnvironmentPricingSettings;
import project.engine.data.jobGenerator.JobGenerator;
import project.engine.data.jobGenerator.RequestGenerator;
import project.engine.data.jobGenerator.JobGeneratorSettings;

/**
 *
 * @author Magica
 */
public class BatchSliceExperiment {
    
    public SchedulingResultsStats slice1Stats;
    public SchedulingResultsStats slice2Stats;
    public SchedulingResultsStats slice3Stats;
    public SchedulingResultsStats slice5Stats;
    public SchedulingResultsStats slice7Stats;
    public SchedulingResultsStats slice12Stats;
    public SchedulingResultsStats slice24Stats;
    public ResourcePerformanceStats perfStats;
    
    //Batch Slicer entity
    private BatchSlicer bs;
    private BatchSlicerSettings bss;

    public ArrayList<UserJob> batchToShow;
    public VOEnvironment envToShow;

    private int cycleLength = 600;

    public BatchSliceExperiment(){
        slice1Stats = new SchedulingResultsStats();
        slice2Stats = new SchedulingResultsStats();
        slice3Stats = new SchedulingResultsStats();
        slice5Stats = new SchedulingResultsStats();
        slice7Stats = new SchedulingResultsStats();
        slice12Stats = new SchedulingResultsStats();
        slice24Stats = new SchedulingResultsStats();
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
    
    private void performSingleExperiment(){
        clearSchedullingData();
        VOEnvironment env = generateNewEnvironment();
        ArrayList<UserJob> batch = generateJobBatch();
        boolean success = true;

        bss.asSettings.limitCalculationType = 0;

        //24 Slice
        clearSchedullingData();
        bss.slicesNum = 20;
        VOEnvironment env24 =VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch24 = VOEHelper.copyJobBatchList(batch);
        bs.solve(bss, env24, batch24);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch24)){
            success = false;
            slice24Stats.addFailExperiment();
            System.out.println("Slice 20 failed to find alternatives");
            //return;
        }

        //bss.asSettings.limitCalculationType = 2;
        //bss.asSettings.limitCountData.externalJobs = batch24;
        
        //1 Slice
        clearSchedullingData();
        bss.slicesNum = 1;
        VOEnvironment env1 =VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch1 = VOEHelper.copyJobBatchList(batch);
        bs.solve(bss, env1, batch1);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch1)){
            success = false;
            slice1Stats.addFailExperiment();
            System.out.println("Slice 1 failed to find alternatives");
        }
        
        //2 Slice
        clearSchedullingData();
        bss.slicesNum = 2;
        VOEnvironment env2 =VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch2 = VOEHelper.copyJobBatchList(batch);
        bs.solve(bss, env2, batch2);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch2)){
            success = false;
            slice2Stats.addFailExperiment();
            System.out.println("Slice 2 failed to find alternatives");
        }
        
        //3 Slice
        clearSchedullingData();
        bss.slicesNum = 3;
        VOEnvironment env3 =VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch3 = VOEHelper.copyJobBatchList(batch);
        bs.solve(bss, env3, batch3);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch3)){
            success = false;
            slice3Stats.addFailExperiment();
            System.out.println("Slice 3 failed to find alternatives");
        }
        
        //5 Slice
        clearSchedullingData();
        bss.slicesNum = 5;
        VOEnvironment env5 =VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch5 = VOEHelper.copyJobBatchList(batch);
        bs.solve(bss, env5, batch5);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch5)){
            success = false;
            slice5Stats.addFailExperiment();
            System.out.println("Slice 5 failed to find alternatives");
        }

        //7 Slice
        clearSchedullingData();
        bss.slicesNum = 6;
        VOEnvironment env7 =VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch7 = VOEHelper.copyJobBatchList(batch);
        bs.solve(bss, env7, batch7);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch7)){
            success = false;
            slice7Stats.addFailExperiment();
            System.out.println("Slice 6 failed to find alternatives");
        }
        
        //12 Slice
        clearSchedullingData();
        bss.slicesNum = 10;
        VOEnvironment env12 =VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch12 = VOEHelper.copyJobBatchList(batch);
        bs.solve(bss, env12, batch12);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch12)){
            success = false;
            slice12Stats.addFailExperiment();
            System.out.println("Slice 10 failed to find alternatives");
        }
        

        
        if(success){
            slice1Stats.processResults(batch1);
            slice2Stats.processResults(batch2);
            slice3Stats.processResults(batch3);
            slice5Stats.processResults(batch5);
            slice7Stats.processResults(batch7);
            slice12Stats.processResults(batch12);
            slice24Stats.processResults(batch24);

            perfStats.processEnvironment(env);
            envToShow = env;
            batchToShow = batch7;
        }
        
        
    }
    
    private void batchSlicerConfiguration(){
        bs = new BatchSlicer();
        bss = new BatchSlicerSettings();

        bss.periodStart = 0;
        bss.periodEnd = cycleLength;
        bss.sliceAlgorithm = 0;
        bss.spAlgorithmType = "MODIFIED";
        //bss.spConceptType = "EXTREME";
        //bss.spCriteriaHelper = new MinSumTimeCriteria();
        //bss.slicesNum = 3;
        bss.shiftAlternatives = false;// true - Shifting - another experiment
        bss.asSettings = new AlternativeSolverSettings();
        bss.asSettings.usePareto = false;
        bss.asSettings.limitedVar = 1;      //Cost
        bss.asSettings.optimizedVar = 0;    //Time
        bss.asSettings.optType = "MIN";
        bss.asSettings.optimalOnly = true;
        bss.asSettings.limitCalculationType = 0;    //average
        bss.asSettings.limitQuotient = 1.0;
        bss.asSettings.limitCountData = new LimitCountData();
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
        //envSet.occupancyLevel = 2;
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
        VOEnvironment env = envGen.generate(envSet, lines);
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

    public String getData(){
        String data = "BATCH SLICE EXPERIMENT\n"
                +"Slice 1 Stats\n"
                +this.slice1Stats.getData()+"\n"
                +"Slice 2 Stats\n"
                +this.slice2Stats.getData()+"\n"
                +"Slice 3 Stats\n"
                +this.slice3Stats.getData()+"\n"
                +"Slice 5 Stats\n"
                +this.slice5Stats.getData()+"\n"
                +"Slice 6 Stats\n"
                +this.slice7Stats.getData()+"\n"
                +"Slice 10 Stats\n"
                +this.slice12Stats.getData()+"\n"
                +"Slice 20 Stats\n"
                +this.slice24Stats.getData()+"\n"
                +"Performance Stats\n"
                +this.perfStats.getData();

        return data;
    }
}
