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
public class SquareWindowExtremeOptExperiment extends Experiment {

    public AlternativesExtremeStats[] qStatsArray;
    public AlternativesExtremeStats[] qLiteStatsArray;
    public AlternativesExtremeStats[] qOptStatsArray;
    public AlternativesExtremeStats[] commonStatsArray;
    public AlternativesExtremeStats[] firstFitStatsArray;

    public int[] envNodesVals = {30, 40, 60, 80, 100, 150, 200};
    public int[] envLengthVals = {400, 800, 1200, 1600, 2000, 4000, 6000};
    public int[] jobLengthVals = {200, 400, 600, 800, 1000, 1200, 1600};
    public int[] jobNodesVals = {1, 2, 4, 6, 8, 10, 12, 15};
    public int[] jobPriceVals = {1, 2, 4, 5};

    public ArrayList<UserJob> batchToShow;
    public VOEnvironment envToShow;

    protected int cycleLength = 1200;
    protected SquareWindowFinder simpleFinder = null;

    public SquareWindowExtremeOptExperiment() {

        qStatsArray = new AlternativesExtremeStats[jobNodesVals.length];
        commonStatsArray = new AlternativesExtremeStats[jobNodesVals.length];
        qLiteStatsArray = new AlternativesExtremeStats[jobNodesVals.length];
        qOptStatsArray = new AlternativesExtremeStats[jobNodesVals.length];
        firstFitStatsArray = new AlternativesExtremeStats[jobNodesVals.length];

        for (int i = 0; i < jobNodesVals.length; i++) {
            qStatsArray[i] = new AlternativesExtremeStats();
            commonStatsArray[i] = new AlternativesExtremeStats();
            qLiteStatsArray[i] = new AlternativesExtremeStats();
            qOptStatsArray[i] = new AlternativesExtremeStats();
            firstFitStatsArray[i] = new AlternativesExtremeStats();
        }

    }

    @Override
    public void performExperiments(int expNum) {

        for (int i = 0; i < expNum; i++) {
            System.out.println("Experiment #" + i);
            performExperimentSeries();
        }
    }

