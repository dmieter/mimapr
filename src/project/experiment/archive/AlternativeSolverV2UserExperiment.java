
package project.experiment.archive;

import java.util.ArrayList;
import java.util.List;
import project.engine.alternativeStats.SchedulingResultsStats;
import project.engine.data.Alternative;
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
public class AlternativeSolverV2UserExperiment extends Experiment {
    
    protected AlternativeSolverV2 as2;
    protected AlternativeSolverSettingsV2 ass2;
    
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
    
    protected SchedulingResultsStats as2User1Stats;
    protected SchedulingResultsStats minRuntimeUser1Stats;
    protected SchedulingResultsStats minFinishTimeUser1Stats;
    protected SchedulingResultsStats minCostUser1Stats;
    
    protected SchedulingResultsStats as2User2Stats;
    protected SchedulingResultsStats minRuntimeUser2Stats;
    protected SchedulingResultsStats minFinishTimeUser2Stats;
    protected SchedulingResultsStats minCostUser2Stats;
    
    
    protected SchedulingResultsStats as2User05Stats;
    protected SchedulingResultsStats minRuntimeUser05Stats;
    protected SchedulingResultsStats minFinishTimeUser05Stats;
    protected SchedulingResultsStats minCostUser05Stats;
    
    
    protected SchedulingResultsStats as2User025Stats;
    protected SchedulingResultsStats minRuntimeUser025Stats;
    protected SchedulingResultsStats minFinishTimeUser025Stats;
    protected SchedulingResultsStats minCostUser025Stats;
    
    protected SchedulingResultsStats as2User01Stats;
    protected SchedulingResultsStats minRuntimeUser01Stats;
    protected SchedulingResultsStats minFinishTimeUser01Stats;
    protected SchedulingResultsStats minCostUser01Stats;
    
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
        
        as2User100Stats = new SchedulingResultsStats();
        minRuntimeUser100Stats = new SchedulingResultsStats();
        minFinishTimeUser100Stats = new SchedulingResultsStats();
        minCostUser100Stats = new SchedulingResultsStats();
        
        as2User0Stats = new SchedulingResultsStats();
        minRuntimeUser0Stats = new SchedulingResultsStats();
        minFinishTimeUser0Stats = new SchedulingResultsStats();
        minCostUser0Stats = new SchedulingResultsStats();
        
        as2User01Stats = new SchedulingResultsStats();
        minRuntimeUser01Stats = new SchedulingResultsStats();
        minFinishTimeUser01Stats = new SchedulingResultsStats();
        minCostUser01Stats = new SchedulingResultsStats();
        
        as2User025Stats = new SchedulingResultsStats();
        minRuntimeUser025Stats = new SchedulingResultsStats();
        minFinishTimeUser025Stats = new SchedulingResultsStats();
        minCostUser025Stats = new SchedulingResultsStats();
        
        as2User05Stats = new SchedulingResultsStats();
        minRuntimeUser05Stats = new SchedulingResultsStats();
        minFinishTimeUser05Stats = new SchedulingResultsStats();
        minCostUser05Stats = new SchedulingResultsStats();
        
        as2User1Stats = new SchedulingResultsStats();
        minRuntimeUser1Stats = new SchedulingResultsStats();
        minFinishTimeUser1Stats = new SchedulingResultsStats();
        minCostUser1Stats = new SchedulingResultsStats();
        
        as2User2Stats = new SchedulingResultsStats();
        minRuntimeUser2Stats = new SchedulingResultsStats();
        minFinishTimeUser2Stats = new SchedulingResultsStats();
        minCostUser2Stats = new SchedulingResultsStats();
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
        ls.limitType = LimitSettings.LIMIT_TYPE_CONST;
        ls.constLimit = 3;
        ls.limitStep = 1d;
        ass2.limitSettings = ls;
        
        OptimizationConfig config = new ConfigurableLimitedOptimization(
                                            ConfigurableLimitedOptimization.COST,  /* optimization */
                                            ConfigurableLimitedOptimization.COST,  /* secondary optimization */    
                                            ConfigurableLimitedOptimization.USER); /* limit */
        
        ass2.optimizationConfig = config;
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
        UserRanking ur = new SimpleUserRanking();
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
        
        sp.findAlternatives(batch, env, sps);
        for(UserJob job : batch){
            job.rankAlternatives();
        }
        
        
        /* AS2 with 100 (almost no limit) */
        ass2.limitSettings.constLimit = 5*batch.size();
        
        ArrayList<UserJob> batch100 = VOEHelper.copyJobBatchList(batch);
         success = getBestVOAlternatives(batch100);
        
        
        /* AS2 with 0 User Limit - only first alternatives */
        ass2.limitSettings.constLimit = 0;
        
        ArrayList<UserJob> batch0 = VOEHelper.copyJobBatchList(batch);
        as2.solve(ass2, env, batch0);
        
