package project.experiment.archive;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import project.engine.alternativeStats.SchedulingResultsStats;
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
import project.engine.slot.slotProcessor.criteriaHelpers.ValuationModelAltroTime;
import project.engine.slot.slotProcessor.criteriaHelpers.ValuationModelCustom;
import project.engine.slot.slotProcessor.criteriaHelpers.ValuationModelCustomStats;
import project.engine.slot.slotProcessor.criteriaHelpers.ValuationModelEgoTime;
import project.engine.slot.slotProcessor.userRankings.PercentileUserRanking;
import project.engine.slot.slotProcessor.userRankings.UserRanking;
import project.experiment.Experiment;
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
public class EgoAltroJobFlowSchedulingExperiment extends Experiment {

    private final int cycleLength = 2000;
    private final int expJobsNumber = 100;
    private final int expNodesNumber = 32;
    private final int jobGenerationInterval = 0;

    protected SimpleSquareWindowFinder sqwFinder;
    protected SlotProcessorSettings sps;

    public ArrayList<UserJob> batchToShow;
    public VOEnvironment envToShow;

    protected SchedulingResultsStats coord1Stats;
    protected SchedulingResultsStats coord2Stats;
    protected SchedulingResultsStats coord3Stats;
    protected SchedulingResultsStats bfStatsFinish;

    protected void configureExperiment() {
        coord1Stats = new SchedulingResultsStats("COORDINATED EGO STATS");
        coord2Stats = new SchedulingResultsStats("COORDINATED ALTRO STATS");
        coord3Stats = new SchedulingResultsStats("COORDINATED MIXED STATS");
        bfStatsFinish = new SchedulingResultsStats("BF FINISH STATS");
        
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
        long simpleT, complexT;

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

        simpleT = System.nanoTime();
        bfp.schedule(batchBFFinish, env);
        //sqwFinder.findAlternatives(batchBFFinish, env, sps, 1);
        simpleT = System.nanoTime() - simpleT;
        for (UserJob job : batchBFFinish) {
            if (!job.alternatives.isEmpty()) {
                job.bestAlternative = 0;
            }
        }
        if (!SchedulingResultsStats.checkBatchForSuccess(batchBFFinish)) {
            System.out.println("BF-FINISH failed to find alternatives");
            bfStatsFinish.addFailExperiment();
            success = false;
        }

        flush();
        
       
        /* Ego coordinated solution*/
        System.out.println("COORDINATED EGO scheduling");
        ArrayList<UserJob> batchCoord1 = VOEHelper.copyJobBatchList(batch);
        for (UserJob job : batchCoord1) {
            job.resourceRequest.criteria = new MaxAdditiveUserValuationCriterion(new ValuationModelEgoTime());
        }

        bfp.schedule(batchCoord1, env);
        //sqwFinder.findAlternatives(batchCoordBF, env, sps, 1);
        for (UserJob job : batchCoord1) {
            if (!job.alternatives.isEmpty()) {
                job.bestAlternative = 0;
            }
        }
        if (!SchedulingResultsStats.checkBatchForSuccess(batchCoord1)) {
            System.out.println("COORDINATED EGO failed to find alternatives");
            coord1Stats.addFailExperiment();
            success = false;
        }
        
        flush();
        
        /* Altro coordinated solution*/
        System.out.println("COORDINATED ALTRO scheduling");
        ArrayList<UserJob> batchCoord2 = VOEHelper.copyJobBatchList(batch);
        for (UserJob job : batchCoord2) {
            job.resourceRequest.criteria = new MaxAdditiveUserValuationCriterion(new ValuationModelAltroTime());
        }

        bfp.schedule(batchCoord2, env);
        //sqwFinder.findAlternatives(batchCoordBF, env, sps, 1);
        for (UserJob job : batchCoord2) {
            if (!job.alternatives.isEmpty()) {
                job.bestAlternative = 0;
            }
        }
        if (!SchedulingResultsStats.checkBatchForSuccess(batchCoord2)) {
            System.out.println("COORDINATED ALTRO failed to find alternatives");
            coord2Stats.addFailExperiment();
            success = false;
        }
        
        flush();
        
        /* Mixed coordinated solution*/
        System.out.println("COORDINATED MIXED scheduling");
        ArrayList<UserJob> batchCoord3 = VOEHelper.copyJobBatchList(batch);
        int i = 0;
        for (UserJob job : batchCoord3) {
            if(i%4 > 0){
                job.resourceRequest.criteria = new MaxAdditiveUserValuationCriterion(new ValuationModelAltroTime());
            }else{
                job.resourceRequest.criteria = new MaxAdditiveUserValuationCriterion(new ValuationModelEgoTime());
            }
            
            i++;
        }

        bfp.schedule(batchCoord3, env);
        //sqwFinder.findAlternatives(batchCoordBF, env, sps, 1);
        for (UserJob job : batchCoord3) {
            if (!job.alternatives.isEmpty()) {
                job.bestAlternative = 0;
            }
        }
        if (!SchedulingResultsStats.checkBatchForSuccess(batchCoord3)) {
            System.out.println("COORDINATED MIXED failed to find alternatives");
            coord3Stats.addFailExperiment();
            success = false;
        }
        
        flush();
        
        
        
        if (success) {

            bfStatsFinish.processResults(batchBFFinish);
            coord1Stats.processResults(batchCoord1);
            coord2Stats.processResults(batchCoord2);
            coord3Stats.processResults(batchCoord3);
        }

        batchToShow = batchCoord3;
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
        //HyperGeometricSettings hgSet = new HyperGeometricSettings(1000, 150, 30, 0, 10, 0, 2);
        //envSet.occupGenerator = new HyperGeometricFacade(hgSet);
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
        String data = "\n ============== WE WILL SIMPLE LOOSE TO BACKFILLING EXPERIMENT ================== \n";

        data += this.bfStatsFinish.getData() + "\n"
                + this.coord1Stats.getData() + "\n"
                + this.coord2Stats.getData() + "\n"
                + this.coord3Stats.getData() + "\n";
        return data;
    }

}
