/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package project.experiment.archive;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import project.engine.scheduler.alternativeSolver.v1.AlternativeSolver;
import project.engine.scheduler.alternativeSolver.v1.AlternativeSolverSettings;
import project.engine.alternativeStats.ResourcePerformanceStats;
import project.engine.alternativeStats.SchedulingResultsStats;
import project.engine.scheduler.backFill.Backfill;
import project.engine.scheduler.backFill.BackfillSettings;
import project.engine.scheduler.backSliceFill.BackSliceFill;
import project.engine.scheduler.backSliceFill.BackSliceFillSettings;
import project.engine.scheduler.batchSlicer.BatchSlicer;
import project.engine.scheduler.batchSlicer.BatchSlicerSettings;
import project.engine.data.ComputingNode;
import project.engine.data.ResourceRequest;
import project.engine.data.UserJob;
import project.engine.data.VOEHelper;
import project.engine.data.VOEnvironment;
import project.math.distributions.HyperGeometricFacade;
import project.engine.data.environmentGenerator.EnvironmentGenerator;
import project.engine.data.environmentGenerator.EnvironmentGeneratorSettings;
import project.engine.data.environmentGenerator.EnvironmentPricingSettings;
import project.engine.data.jobGenerator.JobGenerator;
import project.math.distributions.HyperGeometricSettings;
import project.engine.data.jobGenerator.RequestGenerator;
import project.engine.data.jobGenerator.JobGeneratorSettings;
import project.engine.slot.slotProcessor.SlotProcessorSettings;
import project.engine.slot.slotProcessor.SlotProcessorV2;

/**
 *
 * @author Emelyanov
 */
public class RegressionSchedullingExperiment {

    public SchedulingResultsStats alternativeSolverStats = null;
    public SchedulingResultsStats backFillingStats = null;
    public SchedulingResultsStats batchSlicerStats = null;
    public SchedulingResultsStats backSliceFillStats = null;
    public ResourcePerformanceStats resourceStats = null;

    private int expNum = 0;
    //Alternative Solver entities
    private SlotProcessorV2 sp;
    private SlotProcessorSettings sps;
    private AlternativeSolver as;
    private AlternativeSolverSettings ass;
    //Batch Slicer entity
    private BatchSlicer bs;
    private BatchSlicerSettings bss;
    //Backfilling entity
    private Backfill bf;
    private BackfillSettings bfs;
    //BackSliceFilling entity
    private BackSliceFill bsf;
    private BackSliceFillSettings bsfs;

    public ArrayList<UserJob> batchToShow;
    public VOEnvironment envToShow;

    private int cycleLength = 600;

    public RegressionSchedullingExperiment(){
        alternativeSolverStats = new SchedulingResultsStats();
        backFillingStats = new SchedulingResultsStats();
        batchSlicerStats = new SchedulingResultsStats();
        backSliceFillStats = new SchedulingResultsStats();
        resourceStats = new ResourcePerformanceStats();
    }

    private void clearSchedullingData(){
        bf = new Backfill();
        as = new AlternativeSolver();
        bs = new BatchSlicer();
        bsf = new BackSliceFill();
    }

    public void performExperiments(int expNum){
        
        alternativeSolverConfiguration();
        batchSlicerConfiguration();
        backfillingConfiguration();
        backSliceFillingConfiguration();

        for(int i=0; i<expNum;i++){
            System.out.println("--------------Experiment #"+i+" -------------------");
            performSingleExperiment();
        }
    }

