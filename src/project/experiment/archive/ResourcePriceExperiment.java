
package project.experiment.archive;

import java.util.ArrayList;
import java.util.Random;
import project.engine.scheduler.alternativeSolver.v1.AlternativeSolverSettings;
import project.engine.alternativeStats.ResourceUtilizationStats;
import project.engine.alternativeStats.SchedulingResultsStats;
import project.engine.scheduler.batchSlicer.BatchSlicer;
import project.engine.scheduler.batchSlicer.BatchSlicerSettings;
import project.engine.data.*;
import project.math.distributions.HyperGeometricFacade;
import project.math.distributions.HyperGeometricSettings;
import project.engine.data.environmentGenerator.EnvironmentGenerator;
import project.engine.data.environmentGenerator.EnvironmentGeneratorSettings;
import project.engine.data.environmentGenerator.EnvironmentPricingSettings;
import project.engine.data.jobGenerator.JobGenerator;
import project.engine.data.jobGenerator.RequestGenerator;
import project.engine.data.jobGenerator.JobGeneratorSettings;

/**
 *
 * @author Magica
 */
public class ResourcePriceExperiment {
    
    public ResourceUtilizationStats resourceStartStats;
    
    public SchedulingResultsStats price1Stats;
    public ResourceUtilizationStats resource1Stats;
    
    public SchedulingResultsStats price2Stats;
    public ResourceUtilizationStats resource2Stats;
    
    public SchedulingResultsStats price3Stats;
    public ResourceUtilizationStats resource3Stats;
    
    public SchedulingResultsStats price4Stats;
    public ResourceUtilizationStats resource4Stats;
    
    public SchedulingResultsStats price5Stats;
    public ResourceUtilizationStats resource5Stats;
    
    //Batch Slicer entity
    private BatchSlicer bs;
    private BatchSlicerSettings bss;

    public ArrayList<UserJob> batchToShow;
    public VOEnvironment envToShow;

    private int cycleLength = 600;

    public ResourcePriceExperiment(){
        
        resourceStartStats = new ResourceUtilizationStats();
        
        price1Stats = new SchedulingResultsStats();
        resource1Stats = new ResourceUtilizationStats();
        
        price2Stats = new SchedulingResultsStats();
        resource2Stats = new ResourceUtilizationStats();
        
        price3Stats = new SchedulingResultsStats();
        resource3Stats = new ResourceUtilizationStats();
        
        price4Stats = new SchedulingResultsStats();
        resource4Stats = new ResourceUtilizationStats();
        
        price5Stats = new SchedulingResultsStats();
        resource5Stats = new ResourceUtilizationStats();
    }
    
    private void clearSchedullingData(){
        bs = new BatchSlicer();
    }

    public void performExperiments(int expNum){

        batchSlicerConfiguration();

        for(int i=0; i<expNum;i++){
            System.out.println("--------------Experiment #"+i+" -------------------");
            performSingleExperiment();
        }
    }
    
