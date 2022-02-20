package project.experiment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import project.engine.alternativeStats.NamedStats;
import project.engine.alternativeStats.SchedulingResultsStats;
import project.engine.analysis.SchedulingResultsCalculator;
import project.engine.data.ComputingNode;
import project.engine.data.ResourceRequest;
import project.engine.data.UserJob;
import project.engine.data.VOEHelper;
import project.engine.data.VOEnvironment;
import project.engine.data.environmentGenerator.EnvironmentGenerator;
import project.engine.data.environmentGenerator.EnvironmentGeneratorSettings;
import project.engine.data.environmentGenerator.EnvironmentPricingSettings;
import project.engine.data.jobGenerator.JobGenerator;
import project.engine.data.jobGenerator.JobGeneratorSettings;
import project.engine.scheduler.backFill.BackfillingProcedure;
import project.engine.slot.slotProcessor.SimpleSquareWindowFinder;
import project.engine.slot.slotProcessor.SlotProcessorSettings;
import project.engine.slot.slotProcessor.criteriaHelpers.MaxAdditiveUserValuationCriterion;
import project.engine.slot.slotProcessor.criteriaHelpers.MinFinishTimeCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinRunTimeCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinStartTimeCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinSumCostCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.ValuationModelCustom;
import project.engine.slot.slotProcessor.criteriaHelpers.ValuationModelCustomStats;
import project.engine.slot.slotProcessor.userRankings.PercentileUserRanking;
import project.engine.slot.slotProcessor.userRankings.UserRanking;
import project.math.distributions.GaussianFacade;
import project.math.distributions.GaussianSettings;
import project.math.distributions.HyperGeometricFacade;
import project.math.distributions.HyperGeometricSettings;
import project.math.distributions.UniformFacade;
import project.math.utils.MathUtils;

/**
 *
 * @author magica
 */
public class CoordinatedWithBackfillingExperiment extends Experiment {

    private final int cycleLength = 1600;
    private final int expJobsNumber = 64;
    private final int expNodesNumber = 42;
    private final int jobGenerationInterval = 0;

    protected SimpleSquareWindowFinder sqwFinder;
    protected SlotProcessorSettings sps;

    public ArrayList<UserJob> batchToShow;
    public VOEnvironment envToShow;

    protected SchedulingResultsStats copStats;
    protected SchedulingResultsStats effStats;
    protected SchedulingResultsStats randStats;
    protected SchedulingResultsStats bfFinishStats;
    
    /* NEW STATS */
    protected NamedStats namedStats;
    SchedulingResultsCalculator resultCalculator;

    protected void configureExperiment() {
        copStats = new SchedulingResultsStats("CoP STATS");
        effStats = new SchedulingResultsStats("EFF STATS");
        randStats = new SchedulingResultsStats("RAND STATS");
        bfFinishStats = new SchedulingResultsStats("BF STATS");
        
        namedStats = new NamedStats("VARIOUS STATS");
        resultCalculator = new SchedulingResultsCalculator();

        configureSlotProcessorV2();
    }

    @Override
    public void performExperiments(int expNum) {

        configureExperiment();

        for (int i = 0; i < expNum; i++) {
            System.out.println("Experiment #" + i);
            performExperiment();
        }
    }

