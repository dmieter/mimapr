
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
import project.math.distributions.HyperGeometricSettings;
import project.engine.data.environmentGenerator.EnvironmentGenerator;
import project.engine.data.environmentGenerator.EnvironmentGeneratorSettings;
import project.engine.data.environmentGenerator.EnvironmentPricingSettings;
import project.engine.data.jobGenerator.JobGenerator;
import project.engine.data.jobGenerator.RequestGenerator;
import project.engine.data.jobGenerator.JobGeneratorSettings;

/**
 *
 * @author emelyanov
 */
public class ShiftingExperiment {

    public SchedulingResultsStats asStats;
    public SchedulingResultsStats bsStats;
    public SchedulingResultsStats asShiftedStats;
    public SchedulingResultsStats bsShiftedStats;
    public ResourcePerformanceStats perfStats;

    private int expNum = 0;

    //Batch Slicer entity
    private BatchSlicer bs;
    private BatchSlicerSettings bss;


    public ArrayList<UserJob> batchToShow;
    public VOEnvironment envToShow;

    private int cycleLength = 600;


    public ShiftingExperiment(){
        asStats = new SchedulingResultsStats();
        bsStats = new SchedulingResultsStats();
        asShiftedStats = new SchedulingResultsStats();
        bsShiftedStats = new SchedulingResultsStats();
        perfStats = new ResourcePerformanceStats();
    }

    private void clearSchedullingData(){
        bs = new BatchSlicer();
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

        
        //BatchSlicer (AS)
        VOEnvironment env0 = VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch0 = VOEHelper.copyJobBatchList(batch);

        bss.slicesNum = 1;
        bss.shiftAlternatives = false;
        bs.solve(bss, env0, batch0);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch0)){
            System.out.println("AS failed to find alternatives");
            asStats.addFailExperiment();
            //batchToShow = batch0;
            //envToShow = env0;
            success = false;
        }

        clearSchedullingData();


        //BatchSlicer (AS + Shifting)
        VOEnvironment env1 = VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch1 = VOEHelper.copyJobBatchList(batch);

        bss.slicesNum = 1;
        bss.shiftAlternatives = true;
        bs.solve(bss, env1, batch1);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch1)){
            System.out.println("Shifted AS failed to find alternatives");
            asShiftedStats.addFailExperiment();
            //batchToShow = batch1;
            //envToShow = env1;
            success = false;
        }


        clearSchedullingData();


        //BatchSlicer
        VOEnvironment env2 = VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch2 = VOEHelper.copyJobBatchList(batch);

        bss.slicesNum = 5;
        bss.shiftAlternatives = false;
        bs.solve(bss, env2, batch2);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch2)){
            System.out.println("Batchslicing failed to find alternatives");
            bsStats.addFailExperiment();
            //batchToShow = batch2;
            //envToShow = env2;
            success = false;
        }


        clearSchedullingData();


        //BatchSlicer + Shifting
        VOEnvironment env3 = VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch3 = VOEHelper.copyJobBatchList(batch);

        bss.slicesNum = 5;
        bss.shiftAlternatives = true;
        bs.solve(bss, env3, batch3);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch3)){
            System.out.println("Shifted Batchslicing failed to find alternatives");
            bsShiftedStats.addFailExperiment();
            //batchToShow = batch3;
            //envToShow = env3;
            success = false;
        }


        //Stats
        if(success){
            asStats.processResults(batch0);
            asShiftedStats.processResults(batch1);
            bsStats.processResults(batch2);
            bsShiftedStats.processResults(batch3);
            perfStats.processEnvironment(env);
            expNum++;

            batchToShow = batch0;
            envToShow = env1;
        }
    }

    public String getData(){
        String data = "SHIFTING EXPERIMENT\n"
                +"Alternative Solver Stats\n"
                +this.asStats.getData()+"\n"
                +"Shifted Alternative Solver Stats\n"
                +this.asShiftedStats.getData()+"\n"
                +"Batch Slicer Stats\n"
                +this.bsStats.getData()+"\n"
                +"Shifted Batch Slicer Stats\n"
                +this.bsShiftedStats.getData()+"\n"
                +"Performance Stats\n"
                +this.perfStats.getData();

        return data;
    }
}
