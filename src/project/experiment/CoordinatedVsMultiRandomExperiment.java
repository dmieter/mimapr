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
import project.engine.slot.slotProcessor.RandomSquareWindowFinder;
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
public class CoordinatedVsMultiRandomExperiment extends Experiment {

    private final int cycleLength = 1600;
    private final int expJobsNumber = 64;
    private final int expNodesNumber = 42;
    private final int jobGenerationInterval = 1000;

    protected SimpleSquareWindowFinder sqwFinder;
    protected RandomSquareWindowFinder randFinder;
    protected SlotProcessorSettings sps;

    public ArrayList<UserJob> batchToShow;
    public VOEnvironment envToShow;

    protected SchedulingResultsStats copStats;
    protected SchedulingResultsStats randStats1;
    protected SchedulingResultsStats randStats2;
    protected SchedulingResultsStats randStats5;
    protected SchedulingResultsStats randStats10;
    protected SchedulingResultsStats randStats20;
    protected SchedulingResultsStats randStats50;
    protected SchedulingResultsStats randStats100;
    protected SchedulingResultsStats randStats200;
    protected SchedulingResultsStats randStats500;
    protected SchedulingResultsStats randStats1000;
    protected SchedulingResultsStats bfFinishStats;
    
    /* NEW STATS */
    protected NamedStats namedStats;
    SchedulingResultsCalculator resultCalculator;

    protected void configureExperiment() {
        copStats = new SchedulingResultsStats("CoP STATS");
        randStats1 = new SchedulingResultsStats("RAND 1 STATS");
        randStats2 = new SchedulingResultsStats("RAND 2 STATS");
        randStats5 = new SchedulingResultsStats("RAND 5 STATS");
        randStats10 = new SchedulingResultsStats("RAND 10 STATS");
        randStats20 = new SchedulingResultsStats("RAND 20 STATS");
        randStats50 = new SchedulingResultsStats("RAND 50 STATS");
        randStats100 = new SchedulingResultsStats("RAND 100 STATS");
        randStats200 = new SchedulingResultsStats("RAND 200 STATS");
        randStats500 = new SchedulingResultsStats("RAND 500 STATS");
        randStats1000 = new SchedulingResultsStats("RAND 1000 STATS");
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
        long bfT, copT, randT;

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
        
        /* MultiRandom  */
        System.out.println("MultiRandom Scheduling");
        bfp.setFinder(randFinder);
        
        List<ArrayList<UserJob>> randBatches = new ArrayList<>();
        
        randT = System.nanoTime();
        for(int i = 0; i < 1000; i++){
            ArrayList<UserJob> batchRand = VOEHelper.copyJobBatchList(batch);
            batchRand.forEach(j -> j.resourceRequest.criteria = new MinFinishTimeCriteria() ); // backfilling is base 
            
            bfp.schedule(batchRand, env);
            for (UserJob job : batchRand) {
                if (!job.alternatives.isEmpty()) {
                    job.bestAlternative = 0;
                }
            }
            if (SchedulingResultsStats.checkBatchForSuccess(batchRand)) {
                randBatches.add(batchRand);
            }

            flush();
        }
        randT = System.nanoTime() - randT;
        
        if (randBatches.isEmpty()) {
            System.out.println("MultiRandom failed to find alternatives");
            randStats10.addFailExperiment();
            success = false;
        }
        
        if (success) {

            /* standard stats */
            bfFinishStats.processResults(batchBFFinish, bfT);
            copStats.processResults(batchCoP, copT);
            
            // Hindsight over random results
            ArrayList<UserJob> bestRandBatch = randBatches.get(0);
            resultCalculator.calculateResults(bestRandBatch);
            Double bestRandFinish = resultCalculator.getAvFinish();
            
            int batchNum = 1;
            for(ArrayList<UserJob> randBatch : randBatches){
                resultCalculator.calculateResults(randBatch);
                if(resultCalculator.getAvFinish() < bestRandFinish){
                    bestRandFinish = resultCalculator.getAvFinish();
                    bestRandBatch = randBatch;
                }
                
                // processing current best for 1, 10, ... , 1000 first batches
                switch (batchNum) {
                    case 1:
                        randStats1.processResults(bestRandBatch);
                        break;
                    case 2:
                        randStats2.processResults(bestRandBatch);
                        break;
                    case 5:
                        randStats5.processResults(bestRandBatch);
                        break;
                    case 10:
                        randStats10.processResults(bestRandBatch);
                        break;
                    case 20:
                        randStats20.processResults(bestRandBatch);
                        break;
                    case 50:
                        randStats50.processResults(bestRandBatch);
                        break;
                    case 100:
                        randStats100.processResults(bestRandBatch);
                        break;
                    case 200:
                        randStats200.processResults(bestRandBatch);
                        break;
                    case 500:
                        randStats500.processResults(bestRandBatch);
                        break;
                    case 1000:
                        randStats1000.processResults(bestRandBatch, randT);
                        break;
                    default:
                        break;
                }
                
                batchNum++;
            }
            
            /* additional statistics */
            resultCalculator.calculateResults(batchBFFinish);
            Double bfFinishTime = resultCalculator.getAvFinish();
            namedStats.addValue("BF Finish Time", resultCalculator.getAvFinish());
            namedStats.addValue("BF Makespan", resultCalculator.getMaxFinishTime());
            
            resultCalculator.calculateResults(batchCoP);
            Double copFinishTime = resultCalculator.getAvFinish();
            namedStats.addValue("CoP Finish Time", resultCalculator.getAvFinish());
            namedStats.addValue("CoP Makespan", resultCalculator.getMaxFinishTime());
            
            resultCalculator.calculateResults(bestRandBatch);
            Double randFinishTime = resultCalculator.getAvFinish();
            namedStats.addValue("Multi Rand Finish Time", resultCalculator.getAvFinish());
            namedStats.addValue("Multi Rand Makespan", resultCalculator.getMaxFinishTime());
            
            Double diffValue1 = (bfFinishTime-copFinishTime)/copFinishTime;
            namedStats.addValue("CoP-BF Finish Time Advantage", diffValue1);
            //copStats.addCollectedValue(diffValue);
            
            Double diffValue2 = (bfFinishTime-randFinishTime)/randFinishTime;
            namedStats.addValue("Rand-BF Finish Time Advantage", diffValue2);
            
            Double diffValue3 = (copFinishTime-randFinishTime)/randFinishTime;
            namedStats.addValue("Rand-CoP Finish Time Advantage", diffValue3);
            
        }
        
        batchToShow = batchCoP;
        envToShow = env;

        int a = 0;
    }

    protected void configureSlotProcessorV2() {
        sqwFinder = new SimpleSquareWindowFinder();
        sqwFinder.useOptimizedImplementation(true);
        
        randFinder = new RandomSquareWindowFinder();
            
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
        String data = "\n ============== COORDINATION OR CHAOS ================== \n";

        data += this.bfFinishStats.getData() + "\n"
                + this.copStats.getData() + "\n"
                + this.randStats1000.getData() + "\n"
                + this.randStats500.getData() + "\n"
                + this.randStats200.getData() + "\n"
                + this.randStats100.getData() + "\n"
                + this.randStats50.getData() + "\n"
                + this.randStats20.getData() + "\n"
                + this.randStats10.getData() + "\n"
                + this.randStats5.getData() + "\n"
                + this.randStats2.getData() + "\n"
                + this.randStats1.getData() + "\n";

        data += namedStats.getData();
        return data;
    }

}
