package project.experiment.archive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import project.engine.alternativeStats.AlternativesExtremeStats;
import project.engine.alternativeStats.AlternativesExtremeStatsEnv;
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
import project.engine.slot.slotProcessor.criteriaHelpers.MaxDistCriterion;
import project.engine.slot.slotProcessor.criteriaHelpers.MaxSumQCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinFinishTimeCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinRunTimeCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinStartTimeCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinSumCostCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinSumTimeCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.ValuationModel;
import project.engine.slot.slotProcessor.criteriaHelpers.ValuationModelAltroTime;
import project.engine.slot.slotProcessor.criteriaHelpers.ValuationModelEgoTime;
import project.engine.slot.slotProcessor.criteriaHelpers.ValuationModelTimeDistance;
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
public class SquareWindowEgoExperiment extends Experiment {

    public AlternativesExtremeStatsEnv minCostStats;
    public AlternativesExtremeStatsEnv commonStats;
    public AlternativesExtremeStatsEnv egoStats;
    public AlternativesExtremeStatsEnv egoLiteStats;
    public AlternativesExtremeStatsEnv altroStats;
    public AlternativesExtremeStatsEnv altroLiteStats;
    
    public ArrayList<UserJob> batchToShow;
    public VOEnvironment envToShow;

    protected final int cycleLength = 1200;
    protected SquareWindowFinder simpleFinder = null;

    public SquareWindowEgoExperiment() {
        minCostStats = new AlternativesExtremeStatsEnv();
        commonStats = new AlternativesExtremeStatsEnv();
        egoStats = new AlternativesExtremeStatsEnv();
        egoLiteStats = new AlternativesExtremeStatsEnv();
        altroStats = new AlternativesExtremeStatsEnv();
        altroLiteStats = new AlternativesExtremeStatsEnv();
    }

    @Override
    public void performExperiments(int expNum) {

        for (int i = 0; i < expNum; i++) {
            System.out.println("Experiment #" + i);
            performFullExperiment();
        }
    }

