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
public class CoordinatedVsBackfillingExperiment extends Experiment {

    private final int cycleLength = 3300;
    private final int expJobsNumber = 150;
    private final int expNodesNumber = 32;
    private final int jobGenerationInterval = 0;

    protected SimpleSquareWindowFinder sqwFinder;
    protected SlotProcessorSettings sps;

    public ArrayList<UserJob> batchToShow;
    public VOEnvironment envToShow;

    protected SchedulingResultsStats coord1Stats;
    protected SchedulingResultsStats coord2Stats;
    protected SchedulingResultsStats coord3Stats;
    protected SchedulingResultsStats coordBFStats;
    protected SchedulingResultsStats bfStatsFinish;
    protected SchedulingResultsStats bfStatsFinishCheat;
    protected SchedulingResultsStats bfStatsStart;
    
    /* NEW STATS */
    protected NamedStats namedStats;
    SchedulingResultsCalculator resultCalculator;

    protected void configureExperiment() {
        //coord1Stats = new SchedulingResultsStats("COORDINATED 1 STATS");
        //coord1Stats = new SchedulingResultsStats("SIEGEL SIMPLE STATS");
        coord2Stats = new SchedulingResultsStats("COORDINATED INFO STATS");
        coord3Stats = new SchedulingResultsStats("COORDINATED STATS");
        //coordBFStats = new SchedulingResultsStats("COORDINATED BF STATS");
        coordBFStats = new SchedulingResultsStats("SIEGEL STATS");
        bfStatsFinish = new SchedulingResultsStats("BF FINISH STATS");
        bfStatsFinishCheat = new SchedulingResultsStats("BF FINISH CHEAT STATS");
        bfStatsStart = new SchedulingResultsStats("BF START STATS");
        
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
        
        /* Simple backfilling solution with finish time criterion */
        System.out.println("BF-FINISH CHEAT scheduling");
        ArrayList<UserJob> batchBFFinishCheat = VOEHelper.copyJobBatchList(batch);
        for (UserJob job : batchBFFinishCheat) {
            job.resourceRequest.criteria = new MinFinishTimeCriteria();
            job.resourceRequest.volume *= 0.99; 
        }

        bfp.schedule(batchBFFinishCheat, env);
        //sqwFinder.findAlternatives(batchBFFinishCheat, env, sps, 1);
        for (UserJob job : batchBFFinishCheat) {
            if (!job.alternatives.isEmpty()) {
                job.bestAlternative = 0;
            }
        }
        if (!SchedulingResultsStats.checkBatchForSuccess(batchBFFinishCheat)) {
            System.out.println("BF-FINISH CHEAT failed to find alternatives");
            bfStatsFinishCheat.addFailExperiment();
            success = false;
        }

        flush();

        /* Simple backfilling solution with start time criterion */
        System.out.println("BF-START scheduling");
        ArrayList<UserJob> batchBFStart = VOEHelper.copyJobBatchList(batch);
        for (UserJob job : batchBFStart) {
            job.resourceRequest.criteria = new MinStartTimeCriteria();
        }

        bfp.schedule(batchBFStart, env);
        //sqwFinder.findAlternatives(batchBFStart, env, sps, 1);
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
//        System.out.println("SIEGEL BF scheduling");
//        ArrayList<UserJob> batchCoordBF = VOEHelper.copyJobBatchList(batch);
//        for (UserJob job : batchCoordBF) {
//            ValuationModelCustom valuationModel = new ValuationModelCustom(ValuationModelCustom::complexValuationSiegel);//minFinishValuation);
//            MaxAdditiveUserValuationCriterion mauvCriterion = new MaxAdditiveUserValuationCriterion(valuationModel);
//            mauvCriterion.provideSearchStopCondition((cMax,cCur) -> (cMax > cCur*0.9));
//            job.resourceRequest.criteria = mauvCriterion;
//        }
//
//        bfp.schedule(batchCoordBF, env);
//        //sqwFinder.findAlternatives(batchCoordBF, env, sps, 1);
//        for (UserJob job : batchCoordBF) {
//            if (!job.alternatives.isEmpty()) {
//                job.bestAlternative = 0;
//            }
//        }
//        if (!SchedulingResultsStats.checkBatchForSuccess(batchCoordBF)) {
//            System.out.println("SIEGEL BF failed to find alternatives");
//            coordBFStats.addFailExperiment();
//            success = false;
//        }
//        
//        flush();
//        System.out.println(new Date());
        
        

        
        /* INFO-based coordinated solution*/
//        System.out.println("COORDINATED INFO STATS scheduling");
//        ArrayList<UserJob> batchCoord2 = VOEHelper.copyJobBatchList(batch);
//        for (UserJob job : batchCoord2) {
////            ValuationModelCustom valuationModel = new ValuationModelCustom(ValuationModelCustom::complexValuation12);
//            ValuationModelCustomStats valuationModel = new ValuationModelCustomStats(ValuationModelCustom::complexValuation5);
//            valuationModel.setRichJob(job.resourceRequest.maxNodePrice > 1.9);
//            valuationModel.setShortJob(job.resourceRequest.volume < 180);
//            MaxAdditiveUserValuationCriterion mauvCriterion = new MaxAdditiveUserValuationCriterion(valuationModel);
//            mauvCriterion.provideSearchStopCondition((cMax,cCur) -> (cMax > cCur*0.9));
//            job.resourceRequest.criteria = mauvCriterion;
//        }
//
//        bfp.schedule(batchCoord2, env);
//        //sqwFinder.findAlternatives(batchCoord2, env, sps, 1);
//        for (UserJob job : batchCoord2) {
//            if (!job.alternatives.isEmpty()) {
//                job.bestAlternative = 0;
//            }
//        }
//        if (!SchedulingResultsStats.checkBatchForSuccess(batchCoord2)) {
//            System.out.println("COORDINATED INFO STATS failed to find alternatives");
//            coord2Stats.addFailExperiment();
//            success = false;
//        }
//        
//        flush();
//        System.out.println(new Date());
        
        /* Simple coordinated solution*/
        System.out.println("COORDINATED 3 scheduling");
        ArrayList<UserJob> batchCoord3 = VOEHelper.copyJobBatchList(batch);
        for (UserJob job : batchCoord3) {
            ValuationModelCustom valuationModel = new ValuationModelCustom(ValuationModelCustom::complexValuation5);
            MaxAdditiveUserValuationCriterion mauvCriterion = new MaxAdditiveUserValuationCriterion(valuationModel);
            mauvCriterion.provideSearchStopCondition((cMax,cCur) -> (cMax > cCur*0.9));
            job.resourceRequest.criteria = mauvCriterion;
        }

        complexT = System.nanoTime() - simpleT;
        bfp.schedule(batchCoord3, env);
        //sqwFinder.findAlternatives(batchCoord3, env, sps, 1);
        complexT = System.nanoTime() - complexT;
        for (UserJob job : batchCoord3) {
            if (!job.alternatives.isEmpty()) {
                job.bestAlternative = 0;
            }
        }
        if (!SchedulingResultsStats.checkBatchForSuccess(batchCoord3)) {
            System.out.println("COORDINATED 3 failed to find alternatives");
            coord3Stats.addFailExperiment();
            success = false;
        }
             
        System.out.println(new Date());
        
      
        if (success) {

            bfStatsFinish.processResults(batchBFFinish);
            bfStatsFinishCheat.processResults(batchBFFinishCheat);
            bfStatsStart.processResults(batchBFStart, simpleT);
            //coordBFStats.processResults(batchCoordBF);
            //coord1Stats.processResults(batchCoord);
            //coord2Stats.processResults(batchCoord2);
            coord3Stats.processResults(batchCoord3, complexT);
            
            resultCalculator.calculateResults(batchBFFinish);
            Double bfFinishTime = resultCalculator.getAvFinish();
            namedStats.addValue("BF Finish Time", resultCalculator.getAvFinish());
            namedStats.addValue("BF Makespan", resultCalculator.getMaxFinishTime());
            
            resultCalculator.calculateResults(batchCoord3);
            Double copFinishTime = resultCalculator.getAvFinish();
            namedStats.addValue("CoP Finish Time", resultCalculator.getAvFinish());
            namedStats.addValue("CoP Makespan", resultCalculator.getMaxFinishTime());
            
            namedStats.addValue("CoP Finish Time Advantage", (bfFinishTime-copFinishTime)/copFinishTime);
            
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
        String data = "\n ============== WE WILL BEAT BACKFILLING EXPERIMENT ================== \n";

        data += this.bfStatsStart.getData() + "\n"
                + this.bfStatsFinish.getData() + "\n"
                + this.bfStatsFinishCheat.getData() + "\n"
                //+ this.coordBFStats.getData() + "\n"
                //+ this.coord1Stats.getData() + "\n"
                //+ this.coord2Stats.getData() + "\n"
                + this.coord3Stats.getData() + "\n";

        data += "Custom length hits: " + ValuationModelCustom.lengthCounter;
        data += "\nStats positive length hits: " + ValuationModelCustomStats.plusDistCounter;
        data += "\nStats negative length hits: " + ValuationModelCustomStats.negDistCounter;
        data += "\nStats cost hits: " + ValuationModelCustomStats.costCounter;
        data += namedStats.getData();
        return data;
    }

}