    protected void performExperiment() {

        boolean success = true;
        long bfT, copT, effT, randT;

        ArrayList<UserJob> batch = generateJobBatch();
        //ArrayList<UserJob> batch = generateTestJobBatch();
        setRandomSubmissionTime(batch, 0, jobGenerationInterval);

        VOEnvironment env = generateNewEnvironment(expNodesNumber);
        //VOEnvironment env = generateTestEnvironment(10);
        
        BackfillingProcedure bfp = new BackfillingProcedure(sqwFinder, 0, cycleLength);
        
        flush();

        /* Simple backfilling solution with finish time criterion */
        System.out.println("BF-FINISH scheduling");
        ArrayList<UserJob> batchBFFinish = VOEHelper.copyJobBatchList(batch);
        for (UserJob job : batchBFFinish) {
            job.resourceRequest.criteria = new MinFinishTimeCriteria();
        }

        bfT = System.nanoTime();
        bfp.schedule(batchBFFinish, env);
        bfT = System.nanoTime() - bfT;
        for (UserJob job : batchBFFinish) {
            if (!job.alternatives.isEmpty()) {
                job.bestAlternative = 0;
            }
        }
        if (!SchedulingResultsStats.checkBatchForSuccess(batchBFFinish)) {
            System.out.println("BF-FINISH failed to find alternatives");
            bfFinishStats.addFailExperiment();
            success = false;
        }

        flush();
        
        System.out.println(new Date());
        
        /* CoP */
        System.out.println("CoP Scheduling");
        ArrayList<UserJob> batchCoP = VOEHelper.copyJobBatchList(batch);
        for (UserJob job : batchCoP) {
            ValuationModelCustom valuationModel = new ValuationModelCustom(ValuationModelCustom::copValuation);
            MaxAdditiveUserValuationCriterion mauvCriterion = new MaxAdditiveUserValuationCriterion(valuationModel);
            mauvCriterion.provideSearchStopCondition((cMax,cCur) -> (cMax > cCur*0.9));
            job.resourceRequest.criteria = mauvCriterion;
        }

        copT = System.nanoTime();
        bfp.schedule(batchCoP, env);
        //sqwFinder.findAlternatives(batchCoord3, env, sps, 1);
        copT = System.nanoTime() - copT;
        for (UserJob job : batchCoP) {
            if (!job.alternatives.isEmpty()) {
                job.bestAlternative = 0;
            }
        }
        if (!SchedulingResultsStats.checkBatchForSuccess(batchCoP)) {
            System.out.println("CoP failed to find alternatives");
            copStats.addFailExperiment();
            success = false;
        }
        
        flush();
        
        System.out.println(new Date());
        
        /* EFF */
        System.out.println("EFF Scheduling");
        ArrayList<UserJob> batchEff = VOEHelper.copyJobBatchList(batch);
        for (UserJob job : batchEff) {
            ValuationModelCustom valuationModel = new ValuationModelCustom(ValuationModelCustom::leastEffectiveValuation);
            MaxAdditiveUserValuationCriterion mauvCriterion = new MaxAdditiveUserValuationCriterion(valuationModel);
            mauvCriterion.provideSearchStopCondition((cMax,cCur) -> (cMax > cCur*0.9));
            job.resourceRequest.criteria = mauvCriterion;
        }

        effT = System.nanoTime();
        bfp.schedule(batchEff, env);
        //sqwFinder.findAlternatives(batchCoord3, env, sps, 1);
        effT = System.nanoTime() - effT;
        for (UserJob job : batchEff) {
            if (!job.alternatives.isEmpty()) {
                job.bestAlternative = 0;
            }
        }
        if (!SchedulingResultsStats.checkBatchForSuccess(batchEff)) {
            System.out.println("EFF failed to find alternatives");
            effStats.addFailExperiment();
            success = false;
        }
        
        flush();
        
        System.out.println(new Date());
        
        /* EFF */
        System.out.println("RAND Scheduling");
        ArrayList<UserJob> batchRand = VOEHelper.copyJobBatchList(batch);
        for (UserJob job : batchRand) {
            ValuationModelCustom valuationModel = new ValuationModelCustom(ValuationModelCustom::randomValuation);
            MaxAdditiveUserValuationCriterion mauvCriterion = new MaxAdditiveUserValuationCriterion(valuationModel);
            mauvCriterion.provideSearchStopCondition((cMax,cCur) -> (cMax > cCur*0.9));
            job.resourceRequest.criteria = mauvCriterion;
        }

        randT = System.nanoTime();
        bfp.schedule(batchRand, env);
        //sqwFinder.findAlternatives(batchCoord3, env, sps, 1);
        randT = System.nanoTime() - randT;
        for (UserJob job : batchRand) {
            if (!job.alternatives.isEmpty()) {
                job.bestAlternative = 0;
            }
        }
        if (!SchedulingResultsStats.checkBatchForSuccess(batchRand)) {
            System.out.println("RAND failed to find alternatives");
            randStats.addFailExperiment();
            success = false;
        }
             
        System.out.println(new Date());
        
      
        if (success) {

            /* standard stats */
            bfFinishStats.processResults(batchBFFinish);
            copStats.processResults(batchCoP, copT);
            effStats.processResults(batchEff, effT);
            randStats.processResults(batchRand, randT);
            
            /* additional statistics */
            resultCalculator.calculateResults(batchBFFinish);
            Double bfFinishTime = resultCalculator.getAvFinish();
            namedStats.addValue("BF Finish Time", resultCalculator.getAvFinish());
            namedStats.addValue("BF Makespan", resultCalculator.getMaxFinishTime());
            
            resultCalculator.calculateResults(batchCoP);
            Double copFinishTime = resultCalculator.getAvFinish();
            namedStats.addValue("CoP Finish Time", resultCalculator.getAvFinish());
            namedStats.addValue("CoP Makespan", resultCalculator.getMaxFinishTime());
            
            resultCalculator.calculateResults(batchEff);
            Double effFinishTime = resultCalculator.getAvFinish();
            namedStats.addValue("EFF Finish Time", resultCalculator.getAvFinish());
            namedStats.addValue("EFF Makespan", resultCalculator.getMaxFinishTime());
            
            resultCalculator.calculateResults(batchRand);
            Double randFinishTime = resultCalculator.getAvFinish();
            namedStats.addValue("Rand Finish Time", resultCalculator.getAvFinish());
            namedStats.addValue("Rand Makespan", resultCalculator.getMaxFinishTime());
            
            Double diffValue = (bfFinishTime-copFinishTime)/copFinishTime;
            namedStats.addValue("CoP-BF Finish Time Advantage", diffValue);
            copStats.addCollectedValue(diffValue);
            
            diffValue = (effFinishTime-copFinishTime)/copFinishTime;
            namedStats.addValue("CoP-EFF Finish Time Advantage", diffValue);
            effStats.addCollectedValue(diffValue);
            
            diffValue = (randFinishTime-copFinishTime)/copFinishTime;
            namedStats.addValue("CoP-Rand Finish Time Advantage", diffValue);
            randStats.addCollectedValue(diffValue);
            
            /* friendship statistics */
            
            if(copFinishTime <= bfFinishTime && copFinishTime <= effFinishTime && copFinishTime <= randFinishTime){
                resultCalculator.calculateResults(batchCoP);
                namedStats.addValue("CoP Success", 1d);
            }else{
                namedStats.addValue("CoP Success", 0d);
            }
            
            if(bfFinishTime <= copFinishTime && bfFinishTime <= effFinishTime && bfFinishTime <= randFinishTime){
                resultCalculator.calculateResults(batchBFFinish);
                namedStats.addValue("BF Success", 1d);
            }else{
                namedStats.addValue("BF Success", 0d);
            }
            
            if(effFinishTime <= copFinishTime && effFinishTime <= bfFinishTime && effFinishTime <= randFinishTime){
                resultCalculator.calculateResults(batchEff);
                namedStats.addValue("EFF Success", 1d);
            }else{
                namedStats.addValue("EFF Success", 0d);
            }
            
            if(randFinishTime <= copFinishTime && randFinishTime <= bfFinishTime && randFinishTime <= effFinishTime){
                resultCalculator.calculateResults(batchRand);
                namedStats.addValue("RAND Success", 1d);
            }else{
                namedStats.addValue("RAND Success", 0d);
            }
            
            
            namedStats.addValue("Friendship Finish Time", resultCalculator.getAvFinish());
            namedStats.addValue("Friendship Makespan", resultCalculator.getMaxFinishTime());
            
        }
        
        batchToShow = batchCoP;
        envToShow = env;

        int a = 0;
    }

