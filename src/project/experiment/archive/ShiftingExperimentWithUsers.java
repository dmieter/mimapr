
package project.experiment.archive;

import java.util.ArrayList;
import project.engine.scheduler.alternativeSolver.v1.AlternativeSolverSettings;
import project.engine.alternativeStats.ResourcePerformanceStats;
import project.engine.alternativeStats.SchedulingResultsStats;
import project.engine.scheduler.backFill.BackfillSettings;
import project.engine.scheduler.backSliceFill.BackSliceFill;
import project.engine.scheduler.backSliceFill.BackSliceFillSettings;
import project.engine.scheduler.batchSlicer.BatchSlicer;
import project.engine.scheduler.batchSlicer.BatchSlicerSettings;
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
 * @author emelyanov
 */
public class ShiftingExperimentWithUsers {

    public SchedulingResultsStats asStats;
    public SchedulingResultsStats asShiftedStats;
    public SchedulingResultsStats bfStats;
    public SchedulingResultsStats bsfStats;

    public SchedulingResultsStats minCostExtreme;
    public SchedulingResultsStats minRuntimeExtreme;
    public SchedulingResultsStats minFinishTimeExtreme;
    public SchedulingResultsStats minProcTimeExtreme;
    public SchedulingResultsStats BSFNormalBatchStats;
    public SchedulingResultsStats BSFExtremeBatchStats;

    
    public ResourcePerformanceStats perfStats;

    private int expNum = 0;

    //Batch Slicer entity
    private BatchSlicer bs;
    private BatchSlicerSettings bss;

    private BackSliceFill bsf;
    private BackSliceFillSettings bsfs;
    
    public ArrayList<UserJob> batchToShow;
    public VOEnvironment envToShow;

    private int cycleLength = 600;


    public ShiftingExperimentWithUsers(){
        asStats = new SchedulingResultsStats();
        bfStats = new SchedulingResultsStats();
        asShiftedStats = new SchedulingResultsStats();
        bsfStats = new SchedulingResultsStats();
        perfStats = new ResourcePerformanceStats();
        
        minCostExtreme = new SchedulingResultsStats();
        minFinishTimeExtreme = new SchedulingResultsStats();
        minRuntimeExtreme = new SchedulingResultsStats();
        minProcTimeExtreme = new SchedulingResultsStats();
        BSFNormalBatchStats = new SchedulingResultsStats();
        BSFExtremeBatchStats = new SchedulingResultsStats();
    }

    private void clearSchedullingData(){
        bs.flush();
        bsf.flush();
    }


        private VOEnvironment generateNewEnvironment() {
        //Creating resources
        ArrayList<ComputingNode> lines = new ArrayList<ComputingNode>();

        EnvironmentGeneratorSettings envSet = new EnvironmentGeneratorSettings();
        envSet.minResourceSpeed = 2;
        envSet.maxResourceSpeed = 11;
        envSet.resourceLineNum = 12;
        envSet.maxTaskLength = 100;
        envSet.minTaskLength = 10;
        envSet.occupancyLevel = 1;
        //HyperGeometricSettings hgSet = new HyperGeometricSettings(1000, 150, 30, 0, 10, 0, 2);
        //envSet.occupGenerator = new HyperGeometricFacade(hgSet);
        envSet.timeInterval = cycleLength;
        //envSet.hgPerfSet = new HyperGeometricSettings(1000, 60, 100, 1);   //mean = 6.0 e = 2.254125347242491
        EnvironmentGenerator envGen = new EnvironmentGenerator();
        EnvironmentPricingSettings epc = new EnvironmentPricingSettings();
        epc.priceQuotient = 1;
        epc.priceMutationFactor = 0.6;
        epc.speedExtraCharge = 0.01;

        lines = envGen.generateResourceTypes(envSet);

        //creating environment
        VOEnvironment env = envGen.generate(envSet, lines);
        env.applyPricing(epc);

        return env;
    }

    private ArrayList<UserJob> generateJobBatch() {
        JobGenerator jg = new JobGenerator();
        JobGeneratorSettings jgs = new JobGeneratorSettings();
        jgs.taskNumber = 15;

        jgs.minPrice = 1.1;
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
        bss.slicesNum = 1;
        bss.shiftAlternatives = true;
        bss.asSettings = new AlternativeSolverSettings();
        bss.asSettings.usePareto = false;
        bss.asSettings.limitedVar = AlternativeSolverSettings.COST;
        bss.asSettings.optimizedVar = AlternativeSolverSettings.TIME;
        bss.asSettings.optType = "MIN";
        bss.asSettings.optimalOnly = true;
        bss.asSettings.limitCalculationType = 0;
    }
    
