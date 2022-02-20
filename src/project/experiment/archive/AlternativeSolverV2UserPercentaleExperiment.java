package project.experiment.archive;

import java.util.ArrayList;
import java.util.List;
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
public class AlternativeSolverV2UserPercentaleExperiment extends Experiment {
    
    protected AlternativeSolverV2 as2;
    protected AlternativeSolverSettingsV2 ass2;
    
    protected BatchSlicer bs;
    protected BatchSlicerSettings bss;
    
    SlotProcessorV2 sp;
    SlotProcessorSettings sps;
    
    protected SchedulingResultsStats as2User100Stats;
    protected SchedulingResultsStats minRuntimeUser100Stats;
    protected SchedulingResultsStats minFinishTimeUser100Stats;
    protected SchedulingResultsStats minCostUser100Stats;
    
    protected SchedulingResultsStats as2User0Stats;
    protected SchedulingResultsStats minRuntimeUser0Stats;
    protected SchedulingResultsStats minFinishTimeUser0Stats;
    protected SchedulingResultsStats minCostUser0Stats;
    
    protected SchedulingResultsStats as2User7Stats;
    protected SchedulingResultsStats minRuntimeUser7Stats;
    protected SchedulingResultsStats minFinishTimeUser7Stats;
    protected SchedulingResultsStats minCostUser7Stats;
    
    protected SchedulingResultsStats as2User10Stats;
    protected SchedulingResultsStats minRuntimeUser10Stats;
    protected SchedulingResultsStats minFinishTimeUser10Stats;
    protected SchedulingResultsStats minCostUser10Stats;
    
    
    protected SchedulingResultsStats as2User20Stats;
    protected SchedulingResultsStats minRuntimeUser20Stats;
    protected SchedulingResultsStats minFinishTimeUser20Stats;
    protected SchedulingResultsStats minCostUser20Stats;
    
    
    protected SchedulingResultsStats as2User4Stats;
    protected SchedulingResultsStats minRuntimeUser4Stats;
    protected SchedulingResultsStats minFinishTimeUser4Stats;
    protected SchedulingResultsStats minCostUser4Stats;
    
    protected SchedulingResultsStats as2User2Stats;
    protected SchedulingResultsStats minRuntimeUser2Stats;
    protected SchedulingResultsStats minFinishTimeUser2Stats;
    protected SchedulingResultsStats minCostUser2Stats;
    
    protected int cycleLength = 600;
    
    public void flush(){
        as2.flush();
    }

    @Override
    public void performExperiments(int expNum) {
        configureExperiment();
        for(int i=0; i<expNum;i++){
            System.out.println("--------------Experiment #"+i+" -------------------");
            flush();
            performSingleExperiment();
        }
    }

    protected void configureExperiment() {
        configureSlotProcessor();
        configureAlternativeSolverV2();
        configureBatchSlicer();
        
        as2User100Stats = new SchedulingResultsStats();
        minRuntimeUser100Stats = new SchedulingResultsStats();
        minFinishTimeUser100Stats = new SchedulingResultsStats();
        minCostUser100Stats = new SchedulingResultsStats();
        
        as2User0Stats = new SchedulingResultsStats();
        minRuntimeUser0Stats = new SchedulingResultsStats();
        minFinishTimeUser0Stats = new SchedulingResultsStats();
        minCostUser0Stats = new SchedulingResultsStats();
        
        as2User2Stats = new SchedulingResultsStats();
        minRuntimeUser2Stats = new SchedulingResultsStats();
        minFinishTimeUser2Stats = new SchedulingResultsStats();
        minCostUser2Stats = new SchedulingResultsStats();
        
        as2User4Stats = new SchedulingResultsStats();
        minRuntimeUser4Stats = new SchedulingResultsStats();
        minFinishTimeUser4Stats = new SchedulingResultsStats();
        minCostUser4Stats = new SchedulingResultsStats();
        
        as2User20Stats = new SchedulingResultsStats();
        minRuntimeUser20Stats = new SchedulingResultsStats();
        minFinishTimeUser20Stats = new SchedulingResultsStats();
        minCostUser20Stats = new SchedulingResultsStats();
        
        as2User7Stats = new SchedulingResultsStats();
        minRuntimeUser7Stats = new SchedulingResultsStats();
        minFinishTimeUser7Stats = new SchedulingResultsStats();
        minCostUser7Stats = new SchedulingResultsStats();
        
        as2User10Stats = new SchedulingResultsStats();
        minRuntimeUser10Stats = new SchedulingResultsStats();
        minFinishTimeUser10Stats = new SchedulingResultsStats();
        minCostUser10Stats = new SchedulingResultsStats();
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
                                            ConfigurableLimitedOptimization.COST,  /* optimization */
                                            ConfigurableLimitedOptimization.COST,  /* secondary optimization */    
                                            ConfigurableLimitedOptimization.USER); /* limit */
        
