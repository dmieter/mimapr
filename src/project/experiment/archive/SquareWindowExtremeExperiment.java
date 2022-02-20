package project.experiment.archive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import project.engine.alternativeStats.AlternativesExtremeStats;
import project.engine.data.ComputingNode;
import project.engine.data.ComputingResourceLine;
import project.engine.data.ResourceRequest;
import project.engine.data.Slot;
import project.engine.data.UserJob;
import project.engine.data.VOEHelper;
import project.engine.data.VOEnvironment;
import project.engine.data.environmentGenerator.EnvironmentGenerator;
import project.engine.data.environmentGenerator.EnvironmentGeneratorSettings;
import project.engine.data.environmentGenerator.EnvironmentPricingSettings;
import project.engine.data.jobGenerator.JobGenerator;
import project.engine.data.jobGenerator.JobGeneratorSettings;
import project.engine.slot.slotProcessor.SimpleSquareWindowFinder;
import project.engine.slot.slotProcessor.SlotProcessorSettings;
import project.engine.slot.slotProcessor.SquareWindowFinder;
import project.engine.slot.slotProcessor.criteriaHelpers.MaxAdditiveUserValuationCriterion;
import project.engine.slot.slotProcessor.criteriaHelpers.MaxSumQCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinFinishTimeCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinRunTimeCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinStartTimeCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinSumCostCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinSumTimeCriteria;
import project.engine.slot.slotProcessor.userRankings.PercentileUserRanking;
import project.engine.slot.slotProcessor.userRankings.UserRanking;
import project.experiment.Experiment;
import project.math.distributions.GaussianFacade;
import project.math.distributions.GaussianSettings;
import project.math.distributions.HyperGeometricFacade;
import project.math.distributions.HyperGeometricSettings;

/**
 *
 * @author emelyanov
 */
public class SquareWindowExtremeExperiment extends Experiment {

    public AlternativesExtremeStats minCostStats;
    public AlternativesExtremeStats minRuntimeStats;
    public AlternativesExtremeStats minProcTimeStats;
    public AlternativesExtremeStats minStartStats;
    public AlternativesExtremeStats minFinishStats;
    public AlternativesExtremeStats commonStats;
    public AlternativesExtremeStats qStats;

    public AlternativesExtremeStats[] qStatsArray;
    public AlternativesExtremeStats[] commonStatsArray;
    public AlternativesExtremeStats[] finStatsArray;

    public int[] envNodesVals = {30, 40, 60, 80, 100, 150, 200};
    public int[] envLengthVals = {400, 800, 1200, 1600, 2000, 4000, 6000};
    public int[] jobLengthVals = {200, 400, 600, 800, 1000, 1200, 1600};
    public int[] jobNodesVals = {1, 2, 4, 6, 8, 10, 12, 15, 17, 20, 25, 30};
    public int[] jobPriceVals = {1, 2, 4, 5};

    public ArrayList<UserJob> batchToShow;
    public VOEnvironment envToShow;

    protected int cycleLength = 1200;
    protected SquareWindowFinder simpleFinder = null;

    public SquareWindowExtremeExperiment() {
        minCostStats = new AlternativesExtremeStats();
        minRuntimeStats = new AlternativesExtremeStats();
        minProcTimeStats = new AlternativesExtremeStats();
        minStartStats = new AlternativesExtremeStats();
        minFinishStats = new AlternativesExtremeStats();
        commonStats = new AlternativesExtremeStats();
        qStats = new AlternativesExtremeStats();

        qStatsArray = new AlternativesExtremeStats[envLengthVals.length];
        commonStatsArray = new AlternativesExtremeStats[envLengthVals.length];
        finStatsArray = new AlternativesExtremeStats[envLengthVals.length];

        for (int i = 0; i < envLengthVals.length; i++) {
            qStatsArray[i] = new AlternativesExtremeStats();
            commonStatsArray[i] = new AlternativesExtremeStats();
            finStatsArray[i] = new AlternativesExtremeStats();
        }

    }

    @Override
    public void performExperiments(int expNum) {

        for (int i = 0; i < expNum; i++) {
            System.out.println("Experiment #" + i);
            performFullExperiment();
            //performExperimentSeries();
        }
    }

