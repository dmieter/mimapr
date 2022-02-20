
package project.experiment.archive;

import java.util.ArrayList;
import java.util.List;
import project.engine.data.ComputingNode;
import project.engine.data.ComputingResourceLine;
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
import project.engine.slot.slotProcessor.criteriaHelpers.MinFinishTimeCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinRunTimeCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinSumCostCriteria;
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
public class SquareWindowExperiment extends Experiment {
    
    public ArrayList<UserJob> batchToShow;
    public VOEnvironment envToShow;
    
    protected final int cycleLength = 800;
    protected SquareWindowFinder simpleFinder = null;
    
    @Override
    public void performExperiments(int expNum) {
        
        for (int i = 0; i < expNum; i++) {
            System.out.println("Experiment #" + i);
            performExperiment();
        }
    }

    @Override
    public String getData() {
        SimpleSquareWindowFinder finder = (SimpleSquareWindowFinder)simpleFinder;
        return "Criterion Value: " + finder.getBestCriterionValue() + "\n"
                         + "Start Time: " + finder.getWindowStartTime()+ "\n"
                         + "Function calls: " + finder.getRetrieveWindowCallsCnt()+ "\n";
    }
    
    public void performExperiment(){
        VOEnvironment env = generateNewEnvironment(10);
        ArrayList<UserJob> batch = generateJobBatch();
        List<Slot> slots = VOEHelper.getSlotsFromVOE(env, 0, cycleLength);
        List<ComputingResourceLine> nodes = env.resourceLines;
        
//        UserJob someJob = batch.get(0);
//        simpleFinder = new SimpleSquareWindowFinder();
//        Window w = simpleFinder.findSquareWindow(someJob, slots, nodes);
//        
//        Alternative a = new Alternative(w);
//        someJob.alternatives.add(a);
//        someJob.bestAlternative = 0;

        SlotProcessorSettings sps = new SlotProcessorSettings();
        sps.cycleStart = 0;
        sps.cycleLength = cycleLength;
        simpleFinder = new SimpleSquareWindowFinder();
        //simpleFinder.findAlternatives(batch, env, sps);
        
        UserJob job1 = batch.get(0);
        UserJob job2 = job1.clone();
        UserJob job3 = job1.clone();
        UserJob job4 = job1.clone();
        
        job1.resourceRequest.criteria = new MinFinishTimeCriteria();
        job2.resourceRequest.criteria = new MinSumCostCriteria();
        job3.resourceRequest.criteria = new MinRunTimeCriteria();
        job4.resourceRequest.criteria = new MaxAdditiveUserValuationCriterion();
        
        ArrayList<UserJob> batch1 = new ArrayList<>(1);
        batch1.add(job1);
        VOEnvironment env1 = VOEHelper.copyEnvironment(env);
        //simpleFinder.findAlternatives(batch1, env1, sps);
        
        ArrayList<UserJob> batch2 = new ArrayList<>(1);
        batch2.add(job2);
        VOEnvironment env2 = VOEHelper.copyEnvironment(env);
        //simpleFinder.findAlternatives(batch2, env2, sps);
        
        ArrayList<UserJob> batch3 = new ArrayList<>(1);
        batch3.add(job3);
        VOEnvironment env3 = VOEHelper.copyEnvironment(env);
        //simpleFinder.findAlternatives(batch3, env3, sps);
        
        ArrayList<UserJob> batch4 = new ArrayList<>(1);
        batch4.add(job4);
        VOEnvironment env4 = VOEHelper.copyEnvironment(env);
        simpleFinder.findAlternatives(batch4, env4, sps);
        
        batchToShow = batch4;
        envToShow = env;
        
    }
    
    private VOEnvironment generateNewEnvironment(int nodesNumber) {
        //Creating resources
        EnvironmentGeneratorSettings envSet = new EnvironmentGeneratorSettings();
        envSet.minResourceSpeed = 2;
        envSet.maxResourceSpeed = 11;
        envSet.resourceLineNum = nodesNumber;
        envSet.maxTaskLength = 100;
        envSet.minTaskLength = 10;
        //envSet.occupancyLevel = 1;
        HyperGeometricSettings hgSet = new HyperGeometricSettings(1000, 150, 30, 0, 10, 0, 2);
        envSet.occupGenerator = new HyperGeometricFacade(hgSet);
        envSet.timeInterval = cycleLength * 4;
        //envSet.hgPerfSet = new HyperGeometricSettings(1000, 60, 100, 1);   //mean = 6.0 e = 2.254125347242491
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
    
    private ArrayList<UserJob> generateJobBatch() {
        JobGenerator jg = new JobGenerator();
        JobGeneratorSettings jgs = new JobGeneratorSettings();
        jgs.taskNumber = 5;

        jgs.minPrice = 1.0;
        jgs.maxPrice = 1.6;
        jgs.useSpeedPriceFactor = true;

        jgs.minTime = 100;
        jgs.maxTime = 600;

        jgs.minSpeed = 1;
        jgs.maxSpeed = 1;

        jgs.minCPU = 2;
        jgs.maxCPU = 4;

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
    
}
