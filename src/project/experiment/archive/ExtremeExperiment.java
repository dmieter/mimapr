/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package project.experiment.archive;

import java.util.ArrayList;
import project.engine.alternativeStats.AlternativesExtremeStats;
import project.engine.alternativeStats.ResourcePerformanceStats;
import project.engine.data.ComputingNode;
import project.engine.data.ResourceRequest;
import project.engine.data.UserJob;
import project.engine.data.VOEHelper;
import project.engine.data.VOEnvironment;
import project.math.distributions.HyperGeometricFacade;
import project.math.distributions.HyperGeometricSettings;
import project.engine.data.environmentGenerator.EnvironmentGenerator;
import project.engine.data.environmentGenerator.EnvironmentGeneratorSettings;
import project.engine.data.environmentGenerator.EnvironmentPricingSettings;
import project.engine.slot.slotProcessor.SlotProcessorSettings;
import project.engine.slot.slotProcessor.SlotProcessorV2;
import project.engine.slot.slotProcessor.criteriaHelpers.MinFinishTimeCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinRunTimeCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinSumCostCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinSumTimeCriteria;

/**
 *
 * @author emelyanov
 */
public class ExtremeExperiment {
    public AlternativesExtremeStats minCostStats;
    public AlternativesExtremeStats minRuntimeStats;
    public AlternativesExtremeStats minProcTimeStats;
    public AlternativesExtremeStats minStartStats;
    public AlternativesExtremeStats minFinishStats;
    public AlternativesExtremeStats commonStats;

    public ArrayList<UserJob> batchToShow;
    public VOEnvironment envToShow;

    public ResourcePerformanceStats perfStats;
    private int expNum = 0;

    public ExtremeExperiment() {
        minCostStats = new AlternativesExtremeStats();
        minRuntimeStats = new AlternativesExtremeStats();
        minProcTimeStats = new AlternativesExtremeStats();
        minStartStats = new AlternativesExtremeStats();
        minFinishStats = new AlternativesExtremeStats();
        commonStats = new AlternativesExtremeStats();
        perfStats = new ResourcePerformanceStats();
    }

    public void performExperiments(int number){
        for(int i=0; i<number;i++){
            performSingleExperiment();
        }
    }

    private void performSingleExperiment() {
        VOEnvironment env = generateNewEnvironment();
        ArrayList<UserJob> batch = generateJobBatch();
        boolean alternativeExists = true;
        long t1,t2,t3,t4,t5,t6;

        SlotProcessorV2 sp2 = new SlotProcessorV2();
        SlotProcessorSettings sps = new SlotProcessorSettings();
        sps.algorithmType = "MODIFIED";
        sps.clean = false;
        sps.countStats = false;
        sps.algorithmConcept = "EXTREME";
        sps.cycleStart = 0;
        sps.cycleLength = 3600;

        //MinRuntime
        ArrayList<UserJob> batch1 = VOEHelper.copyJobBatchList(batch);
        sps.criteriaHelper = new MinRunTimeCriteria();
        t1 = System.nanoTime();
        sp2.findAlternatives(batch1, env, sps, 1);
        t1 = System.nanoTime() - t1;
        if(batch1.get(0).alternatives.isEmpty()){
            alternativeExists = false;
            return;                     //Go to next experiment
        }

        //MinFinishTime
        ArrayList<UserJob> batch4 = VOEHelper.copyJobBatchList(batch);
        sps.criteriaHelper = new MinFinishTimeCriteria();
        t4 = System.nanoTime();
        sp2.findAlternatives(batch4, env, sps, 1);
        t4 = System.nanoTime() - t4;
        if(batch4.get(0).alternatives.isEmpty()){
            alternativeExists = false;
            return;                     //Go to next experiment
        }

        //MinSumCost
        ArrayList<UserJob> batch2 = VOEHelper.copyJobBatchList(batch);
        sps.criteriaHelper = new MinSumCostCriteria();
        t2 = System.nanoTime();
        sp2.findAlternatives(batch2, env, sps, 1);
        t2 = System.nanoTime() - t2;
        if(batch2.get(0).alternatives.isEmpty()){
            alternativeExists = false;
            return;                     //Go to next experiment
        }
        
        //MinSumTime
        ArrayList<UserJob> batch6 = VOEHelper.copyJobBatchList(batch);
        sps.criteriaHelper = new MinSumTimeCriteria();
        t6 = System.nanoTime();
        sp2.findAlternatives(batch6, env, sps, 1);
        t6 = System.nanoTime() - t6;
        if(batch6.get(0).alternatives.isEmpty()){
            alternativeExists = false;
            return;                     //Go to next experiment
        }

        //MintStartTime
        ArrayList<UserJob> batch3 = VOEHelper.copyJobBatchList(batch);
        sps.algorithmConcept = "COMMON";            //AMP
        t3 = System.nanoTime();
        sp2.findAlternatives(batch3, env, sps, 1);
        t3 = System.nanoTime() - t3;
        if(batch3.get(0).alternatives.isEmpty()){
            alternativeExists = false;
            return;                     //Go to next experiment
        }

        //MintStartTime
        ArrayList<UserJob> batch5 = VOEHelper.copyJobBatchList(batch);
        sps.algorithmConcept = "COMMON";            //AMP
        t5 = System.nanoTime();
        sp2.findAlternatives(batch5, env, sps);
        t5 = System.nanoTime() - t5;
        if(batch5.get(0).alternatives.isEmpty()){
            alternativeExists = false;
            return;                     //Go to next experiment
        }

        minRuntimeStats.processAlternatives(batch1.get(0).alternatives, t1);
        minCostStats.processAlternatives(batch2.get(0).alternatives, t2);
        minProcTimeStats.processAlternatives(batch6.get(0).alternatives, t6);
        minStartStats.processAlternatives(batch3.get(0).alternatives, t3);
        minFinishStats.processAlternatives(batch4.get(0).alternatives, t4);
        commonStats.processAlternatives(batch5.get(0).alternatives, t5);

        perfStats.processEnvironment(env);

        batchToShow = batch5;
        envToShow = env;
        System.out.println(expNum++);
    }

