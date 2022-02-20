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
import project.math.utils.MathUtils;

/**
 *
 * @author magica
 */
public class CoordinatedPreferencesExperimentSmall extends Experiment {

    private final int cycleLength = 800;
    private final int expJobsNumber = 64;
    private final int expNodesNumber = 32;
    private final int jobGenerationInterval = 0;

    protected SimpleSquareWindowFinder sqwFinder;
    protected SlotProcessorSettings sps;

    public ArrayList<UserJob> batchToShow;
    public VOEnvironment envToShow;

    protected SchedulingResultsStats coordStats;
    protected SchedulingResultsStats coordTimeStats;
    protected SchedulingResultsStats coordCostStats;
    protected SchedulingResultsStats coordStats2;
    protected SchedulingResultsStats coordTimeStats2;
    protected SchedulingResultsStats coordCostStats2;
    protected SchedulingResultsStats coordStats3;
    protected SchedulingResultsStats coordTimeStats3;
    protected SchedulingResultsStats coordCostStats3;
    protected SchedulingResultsStats coordStats4;
    protected SchedulingResultsStats coordTimeStats4;
    protected SchedulingResultsStats coordCostStats4;
    protected SchedulingResultsStats coordStats5;
    protected SchedulingResultsStats coordTimeStats5;
    protected SchedulingResultsStats coordCostStats5;
    protected SchedulingResultsStats coordStats6;
    protected SchedulingResultsStats coordTimeStats6;
    protected SchedulingResultsStats coordCostStats6;
    protected SchedulingResultsStats bfStatsFinish;
    protected SchedulingResultsStats bfStatsStart;

