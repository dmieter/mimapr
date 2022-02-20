
package project.experiment.archive;

import java.util.ArrayList;
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
import project.engine.scheduler.SchedulerOperations;
import project.engine.scheduler.alternativeSolver.v1.AlternativeSolver;
import project.engine.scheduler.alternativeSolver.v1.AlternativeSolverSettings;
import project.engine.scheduler.alternativeSolver.v1.LimitCountData;
import project.engine.scheduler.alternativeSolver.v2.AlternativeSolverSettingsV2;
import project.engine.scheduler.alternativeSolver.v2.AlternativeSolverV2;
import project.engine.scheduler.alternativeSolver.v2.LimitSettings;
import project.engine.scheduler.alternativeSolver.v2.data.OptimizationEntity;
import project.engine.scheduler.alternativeSolver.v2.optimization.ConfigurableLimitedOptimization;
import project.engine.scheduler.alternativeSolver.v2.optimization.OptimizationConfig;
import project.engine.slot.slotProcessor.SlotProcessorSettings;
import project.engine.slot.slotProcessor.SlotProcessorV2;
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
public class AlternativeSolverV2RegressionExperiment extends Experiment {
    protected AlternativeSolver as;
    protected AlternativeSolverSettings ass;
    
    protected AlternativeSolverV2 as2;
    protected AlternativeSolverSettingsV2 ass2;
    
    SlotProcessorV2 sp;
    SlotProcessorSettings sps;
    
    protected SchedulingResultsStats asStats;
    protected SchedulingResultsStats as2Stats;
    
    protected int cycleLength = 600;
    
    public void flush(){
        as.flush();
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
        configureAlternativeSolver();
        configureAlternativeSolverV2();
        
        asStats = new SchedulingResultsStats();
        as2Stats = new SchedulingResultsStats();
    }

    protected void performSingleExperiment() {

        long t1,t2;
        boolean success = true;
        
        VOEnvironment env = generateNewEnvironment();
        ArrayList<UserJob> batch = generateJobBatch();
        
        sp.findAlternatives(batch, env, sps);
        for(UserJob job : batch){
            job.rankAlternatives();
        }
        
        
        //Alternative Solver
        ArrayList<UserJob> batch1 = VOEHelper.copyJobBatchList(batch);

        t1 = System.nanoTime();
        as.solve(ass, env, batch1);
        t1 = System.nanoTime() - t1;
        
        if(!SchedulingResultsStats.checkBatchForSuccess(batch1)){
            System.out.println("AS failed to find alternatives");
            asStats.failStats.processEnvironment(env);
            asStats.addFailExperiment();
            success = false;
        }
        String asResult = SchedulerOperations.getAltenativesCombinationString(batch1);
        System.out.println(asResult);
        
        
        
        //Alternative Solver V2
        ArrayList<UserJob> batch2 = VOEHelper.copyJobBatchList(batch);

        t2 = System.nanoTime();
        as2.solve(ass2, env, batch2);
        t2 = System.nanoTime() - t2;
        
        if(!SchedulingResultsStats.checkBatchForSuccess(batch2)){
            System.out.println("AS2 failed to find alternatives");
            as2Stats.failStats.processEnvironment(env);
            as2Stats.addFailExperiment();
            success = false;
        }
        String as2Result = SchedulerOperations.getAltenativesCombinationString(batch2);
        System.out.println(as2Result);
        
        //Stats
        if(success){
            asStats.processResults(batch1, t1);
            as2Stats.processResults(batch2, t2);
            
//            if(!asResult.equals(as2Result)){
//                as2.solve(ass2, env, batch2);
//            }
            
            int b=0;
        }else{
            OptimizationEntity oe = as2.optimizationTable[0];
            if(oe != null && oe.options.size() > 0){
                int a = 0;
            }
        }
    }

    protected void configureAlternativeSolver() {
        as = new AlternativeSolver();
        ass = new AlternativeSolverSettings();

        ass.optimalOnly = true;
        ass.limitedVar = AlternativeSolverSettings.COST;
        ass.optimizedVar = AlternativeSolverSettings.TIME;
        ass.optType = "MIN";
        ass.usePareto = false;
        
        ass.limitCalculationType = 0; //average
        
        // const budget limit 1200 
        //ass.limitCalculationType = 1;
        ass.limitCountData = new LimitCountData(1300*40, 0);
    }

    protected void configureAlternativeSolverV2() {
        as2 = new AlternativeSolverV2();
        ass2 = new AlternativeSolverSettingsV2();
        
        ass2.optType = AlternativeSolverSettingsV2.MIN;
        ass2.secondaryOptType = AlternativeSolverSettingsV2.MIN;
        ass2.periodStart = 0;
        ass2.periodEnd = cycleLength;
        
        LimitSettings ls = new LimitSettings();
        ls.limitType = LimitSettings.LIMIT_TYPE_AVERAGE;
        //ls.limitType = LimitSettings.LIMIT_TYPE_CONST;
        ls.constLimit = 1300d*40;
        ls.limitStep = 1d;
        ass2.limitSettings = ls;
        
        OptimizationConfig config = new ConfigurableLimitedOptimization(
                                            ConfigurableLimitedOptimization.TIME,  /* optimization */
                                            ConfigurableLimitedOptimization.USER,  /* secondary optimization */    
                                            ConfigurableLimitedOptimization.COST); /* limit */
        
        ass2.optimizationConfig = config;
    }
    
    protected void configureSlotProcessor() {
        sp = new SlotProcessorV2();
        sps = new SlotProcessorSettings();
        
        sps.algorithmConcept = "COMMON";
        sps.algorithmType = "MODIFIED";
        sps.cycleLength = cycleLength;
        sps.cycleStart = 0;
        sps.countStats = false;
        sps.clean = true;
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
        //UserRanking ur = new SimpleUserRanking();
        UserRanking ur = new PercentileUserRanking();
        for(UserJob job : jobs){
            job.rankingAlgorithm = ur;
        }
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

    @Override
    public String getData() {
        String data = "ALTERNATIVE SOLVER REGRESSION EXPERIMENT\n"
                +"Alternative Solver Stats\n"
                +this.asStats.getData()+"\n"
                +"Alternative Solver V2 Stats\n"
                +this.as2Stats.getData()+"\n";
        
        return data;
    }
    
    
    
}