    public void performFullExperiment() {
        VOEnvironment env = generateNewEnvironment(30);
        envToShow = env;
        ArrayList<UserJob> batch = new ArrayList<>(); //generateJobBatch();
        generateJobBatch();
        batchToShow = batch;

        SlotProcessorSettings sps = new SlotProcessorSettings();
        sps.cycleStart = 0;
        sps.cycleLength = cycleLength;
        simpleFinder = new SimpleSquareWindowFinder();

        ResourceRequest rr = new ResourceRequest(7, 800, 0.115, 1);
        UserJob job = new UserJob(0, "Job", rr, null, 0);
        job.id = 0;

        long multT, procT, finT, runT, startT, costT, qT;
        boolean success = true;

        /* Min Processor Time */
//        System.out.println("MIN PROC\n");
        UserJob procJob = job.clone();
        procJob.id = 2;
        procJob.name = "jProc";
        procJob.resourceRequest.criteria = new MinSumTimeCriteria();
        ArrayList<UserJob> procBatch = new ArrayList<>(Arrays.asList(procJob));
        VOEnvironment procVOE = VOEHelper.copyEnvironment(env);
        procT = System.nanoTime();
        simpleFinder.findAlternatives(procBatch, procVOE, sps, 1);
        procT = System.nanoTime() - procT;
        if (procBatch.get(0).alternatives.isEmpty()) {
            success = false;
            minProcTimeStats.failsNum++;
            System.out.println("MIN PROC FAILED");
        }

        /* Min Finish */
//        System.out.println("MIN FIN\n");
        UserJob finJob = job.clone();
        finJob.id = 3;
        finJob.name = "jFin";
        finJob.resourceRequest.criteria = new MinFinishTimeCriteria();
        ArrayList<UserJob> finBatch = new ArrayList<>(Arrays.asList(finJob));
        VOEnvironment finVOE = VOEHelper.copyEnvironment(env);
        finT = System.nanoTime();
        simpleFinder.findAlternatives(finBatch, finVOE, sps, 1);
        finT = System.nanoTime() - finT;
        if (finBatch.get(0).alternatives.isEmpty()) {
            success = false;
            minFinishStats.failsNum++;
            System.out.println("MIN FIN FAILED");
        }

        /* Min Runtime */
//        System.out.println("MIN RUN\n");
        UserJob runJob = job.clone();
        runJob.id = 4;
        runJob.name = "jRun";
        runJob.resourceRequest.criteria = new MinRunTimeCriteria();
        ArrayList<UserJob> runBatch = new ArrayList<>(Arrays.asList(runJob));
        VOEnvironment runVOE = VOEHelper.copyEnvironment(env);
        runT = System.nanoTime();
        simpleFinder.findAlternatives(runBatch, runVOE, sps, 1);
        runT = System.nanoTime() - runT;
        if (runBatch.get(0).alternatives.isEmpty()) {
            success = false;
            minRuntimeStats.failsNum++;
            System.out.println("MIN RUN FAILED");
        }

        /* Min Start Time */
//        System.out.println("MIN START\n");
        UserJob startJob = job.clone();
        startJob.id = 5;
        startJob.name = "jStart";
        startJob.resourceRequest.criteria = new MinStartTimeCriteria();
        ArrayList<UserJob> startBatch = new ArrayList<>(Arrays.asList(startJob));
        VOEnvironment startVOE = VOEHelper.copyEnvironment(env);
        startT = System.nanoTime();
        simpleFinder.findAlternatives(startBatch, startVOE, sps, 1);
        startT = System.nanoTime() - startT;
        if (startBatch.get(0).alternatives.isEmpty()) {
            success = false;
            minStartStats.failsNum++;
            System.out.println("MIN START FAILED");
        }

        /* Min Cost */
//        System.out.println("MIN COST\n");
        UserJob costJob = job.clone();
        costJob.id = 6;
        costJob.name = "jCost";
        costJob.resourceRequest.criteria = new MinSumCostCriteria();
        ArrayList<UserJob> costBatch = new ArrayList<>(Arrays.asList(costJob));
        VOEnvironment costVOE = VOEHelper.copyEnvironment(env);
        costT = System.nanoTime();
        simpleFinder.findAlternatives(costBatch, costVOE, sps, 1);
        costT = System.nanoTime() - costT;
        if (costBatch.get(0).alternatives.isEmpty()) {
            success = false;
            minCostStats.failsNum++;
            System.out.println("MIN COST FAILED");
        }

        /* Max Q */
//        System.out.println("MAX Q\n");
        UserJob qJob = job.clone();
        qJob.id = 7;
        qJob.name = "jQ";
        qJob.resourceRequest.criteria = new MaxAdditiveUserValuationCriterion();
        ArrayList<UserJob> qBatch = new ArrayList<>(Arrays.asList(qJob));
        VOEnvironment qVOE = VOEHelper.copyEnvironment(env);
        qT = System.nanoTime();
        simpleFinder.findAlternatives(qBatch, qVOE, sps, 1);
        qT = System.nanoTime() - qT;
        if (qBatch.get(0).alternatives.isEmpty()) {
            success = false;
            qStats.failsNum++;
            System.out.println("MAX Q FAILED");
        }

        /* Searching for multiple alternatives */
//        System.out.println("MULTIPLE Search\n");
        UserJob multJob = job.clone();
        multJob.id = 1;
        multJob.name = "jMult";
        multJob.resourceRequest.criteria = new MinStartTimeCriteria();
        ArrayList<UserJob> multBatch = new ArrayList<>(Arrays.asList(multJob));
        VOEnvironment multVOE = VOEHelper.copyEnvironment(env);
        multT = System.nanoTime();
        simpleFinder.findAlternatives(multBatch, multVOE, sps);
        multT = System.nanoTime() - multT;
        if (multBatch.get(0).alternatives.isEmpty()) {
            success = false;
            commonStats.failsNum++;
            System.out.println("MULTIPLE Search FAILED");
        }

        if (success) {
            minRuntimeStats.processAlternatives(runBatch.get(0).alternatives, runT);
            minCostStats.processAlternatives(costBatch.get(0).alternatives, costT);
            minProcTimeStats.processAlternatives(procBatch.get(0).alternatives, procT);
            minStartStats.processAlternatives(startBatch.get(0).alternatives, startT);
            minFinishStats.processAlternatives(finBatch.get(0).alternatives, finT);
            commonStats.processAlternatives(multBatch.get(0).alternatives, multT);
            qStats.processAlternatives(qBatch.get(0).alternatives, qT);

            ArrayList showBatch = new ArrayList(7);
            showBatch.addAll(multBatch);
            showBatch.addAll(procBatch);
            showBatch.addAll(finBatch);
            showBatch.addAll(runBatch);
            showBatch.addAll(startBatch);
            showBatch.addAll(costBatch);
            showBatch.addAll(qBatch);

            batchToShow = showBatch;
            envToShow = env;

        }

    }