    private void performSingleExperiment(){

        clearSchedullingData();
        VOEnvironment env = loadEnvironment();
        ArrayList<UserJob> batch = generateJobBatch();






        //BackFilling
        VOEnvironment env0 = VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch0 = VOEHelper.copyJobBatchList(batch);

        boolean success = true;

        bf.solve(bfs, env0, batch0);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch0)){
            System.out.println("Backfilling failed to find alternatives");
            backFillingStats.failStats.processEnvironment(env0);
            backFillingStats.addFailExperiment();
            success = false;
        }else{
            //System.out.println("    Backfilling completed");
        }






        //Alternative Solver
        VOEnvironment env1 = VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch1 = VOEHelper.copyJobBatchList(batch);

        sp.findAlternatives(batch1, env1, sps);
        ass.limitCalculationType = 0;
        //ass.limitCountData = new LimitCountData(0, 0);
        //ass.limitCountData.externalJobs = batch0;               //using BF batch as a limit
        as.solve(ass, batch1);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch1)){
            System.out.println("AS failed to find alternatives");
            alternativeSolverStats.failStats.processEnvironment(env1);
            alternativeSolverStats.addFailExperiment();
            success = false;
        }else{
            //System.out.println("    AS completed");
        }







        //BatchSlicer
        VOEnvironment env2 = VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch2 = VOEHelper.copyJobBatchList(batch);

        bss.asSettings.limitCalculationType = 0;
        //bss.asSettings.limitCountData = new LimitCountData(0, 0);
        //bss.asSettings.limitCountData.externalJobs = batch0;               //using BF batch as a limit
        bs.solve(bss, env2, batch2);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch2)){
            System.out.println("Batchslicing failed to find alternatives");
            batchSlicerStats.failStats.processEnvironment(env2);
            batchSlicerStats.addFailExperiment();
            //batchToShow = batch2;
            //envToShow = env2;
            success = false;
        }else{
            //System.out.println("    BS completed");
        }    






        //BackSliceFiller
        VOEnvironment env3 = VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch3 = VOEHelper.copyJobBatchList(batch);
        bsf.solve(bsfs, env3, batch3);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch3)){
            System.out.println("BSF failed to find alternatives");
            backSliceFillStats.failStats.processEnvironment(env3);
            backSliceFillStats.addFailExperiment();
            success = false;
        }else{
            //System.out.println("    BSF completed");
        }





        //Stats
        if(success){
            backFillingStats.processResults(batch0);
            alternativeSolverStats.processResults(batch1);
            batchSlicerStats.processResults(batch2);
            backSliceFillStats.processResults(batch3);
            resourceStats.processEnvironment(env);
            expNum++;

            batchToShow = batch2;
            envToShow = env2;
            int b=0;
        }
    }

    private VOEnvironment generateNewEnvironment() {
        //Creating resources
        ArrayList<ComputingNode> lines = new ArrayList<ComputingNode>();

        EnvironmentGeneratorSettings envSet = new EnvironmentGeneratorSettings();
        envSet.minResourceSpeed = 2;
        envSet.maxResourceSpeed = 11;
        envSet.resourceLineNum = 21;
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
        epc.priceMutationFactor = 0.2;
        epc.speedExtraCharge = 0.02;

        lines = envGen.generateResourceTypes(envSet);

        //creating environment
        VOEnvironment env = envGen.generate(envSet, lines);
        env.applyPricing(epc);

        return env;
    }
    
    private VOEnvironment loadEnvironment() {
        
        try{
            FileInputStream fis = new FileInputStream("env1.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            VOEnvironment env = (VOEnvironment)ois.readObject();
            ois.close();
            fis.close();
            int e = 4;
            return env;
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private ArrayList<UserJob> generateJobBatch() {
        ArrayList<UserJob> batch = new ArrayList<UserJob>();
        
        ResourceRequest r1 = new ResourceRequest(3, 200, 1.4, 1);
        ResourceRequest r2 = new ResourceRequest(2, 500, 1.2, 1);
        ResourceRequest r3 = new ResourceRequest(4, 150, 1.6, 1);
        ResourceRequest r4 = new ResourceRequest(5, 270, 1.1, 1);
        ResourceRequest r5 = new ResourceRequest(2, 450, 1.4, 1);
        ResourceRequest r6 = new ResourceRequest(3, 410, 1.3, 1);
        ResourceRequest r7 = new ResourceRequest(3, 320, 1.4, 1);
        
        UserJob j1 = new UserJob(0, "Job1", r1, null, null);
        UserJob j2 = new UserJob(1, "Job1", r2, null, null);
        UserJob j3 = new UserJob(2, "Job1", r3, null, null);
        UserJob j4 = new UserJob(3, "Job1", r4, null, null);
        UserJob j5 = new UserJob(4, "Job1", r5, null, null);
        UserJob j6 = new UserJob(5, "Job1", r6, null, null);
        UserJob j7 = new UserJob(6, "Job1", r7, null, null);   
        
        batch.add(j1);
        batch.add(j2);
        batch.add(j3);
        batch.add(j4);
        batch.add(j5);
        batch.add(j6);
        batch.add(j7);
        
        return batch;
    }

    private void alternativeSolverConfiguration(){
        sp = new SlotProcessorV2();
        as = new AlternativeSolver();
        sps = new SlotProcessorSettings();
        ass = new AlternativeSolverSettings();

        sps.algorithmConcept = "COMMON";
        sps.algorithmType = "MODIFIED";
        sps.cycleLength = cycleLength;
        sps.cycleStart = 0;
        sps.countStats = false;
        sps.clean = true;

        ass.optimalOnly = true;
        ass.limitedVar = AlternativeSolverSettings.COST;
        ass.optimizedVar = AlternativeSolverSettings.TIME;
        ass.optType = "MIN";
        ass.usePareto = false;
        ass.limitCalculationType = 0;
    }

    private void batchSlicerConfiguration(){
        bs = new BatchSlicer();
        bss = new BatchSlicerSettings();

        bss.periodStart = 0;
        bss.periodEnd = cycleLength;
        bss.sliceAlgorithm = 0;
        bss.spAlgorithmType = "MODIFIED";
        bss.spConceptType = "COMMON";
        bss.slicesNum = 5;
        bss.shiftAlternatives = true;
        bss.asSettings = new AlternativeSolverSettings();
        bss.asSettings.usePareto = false;
        bss.asSettings.limitedVar = AlternativeSolverSettings.COST;
        bss.asSettings.optimizedVar = AlternativeSolverSettings.TIME;
        bss.asSettings.optType = "MIN";
        bss.asSettings.optimalOnly = true;
        bss.asSettings.limitCalculationType = 0;
    }

    private void backfillingConfiguration(){
        bf = new Backfill();
        bfs = new BackfillSettings();

        bfs.aggressive = true;
        bfs.backfillMetric = "PROCSECONDS";
        bfs.periodStart = 0;
        bfs.periodEnd = cycleLength;
        bfs.policy = "BESTFIT";
    }

    private void backSliceFillingConfiguration(){
        bsf = new BackSliceFill();
        bsfs = new BackSliceFillSettings();

        bsfs.slicerQuotient = 0.5;

        bsfs.bfs = new BackfillSettings();
        bsfs.bfs.aggressive = true;
        bsfs.bfs.backfillMetric = "PROCSECONDS";
        bsfs.bfs.periodStart = 0;
        bsfs.bfs.periodEnd = cycleLength;
        bsfs.bfs.policy = "BESTFIT";

        bsfs.bss = new BatchSlicerSettings();
        bsfs.bss.periodStart = 0;
        bsfs.bss.periodEnd = cycleLength;
        bsfs.bss.sliceAlgorithm = 0;
        bsfs.bss.spAlgorithmType = "MODIFIED";
        bsfs.bss.spConceptType = "COMMON";
        bsfs.bss.slicesNum = 2;
        bsfs.bss.shiftAlternatives = true;
        bsfs.bss.asSettings = new AlternativeSolverSettings();
        bsfs.bss.asSettings.usePareto = false;
        bsfs.bss.asSettings.optimizedVar = AlternativeSolverSettings.TIME;    //Cost
        bsfs.bss.asSettings.limitedVar = AlternativeSolverSettings.COST;      //Time
        bsfs.bss.asSettings.optType = "MIN";
        bsfs.bss.asSettings.optimalOnly = true;
        bsfs.bss.asSettings.limitCalculationType = 0;
        bsfs.bss.shiftAlternatives = true;
    }

    public String getData(){
        String data = "FULL SCHEDULING EXPERIMENT\n"
                +"Backfilling Stats\n"
                +this.backFillingStats.getData()+"\n"
                +"Alternative Solver Stats\n"
                +this.alternativeSolverStats.getData()+"\n"
                +"Batch Slicer Stats\n"
                +this.batchSlicerStats.getData()+"\n"
                +"Batch Clice Filler Stats\n"
                +this.backSliceFillStats.getData()+"\n"
                +"Performance Stats\n"
                +this.resourceStats.getData();

        return data;
    }
    
    
}
