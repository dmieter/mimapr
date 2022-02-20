package project.experiment.archive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import project.engine.alternativeStats.SchedulingResultsStats;
import project.engine.data.Alternative;
import project.engine.data.DistributedTask;
import project.engine.data.ComputingNode;
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
import project.engine.slot.slotProcessor.SlotProcessorSettings;
import project.engine.slot.slotProcessor.SlotProcessorV2;
import project.engine.slot.slotProcessor.criteriaHelpers.MinFinishTimeCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinRunTimeCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinSumCostCriteria;
import project.engine.slot.slotProcessor.userRankings.PercentileUserRanking;
import project.engine.slot.slotProcessor.userRankings.SimpleUserRanking;
import project.engine.slot.slotProcessor.userRankings.UserRanking;
import project.experiment.Experiment;
import project.math.distributions.HyperGeometricFacade;
import project.math.distributions.HyperGeometricSettings;

/**
 *
 * @author emelyanov
 */
public class BatchSlicerUserPercentaleExperiment extends Experiment {

    protected AlternativeSolverV2 as2;
    protected AlternativeSolverSettingsV2 ass2;

    protected BatchSlicer bs;
    protected BatchSlicerSettings bss;

    SlotProcessorV2 sp;
    SlotProcessorSettings sps;

    protected SchedulingResultsStats[] asStats;
    protected SchedulingResultsStats[] minRuntimeASStats;
    protected SchedulingResultsStats[] minFinishTimeASStats;
    protected SchedulingResultsStats[] minCostASStats;

    protected SchedulingResultsStats[] bsStats;
    protected SchedulingResultsStats[] minRuntimeBSStats;
    protected SchedulingResultsStats[] minFinishTimeBSStats;
    protected SchedulingResultsStats[] minCostBSStats;
    
    /* Stats for BS with pre found alternatives */
    protected SchedulingResultsStats[] bsaStats;
    protected SchedulingResultsStats[] minRuntimeBSAStats;
    protected SchedulingResultsStats[] minFinishTimeBSAStats;
    protected SchedulingResultsStats[] minCostBSAStats;

    protected int cycleLength = 600;
    Double[] userLimits = new Double[8];