    private void performSingleExperiment(){
        clearSchedullingData();
        VOEnvironment env = generateNewEnvironment();
        ArrayList<UserJob> batch = generateJobBatch();
        
        ComputingNode rc = new ComputingNode("Price resource", 6);
        rc.id = (new Random()).nextInt();
        ComputingResourceLine rLine = new ComputingResourceLine(rc);
        rLine.id = rc.id;
        rLine.environment = env;
        
        boolean success = true;
        
        resourceStartStats.processResourceLine(0, cycleLength, rLine);
        
        //price 1
        clearSchedullingData();
        VOEnvironment env1 = VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch1 = VOEHelper.copyJobBatchList(batch);

        ComputingResourceLine rLine1 = new ComputingResourceLine(rLine);
        rLine1.environment = env1;
        rLine1.price = 2;
        env1.resourceLines.set(0, rLine1);
        
        bs.solve(bss, env1, batch1);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch1)){
            success = false;
            price1Stats.addFailExperiment();
            System.out.println("Price 1 failed to find alternatives");
        }else{
            VOEHelper.applyBestAlternativesToVOE(batch1, env1);
        }
        
        //price 2
        clearSchedullingData();
        VOEnvironment env2 = VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch2 = VOEHelper.copyJobBatchList(batch);
        
        ComputingResourceLine rLine2 = new ComputingResourceLine(rLine);
        rLine2.environment = env2;
        rLine2.price = 4;
        env2.resourceLines.set(0, rLine2);
        
        bs.solve(bss, env2, batch2);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch2)){
            success = false;
            price2Stats.addFailExperiment();
            System.out.println("Price 2 failed to find alternatives");
        }else{
            VOEHelper.applyBestAlternativesToVOE(batch2, env2);
        }
        
        
        //price 3
        clearSchedullingData();
        VOEnvironment env3 = VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch3 = VOEHelper.copyJobBatchList(batch);
        
        ComputingResourceLine rLine3 = new ComputingResourceLine(rLine);
        rLine3.environment = env3;
        rLine3.price = 6;
        env3.resourceLines.set(0, rLine3);
        
        bs.solve(bss, env3, batch3);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch3)){
            success = false;
            price3Stats.addFailExperiment();
            System.out.println("Price 3 failed to find alternatives");
        }else{
            VOEHelper.applyBestAlternativesToVOE(batch3, env3);
        }
        
        
        //price 4
        clearSchedullingData();
        VOEnvironment env4 = VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch4 = VOEHelper.copyJobBatchList(batch);
        
        ComputingResourceLine rLine4 = new ComputingResourceLine(rLine);
        rLine4.environment = env4;
        rLine4.price = 8;
        env4.resourceLines.set(0, rLine4);
        
        bs.solve(bss, env4, batch4);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch4)){
            success = false;
            price4Stats.addFailExperiment();
            System.out.println("Price 4 failed to find alternatives");
        }else{
            VOEHelper.applyBestAlternativesToVOE(batch4, env4);
        }
        
        
        //price 5
        clearSchedullingData();
        VOEnvironment env5 = VOEHelper.copyEnvironment(env);
        ArrayList<UserJob> batch5 = VOEHelper.copyJobBatchList(batch);
        
        ComputingResourceLine rLine5 = new ComputingResourceLine(rLine);
        rLine5.environment = env5;
        rLine5.price = 10;
        env5.resourceLines.set(0, rLine5);
        
        bs.solve(bss, env5, batch5);
        if(!SchedulingResultsStats.checkBatchForSuccess(batch5)){
            success = false;
            price5Stats.addFailExperiment();
            System.out.println("Price 5 failed to find alternatives");
        }else{
            VOEHelper.applyBestAlternativesToVOE(batch5, env5);
        }
        
        if(success){
            //price1Stats.processResults(batch1);

            this.price1Stats.processResults(batch1);
            this.price2Stats.processResults(batch2);
            this.price3Stats.processResults(batch3);
            this.price4Stats.processResults(batch4);
            this.price5Stats.processResults(batch5);
            resource1Stats.processResourceLine(0, cycleLength, env1.resourceLines.get(0));
            resource2Stats.processResourceLine(0, cycleLength, env2.resourceLines.get(0));
            resource3Stats.processResourceLine(0, cycleLength, env3.resourceLines.get(0));
            resource4Stats.processResourceLine(0, cycleLength, env4.resourceLines.get(0));
            resource5Stats.processResourceLine(0, cycleLength, env5.resourceLines.get(0));
        }
        
        
    }
    
    private void batchSlicerConfiguration(){
        bs = new BatchSlicer();
        bss = new BatchSlicerSettings();

        bss.periodStart = 0;
        bss.periodEnd = cycleLength;
        bss.sliceAlgorithm = 0;
        bss.spAlgorithmType = "MODIFIED";
        //bss.spConceptType = "EXTREME";
        bss.slicesNum = 5;
        bss.shiftAlternatives = true;// true - Shifting - another experiment
        bss.asSettings = new AlternativeSolverSettings();
        bss.asSettings.usePareto = false;
        bss.asSettings.limitedVar = 1;      //Cost
        bss.asSettings.optimizedVar = 0;    //Time
        bss.asSettings.optType = "MIN";
        bss.asSettings.optimalOnly = true;
        bss.asSettings.limitCalculationType = 0;    //average
    }
    
    private VOEnvironment generateNewEnvironment() {
        //Creating resources
        ArrayList<ComputingNode> lines = new ArrayList<ComputingNode>();

        EnvironmentGeneratorSettings envSet = new EnvironmentGeneratorSettings();
        envSet.minResourceSpeed = 2;
        envSet.maxResourceSpeed = 11;
        envSet.resourceLineNum = 24;
        envSet.maxTaskLength = 100;
        envSet.minTaskLength = 10;
        //envSet.occupancyLevel = 2;
        HyperGeometricSettings hgSet = new HyperGeometricSettings(1000, 150, 30, 0, 10, 0, 2);
        envSet.occupGenerator = new HyperGeometricFacade(hgSet);
        envSet.timeInterval = cycleLength;
        //envSet.hgPerfSet = new HyperGeometricSettings(1000, 60, 100, 1);   //mean = 6.0 e = 2.254125347242491
        EnvironmentGenerator envGen = new EnvironmentGenerator();
        EnvironmentPricingSettings epc = new EnvironmentPricingSettings();
        epc.priceQuotient = 1;
        epc.priceMutationFactor = 0.6;
        epc.speedExtraCharge = 0.02;

        lines = envGen.generateResourceTypes(envSet);

        //creating environment
        VOEnvironment env = envGen.generate(envSet, lines);
        env.applyPricing(epc);

        return env;
    }

    private ArrayList<UserJob> generateJobBatch() {
        JobGenerator jg = new JobGenerator();
        JobGeneratorSettings jgs = new JobGeneratorSettings();
        jgs.taskNumber = 20;

        jgs.minPrice = 1.0;
        jgs.maxPrice = 1.6;
        jgs.useSpeedPriceFactor = true;

        jgs.minTime = 100;
        jgs.maxTime = 500;

        jgs.minSpeed = 1;
        jgs.maxSpeed = 1;

        jgs.minCPU = 2;
        jgs.maxCPU = 5;

        return jg.generate(jgs);
    }

    public String getData(){
        String data = "RESOURCE PRICE EXPERIMENT\n"
                +"Price 1 Stats\n"
                +this.price1Stats.getData()+"\n"
                +"Price 2 Stats\n"
                +this.price2Stats.getData()+"\n"
                +"Price 3 Stats\n"
                +this.price3Stats.getData()+"\n"
                +"Price 4 Stats\n"
                +this.price4Stats.getData()+"\n"
                +"Price 5 Stats\n"
                +this.price5Stats.getData()+"\n"
                +"Utilization Start Stats\n"
                +this.resourceStartStats.getData()+"\n"
                +"Utilization 1 Stats\n"
                +this.resource1Stats.getData()+"\n"
                +"Utilization 2 Stats\n"
                +this.resource2Stats.getData()+"\n"
                +"Utilization 3 Stats\n"
                +this.resource3Stats.getData()+"\n"
                +"Utilization 4 Stats\n"
                +this.resource4Stats.getData()+"\n"
                +"Utilization 5 Stats\n"
                +this.resource5Stats.getData();

        return data;
    }
}