        ass2.optimizationConfig = config;
    }
    
    protected void configureBatchSlicer(){
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
        for(UserJob job : jobs){
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
        
        /*TEST*/
        //bs.solve(bss, env, batch);
        /*END OF TEST*/
        
        sp.findAlternatives(batch, env, sps);
        for(UserJob job : batch){
            job.rankAlternatives();
        }
        int b1 = 0;

        /* Just a double check */
        if(VOEHelper.checkBatchIntersectionsWithVOE(batch, env)){
            throw new RuntimeException("Alternatives have intersections with environment!!!");
        }
        
        /* AS2 with 100% (No limit) - any alternative is ok */
        //ass2.limitSettings.constLimit = 5*batch.size();
        
        ArrayList<UserJob> batch100 = VOEHelper.copyJobBatchList(batch);
        success = getBestVOAlternatives(batch100);
        
        /*for(UserJob j : batch100){
            Alternative ab = j.getBestAlternative();
            Alternative a1 = j.alternatives.get(0);
            Alternative a2 = j.alternatives.get(j.alternatives.size()-1);
            System.out.println("First: "+a1.getCost() + " Best: " + ab.getCost() + " Last: " + a2.getCost()+" Perc: " + ab.getUserRating());
        }*/
        
        /* AS2 with 0 User Limit - only first alternatives */
        ass2.limitSettings.constLimit = 0.0001;
        
        ArrayList<UserJob> batch0 = VOEHelper.copyJobBatchList(batch);
        as2.solve(ass2, env, batch0);
        
        if(!SchedulingResultsStats.checkBatchForSuccess(batch0)){
            System.out.println("AS2 0% failed to find alternatives");
            as2User0Stats.failStats.processEnvironment(env);
            as2User0Stats.addFailExperiment();
            success = false;
            as2.solve(ass2, env, batch0);
        }
        
        /* AS2 with 2% User Limit*/
        ass2.limitSettings.constLimit = 2; /* 2% - proportional limit */
        
        ArrayList<UserJob> batch2 = VOEHelper.copyJobBatchList(batch);
        as2.solve(ass2, env, batch2);
        
        if(!SchedulingResultsStats.checkBatchForSuccess(batch2)){
            System.out.println("AS2 2% failed to find alternatives");
            as2User2Stats.failStats.processEnvironment(env);
            as2User2Stats.addFailExperiment();
            success = false;
        }
        
        /* AS2 with 4% User Limit */
        ass2.limitSettings.constLimit = 4;
        
        ArrayList<UserJob> batch4 = VOEHelper.copyJobBatchList(batch);
        as2.solve(ass2, env, batch4);
        
        if(!SchedulingResultsStats.checkBatchForSuccess(batch4)){
            System.out.println("AS2 4% failed to find alternatives");
            as2User4Stats.failStats.processEnvironment(env);
            as2User4Stats.addFailExperiment();
            success = false;
        }
        
         /* AS2 with 7% User Limit */
        ass2.limitSettings.constLimit = 8;
        
        ArrayList<UserJob> batch7 = VOEHelper.copyJobBatchList(batch);
        as2.solve(ass2, env, batch7);
        
        if(!SchedulingResultsStats.checkBatchForSuccess(batch7)){
            System.out.println("AS2 7% failed to find alternatives");
            as2User7Stats.failStats.processEnvironment(env);
            as2User7Stats.addFailExperiment();
            success = false;
        }
        
        /* AS2 with 10% User Limit */
        ass2.limitSettings.constLimit = 16;
        
        ArrayList<UserJob> batch10 = VOEHelper.copyJobBatchList(batch);
        t = System.nanoTime();
        as2.solve(ass2, env, batch10);
        t = System.nanoTime() - t;
        
        
        if(!SchedulingResultsStats.checkBatchForSuccess(batch10)){
            System.out.println("AS2 10% failed to find alternatives");
            as2User10Stats.failStats.processEnvironment(env);
            as2User10Stats.addFailExperiment();
            success = false;
        }
        
        /* AS2 with 20% User Limit */
        ass2.limitSettings.constLimit = 32;
        
        ArrayList<UserJob> batch20 = VOEHelper.copyJobBatchList(batch);
        as2.solve(ass2, env, batch20);
        
        if(!SchedulingResultsStats.checkBatchForSuccess(batch20)){
            System.out.println("AS2 20% failed to find alternatives");
            as2User20Stats.failStats.processEnvironment(env);
            as2User20Stats.addFailExperiment();
            success = false;
        }
        
        
        
        //Stats
        if(success){
            
            ArrayList<UserJob> minRuntimeUser100Jobs = new ArrayList<UserJob>();
            ArrayList<UserJob> minFinishTimeUser100Jobs = new ArrayList<UserJob>();
            ArrayList<UserJob> minCostUser100Jobs = new ArrayList<UserJob>();
            
            for(UserJob job : batch100){
                if(job.resourceRequest.criteria instanceof MinSumCostCriteria){
                    minCostUser100Jobs.add(job);
                }else if(job.resourceRequest.criteria instanceof MinRunTimeCriteria){
                    minRuntimeUser100Jobs.add(job);
                }else if(job.resourceRequest.criteria instanceof MinFinishTimeCriteria){
                    minFinishTimeUser100Jobs.add(job);
                }
            }
            
            as2User100Stats.processResults(batch100, t);
            minRuntimeUser100Stats.processResults(minRuntimeUser100Jobs, t);
            minFinishTimeUser100Stats.processResults(minFinishTimeUser100Jobs, t);
            minCostUser100Stats.processResults(minCostUser100Jobs, t);
            
            ArrayList<UserJob> minRuntimeUser0Jobs = new ArrayList<UserJob>();
            ArrayList<UserJob> minFinishTimeUser0Jobs = new ArrayList<UserJob>();
            ArrayList<UserJob> minCostUser0Jobs = new ArrayList<UserJob>();
            
            for(UserJob job : batch0){
                if(job.resourceRequest.criteria instanceof MinSumCostCriteria){
                    minCostUser0Jobs.add(job);
                }else if(job.resourceRequest.criteria instanceof MinRunTimeCriteria){
                    minRuntimeUser0Jobs.add(job);
                }else if(job.resourceRequest.criteria instanceof MinFinishTimeCriteria){
                    minFinishTimeUser0Jobs.add(job);
                }
            }
            
            as2User0Stats.processResults(batch0);
            minRuntimeUser0Stats.processResults(minRuntimeUser0Jobs, t);
            minFinishTimeUser0Stats.processResults(minFinishTimeUser0Jobs, t);
            minCostUser0Stats.processResults(minCostUser0Jobs, t);
            
            
            ArrayList<UserJob> minRuntimeUser2Jobs = new ArrayList<UserJob>();
            ArrayList<UserJob> minFinishTimeUser2Jobs = new ArrayList<UserJob>();
            ArrayList<UserJob> minCostUser2Jobs = new ArrayList<UserJob>();
            
            for(UserJob job : batch2){
                if(job.resourceRequest.criteria instanceof MinSumCostCriteria){
                    minCostUser2Jobs.add(job);
                }else if(job.resourceRequest.criteria instanceof MinRunTimeCriteria){
                    minRuntimeUser2Jobs.add(job);
                }else if(job.resourceRequest.criteria instanceof MinFinishTimeCriteria){
                    minFinishTimeUser2Jobs.add(job);
                }
            }
            
            as2User2Stats.processResults(batch2);
            minRuntimeUser2Stats.processResults(minRuntimeUser2Jobs, t);
            minFinishTimeUser2Stats.processResults(minFinishTimeUser2Jobs, t);
            minCostUser2Stats.processResults(minCostUser2Jobs, t);
            
            
            ArrayList<UserJob> minRuntimeUser4Jobs = new ArrayList<UserJob>();
            ArrayList<UserJob> minFinishTimeUser4Jobs = new ArrayList<UserJob>();
            ArrayList<UserJob> minCostUser4Jobs = new ArrayList<UserJob>();
            
            for(UserJob job : batch4){
                if(job.resourceRequest.criteria instanceof MinSumCostCriteria){
                    minCostUser4Jobs.add(job);
                }else if(job.resourceRequest.criteria instanceof MinRunTimeCriteria){
                    minRuntimeUser4Jobs.add(job);
                }else if(job.resourceRequest.criteria instanceof MinFinishTimeCriteria){
                    minFinishTimeUser4Jobs.add(job);
                }
            }
            
            as2User4Stats.processResults(batch4);
            minRuntimeUser4Stats.processResults(minRuntimeUser4Jobs, t);
            minFinishTimeUser4Stats.processResults(minFinishTimeUser4Jobs, t);
            minCostUser4Stats.processResults(minCostUser4Jobs, t);
            
            
            ArrayList<UserJob> minRuntimeUser20Jobs = new ArrayList<UserJob>();
            ArrayList<UserJob> minFinishTimeUser20Jobs = new ArrayList<UserJob>();
            ArrayList<UserJob> minCostUser20Jobs = new ArrayList<UserJob>();
            
            for(UserJob job : batch20){
                if(job.resourceRequest.criteria instanceof MinSumCostCriteria){
                    minCostUser20Jobs.add(job);
                }else if(job.resourceRequest.criteria instanceof MinRunTimeCriteria){
                    minRuntimeUser20Jobs.add(job);
                }else if(job.resourceRequest.criteria instanceof MinFinishTimeCriteria){
                    minFinishTimeUser20Jobs.add(job);
                }
            }
            
            as2User20Stats.processResults(batch20);
            minRuntimeUser20Stats.processResults(minRuntimeUser20Jobs, t);
            minFinishTimeUser20Stats.processResults(minFinishTimeUser20Jobs, t);
            minCostUser20Stats.processResults(minCostUser20Jobs, t);
            
            
            
            ArrayList<UserJob> minRuntimeUser7Jobs = new ArrayList<UserJob>();
            ArrayList<UserJob> minFinishTimeUser7Jobs = new ArrayList<UserJob>();
            ArrayList<UserJob> minCostUser7Jobs = new ArrayList<UserJob>();
            
            for(UserJob job : batch7){
                if(job.resourceRequest.criteria instanceof MinSumCostCriteria){
                    minCostUser7Jobs.add(job);
                }else if(job.resourceRequest.criteria instanceof MinRunTimeCriteria){
                    minRuntimeUser7Jobs.add(job);
                }else if(job.resourceRequest.criteria instanceof MinFinishTimeCriteria){
                    minFinishTimeUser7Jobs.add(job);
                }
            }
            
            as2User7Stats.processResults(batch7);
            minRuntimeUser7Stats.processResults(minRuntimeUser7Jobs, t);
            minFinishTimeUser7Stats.processResults(minFinishTimeUser7Jobs, t);
            minCostUser7Stats.processResults(minCostUser7Jobs, t);
            
            
            
            ArrayList<UserJob> minRuntimeUser10Jobs = new ArrayList<UserJob>();
            ArrayList<UserJob> minFinishTimeUser10Jobs = new ArrayList<UserJob>();
            ArrayList<UserJob> minCostUser10Jobs = new ArrayList<UserJob>();
            
            for(UserJob job : batch10){
                if(job.resourceRequest.criteria instanceof MinSumCostCriteria){
                    minCostUser10Jobs.add(job);
                }else if(job.resourceRequest.criteria instanceof MinRunTimeCriteria){
                    minRuntimeUser10Jobs.add(job);
                }else if(job.resourceRequest.criteria instanceof MinFinishTimeCriteria){
                    minFinishTimeUser10Jobs.add(job);
                }
            }
            
            as2User10Stats.processResults(batch10);
            minRuntimeUser10Stats.processResults(minRuntimeUser10Jobs, t);
            minFinishTimeUser10Stats.processResults(minFinishTimeUser10Jobs, t);
            minCostUser10Stats.processResults(minCostUser10Jobs, t);
            
        }
    }
    
    @Override
    public String getData() {
        String data = "ALTERNATIVE SOLVER USER EXPERIMENT\n"
                +"Alternative Solver Stats With 0 Limit\n"
                +this.as2User0Stats.getData()+"\n"
                +"RUNTIME Stats With 0 Limit\n"
                +this.minRuntimeUser0Stats.getData()+"\n"
                +"FINISH TIME Stats With 0 Limit\n"
                +this.minFinishTimeUser0Stats.getData()+"\n"
                +"COST Stats With 0 Limit\n"
                +this.minCostUser0Stats.getData()+"\n"
                +"/*****************************************/ \n"
                +"Alternative Solver Stats With 2% Limit\n"
                +this.as2User2Stats.getData()+"\n"
                +"RUNTIME Stats With 2% Limit\n"
                +this.minRuntimeUser2Stats.getData()+"\n"
                +"FINISH TIME Stats With 2% Limit\n"
                +this.minFinishTimeUser2Stats.getData()+"\n"
                +"COST Stats With 2% Limit\n"
                +this.minCostUser2Stats.getData()+"\n"
                +"/*****************************************/ \n"
                +"Alternative Solver Stats With 4% Limit\n"
                +this.as2User4Stats.getData()+"\n"
                +"RUNTIME Stats With 4% Limit\n"
                +this.minRuntimeUser4Stats.getData()+"\n"
                +"FINISH TIME Stats With 4% Limit\n"
                +this.minFinishTimeUser4Stats.getData()+"\n"
                +"COST Stats With 4% Limit\n"
                +this.minCostUser4Stats.getData()+"\n"
                +"/*****************************************/ \n"
                +"Alternative Solver Stats With 7% Limit\n"
                +this.as2User7Stats.getData()+"\n"
                +"RUNTIME Stats With 7% Limit\n"
                +this.minRuntimeUser7Stats.getData()+"\n"
                +"FINISH TIME Stats With 7% Limit\n"
                +this.minFinishTimeUser7Stats.getData()+"\n"
                +"COST Stats With 7% Limit\n"
                +this.minCostUser7Stats.getData()+"\n"
                +"/*****************************************/ \n"
                +"Alternative Solver Stats With 10% Limit\n"
                +this.as2User10Stats.getData()+"\n"
                +"RUNTIME Stats With 10% Limit\n"
                +this.minRuntimeUser10Stats.getData()+"\n"
                +"FINISH TIME Stats With 10% Limit\n"
                +this.minFinishTimeUser10Stats.getData()+"\n"
                +"COST Stats With 10% Limit\n"
                +this.minCostUser10Stats.getData()+"\n"
                +"/*****************************************/ \n"
                +"Alternative Solver Stats With 20% Limit\n"
                +this.as2User20Stats.getData()+"\n"
                +"RUNTIME Stats With 20% Limit\n"
                +this.minRuntimeUser20Stats.getData()+"\n"
                +"FINISH TIME Stats With 20% Limit\n"
                +this.minFinishTimeUser20Stats.getData()+"\n"
                +"COST Stats With 20% Limit\n"
                +this.minCostUser20Stats.getData()+"\n"
                +"/*****************************************/ \n"
                +"Alternative Solver Stats Without User Limit\n"
                +this.as2User100Stats.getData()+"\n"
                +"RUNTIME Stats Without User Limit\n"
                +this.minRuntimeUser100Stats.getData()+"\n"
                +"FINISH TIME Stats Without User Limit\n"
                +this.minFinishTimeUser100Stats.getData()+"\n"
                +"COST Stats Without User Limit\n"
                +this.minCostUser100Stats.getData()+"\n";
        
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
        for(UserJob job : batch){
            double maxCost = Double.NEGATIVE_INFINITY;
            
            for(Alternative a : job.alternatives){
                if(a.getCost() > maxCost){
                    maxCost = a.getCost();
                    job.bestAlternative = a.num;
                }
            }
        }
        
        return true;
    }


}