    public void flush() {
        as2.flush();
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

    protected void configureExperiment() {
        configureSlotProcessor();
        configureAlternativeSolverV2();
        configureBatchSlicer();

        asStats = new SchedulingResultsStats[8];
        for (int i = 0; i < 8; i++) {
            asStats[i] = new SchedulingResultsStats();
        }

        minRuntimeASStats = new SchedulingResultsStats[8];
        for (int i = 0; i < 8; i++) {
            minRuntimeASStats[i] = new SchedulingResultsStats();
        }

        minFinishTimeASStats = new SchedulingResultsStats[8];
        for (int i = 0; i < 8; i++) {
            minFinishTimeASStats[i] = new SchedulingResultsStats();
        }

        minCostASStats = new SchedulingResultsStats[8];
        for (int i = 0; i < 8; i++) {
            minCostASStats[i] = new SchedulingResultsStats();
        }

        bsStats = new SchedulingResultsStats[8];
        for (int i = 0; i < 8; i++) {
            bsStats[i] = new SchedulingResultsStats();
        }

        minRuntimeBSStats = new SchedulingResultsStats[8];
        for (int i = 0; i < 8; i++) {
            minRuntimeBSStats[i] = new SchedulingResultsStats();
        }

        minFinishTimeBSStats = new SchedulingResultsStats[8];
        for (int i = 0; i < 8; i++) {
            minFinishTimeBSStats[i] = new SchedulingResultsStats();
        }

        minCostBSStats = new SchedulingResultsStats[8];
        for (int i = 0; i < 8; i++) {
            minCostBSStats[i] = new SchedulingResultsStats();
        }
        
        bsaStats = new SchedulingResultsStats[8];
        for (int i = 0; i < 8; i++) {
            bsaStats[i] = new SchedulingResultsStats();
        }

        minRuntimeBSAStats = new SchedulingResultsStats[8];
        for (int i = 0; i < 8; i++) {
            minRuntimeBSAStats[i] = new SchedulingResultsStats();
        }

        minFinishTimeBSAStats = new SchedulingResultsStats[8];
        for (int i = 0; i < 8; i++) {
            minFinishTimeBSAStats[i] = new SchedulingResultsStats();
        }

        minCostBSAStats = new SchedulingResultsStats[8];
        for (int i = 0; i < 8; i++) {
            minCostBSAStats[i] = new SchedulingResultsStats();
        }

    }

    protected void configureSlotProcessor() {
        sp = new SlotProcessorV2();
        sps = new SlotProcessorSettings();

        sps.algorithmConcept = "EXTREME";
        sps.algorithmType = "MODIFIED";
        sps.cycleLength = cycleLength;
        sps.cycleStart = 0;
        sps.countStats = false;
        sps.clean = true;
    }

    protected void configureAlternativeSolverV2() {
        as2 = new AlternativeSolverV2();
        ass2 = new AlternativeSolverSettingsV2();

        ass2.optType = AlternativeSolverSettingsV2.MAX;
        ass2.secondaryOptType = AlternativeSolverSettingsV2.MAX;
        ass2.periodStart = 0;
        ass2.periodEnd = cycleLength;

        LimitSettings ls = new LimitSettings();
        //ls.limitType = LimitSettings.LIMIT_TYPE_AVERAGE;
        ls.limitType = LimitSettings.LIMIT_TYPE_CONST_PROPORTIONAL;
        ls.constLimit = 3;
        ls.limitStepQuotient = 100;      // For each limit we will have 100 steps in incremental mode
        //ls.limitStep = 2d;
        ass2.limitSettings = ls;

        OptimizationConfig config = new ConfigurableLimitedOptimization(
                ConfigurableLimitedOptimization.COST, /* optimization */
                ConfigurableLimitedOptimization.COST, /* secondary optimization */
                ConfigurableLimitedOptimization.USER); /* limit */

        ass2.optimizationConfig = config;
    }

    protected void configureBatchSlicer() {
        bs = new BatchSlicer();
        bss = new BatchSlicerSettings(as2, ass2);

        bss.periodStart = 0;
        bss.periodEnd = cycleLength;

        bss.sliceAlgorithm = BatchSlicerSettings.criteriaBased;

        bss.spAlgorithmType = SlotProcessorSettings.TYPE_MODIFIED;
        bss.spConceptType = SlotProcessorSettings.CONCEPT_EXTREME;

        bss.shiftAlternatives = true;
    }

    protected ArrayList<UserJob> generateJobBatch() {
        JobGenerator jg = new JobGenerator();
        JobGeneratorSettings jgs = new JobGeneratorSettings();
        jgs.taskNumber = 40;

        jgs.minPrice = 1.0;
        jgs.maxPrice = 1.6;
        jgs.useSpeedPriceFactor = true;

        jgs.minTime = 100;
        jgs.maxTime = 500;

        jgs.minSpeed = 1;
        jgs.maxSpeed = 1;

        jgs.minCPU = 2;
        jgs.maxCPU = 6;

        ArrayList<UserJob> jobs = jg.generate(jgs);
        UserRanking ur = new PercentileUserRanking();
        //UserRanking ur = new PercentileUserRanking();
        for (UserJob job : jobs) {
            job.rankingAlgorithm = ur;
        }

        jg.setRandomBatchCriterias(jobs);

        return jobs;
    }

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

        return env;
    }

