package project.experiment;

import java.util.ArrayList;
import java.util.List;
import project.engine.alternativeStats.SchedulingResultsStats;
import project.engine.data.ComputingNode;
import project.engine.data.ComputingResourceLine;
import project.engine.data.UserJob;
import project.engine.data.VOEHelper;
import project.engine.data.VOEnvironment;
import project.engine.data.environmentGenerator.EnvironmentGenerator;
import project.engine.data.environmentGenerator.EnvironmentGeneratorSettings;
import project.engine.data.environmentGenerator.EnvironmentPricingSettings;
import project.engine.data.jobGenerator.JobGenerator;
import project.engine.data.jobGenerator.JobGeneratorSettings;
import project.engine.scheduler.alternativeSolver.v2.AlternativeSolverSettingsV2;
import project.engine.scheduler.alternativeSolver.v2.AlternativeSolverV2;
import project.engine.scheduler.alternativeSolver.v2.LimitSettings;
import project.engine.scheduler.alternativeSolver.v2.optimization.ConfigurableLimitedOptimization;
import project.engine.scheduler.alternativeSolver.v2.optimization.OptimizationConfig;
import project.engine.scheduler.batchSlicer.BatchSlicer;
import project.engine.scheduler.batchSlicer.BatchSlicerSettings;
import project.engine.slot.slotProcessor.criteriaHelpers.MinFinishTimeCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinRunTimeCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinSumCostCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinSumTimeCriteria;
import project.engine.slot.slotProcessor.userRankings.PercentileUserRanking;
import project.engine.slot.slotProcessor.userRankings.SimpleUserRanking;
import project.engine.slot.slotProcessor.userRankings.UserRanking;
import project.math.distributions.HyperGeometricFacade;
import project.math.distributions.HyperGeometricSettings;
import project.math.utils.MathUtils;

/**
 *
 * @author magica
 */