    protected void configureSlotProcessorV2() {
        sqwFinder = new SimpleSquareWindowFinder();
        sqwFinder.useOptimizedImplementation(true);
            
        sps = new SlotProcessorSettings();
        sps.cycleStart = 0;
        sps.cycleLength = cycleLength;

    }

    private ArrayList<UserJob> generateTestJobBatch() {
        ArrayList<UserJob> jobs = new ArrayList<>();
        
        ResourceRequest rr1 = new ResourceRequest(2, 1000, 2, 1);
        jobs.add(new UserJob(0, "1", rr1, null, 0));
        
        ResourceRequest rr2 = new ResourceRequest(9, 300, 2, 1);
        jobs.add(new UserJob(2, "2", rr2, null, 2));
        
        return jobs;
    }
    
    private ArrayList<UserJob> generateJobBatch() {
        JobGenerator jg = new JobGenerator();
        JobGeneratorSettings jgs = new JobGeneratorSettings();
        jgs.taskNumber = expJobsNumber;

        jgs.minPrice = 1.5;
        jgs.maxPrice = 2;
        jgs.useSpeedPriceFactor = true;

        jgs.minTime = 60;
        jgs.maxTime = 1200;

        jgs.minSpeed = 1;
        jgs.maxSpeed = 1;

        jgs.minCPU = 1;
        jgs.maxCPU = 8;

        GaussianSettings gs = new GaussianSettings(0.2, 0.6, 1);
        jgs.timeCorrectiveCoefGen = new GaussianFacade(gs);

        ArrayList<UserJob> jobs = jg.generate(jgs);

        UserRanking ur = new PercentileUserRanking();
        for (UserJob job : jobs) {
            job.rankingAlgorithm = ur;
        }

        setRandomRequestCriteria(jobs, 50);

        return jobs;
    }
    
