package project.experiment.archive;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import project.engine.alternativeStats.SchedulingResultsStats;
import project.engine.data.ComputingNode;
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
public class BackfillingErrorPriceExperiment extends Experiment {

    private final int cycleLength = 8000;
    private final int expJobsNumber = 200;
    private final int expNodesNumber = 30;
    private final int jobGenerationInterval = 1800;

    protected SimpleSquareWindowFinder sqwFinder;
    protected SlotProcessorSettings sps;

    public ArrayList<UserJob> batchToShow;
    public VOEnvironment envToShow;

    protected SchedulingResultsStats bfDelStatsFinish;
    protected SchedulingResultsStats bfStatsFinish;
    protected SchedulingResultsStats bfStatsStart;

    protected void configureExperiment() {
        bfDelStatsFinish = new SchedulingResultsStats("DELAYED BF FINISH STATS");
        bfStatsFinish = new SchedulingResultsStats("BF FINISH STATS");
        bfStatsStart = new SchedulingResultsStats("BF START STATS");

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

        ArrayList<UserJob> batch = generateJobBatch();
        setRandomSubmissionTime(batch, 0, jobGenerationInterval);

        VOEnvironment env = generateNewEnvironment(expNodesNumber);
        
        BackfillingProcedure bfp = new BackfillingProcedure(sqwFinder, cycleLength, cycleLength);
        
        flush();

        /* Simple backfilling solution with finish time criterion */
        System.out.println("BF-FINISH scheduling");
        ArrayList<UserJob> batchBFFinish = VOEHelper.copyJobBatchList(batch);
        for (UserJob job : batchBFFinish) {
            job.resourceRequest.criteria = new MinFinishTimeCriteria();
        }

        //bfp.schedule(batchBFFinish, env);
        sqwFinder.findAlternatives(batchBFFinish, env, sps, 1);
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
        
         /* Simple backfilling solution with finish time criterion */
        System.out.println("BF-FINISH DELAYED scheduling");
        ArrayList<UserJob> batchBFFinishDel = VOEHelper.copyJobBatchList(batch);
        for (UserJob job : batchBFFinishDel) {
            job.resourceRequest.volume *= 1.01;
            job.resourceRequest.criteria = new MinFinishTimeCriteria();
        }

        //bfp.schedule(batchBFFinish, env);
        sqwFinder.findAlternatives(batchBFFinishDel, env, sps, 1);
        for (UserJob job : batchBFFinishDel) {
            if (!job.alternatives.isEmpty()) {
                job.bestAlternative = 0;
            }
        }
        if (!SchedulingResultsStats.checkBatchForSuccess(batchBFFinishDel)) {
            System.out.println("BF-FINISH DELAYED failed to find alternatives");
            bfDelStatsFinish.addFailExperiment();
            success = false;
        }

        flush();

        /* Simple backfilling solution with start time criterion */
        System.out.println("BF-START scheduling");
        ArrayList<UserJob> batchBFStart = VOEHelper.copyJobBatchList(batch);
        for (UserJob job : batchBFStart) {
            job.resourceRequest.criteria = new MinStartTimeCriteria();
        }

        sqwFinder.findAlternatives(batchBFStart, env, sps, 1);
        for (UserJob job : batchBFStart) {
            if (!job.alternatives.isEmpty()) {
                job.bestAlternative = 0;
            }
        }
        if (!SchedulingResultsStats.checkBatchForSuccess(batchBFStart)) {
            System.out.println("BF-START failed to find alternatives");
            bfStatsStart.addFailExperiment();
            success = false;
        }
        
        flush();
        System.out.println(new Date());
        
        if (success) {

            bfDelStatsFinish.processResults(batchBFFinishDel);
            bfStatsFinish.processResults(batchBFFinish);
            bfStatsStart.processResults(batchBFStart);

        }

        batchToShow = batchBFFinish;
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

    private ArrayList<UserJob> generateJobBatch() {
        JobGenerator jg = new JobGenerator();
        JobGeneratorSettings jgs = new JobGeneratorSettings();
        jgs.taskNumber = expJobsNumber;

        jgs.minPrice = 1.4;
        jgs.maxPrice = 2;
        jgs.useSpeedPriceFactor = true;

        jgs.minTime = 60;
        jgs.maxTime = 600;

        jgs.minSpeed = 1;
        jgs.maxSpeed = 1;

        jgs.minCPU = 2;
        jgs.maxCPU = 6;

        GaussianSettings gs = new GaussianSettings(0.2, 0.6, 1);
        jgs.timeCorrectiveCoefGen = new GaussianFacade(gs);

        ArrayList<UserJob> jobs = jg.generate(jgs);

        return jobs;
    }
    
    private VOEnvironment generateNewEnvironment(int nodesNumber) {
        //Creating resources
        EnvironmentGeneratorSettings envSet = new EnvironmentGeneratorSettings();
        envSet.minResourceSpeed = 2;
        envSet.maxResourceSpeed = 17;
        envSet.resourceLineNum = nodesNumber;
        envSet.maxTaskLength = 100;
        envSet.minTaskLength = 5;
        envSet.occupGenerator = new UniformFacade(1, 2);
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
    
    public String getData() {
        String data = "\n ============== PRICE OF ERROR EXPERIMENT ================== \n";

        data += this.bfStatsStart.getData() + "\n"
                + this.bfStatsFinish.getData() + "\n"
                + this.bfDelStatsFinish.getData() + "\n";

        return data;
    }

}