public class UserCriteriaRelationsExperiment extends Experiment {

    
    public int[] relations = {0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
    public SchedulingResultsStats[] schedulingResults = new SchedulingResultsStats[relations.length];
    public ArrayList[] batches = new ArrayList[relations.length];
    
    public SchedulingResultsStats[] schedulingResults2 = new SchedulingResultsStats[relations.length];
    public ArrayList[] batches2 = new ArrayList[relations.length];
    
    protected BatchSlicer bs;
    protected BatchSlicerSettings bss;

    public static final int cycleLength = 800;
    
    public ArrayList<UserJob> batchToShow;
    public VOEnvironment envToShow;

    protected VOEnvironment generateNewEnvironment() {

        EnvironmentGeneratorSettings envSet = new EnvironmentGeneratorSettings();
        envSet.minResourceSpeed = 2;
        envSet.maxResourceSpeed = 11;
        envSet.resourceLineNum = 100;
        envSet.maxTaskLength = 100;
        envSet.minTaskLength = 10;
        HyperGeometricSettings hgSet = new HyperGeometricSettings(1000, 150, 30, 0, 10, 0, 2);
        envSet.occupGenerator = new HyperGeometricFacade(hgSet);
        envSet.timeInterval = cycleLength;

        // Creating resources
        EnvironmentGenerator envGen = new EnvironmentGenerator();
        ArrayList<ComputingNode> lines = envGen.generateResourceTypes(envSet);

        //creating environment
        VOEnvironment env = envGen.generate(envSet, lines);

        // Pricing
        EnvironmentPricingSettings epc = new EnvironmentPricingSettings();
        epc.priceQuotient = 1;
        epc.priceMutationFactor = 0.6;
        epc.speedExtraCharge = 0.02;

        env.applyPricing(epc);
        
//        int cnt = 0;
//        for(ResourceLine rl : env.resourceLines){
//            if(cnt < env.resourceLines.size()/2){
//                rl.resourceType.setSpeed(3);
//                rl.price = 2.2;
//            }else{
//                rl.resourceType.setSpeed(9);
//                rl.price = 10;
//            }
//            cnt++;
//        }

        return env;
    }

    protected ArrayList<UserJob> generateJobBatch() {
        JobGenerator jg = new JobGenerator();
        JobGeneratorSettings jgs = new JobGeneratorSettings();
        jgs.taskNumber = 100;

        jgs.minPrice = 1.0;
        jgs.maxPrice = 1.6;
        jgs.useSpeedPriceFactor = true;

        jgs.minTime = 100;
        jgs.maxTime = 500;

        jgs.minSpeed = 1;
        jgs.maxSpeed = 1;

        jgs.minCPU = 2;
        jgs.maxCPU = 5;

        ArrayList<UserJob> jobs = jg.generate(jgs);
        //UserRanking ur = new SimpleUserRanking();
        UserRanking ur = new PercentileUserRanking();
        for (UserJob job : jobs) {
            job.rankingAlgorithm = ur;
        }

        jg.setRandomBatchCriterias(jobs);

        return jobs;
    }

    protected void configureExperiment() {
        for (int i = 0; i < relations.length; i++) {
            schedulingResults[i] = new SchedulingResultsStats();
        }
        for (int i = 0; i < relations.length; i++) {
            schedulingResults2[i] = new SchedulingResultsStats();
        }
        configureBatchSlicer();
    }

    protected void flush() {
        bs.flush();
    }

    @Override
    public void performExperiments(int expNum) {
        configureExperiment();
        for (int i = 0; i < expNum; i++) {
            System.out.println("--------------Experiment #" + i + " -------------------");
            flush();
            performSingleExperiment();
        }
    }

    private void performSingleExperiment() {

        VOEnvironment env = generateNewEnvironment();
        ArrayList<UserJob> batch = generateJobBatch();
        
        boolean success = true;
        envToShow = env;

        for (int i = 0; i < relations.length; i++) {

            int limit = 100;
            System.out.println("Scheduling batch with " + relations[i] + " cost optimizaing jobs and limit "+limit);

            ArrayList<UserJob> tempBatch = VOEHelper.copyJobBatchList(batch);
            VOEnvironment envTemp = VOEHelper.copyEnvironment(env);
            setRandomRequestCriteria(tempBatch, relations[i]);
            ArrayList<UserJob> tempBatch2 = VOEHelper.copyJobBatchList(tempBatch);

            AlternativeSolverSettingsV2 assv2 = (AlternativeSolverSettingsV2)bss.localSchedulerSettings;
            assv2.limitSettings.constLimit = limit;
            bs.solve(bss, envTemp, tempBatch);

            if (!SchedulingResultsStats.checkBatchForSuccess(tempBatch)) {
                System.out.println("Failed to find alternatives");
                schedulingResults[i].failStats.processEnvironment(env);
                schedulingResults[i].addFailExperiment();
                success = false;
            }else{
                batches[i] = tempBatch;
            }
            
            bs.flush();
            
            limit = 20;
            System.out.println("Scheduling batch with " + relations[i] + " cost optimizaing jobs and limit "+limit);

            envTemp = VOEHelper.copyEnvironment(env);

            assv2 = (AlternativeSolverSettingsV2)bss.localSchedulerSettings;
            assv2.limitSettings.constLimit = limit;
            bs.solve(bss, envTemp, tempBatch2);

            if (!SchedulingResultsStats.checkBatchForSuccess(tempBatch2)) {
                System.out.println("Failed to find alternatives");
                schedulingResults2[i].failStats.processEnvironment(env);
                schedulingResults2[i].addFailExperiment();
                success = false;
            }else{
                batches2[i] = tempBatch2;
            }
            
            bs.flush();
            
        }
        
        if(success){
            for(int i = 0; i < relations.length; i++){
                schedulingResults[i].processResults(batches[i]);
                schedulingResults2[i].processResults(batches2[i]);
                List<Double> relation = new ArrayList<>(1);
                relation.add(calculateCriteriaRelation(batches[i]));
                schedulingResults[i].addAverageList(relation);
            }
        }
        batchToShow = batches[3];

    }

    protected void setRandomRequestCriteria(List<UserJob> jobs, int costPercent) {
        for (UserJob job : jobs) {
            int percent = MathUtils.getUniform(0, 100);

            if (percent < costPercent) {
                job.resourceRequest.criteria = new MinSumCostCriteria();
            } else {
                job.resourceRequest.criteria = new MinRunTimeCriteria();
            }

            try {
                Thread.sleep(7);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void configureBatchSlicer() {
        AlternativeSolverV2 as2 = new AlternativeSolverV2();
        AlternativeSolverSettingsV2 ass2 = new AlternativeSolverSettingsV2();

        ass2.optType = AlternativeSolverSettingsV2.MIN;
        ass2.secondaryOptType = AlternativeSolverSettingsV2.MIN;
        ass2.setSchedulingInterval(0, cycleLength);

        LimitSettings ls = new LimitSettings();
        ls.limitType = LimitSettings.LIMIT_TYPE_AVERAGE;
        ls.limitType = LimitSettings.LIMIT_TYPE_CONST_PROPORTIONAL;
        ls.constLimit= 100;
        ls.roundLimitUp = true;
        ass2.limitSettings = ls;

        OptimizationConfig config = new ConfigurableLimitedOptimization(
                ConfigurableLimitedOptimization.TIME, /* optimization */
                ConfigurableLimitedOptimization.TIME, /* secondary optimization */
                ConfigurableLimitedOptimization.USER); /* limit */

        ass2.optimizationConfig = config;

        bs = new BatchSlicer();
        bss = new BatchSlicerSettings(as2, ass2);

        bss.setSchedulingInterval(0, cycleLength);
        bss.sliceAlgorithm = BatchSlicerSettings.defaultOrder;
        bss.sliceAlgorithm = BatchSlicerSettings.criteriaBased;
        bss.spAlgorithmType = "MODIFIED";
        bss.spConceptType = "EXTREME";
        bss.slicesNum = 2;
        bss.shiftAlternatives = true;
    }

    @Override
    public String getData() {
        String data = "USER CRITERIA RELATIONS EXPERIMENT\n";

        data += "\nUSER CRITERIA RELATIONS EXPERIMENT WITHOUT LIMIT\n";
        for (int i = 0; i < relations.length; i++) {
            data += "Stats With " + relations[i] + " Cost criteria\n"
                    + schedulingResults[i].getData() + "\n";
        }
        
        data += "\nUSER CRITERIA RELATIONS EXPERIMENT WITH LIMIT\n";
        for (int i = 0; i < relations.length; i++) {
            data += "Stats With " + relations[i] + " Cost criteria\n"
                    + schedulingResults2[i].getData() + "\n";
        }
        return data;
    }

    
    protected double calculateCriteriaRelation(List<UserJob> batch){
        if(batch == null || batch.isEmpty()){
            return -1d;
        }
        
        int costCriterionCnt = 0;
        for(UserJob job : batch){
            if(job.resourceRequest.criteria instanceof MinSumCostCriteria){
                costCriterionCnt++;
            }
        }
        
        return (double)costCriterionCnt/batch.size();
    }
}