    protected void configureExperiment() {
        coordStats4 = new SchedulingResultsStats("COORDINATED STATS 5.5");
        coordTimeStats4 = new SchedulingResultsStats("COORDINATED PERF STATS 5.5");
        coordCostStats4 = new SchedulingResultsStats("COORDINATED COST STATS 5.5");
        coordStats5 = new SchedulingResultsStats("COORDINATED STATS 5");
        coordTimeStats5 = new SchedulingResultsStats("COORDINATED PERF STATS 5");
        coordCostStats5 = new SchedulingResultsStats("COORDINATED COST STATS 5");
        coordStats6 = new SchedulingResultsStats("COORDINATED STATS 6");
        coordTimeStats6 = new SchedulingResultsStats("COORDINATED PERF STATS 6");
        coordCostStats6 = new SchedulingResultsStats("COORDINATED COST STATS 6");
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
        
        
        /* Simple coordinated solution*/
        System.out.println("COORDINATED scheduling 5.5");
        ArrayList<UserJob> batchCoord4 = VOEHelper.copyJobBatchList(batch);
        for (UserJob job : batchCoord4) {
            ValuationModelCustom valuationModel = null;
            job.resourceRequest.initialCriteria = job.resourceRequest.criteria.getClass();
            if(job.resourceRequest.criteria instanceof MinSumCostCriteria){
                valuationModel = new ValuationModelCustom(ValuationModelCustom::minCostValuation55);
            }else{
                valuationModel = new ValuationModelCustom(ValuationModelCustom::maxPerformanceValuation55);
            }
            
            MaxAdditiveUserValuationCriterion mauvCriterion = new MaxAdditiveUserValuationCriterion(valuationModel);
            job.resourceRequest.criteria = mauvCriterion;
        }

        //bfp.schedule(batchCoord, env);
        sqwFinder.findAlternatives(batchCoord4, env, sps, 1);
        for (UserJob job : batchCoord4) {
            if (!job.alternatives.isEmpty()) {
                job.bestAlternative = 0;
            }
        }
        if (!SchedulingResultsStats.checkBatchForSuccess(batchCoord4)) {
            System.out.println("COORDINATED 5.5 failed to find alternatives");
            coordStats4.addFailExperiment();
            success = false;
        }
        
        flush();
        System.out.println(new Date());
        
        /* Simple coordinated solution*/
        System.out.println("COORDINATED scheduling 5");
        ArrayList<UserJob> batchCoord5 = VOEHelper.copyJobBatchList(batch);
        for (UserJob job : batchCoord5) {
            ValuationModelCustom valuationModel = null;
            job.resourceRequest.initialCriteria = job.resourceRequest.criteria.getClass();
            if(job.resourceRequest.criteria instanceof MinSumCostCriteria){
                valuationModel = new ValuationModelCustom(ValuationModelCustom::minCostValuation5);
            }else{
                valuationModel = new ValuationModelCustom(ValuationModelCustom::maxPerformanceValuation5);
            }
            
            MaxAdditiveUserValuationCriterion mauvCriterion = new MaxAdditiveUserValuationCriterion(valuationModel);
            job.resourceRequest.criteria = mauvCriterion;
        }

        //bfp.schedule(batchCoord, env);
        sqwFinder.findAlternatives(batchCoord5, env, sps, 1);
        for (UserJob job : batchCoord5) {
            if (!job.alternatives.isEmpty()) {
                job.bestAlternative = 0;
            }
        }
        if (!SchedulingResultsStats.checkBatchForSuccess(batchCoord5)) {
            System.out.println("COORDINATED 5 failed to find alternatives");
            coordStats5.addFailExperiment();
            success = false;
        }
        
        flush();
        System.out.println(new Date());
        
        /* Simple coordinated solution*/
        System.out.println("COORDINATED scheduling 6");
        ArrayList<UserJob> batchCoord6 = VOEHelper.copyJobBatchList(batch);
        for (UserJob job : batchCoord6) {
            ValuationModelCustom valuationModel = null;
            job.resourceRequest.initialCriteria = job.resourceRequest.criteria.getClass();
            if(job.resourceRequest.criteria instanceof MinSumCostCriteria){
                valuationModel = new ValuationModelCustom(ValuationModelCustom::minCostValuation6);
            }else{
                valuationModel = new ValuationModelCustom(ValuationModelCustom::maxPerformanceValuation6);
            }
            
            MaxAdditiveUserValuationCriterion mauvCriterion = new MaxAdditiveUserValuationCriterion(valuationModel);
            job.resourceRequest.criteria = mauvCriterion;
        }

        //bfp.schedule(batchCoord, env);
        sqwFinder.findAlternatives(batchCoord6, env, sps, 1);
        for (UserJob job : batchCoord6) {
            if (!job.alternatives.isEmpty()) {
                job.bestAlternative = 0;
            }
        }
        if (!SchedulingResultsStats.checkBatchForSuccess(batchCoord6)) {
            System.out.println("COORDINATED 6 failed to find alternatives");
            coordStats6.addFailExperiment();
            success = false;
        }
        
        flush();
        System.out.println(new Date());
        
        if (success) {

            ArrayList<UserJob> timeJobs4 = new ArrayList<>();
            ArrayList<UserJob> costJobs4 = new ArrayList<>();
            
            for(UserJob job : batchCoord4){
                if(job.resourceRequest.initialCriteria == MinSumCostCriteria.class){
                    costJobs4.add(job);
                }else{
                    timeJobs4.add(job);
                }
            }
            
            ArrayList<UserJob> timeJobs5 = new ArrayList<>();
            ArrayList<UserJob> costJobs5 = new ArrayList<>();
            
            for(UserJob job : batchCoord5){
                if(job.resourceRequest.initialCriteria == MinSumCostCriteria.class){
                    costJobs5.add(job);
                }else{
                    timeJobs5.add(job);
                }
            }
            
            ArrayList<UserJob> timeJobs6 = new ArrayList<>();
            ArrayList<UserJob> costJobs6 = new ArrayList<>();
            
            for(UserJob job : batchCoord6){
                if(job.resourceRequest.initialCriteria == MinSumCostCriteria.class){
                    costJobs6.add(job);
                }else{
                    timeJobs6.add(job);
                }
            }
            
            bfStatsFinish.processResults(batchBFFinish);
            bfStatsStart.processResults(batchBFStart);
            coordStats4.processResults(batchCoord4);
            coordTimeStats4.processResults(timeJobs4);
            coordCostStats4.processResults(costJobs4);
            coordStats5.processResults(batchCoord5);
            coordTimeStats5.processResults(timeJobs5);
            coordCostStats5.processResults(costJobs5);
            coordStats6.processResults(batchCoord6);
            coordTimeStats6.processResults(timeJobs6);
            coordCostStats6.processResults(costJobs6);
        }

        batchToShow = batchCoord4;
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
        jgs.maxCPU = 5;

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
    
    private VOEnvironment generateNewEnvironment(int nodesNumber) {
        //Creating resources
        EnvironmentGeneratorSettings envSet = new EnvironmentGeneratorSettings();
        envSet.minResourceSpeed = 2;
        envSet.maxResourceSpeed = 17;
        envSet.resourceLineNum = nodesNumber;
        envSet.maxTaskLength = 100;
        envSet.minTaskLength = 5;
        HyperGeometricSettings hgSet = new HyperGeometricSettings(1000, 150, 40, 0, 10, 0, 2);
        envSet.occupGenerator = new HyperGeometricFacade(hgSet);
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
                job.resourceRequest.criteria = new MinFinishTimeCriteria();
            }

            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String getData() {
        String data = "\n ============== BACKFILLING PREFERENCE EXPERIMENT ================== \n";

        data += this.bfStatsStart.getData() + "\n"
                + this.bfStatsFinish.getData() + "\n"
                + this.coordStats4.getData() + "\n"
                + this.coordTimeStats4.getData() + "\n"
                + this.coordCostStats4.getData() + "\n"
                + this.coordStats5.getData() + "\n"
                + this.coordTimeStats5.getData() + "\n"
                + this.coordCostStats5.getData() + "\n"
                + this.coordStats6.getData() + "\n"
                + this.coordTimeStats6.getData() + "\n"
                + this.coordCostStats6.getData() + "\n"
                ;

        return data;
    }

}