    public void performFullExperiment() {
        VOEnvironment env = generateNewEnvironment(100);
        env.intervalEndTime = cycleLength;
        envToShow = env;
        ArrayList<UserJob> batch = new ArrayList<>(); //generateJobBatch();
        //generateJobBatch();
        batchToShow = batch;

        SlotProcessorSettings sps = new SlotProcessorSettings();
        sps.cycleStart = 0;
        sps.cycleLength = cycleLength;
        simpleFinder = new SimpleSquareWindowFinder();

        ResourceRequest rr = new ResourceRequest(7, 800, 0.115, 1);
        UserJob job = new UserJob(0, "Job", rr, null, 0);
        job.id = 0;

        long multT, egoT, costT, egoLiteT, altroT, altroLiteT;
        boolean success = true;

        /* Min Cost */
        //System.out.println("MIN COST");
        UserJob costJob = job.clone();
        costJob.id = 2;
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

       

        /* Min Distance Ego */
        //System.out.println("EGO");
        UserJob egoJob = job.clone();
        egoJob.id = 3;
        egoJob.name = "jEgo";
        egoJob.resourceRequest.criteria = new MaxAdditiveUserValuationCriterion(new ValuationModelEgoTime());
        ArrayList<UserJob> egoBatch = new ArrayList<>(Arrays.asList(egoJob));
        VOEnvironment egoVOE = VOEHelper.copyEnvironment(env);
        egoT = System.nanoTime();
        simpleFinder.findAlternatives(egoBatch, egoVOE, sps, 1);
        egoT = System.nanoTime() - egoT;
        if (egoBatch.get(0).alternatives.isEmpty()) {
            success = false;
            egoStats.failsNum++;
            System.out.println("EGO FAILED");
        }
        
        /* Min Distance Ego Lite*/
        //System.out.println("EGO Lite");
        UserJob egoLiteJob = job.clone();
        egoLiteJob.id = 4;
        egoLiteJob.name = "jEgoLite";
        egoLiteJob.resourceRequest.criteria = new MaxDistCriterion(new ValuationModelEgoTime());
        ArrayList<UserJob> egoLiteBatch = new ArrayList<>(Arrays.asList(egoLiteJob));
        VOEnvironment egoLiteVOE = VOEHelper.copyEnvironment(env);
        egoLiteT = System.nanoTime();
        simpleFinder.findAlternatives(egoLiteBatch, egoLiteVOE, sps, 1);
        egoLiteT = System.nanoTime() - egoLiteT;
        if (egoLiteBatch.get(0).alternatives.isEmpty()) {
            success = false;
            egoLiteStats.failsNum++;
            System.out.println("EGO LITE FAILED");
        }
        
        
        /* Min Distance Altro */
        //System.out.println("Altro");
        UserJob altroJob = job.clone();
        altroJob.id = 5;
        altroJob.name = "jAltro";
        altroJob.resourceRequest.criteria = new MaxAdditiveUserValuationCriterion(new ValuationModelAltroTime());
        ArrayList<UserJob> altroBatch = new ArrayList<>(Arrays.asList(altroJob));
        VOEnvironment altroVOE = VOEHelper.copyEnvironment(env);
        altroT = System.nanoTime();
        simpleFinder.findAlternatives(altroBatch, altroVOE, sps, 1);
        altroT = System.nanoTime() - altroT;
        if (altroBatch.get(0).alternatives.isEmpty()) {
            success = false;
            altroStats.failsNum++;
            System.out.println("ALTRO FAILED");
        }
        
        /* Min Distance Altro Lite*/
        //System.out.println("Altro Lite");
        UserJob altroLiteJob = job.clone();
        altroLiteJob.id = 6;
        altroLiteJob.name = "jAltroLite";
        altroLiteJob.resourceRequest.criteria = new MaxDistCriterion(new ValuationModelAltroTime());
        ArrayList<UserJob> altroLiteBatch = new ArrayList<>(Arrays.asList(altroLiteJob));
        VOEnvironment altroLiteVOE = VOEHelper.copyEnvironment(env);
        altroLiteT = System.nanoTime();
        simpleFinder.findAlternatives(altroLiteBatch, altroLiteVOE, sps, 1);
        altroLiteT = System.nanoTime() - altroLiteT;
        if (altroLiteBatch.get(0).alternatives.isEmpty()) {
            success = false;
            altroLiteStats.failsNum++;
            System.out.println("ALTRO LITE FAILED");
        }
        

        /* Searching for multiple alternatives */
        //System.out.println("MULTIPLE Search");
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
            minCostStats.processAlternatives(costBatch.get(0).alternatives, env, costT);
            commonStats.processAlternatives(multBatch.get(0).alternatives, env, multT);
            egoStats.processAlternatives(egoBatch.get(0).alternatives, env, egoT);
            egoLiteStats.processAlternatives(egoLiteBatch.get(0).alternatives, env, egoLiteT);
            altroStats.processAlternatives(altroBatch.get(0).alternatives, env, altroT);
            altroLiteStats.processAlternatives(altroLiteBatch.get(0).alternatives, env, altroLiteT);

            ArrayList showBatch = new ArrayList(3);
            showBatch.addAll(multBatch);
            showBatch.addAll(costBatch);
            showBatch.addAll(egoBatch);
            showBatch.addAll(egoLiteBatch);
            showBatch.addAll(altroBatch);
            showBatch.addAll(altroLiteBatch);

            batchToShow = showBatch;
            envToShow = env;

        }

    }

    

    private VOEnvironment generateNewEnvironment(int nodesNumber) {
        //Creating resources
        EnvironmentGeneratorSettings envSet = new EnvironmentGeneratorSettings();
        envSet.minResourceSpeed = 2;
        envSet.maxResourceSpeed = 11;
        envSet.resourceLineNum = nodesNumber;
        envSet.maxTaskLength = 150;
        envSet.minTaskLength = 10;
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
            job.resourceRequest.criteria = new MaxAdditiveUserValuationCriterion(new ValuationModelEgoTime());
        }

        return jobs;
    }

    public String getData() {
        String data = "EGO EXPERIMENT\n"
                + "Common Stats\n"
                + this.commonStats.getData() + "\n"
                + "MinStart Stats\n"
                + this.minCostStats.getData() + "\n"
                + "Ego Lite Stats\n"
                + this.egoLiteStats.getData() + "\n"
                + "Ego Stats\n"
                + this.egoStats.getData() + "\n"
                + "Altro Lite Stats\n"
                + this.altroLiteStats.getData() + "\n"
                + "Altro Stats\n"
                + this.altroStats.getData();

        return data;
    }

}