    public void performExperimentSeries() {

        ArrayList<UserJob> batch = new ArrayList<>(); //generateJobBatch();
        batchToShow = batch;

        SlotProcessorSettings sps = new SlotProcessorSettings();
        sps.cycleStart = 0;
        sps.cycleLength = cycleLength;
        simpleFinder = new SimpleSquareWindowFinder();

        for (int i = 0; i < jobNodesVals.length; i++) {

            int envNodes = 100;//envNodesVals[i];
            int jobNodes = jobNodesVals[i];// 7;//jobNodesVals[i];
            int jobLength = 800;
            cycleLength = 1200;//envLengthVals[i];
            sps.cycleLength = cycleLength;
            Double jobPrice = 0.115;
            System.out.println(jobNodesVals[i]);

            ResourceRequest rr = new ResourceRequest(jobNodes, jobLength, jobPrice, 1);
            UserJob job = new UserJob(0, "Job", rr, null, 0);
            job.id = 0;

            VOEnvironment env = generateNewEnvironment(envNodes);
            envToShow = env;

            long multT, qT, qOptT, qLiteT, firstFitT;
            boolean success = true;

            /* Max Q Lite */
//        System.out.println("Max Q Lite\n");
            UserJob qLiteJob = job.clone();
            qLiteJob.id = 1;
            qLiteJob.name = "jQLite";
            qLiteJob.resourceRequest.criteria = new MaxSumQCriteria();
            ArrayList<UserJob> qLiteBatch = new ArrayList<>(Arrays.asList(qLiteJob));
            VOEnvironment qLiteVOE = VOEHelper.copyEnvironment(env);
            qLiteT = System.nanoTime();
            simpleFinder = new SimpleSquareWindowFinder();
            simpleFinder.useOptimizedImplementation(false);
            simpleFinder.findAlternatives(qLiteBatch, qLiteVOE, sps, 1);
            qLiteT = System.nanoTime() - qLiteT;
            if (qLiteBatch.get(0).alternatives.isEmpty()) {
                success = false;
                qLiteStatsArray[i].failsNum++;
                System.out.println("Max Q Lite FAILED");
            }

            /* Max Q */
//        System.out.println("MAX Q\n");
            UserJob qJob = job.clone();
            qJob.id = 2;
            qJob.name = "jQ";
            qJob.resourceRequest.criteria = new MaxAdditiveUserValuationCriterion();
            ArrayList<UserJob> qBatch = new ArrayList<>(Arrays.asList(qJob));
            VOEnvironment qVOE = VOEHelper.copyEnvironment(env);
            qT = System.nanoTime();
            simpleFinder = new SimpleSquareWindowFinder();
            simpleFinder.useOptimizedImplementation(false);
            simpleFinder.findAlternatives(qBatch, qVOE, sps, 1);
            qT = System.nanoTime() - qT;
            if (qBatch.get(0).alternatives.isEmpty()) {
                success = false;
                qStatsArray[i].failsNum++;
                System.out.println("MAX Q FAILED");
            }

            /* Max Q Opt*/
//        System.out.println("MAX Q Opt\n");
            UserJob qOptJob = job.clone();
            qOptJob.id = 3;
            qOptJob.name = "jQOpt";
            qOptJob.resourceRequest.criteria = new MaxAdditiveUserValuationCriterion();
            ArrayList<UserJob> qOptBatch = new ArrayList<>(Arrays.asList(qOptJob));
            VOEnvironment qOptVOE = VOEHelper.copyEnvironment(env);
            qOptT = System.nanoTime();
            simpleFinder = new SimpleSquareWindowFinder();
            simpleFinder.useOptimizedImplementation(true);
            simpleFinder.findAlternatives(qOptBatch, qOptVOE, sps, 1);
            qOptT = System.nanoTime() - qOptT;
            if (qOptBatch.get(0).alternatives.isEmpty()) {
                success = false;
                qOptStatsArray[i].failsNum++;
                System.out.println("MAX Q Opt FAILED");
            }

            /* Searching for multiple alternatives */
//        System.out.println("MULTIPLE Search\n");
            UserJob multJob = job.clone();
            multJob.id = 4;
            multJob.name = "jMult";
            multJob.resourceRequest.isFirstFit = true;
            multJob.resourceRequest.criteria = new MinStartTimeCriteria();
            ArrayList<UserJob> multBatch = new ArrayList<>(Arrays.asList(multJob));
            VOEnvironment multVOE = VOEHelper.copyEnvironment(env);
            multT = System.nanoTime();
            simpleFinder = new SimpleSquareWindowFinder();
            simpleFinder.useOptimizedImplementation(false);
            simpleFinder.findAlternatives(multBatch, multVOE, sps);
            multT = System.nanoTime() - multT;
            if (multBatch.get(0).alternatives.isEmpty()) {
                success = false;
                commonStatsArray[i].failsNum++;
                System.out.println("MULTIPLE Search FAILED");
            }
            
            /* First Fit */
//        System.out.println("Max Q Lite\n");
            UserJob firstFitJob = job.clone();
            firstFitJob.id = 5;
            firstFitJob.name = "First Fit";
            firstFitJob.resourceRequest.criteria = new MinStartTimeCriteria();
            ArrayList<UserJob> firstFitBatch = new ArrayList<>(Arrays.asList(firstFitJob));
            VOEnvironment firstFitVOE = VOEHelper.copyEnvironment(env);
            firstFitT = System.nanoTime();
            simpleFinder = new SimpleSquareWindowFinder();
            simpleFinder.useOptimizedImplementation(false);
            simpleFinder.findAlternatives(firstFitBatch, firstFitVOE, sps, 1);
            firstFitT = System.nanoTime() - firstFitT;
            if (firstFitBatch.get(0).alternatives.isEmpty()) {
                success = false;
                firstFitStatsArray[i].failsNum++;
                System.out.println("First Fit FAILED");
            }

            if (success) {
                qLiteStatsArray[i].processAlternatives(qLiteBatch.get(0).alternatives, qLiteT);
                qOptStatsArray[i].processAlternatives(qOptBatch.get(0).alternatives, qOptT);
                commonStatsArray[i].processAlternatives(multBatch.get(0).alternatives, multT);
                qStatsArray[i].processAlternatives(qBatch.get(0).alternatives, qT);
                firstFitStatsArray[i].processAlternatives(firstFitBatch.get(0).alternatives, firstFitT);

                ArrayList showBatch = new ArrayList(3);
                showBatch.addAll(multBatch);
                showBatch.addAll(qLiteBatch);
                showBatch.addAll(qBatch);
                showBatch.addAll(qOptBatch);
                showBatch.addAll(firstFitBatch);

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

    public String getData() {
        String data = "SQUARE EXTREME OPTIMIZED EXPERIMENT SERIES\n";

        for (int i = 0; i < jobNodesVals.length; i++) {
            data += "\nJOB NODES: " + jobNodesVals[i] + "\n\n"
                    + "First Fit Stats\n"
                    + this.firstFitStatsArray[i].getData() + "\n"
                    + "Common Stats\n"
                    + this.commonStatsArray[i].getData() + "\n"
                    + "Max Q Stats\n"
                    + this.qStatsArray[i].getData() + "\n"
                    + "Max Q Opt Stats\n"
                    + this.qOptStatsArray[i].getData() + "\n"
                    + "Max QLite Stats\n"
                    + this.qLiteStatsArray[i].getData();
        }

        return data;
    }

}