    private void backSliceFillConfiguration(){
        bsf = new BackSliceFill();
        bsfs = new BackSliceFillSettings();

        //bsfs.slicerQuotient = 0.5;

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
        bsfs.bss.spConceptType = "EXTREME";
        bsfs.bss.slicesNum = 2;
        bsfs.bss.shiftAlternatives = true;
        bsfs.bss.asSettings = new AlternativeSolverSettings();
        bsfs.bss.asSettings.usePareto = false;
        bsfs.bss.asSettings.limitedVar = AlternativeSolverSettings.COST;
        bsfs.bss.asSettings.optimizedVar = AlternativeSolverSettings.TIME;
        bsfs.bss.asSettings.optType = "MIN";
        bsfs.bss.asSettings.optimalOnly = true;
        bsfs.bss.asSettings.limitCalculationType = 0;   //average
    }
    
    public void performExperiments(int expNum){

        batchSlicerConfiguration();
        backSliceFillConfiguration();

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


        //BackFilling
        VOEnvironment env2 = VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch2 = VOEHelper.copyJobBatchList(batch);

        bsfs.slicerQuotient = 0;
        bsf.solve(bsfs, env2, batch2);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch2)){
            System.out.println("BF failed to find alternatives");
            bfStats.addFailExperiment();
            //batchToShow = batch2;
            //envToShow = env2;
            success = false;
        }


        clearSchedullingData();


        //BSF
        VOEnvironment bsfEnv = VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> bsfBatch = VOEHelper.copyJobBatchList(batch);
        JobGenerator jg = new JobGenerator();
        jg.setRandomBatchCriterias(bsfBatch);

        bsfs.slicerQuotient = 0.5;
        bsf.solve(bsfs, bsfEnv, bsfBatch);
        if(!SchedulingResultsStats.checkBatchForSuccess(bsfBatch)){
            System.out.println("BSF failed to find alternatives");
            bsfStats.addFailExperiment();
            //batchToShow = bsfBatch;
            //envToShow = bsfEnv;
            success = false;
        }


        //Stats
        if(success){
            
            ArrayList<UserJob> minFinishExtremeList = new ArrayList<UserJob>();
            ArrayList<UserJob> minProcTimeExtremeList = new ArrayList<UserJob>();
            ArrayList<UserJob> minCostExtremeList = new ArrayList<UserJob>();
            ArrayList<UserJob> minRuntimeExtremeList = new ArrayList<UserJob>();
            ArrayList<UserJob> extremeList = new ArrayList<UserJob>();
            ArrayList<UserJob> normalList = new ArrayList<UserJob>();
            
            for(UserJob job : bsf.jobsForBS){
                if(job.resourceRequest.criteria instanceof MinSumCostCriteria){
                    minCostExtremeList.add(job);
                }else if(job.resourceRequest.criteria instanceof MinRunTimeCriteria){
                    minRuntimeExtremeList.add(job);
                }else if(job.resourceRequest.criteria instanceof MinFinishTimeCriteria){
                    minFinishExtremeList.add(job);
                }else if(job.resourceRequest.criteria instanceof MinSumTimeCriteria){
                    minProcTimeExtremeList.add(job);
                }
            }
            
            normalList = bsf.jobsForBF;
            extremeList = bsf.jobsForBS;
            
            minCostExtreme.processResults(minCostExtremeList);
            minRuntimeExtreme.processResults(minRuntimeExtremeList);
            minFinishTimeExtreme.processResults(minFinishExtremeList);
            minProcTimeExtreme.processResults(minProcTimeExtremeList);
            BSFNormalBatchStats.processResults(normalList);
            BSFExtremeBatchStats.processResults(extremeList);        
            
            
            asStats.processResults(batch0);
            asShiftedStats.processResults(batch1);
            bfStats.processResults(batch2);
            bsfStats.processResults(bsfBatch);
            perfStats.processEnvironment(env);
            expNum++;

            batchToShow = bsfBatch;
            envToShow = env1;
        }
    }

    public String getData(){
        String data = "SHIFTING EXPERIMENT\n"
                +"Alternative Solver Stats\n"
                +this.asStats.getData()+"\n"
                +"Shifted Alternative Solver Stats\n"
                +this.asShiftedStats.getData()+"\n"
                +"BSF Stats\n"
                +this.bsfStats.getData()+"\n"
                +"Backfilling Stats\n"
                +this.bfStats.getData()+"\n"
                
                +"BSF USER DATA\n"
                +"BSF Extreme Batch\n"
                +this.BSFExtremeBatchStats.getData()+"\n"
                +"BSF Normal Batch\n"
                +this.BSFNormalBatchStats.getData()+"\n"
                +"BSF Runtime Jobs\n"
                +this.minRuntimeExtreme.getData()+"\n"
                +"BSF Cost Jobs\n"
                +this.minCostExtreme.getData()+"\n"
                +"BSF Finish Time Jobs\n"
                +this.minFinishTimeExtreme.getData()+"\n"
                +"BSF Processor Time Jobs\n"
                +this.minProcTimeExtreme.getData()+"\n"
                +"Performance Stats\n"
                +this.perfStats.getData()
                ;

        return data;
    }
}