    public void performExperimentSeries() {

        ArrayList<UserJob> batch = new ArrayList<>(); //generateJobBatch();
        batchToShow = batch;

        SlotProcessorSettings sps = new SlotProcessorSettings();
        sps.cycleStart = 0;
        sps.cycleLength = cycleLength;
        simpleFinder = new SimpleSquareWindowFinder();

        for (int i = 0; i < envLengthVals.length; i++) {

            int envNodes = 100;//envNodesVals[i];
            int jobNodes = 7;//jobNodesVals[i];
            int jobLength = 800;
            cycleLength = envLengthVals[i];
            sps.cycleLength = cycleLength;
            Double jobPrice = 0.115;
            System.out.println(envLengthVals[i]);
            
            ResourceRequest rr = new ResourceRequest(jobNodes, jobLength, jobPrice, 1);
            UserJob job = new UserJob(0, "Job", rr, null, 0);
            job.id = 0;

            VOEnvironment env = generateNewEnvironment(envNodes);
            envToShow = env;

            long multT, finT, qT;
            boolean success = true;

            /* Max Q Lite */
//        System.out.println("Max Q Lite\n");
            UserJob finJob = job.clone();
            finJob.id = 3;
            finJob.name = "jQLite";
            finJob.resourceRequest.criteria = new MaxSumQCriteria();
            ArrayList<UserJob> finBatch = new ArrayList<>(Arrays.asList(finJob));
            VOEnvironment finVOE = VOEHelper.copyEnvironment(env);
            finT = System.nanoTime();
            simpleFinder.findAlternatives(finBatch, finVOE, sps, 1);
            finT = System.nanoTime() - finT;
            if (finBatch.get(0).alternatives.isEmpty()) {
                success = false;
                finStatsArray[i].failsNum++;
                System.out.println("Max Q Lite FAILED");
            }

            /* Max Q */
//        System.out.println("MAX Q\n");
            UserJob qJob = job.clone();
            qJob.id = 7;
            qJob.name = "jQ";
            qJob.resourceRequest.criteria = new MaxAdditiveUserValuationCriterion();
            ArrayList<UserJob> qBatch = new ArrayList<>(Arrays.asList(qJob));
            VOEnvironment qVOE = VOEHelper.copyEnvironment(env);
            qT = System.nanoTime();
            simpleFinder.findAlternatives(qBatch, qVOE, sps, 1);
            qT = System.nanoTime() - qT;
            if (qBatch.get(0).alternatives.isEmpty()) {
                success = false;
                qStatsArray[i].failsNum++;
                System.out.println("MAX Q FAILED");
            }

            /* Searching for multiple alternatives */
//        System.out.println("MULTIPLE Search\n");
            UserJob multJob = job.clone();
            multJob.id = 1;
            multJob.name = "jMult";
            multJob.resourceRequest.criteria = new MinSumCostCriteria();
            ArrayList<UserJob> multBatch = new ArrayList<>(Arrays.asList(multJob));
            VOEnvironment multVOE = VOEHelper.copyEnvironment(env);
            multT = System.nanoTime();
            simpleFinder.findAlternatives(multBatch, multVOE, sps);
            multT = System.nanoTime() - multT;
            if (multBatch.get(0).alternatives.isEmpty()) {
                success = false;
                commonStatsArray[i].failsNum++;
                System.out.println("MULTIPLE Search FAILED");
            }
            
            if (success) {
                finStatsArray[i].processAlternatives(finBatch.get(0).alternatives, finT);
                commonStatsArray[i].processAlternatives(multBatch.get(0).alternatives, multT);
                qStatsArray[i].processAlternatives(qBatch.get(0).alternatives, qT);

                ArrayList showBatch = new ArrayList(3);
                showBatch.addAll(multBatch);
                showBatch.addAll(finBatch);
                showBatch.addAll(qBatch);

                batchToShow = showBatch;
                envToShow = env;

            }
        }

    }

