/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package project.experiment.archive;

import java.util.ArrayList;
import project.engine.alternativeStats.AlternativeStats;
import project.engine.data.ComputingNode;
import project.engine.data.ResourceRequest;
import project.engine.data.UserJob;
import project.engine.data.VOEHelper;
import project.engine.data.VOEnvironment;
import project.engine.data.environmentGenerator.EnvironmentGenerator;
import project.engine.data.environmentGenerator.EnvironmentGeneratorSettings;
import project.engine.data.environmentGenerator.EnvironmentPricingSettings;
import project.engine.slot.slotProcessor.SlotProcessorSettings;
import project.engine.slot.slotProcessor.SlotProcessorV2;
import project.engine.slot.slotProcessor.criteriaHelpers.MinSumCostCriteria;
/**
 *
 * @author Magica
 */
public class AlternativesStatsExperiment {

    public AlternativeStats AMPStats;
    public AlternativeStats UniqueAMPStats;
    public AlternativeStats extremeStats;
    private int expNum = 0;

    public AlternativesStatsExperiment() {
        AMPStats = new AlternativeStats();
        UniqueAMPStats = new AlternativeStats();
        extremeStats = new AlternativeStats();

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
        long t1,t2,t3;

        SlotProcessorV2 sp2 = new SlotProcessorV2();
        SlotProcessorSettings sps = new SlotProcessorSettings();
        sps.algorithmType = "MODIFIED";
        sps.clean = true;
        sps.countStats = false;
        sps.algorithmConcept = "COMMON";
        sps.cycleStart = 0;
        sps.cycleLength = 600;


        //AMP
        ArrayList<UserJob> batch1 = VOEHelper.copyJobBatchList(batch);
        sps.algorithmConcept = "COMMON";            //AMP
        t1 = System.nanoTime();
        sp2.findAlternatives(batch1, env, sps);
        t1 = System.nanoTime() - t1;
        if(batch1.get(0).alternatives.isEmpty()){
            alternativeExists = false;
            return;                     //Go to next experiment
        }

        //UAMP
        ArrayList<UserJob> batch2 = VOEHelper.copyJobBatchList(batch);
        sps.algorithmConcept = "COMMON";            //AMP
        sps.check4PreviousAlternatives = true;
        sps.alternativesMinDistance = 0.1;
        t2 = System.nanoTime();
        sp2.findAlternatives(batch2, env, sps);
        t2 = System.nanoTime() - t2;
        if(batch2.get(0).alternatives.isEmpty()){
            alternativeExists = false;
            return;                     //Go to next experiment
        }

        //Extreme
        ArrayList<UserJob> batch3 = VOEHelper.copyJobBatchList(batch);
        sps.algorithmConcept = "EXTREME";
        sps.criteriaHelper = new MinSumCostCriteria();
        sps.check4PreviousAlternatives = true;
        sps.alternativesMinDistance = 0.1;
        t3 = System.nanoTime();
        sp2.findAlternatives(batch3, env, sps);
        t3 = System.nanoTime() - t3;
        if(batch3.get(0).alternatives.isEmpty()){
            alternativeExists = false;
            return;                     //Go to next experiment
        }

        AMPStats.processResults(batch1);
        UniqueAMPStats.processResults(batch2);
        extremeStats.processResults(batch3);

        System.out.println(expNum++);
    }

    private VOEnvironment generateNewEnvironment() {
        //Creating resources
        ArrayList<ComputingNode> lines = new ArrayList<ComputingNode>();
        ComputingNode rc = new ComputingNode("Pentium 4", 1);
        rc.id = 7;
        ComputingNode rc1 = new ComputingNode("Intel Quad", 1);
        rc1.id = 1;
        ComputingNode rc2 = new ComputingNode("AMD 64", 2);
        rc2.id = 4;
        ComputingNode rc3 = new ComputingNode("iPad", 2);
        rc3.id = 5;

        lines.add(rc);
        lines.add(rc1);
        lines.add(rc2);
        lines.add(rc3);


        //creating environment
        EnvironmentGeneratorSettings envSet = new EnvironmentGeneratorSettings();
        envSet.minResourceSpeed = 1;
        envSet.maxResourceSpeed = 10;
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
        VOEnvironment env = new VOEnvironment();
        env = envGen.generate(envSet, lines);
        env.applyPricing(epc);

        return env;
    }

    private ArrayList<UserJob> generateJobBatch() {

        ResourceRequest R = new ResourceRequest(5,150, 2, 2);
        UserJob J = new UserJob(0, "Job 0", R, null, null);
        
        ResourceRequest R1 = new ResourceRequest(4,100, 2.3, 2);
        UserJob J1 = new UserJob(1, "Job 1", R1, null, null);

        ResourceRequest R2 = new ResourceRequest(6,60, 3.5, 3);
        UserJob J2 = new UserJob(2, "Job 2", R2, null, null);

        ResourceRequest R3 = new ResourceRequest(3,200, 1.2, 1);
        UserJob J3 = new UserJob(3, "Job 3", R3, null, null);

        ArrayList<UserJob> jobs = new ArrayList<UserJob>();
        jobs.add(J);
        jobs.add(J1);
        jobs.add(J2);
        jobs.add(J3);

        return jobs;
    }

}