        if(!SchedulingResultsStats.checkBatchForSuccess(batch0)){
            System.out.println("AS2 0 failed to find alternatives");
            as2User0Stats.failStats.processEnvironment(env);
            as2User0Stats.addFailExperiment();
            success = false;
        }
        
        /* AS2 with 0.1 User Limit - at average 2nd alternatives */
        ass2.limitSettings.constLimit = 0.1*batch.size();
        
        ArrayList<UserJob> batch01 = VOEHelper.copyJobBatchList(batch);
        as2.solve(ass2, env, batch01);
        
        if(!SchedulingResultsStats.checkBatchForSuccess(batch01)){
            System.out.println("AS2 01 failed to find alternatives");
            as2User01Stats.failStats.processEnvironment(env);
            as2User01Stats.addFailExperiment();
            success = false;
        }
        
        /* AS2 with 0.25 User Limit */
        ass2.limitSettings.constLimit = 0.25*batch.size();
        
        ArrayList<UserJob> batch025 = VOEHelper.copyJobBatchList(batch);
        as2.solve(ass2, env, batch025);
        
        if(!SchedulingResultsStats.checkBatchForSuccess(batch025)){
            System.out.println("AS2 025 failed to find alternatives");
            as2User025Stats.failStats.processEnvironment(env);
            as2User025Stats.addFailExperiment();
            success = false;
        }
        
        /* AS2 with 0.5 User Limit - at average 1st and 2nd alternatives */
        ass2.limitSettings.constLimit = 0.5*batch.size();
        
        ArrayList<UserJob> batch05 = VOEHelper.copyJobBatchList(batch);
        as2.solve(ass2, env, batch05);
        
        if(!SchedulingResultsStats.checkBatchForSuccess(batch05)){
            System.out.println("AS2 05 failed to find alternatives");
            as2User05Stats.failStats.processEnvironment(env);
            as2User05Stats.addFailExperiment();
            success = false;
        }
        
        /* AS2 with 1 User Limit - at average 2nd alternatives */
        ass2.limitSettings.constLimit = 1*batch.size();
        
        ArrayList<UserJob> batch1 = VOEHelper.copyJobBatchList(batch);
        as2.solve(ass2, env, batch1);
        
        if(!SchedulingResultsStats.checkBatchForSuccess(batch1)){
            System.out.println("AS2 1 failed to find alternatives");
            as2User1Stats.failStats.processEnvironment(env);
            as2User1Stats.addFailExperiment();
            success = false;
        }
        
        /* AS2 with 2 User Limit - at average 3d alternatives */
        ass2.limitSettings.constLimit = 2*batch.size();
        