    private VOEnvironment generateNewEnvironment(int nodesNumber) {
        //Creating resourceso
        EnvironmentGeneratorSettings envSet = new EnvironmentGeneratorSettings();
        envSet.minResourceSpeed = 2;
        envSet.maxResourceSpeed = 11;
        envSet.resourceLineNum = nodesNumber;
        envSet.maxTaskLength = 50;
        envSet.minTaskLength = 1;
        //envSet.occupancyLevel = 1;
        //HyperGeometricSettings hgSet = new HyperGeometricSettings(1000, 400, 40, 0, 80, 0, 2);
        //envSet.occupGenerator = new HyperGeometricFacade(hgSet);
        GaussianSettings gs = new GaussianSettings(0.9, 1.5, 2);
        envSet.occupGenerator = new GaussianFacade(gs);
        envSet.timeInterval = cycleLength * 4;
        //envSet.hgPerfSet = new HyperGeometricSettings(1000, 60, 100, 1);   //mean = 6.0 e = 2.254125347242491
        EnvironmentGenerator envGen = new EnvironmentGenerator();
        EnvironmentPricingSettings epc = new EnvironmentPricingSettings();
        epc.priceQuotient = 0.1;
        epc.priceMutationFactor = 0.6;
        epc.speedExtraCharge = 0.02;

        ArrayList<ComputingNode> lines = envGen.generateResourceTypes(envSet);

        //creating environment
        VOEnvironment env = envGen.generate(envSet, lines);
        env.applyPricing(epc);

        return env;
    }

    private ArrayList<UserJob> generateJobBatch() {
        JobGenerator jg = new JobGenerator();
        JobGeneratorSettings jgs = new JobGeneratorSettings();
        jgs.taskNumber = 5;

        jgs.minPrice = 0.3;
        jgs.maxPrice = 0.6;
        jgs.useSpeedPriceFactor = true;

        jgs.minTime = 100;
        jgs.maxTime = 600;

        jgs.minSpeed = 1;
        jgs.maxSpeed = 1;

        jgs.minCPU = 7;
        jgs.maxCPU = 7;

        GaussianSettings gs = new GaussianSettings(0.2, 0.6, 1);
        jgs.timeCorrectiveCoefGen = new GaussianFacade(gs);

        ArrayList<UserJob> jobs = jg.generate(jgs);

        UserRanking ur = new PercentileUserRanking();
        for (UserJob job : jobs) {
            job.rankingAlgorithm = ur;
            job.resourceRequest.criteria = new MaxAdditiveUserValuationCriterion();
        }

        return jobs;
    }

    public String getData() {
        String data = "EXTREME EXPERIMENT\n"
                + "Common Stats\n"
                + this.commonStats.getData() + "\n"
                + "MinStart Stats\n"
                + this.minStartStats.getData() + "\n"
                + "MinRunTime Stats\n"
                + this.minRuntimeStats.getData() + "\n"
                + "MinFinish Stats\n"
                + this.minFinishStats.getData() + "\n"
                + "Minproctime Stats\n"
                + this.minProcTimeStats.getData() + "\n"
                + "MinCost Stats\n"
                + this.minCostStats.getData() + "\n"
                + "Max Q Stats\n"
                + this.qStats.getData();

        return data + getDataSeries();
    }

    public String getDataSeries() {
        String data = "EXTREME EXPERIMENT SERIES\n";

        for (int i = 0; i < envLengthVals.length; i++) {
            data += "\nLENGTH = " + envLengthVals[i] + "\n\n"
                    + "Common Stats\n"
                    + this.commonStatsArray[i].getData() + "\n"
                    + "MinFin Stats\n"
                    + this.finStatsArray[i].getData() + "\n"
                    + "Max Q Stats\n"
                    + this.qStatsArray[i].getData();
        }

        return data;
    }

}