    private VOEnvironment generateNewEnvironment() {
        //creating environment
        EnvironmentGeneratorSettings envSet = new EnvironmentGeneratorSettings();
        envSet.minResourceSpeed = 2;
        envSet.maxResourceSpeed = 11;
        envSet.resourceLineNum = 100;
        envSet.maxTaskLength = 100;
        envSet.minTaskLength = 10;
        
        //envSet.occupancyLevel = 3;
        HyperGeometricSettings hgSet = new HyperGeometricSettings(1000, 150, 30, 0, 10, 1, 5);
        envSet.occupGenerator = new HyperGeometricFacade(hgSet);
        
        envSet.timeInterval = 3600;
        EnvironmentGenerator envGen = new EnvironmentGenerator();
        EnvironmentPricingSettings epc = new EnvironmentPricingSettings();
        epc.priceQuotient = 1;
        epc.speedExtraCharge = 0.02;
        epc.priceMutationFactor = 0.6;

        ArrayList<ComputingNode> lines = envGen.generateResourceTypes(envSet);
        VOEnvironment env = envGen.generate(envSet, lines);
        env.applyPricing(epc);

        return env;
    }

    private VOEnvironment generateNewEnvironment2() {
        //Creating resources
        ArrayList<ComputingNode> lines = new ArrayList<ComputingNode>();

        //creating environment
        EnvironmentGeneratorSettings envSet = new EnvironmentGeneratorSettings();

        //N = 100000 D = 600 n = 1000 mean = 6.0 e = 2.43
        //HyperGeometricSettings hgSet = new HyperGeometricSettings(100000, 600, 1000, 1);
        HyperGeometricSettings hgSet = new HyperGeometricSettings(1, 10);
        envSet.perfGenerator = new HyperGeometricFacade(hgSet);

        envSet.resourceLineNum = 100;

        envSet.maxTaskLength = 60;
        envSet.minTaskLength = 10;
        envSet.occupancyLevel = 3;
        envSet.timeInterval = 600;
        EnvironmentGenerator envGen = new EnvironmentGenerator();
        EnvironmentPricingSettings epc = new EnvironmentPricingSettings();
        epc.priceQuotient = 1;
        epc.priceMutationFactor = 0.4;

        lines = envGen.generateResourceTypes(envSet);
        VOEnvironment env = envGen.generate(envSet, lines);
        env.applyPricing(epc);

        return env;
    }

    private ArrayList<UserJob> generateJobBatch() {

        ResourceRequest R = new ResourceRequest(5/*cpuNum*/,150/*time*/, 2/*price per slot per time*/, 2/*min res speed*/);
        UserJob J = new UserJob(1, "Job 1", R, null, null);

        ArrayList<UserJob> jobs = new ArrayList<UserJob>();
        jobs.add(J);

        return jobs;
    }

    public String getData(){
        String data = "EXTREME EXPERIMENT\n"
                +"Common Stats\n"
                +this.commonStats.getData()+"\n"
                +"MinStart Stats\n"
                +this.minStartStats.getData()+"\n"
                +"MinRunTime Stats\n"
                +this.minRuntimeStats.getData()+"\n"
                +"MinFinish Stats\n"
                +this.minFinishStats.getData()+"\n"
                +"Minproctime Stats\n"
                +this.minProcTimeStats.getData()+"\n"
                +"MinCost Stats\n"
                +this.minCostStats.getData()+"\n"
                +"Performance Stats\n"
                +this.perfStats.getData();

        return data;
    }

}