        ArrayList<UserJob> batch2 = VOEHelper.copyJobBatchList(batch);
        t = System.nanoTime();
        as2.solve(ass2, env, batch2);
        t = System.nanoTime() - t;

        
        if(!SchedulingResultsStats.checkBatchForSuccess(batch2)){
            System.out.println("AS2 2 failed to find alternatives");
            as2User2Stats.failStats.processEnvironment(env);
            as2User2Stats.addFailExperiment();
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
            
            
            ArrayList<UserJob> minRuntimeUser01Jobs = new ArrayList<UserJob>();
            ArrayList<UserJob> minFinishTimeUser01Jobs = new ArrayList<UserJob>();
            ArrayList<UserJob> minCostUser01Jobs = new ArrayList<UserJob>();
            
            for(UserJob job : batch01){
                if(job.resourceRequest.criteria instanceof MinSumCostCriteria){
                    minCostUser01Jobs.add(job);
                }else if(job.resourceRequest.criteria instanceof MinRunTimeCriteria){
                    minRuntimeUser01Jobs.add(job);
                }else if(job.resourceRequest.criteria instanceof MinFinishTimeCriteria){
                    minFinishTimeUser01Jobs.add(job);
                }
            }
            
            as2User01Stats.processResults(batch01);
            minRuntimeUser01Stats.processResults(minRuntimeUser01Jobs, t);
            minFinishTimeUser01Stats.processResults(minFinishTimeUser01Jobs, t);
            minCostUser01Stats.processResults(minCostUser01Jobs, t);
            
            
            ArrayList<UserJob> minRuntimeUser025Jobs = new ArrayList<UserJob>();
            ArrayList<UserJob> minFinishTimeUser025Jobs = new ArrayList<UserJob>();
            ArrayList<UserJob> minCostUser025Jobs = new ArrayList<UserJob>();
            
            for(UserJob job : batch025){
                if(job.resourceRequest.criteria instanceof MinSumCostCriteria){
                    minCostUser025Jobs.add(job);
                }else if(job.resourceRequest.criteria instanceof MinRunTimeCriteria){
                    minRuntimeUser025Jobs.add(job);
                }else if(job.resourceRequest.criteria instanceof MinFinishTimeCriteria){
                    minFinishTimeUser025Jobs.add(job);
                }
            }
            
            as2User025Stats.processResults(batch025);
            minRuntimeUser025Stats.processResults(minRuntimeUser025Jobs, t);
            minFinishTimeUser025Stats.processResults(minFinishTimeUser025Jobs, t);
            minCostUser025Stats.processResults(minCostUser025Jobs, t);
            
            
            ArrayList<UserJob> minRuntimeUser05Jobs = new ArrayList<UserJob>();
            ArrayList<UserJob> minFinishTimeUser05Jobs = new ArrayList<UserJob>();
            ArrayList<UserJob> minCostUser05Jobs = new ArrayList<UserJob>();
            
            for(UserJob job : batch05){
                if(job.resourceRequest.criteria instanceof MinSumCostCriteria){
                    minCostUser05Jobs.add(job);
                }else if(job.resourceRequest.criteria instanceof MinRunTimeCriteria){
                    minRuntimeUser05Jobs.add(job);
                }else if(job.resourceRequest.criteria instanceof MinFinishTimeCriteria){
                    minFinishTimeUser05Jobs.add(job);
                }
            }
            
            as2User05Stats.processResults(batch05);
            minRuntimeUser05Stats.processResults(minRuntimeUser05Jobs, t);
            minFinishTimeUser05Stats.processResults(minFinishTimeUser05Jobs, t);
            minCostUser05Stats.processResults(minCostUser05Jobs, t);
            
            
            
            ArrayList<UserJob> minRuntimeUser1Jobs = new ArrayList<UserJob>();
            ArrayList<UserJob> minFinishTimeUser1Jobs = new ArrayList<UserJob>();
            ArrayList<UserJob> minCostUser1Jobs = new ArrayList<UserJob>();
            
            for(UserJob job : batch1){
                if(job.resourceRequest.criteria instanceof MinSumCostCriteria){
                    minCostUser1Jobs.add(job);
                }else if(job.resourceRequest.criteria instanceof MinRunTimeCriteria){
                    minRuntimeUser1Jobs.add(job);
                }else if(job.resourceRequest.criteria instanceof MinFinishTimeCriteria){
                    minFinishTimeUser1Jobs.add(job);
                }
            }
            
            as2User1Stats.processResults(batch1);
            minRuntimeUser1Stats.processResults(minRuntimeUser1Jobs, t);
            minFinishTimeUser1Stats.processResults(minFinishTimeUser1Jobs, t);
            minCostUser1Stats.processResults(minCostUser1Jobs, t);
            
            
            
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
                +"Alternative Solver Stats With 0.1 Limit\n"
                +this.as2User01Stats.getData()+"\n"
                +"RUNTIME Stats With 0.1 Limit\n"
                +this.minRuntimeUser01Stats.getData()+"\n"
                +"FINISH TIME Stats With 0.1 Limit\n"
                +this.minFinishTimeUser01Stats.getData()+"\n"
                +"COST Stats With 0.1 Limit\n"
                +this.minCostUser01Stats.getData()+"\n"
                +"/*****************************************/ \n"
                +"Alternative Solver Stats With 0.25 Limit\n"
                +this.as2User025Stats.getData()+"\n"
                +"RUNTIME Stats With 0.25 Limit\n"
                +this.minRuntimeUser025Stats.getData()+"\n"
                +"FINISH TIME Stats With 0.25 Limit\n"
                +this.minFinishTimeUser025Stats.getData()+"\n"
                +"COST Stats With 0.25 Limit\n"
                +this.minCostUser025Stats.getData()+"\n"
                +"/*****************************************/ \n"
                +"Alternative Solver Stats With 0.5 Limit\n"
                +this.as2User05Stats.getData()+"\n"
                +"RUNTIME Stats With 0.5 Limit\n"
                +this.minRuntimeUser05Stats.getData()+"\n"
                +"FINISH TIME Stats With 0.5 Limit\n"
                +this.minFinishTimeUser05Stats.getData()+"\n"
                +"COST Stats With 0.5 Limit\n"
                +this.minCostUser05Stats.getData()+"\n"
                +"/*****************************************/ \n"
                +"Alternative Solver Stats With 1 Limit\n"
                +this.as2User1Stats.getData()+"\n"
                +"RUNTIME Stats With 1 Limit\n"
                +this.minRuntimeUser1Stats.getData()+"\n"
                +"FINISH TIME Stats With 1 Limit\n"
                +this.minFinishTimeUser1Stats.getData()+"\n"
                +"COST Stats With 1 Limit\n"
                +this.minCostUser1Stats.getData()+"\n"
                +"/*****************************************/ \n"
                +"Alternative Solver Stats With 2 Limit\n"
                +this.as2User2Stats.getData()+"\n"
                +"RUNTIME Stats With 2 Limit\n"
                +this.minRuntimeUser2Stats.getData()+"\n"
                +"FINISH TIME Stats With 2 Limit\n"
                +this.minFinishTimeUser2Stats.getData()+"\n"
                +"COST Stats With 2 Limit\n"
                +this.minCostUser2Stats.getData()+"\n"
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