    private void performSingleExperiment() {
        long t;
        boolean success = true;

        VOEnvironment env = generateNewEnvironment();
        ArrayList<UserJob> batch = generateJobBatch();

        /* prepare alternatives for AS2 */
        ArrayList<UserJob> batchWithAlts = VOEHelper.copyJobBatchList(batch);
        sp.findAlternatives(batchWithAlts, env, sps);
        for (UserJob job : batchWithAlts) {
            job.rankAlternatives();
        }

        /* Just a double check */
        if (VOEHelper.checkBatchIntersectionsWithVOE(batchWithAlts, env)) {
            throw new RuntimeException("Alternatives have intersections with environment!!!");
        }

        
        userLimits[0] = 0.0001d;
        userLimits[1] = 2d;
        userLimits[2] = 4d;
        userLimits[3] = 8d;
        userLimits[4] = 16d;
        userLimits[5] = 32d;
        userLimits[6] = 64d;
        userLimits[7] = 100d;

        Map<SchedulingResultsStats, ArrayList<UserJob>> statBatches = new HashMap<>();

        for (int i = 0; i < 8; i++) {
            
            flush();
            
            /* AS */
            
            ass2.limitSettings.constLimit = userLimits[i];

            //System.out.println("processing AS2 with limit: " + userLimits[i] + "%");
            
            ArrayList<UserJob> asBatch = VOEHelper.copyJobBatchList(batchWithAlts);
            as2.solve(ass2, env, asBatch);

            if (!SchedulingResultsStats.checkBatchForSuccess(asBatch)) {
                System.out.println("AS2 " + userLimits[i] + "% failed to find alternatives");
                asStats[i].failStats.processEnvironment(env);
                asStats[i].addFailExperiment();
                success = false;
                //as2.solve(ass2, env, batch0);
            } else {
                statBatches.put(asStats[i], asBatch);
            }
            
            flush();
            
            /* BS with pre found alternatives */
            //System.out.println("processing BSA with limit: " + userLimits[i] + "%");
            
            ArrayList<UserJob> bsaBatch = VOEHelper.copyJobBatchList(batchWithAlts);
            bss.alternativesAlreadyFound = true;
            bs.solve(bss, env, bsaBatch);

            if (!SchedulingResultsStats.checkBatchForSuccess(bsaBatch)) {
                System.out.println("BSA " + userLimits[i] + "% failed to find alternatives");
                bsaStats[i].failStats.processEnvironment(env);
                bsaStats[i].addFailExperiment();
                success = false;
            } else {
                statBatches.put(bsaStats[i], bsaBatch);
            }
            
            flush();
            
            /* BS */
            //System.out.println("processing BS with limit: " + userLimits[i] + "%");
            
            ArrayList<UserJob> bsBatch = VOEHelper.copyJobBatchList(batch);
            bss.alternativesAlreadyFound = false;
            bs.solve(bss, env, bsBatch);

            if (!SchedulingResultsStats.checkBatchForSuccess(bsBatch)) {
                System.out.println("BS " + userLimits[i] + "% failed to find alternatives");
                bsStats[i].failStats.processEnvironment(env);
                bsStats[i].addFailExperiment();
                success = false;
            } else {
                statBatches.put(bsStats[i], bsBatch);
            }
            

        }

        //ArrayList<UserJob> batch100 = VOEHelper.copyJobBatchList(batchWithAlts);
        //success = getBestVOAlternatives(batch100);

        //Stats
        if (success) {

            for (int i = 0; i < 8; i++) {
                
                /* AS */
                ArrayList<UserJob> curASBatch = statBatches.get(asStats[i]);
                ArrayList<UserJob> minRuntimeASJobs = new ArrayList<UserJob>();
                ArrayList<UserJob> minFinishTimeASJobs = new ArrayList<UserJob>();
                ArrayList<UserJob> minCostASJobs = new ArrayList<UserJob>();

                
                for (UserJob job : curASBatch) {
                    if (job.resourceRequest.criteria instanceof MinSumCostCriteria) {
                        minCostASJobs.add(job);
                    } else if (job.resourceRequest.criteria instanceof MinRunTimeCriteria) {
                        minRuntimeASJobs.add(job);
                    } else if (job.resourceRequest.criteria instanceof MinFinishTimeCriteria) {
                        minFinishTimeASJobs.add(job);
                    }
                }

                asStats[i].processResults(curASBatch);
                minRuntimeASStats[i].processResults(minRuntimeASJobs);
                minFinishTimeASStats[i].processResults(minFinishTimeASJobs);
                minCostASStats[i].processResults(minCostASJobs);
                
                /* BS */
                ArrayList<UserJob> curBSBatch = statBatches.get(bsStats[i]);
                ArrayList<UserJob> minRuntimeBSJobs = new ArrayList<UserJob>();
                ArrayList<UserJob> minFinishTimeBSJobs = new ArrayList<UserJob>();
                ArrayList<UserJob> minCostBSJobs = new ArrayList<UserJob>();

                
                for (UserJob job : curBSBatch) {
                    if (job.resourceRequest.criteria instanceof MinSumCostCriteria) {
                        minCostBSJobs.add(job);
                    } else if (job.resourceRequest.criteria instanceof MinRunTimeCriteria) {
                        minRuntimeBSJobs.add(job);
                    } else if (job.resourceRequest.criteria instanceof MinFinishTimeCriteria) {
                        minFinishTimeBSJobs.add(job);
                    }
                }

                bsStats[i].processResults(curBSBatch);
                minRuntimeBSStats[i].processResults(minRuntimeBSJobs);
                minFinishTimeBSStats[i].processResults(minFinishTimeBSJobs);
                minCostBSStats[i].processResults(minCostBSJobs);
                
                
                /* BSA */
                ArrayList<UserJob> curBSABatch = statBatches.get(bsaStats[i]);
                ArrayList<UserJob> minRuntimeBSAJobs = new ArrayList<UserJob>();
                ArrayList<UserJob> minFinishTimeBSAJobs = new ArrayList<UserJob>();
                ArrayList<UserJob> minCostBSAJobs = new ArrayList<UserJob>();

                
                for (UserJob job : curBSABatch) {
                    if (job.resourceRequest.criteria instanceof MinSumCostCriteria) {
                        minCostBSAJobs.add(job);
                    } else if (job.resourceRequest.criteria instanceof MinRunTimeCriteria) {
                        minRuntimeBSAJobs.add(job);
                    } else if (job.resourceRequest.criteria instanceof MinFinishTimeCriteria) {
                        minFinishTimeBSAJobs.add(job);
                    }
                }

                bsaStats[i].processResults(curBSABatch);
                minRuntimeBSAStats[i].processResults(minRuntimeBSAJobs);
                minFinishTimeBSAStats[i].processResults(minFinishTimeBSAJobs);
                minCostBSAStats[i].processResults(minCostBSAJobs);
                
            }

        }
    }

