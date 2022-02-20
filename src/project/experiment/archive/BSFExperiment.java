
package project.experiment.archive;

import java.util.ArrayList;
import project.engine.scheduler.alternativeSolver.v1.AlternativeSolverSettings;
import project.engine.alternativeStats.ResourcePerformanceStats;
import project.engine.alternativeStats.SchedulingResultsStats;
import project.engine.scheduler.backFill.BackfillSettings;
import project.engine.scheduler.backSliceFill.BackSliceFill;
import project.engine.scheduler.backSliceFill.BackSliceFillSettings;
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
public class BSFExperiment {
    
    public SchedulingResultsStats bspart02;
    public SchedulingResultsStats bspart04;
    public SchedulingResultsStats bspart06;
    public SchedulingResultsStats bspart08;
    public ResourcePerformanceStats perfStats;
    
    //Batch Slicer entity
    private BackSliceFill bsf;
    private BackSliceFillSettings bsfs;

    public ArrayList<UserJob> batchToShow;
    public VOEnvironment envToShow;

    private int cycleLength = 600;

    public BSFExperiment(){
        bspart02 = new SchedulingResultsStats();
        bspart04 = new SchedulingResultsStats();
        bspart06 = new SchedulingResultsStats();
        bspart08 = new SchedulingResultsStats();
        perfStats = new ResourcePerformanceStats();
    }
    
    private void backSliceFillConfiguration(){
        bsf = new BackSliceFill();
        bsfs = new BackSliceFillSettings();

        //bsfs.slicerQuotient = 0.5;

        bsfs.bfs = new BackfillSettings();
        bsfs.bfs.aggressive = true;
        bsfs.bfs.backfillMetric = "COSTMIN";
        bsfs.bfs.periodStart = 0;
        bsfs.bfs.periodEnd = cycleLength;
        bsfs.bfs.policy = "BESTFIT";

        bsfs.bss = new BatchSlicerSettings();
        bsfs.bss.periodStart = 0;
        bsfs.bss.periodEnd = cycleLength;
        bsfs.bss.sliceAlgorithm = 0;
        bsfs.bss.spAlgorithmType = "MODIFIED";
        bsfs.bss.spConceptType = "COMMON";
        bsfs.bss.slicesNum = 4;
        bsfs.bss.shiftAlternatives = false;
        bsfs.bss.asSettings = new AlternativeSolverSettings();
        bsfs.bss.asSettings.usePareto = false;
        bsfs.bss.asSettings.limitedVar = AlternativeSolverSettings.COST;
        bsfs.bss.asSettings.optimizedVar = AlternativeSolverSettings.TIME;
        bsfs.bss.asSettings.optType = "MIN";
        bsfs.bss.asSettings.optimalOnly = true;
        bsfs.bss.asSettings.limitCalculationType = 0;   //average
    }
    
    public void performExperiments(int expNum){

        backSliceFillConfiguration();

        for(int i=0; i<expNum;i++){
            System.out.println("--------------Experiment #"+i+" -------------------");
            performSingleExperiment();
        }
    }
    
    private void performSingleExperiment(){
        VOEnvironment env = generateNewEnvironment();
        ArrayList<UserJob> batch = generateJobBatch();
        boolean success = true;
        
        //0.2 bs part
        VOEnvironment env02 = VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch02 = VOEHelper.copyJobBatchList(batch);
        bsfs.slicerQuotient = 0.2;
        bsfs.bss.slicesNum = 1;
        bsf.solve(bsfs, env02, batch02);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch02)){
            success = false;
            bspart02.addFailExperiment();
            System.out.println("0.2 quotient failed to find alternatives");
        }
        
        //0.4 bs part
        VOEnvironment env04 = VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch04 = VOEHelper.copyJobBatchList(batch);
        bsfs.slicerQuotient = 0.4;
        bsfs.bss.slicesNum = 2;
        bsf.solve(bsfs, env04, batch04);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch04)){
            success = false;
            bspart04.addFailExperiment();
            System.out.println("0.4 quotient failed to find alternatives");
        }
        
        //0.6 bs part
        VOEnvironment env06 = VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch06 = VOEHelper.copyJobBatchList(batch);
        bsfs.slicerQuotient = 0.6;
        bsfs.bss.slicesNum = 3;
        bsf.solve(bsfs, env06, batch06);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch06)){
            success = false;
            bspart06.addFailExperiment();
            System.out.println("0.6 quotient failed to find alternatives");
        }
        
        //0.8 bs part
        VOEnvironment env08 = VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch08 = VOEHelper.copyJobBatchList(batch);
        bsfs.slicerQuotient = 0.8;
        bsfs.bss.slicesNum = 4;
        bsf.solve(bsfs, env08, batch08);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch08)){
            success = false;
            bspart08.addFailExperiment();
            System.out.println("0.8 quotient failed to find alternatives");
        }
        envToShow = env;
        batchToShow = batch04;

        if(success){
            bspart02.processResults(batch02);
            bspart04.processResults(batch04);
            bspart06.processResults(batch06);
            bspart08.processResults(batch08);
            perfStats.processEnvironment(env);
        }
    }
    
    private VOEnvironment generateNewEnvironment() {
        //Creating resources
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

        ArrayList<ComputingNode> lines = envGen.generateResourceTypes(envSet);

        //creating environment
        VOEnvironment env = envGen.generate(envSet, lines);
        env.applyPricing(epc);

        return env;
    }
    
    private void clearSchedullingData(){
        bsf = new BackSliceFill();
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
        String data = "BACK SLICE FILL EXPERIMENT\n"
                +"Part 0.2 Stats\n"
                +this.bspart02.getData()+"\n"
                +"Part 0.4 Stats\n"
                +this.bspart04.getData()+"\n"
                +"Part 0.6 Stats\n"
                +this.bspart06.getData()+"\n"
                +"Part 0.8 Stats\n"
                +this.bspart08.getData()+"\n"
                +"Performance Stats\n"
                +this.perfStats.getData();

        return data;
    }

}