    private VOEnvironment generateTestEnvironment(int nodesNumber) {
        //Creating resources
        EnvironmentGeneratorSettings envSet = new EnvironmentGeneratorSettings();
        envSet.minResourceSpeed = 5;
        envSet.maxResourceSpeed = 5;
        envSet.resourceLineNum = nodesNumber;
        envSet.maxTaskLength = 0;
        envSet.minTaskLength = 0;
        envSet.timeInterval = cycleLength * 2;
        EnvironmentGenerator envGen = new EnvironmentGenerator();
        EnvironmentPricingSettings epc = new EnvironmentPricingSettings();
        epc.priceQuotient = 1;
        epc.priceMutationFactor = 0;
        epc.speedExtraCharge = 0;

        ArrayList<ComputingNode> lines = envGen.generateResourceTypes(envSet);
        
        //creating environment
        VOEnvironment env = envGen.generate(envSet, lines);
        env.applyPricing(epc);

        return env;
    }
    
    private VOEnvironment generateNewEnvironment(int nodesNumber) {
        //Creating resources
        EnvironmentGeneratorSettings envSet = new EnvironmentGeneratorSettings();
        envSet.minResourceSpeed = 2;
        envSet.maxResourceSpeed = 17;
        envSet.resourceLineNum = nodesNumber;
        envSet.maxTaskLength = 200;
        envSet.minTaskLength = 5;
        //envSet.occupGenerator = new UniformFacade(0,4);
        envSet.occupGenerator = new UniformFacade(0,0);
        envSet.timeInterval = cycleLength * 2;
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

    protected void flush() {
        sqwFinder.flush();
    }

    protected void setRandomSubmissionTime(List<UserJob> jobs, int intervalStart, int intervalEnd){
        for(UserJob job : jobs){
            job.timestamp = MathUtils.getUniform(intervalStart, intervalEnd);
        }
    }
    
    protected void setRandomRequestCriteria(List<UserJob> jobs, int costTimeRatioInPercents) {
        for (UserJob job : jobs) {
            int percent = MathUtils.getUniform(0, 100);

            if (percent < costTimeRatioInPercents) {
                job.resourceRequest.criteria = new MinSumCostCriteria();
            } else {
                job.resourceRequest.criteria = new MinRunTimeCriteria();
            }

            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String getData() {
        String data = "\n ============== WE WILL BE FRIENDS WITH BACKFILLING EXPERIMENT ================== \n";

        data += this.bfFinishStats.getData() + "\n"
                + this.copStats.getData() + "\n"
                + this.effStats.getData() + "\n"
                + this.randStats.getData() + "\n";

        data += namedStats.getData();
        return data;
    }

}