    @Override
    public String getData() {
        String data = "ALTERNATIVE SOLVER USER EXPERIMENT\n";
        
            for(int i=0;i<userLimits.length;i++){
                data+=
                "ALTERNATIVE SOLVER Stats With "+userLimits[i]+" Limit\n"
                + this.asStats[i].getData() + "\n"
                + "RUNTIME Stats With "+userLimits[i]+" Limit\n"
                + this.minRuntimeASStats[i].getData() + "\n"
                + "FINISH TIME Stats With "+userLimits[i]+" Limit\n"
                + this.minFinishTimeASStats[i].getData() + "\n"
                + "COST Stats With "+userLimits[i]+" Limit\n"
                + this.minCostASStats[i].getData() + "\n"
                + "/*****************************************/ \n";
                
                data+=
                "BATCH SLICER with Pre-found Alternatives Stats With "+userLimits[i]+" Limit\n"
                + this.bsaStats[i].getData() + "\n"
                + "RUNTIME Stats With "+userLimits[i]+" Limit\n"
                + this.minRuntimeBSAStats[i].getData() + "\n"
                + "FINISH TIME Stats With "+userLimits[i]+" Limit\n"
                + this.minFinishTimeBSAStats[i].getData() + "\n"
                + "COST Stats With "+userLimits[i]+" Limit\n"
                + this.minCostBSAStats[i].getData() + "\n"
                + "/*****************************************/ \n";
                
                data+=
                "BATCH SLICER Stats With "+userLimits[i]+" Limit\n"
                + this.bsStats[i].getData() + "\n"
                + "RUNTIME Stats With "+userLimits[i]+" Limit\n"
                + this.minRuntimeBSStats[i].getData() + "\n"
                + "FINISH TIME Stats With "+userLimits[i]+" Limit\n"
                + this.minFinishTimeBSStats[i].getData() + "\n"
                + "COST Stats With "+userLimits[i]+" Limit\n"
                + this.minCostBSStats[i].getData() + "\n"
                + "/*****************************************/ \n";
                
            }
            
        return data;
    }

    private boolean getBestVOAlternatives(ArrayList<UserJob> batch) {

        /* Min TIME */
//        for(UserJob job : batch){
//            double minTime = Double.POSITIVE_INFINITY;
//            
//            for(Alternative a : job.alternatives){
//                if(a.getLength() < minTime){
//                    minTime = a.getLength();
//                    job.bestAlternative = a.num;
//                }
//            }
//        }
        /* Max COST */
        for (UserJob job : batch) {
            double maxCost = Double.NEGATIVE_INFINITY;

            for (Alternative a : job.alternatives) {
                if (a.getCost() > maxCost) {
                    maxCost = a.getCost();
                    job.bestAlternative = a.num;
                }
            }
        }

        return true;
    }

}
