/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MagicaTestForm.java
 *
 * Created on 15.04.2010, 21:04:55
 */

package project.application.forms;

import java.util.*;
import project.experiment.processor.DynamicProcessorHelper;
import project.engine.scheduler.alternativeSolver.v1.AlternativeSolverSettings;
import project.engine.scheduler.alternativeSolver.v1.LimitCountData;
import project.engine.alternativeStats.DistributionStats;
import project.engine.scheduler.batchSlicer.BatchSlicer;
import project.engine.scheduler.batchSlicer.BatchSlicerSettings;
import project.engine.data.Alternative;
import project.engine.data.ComputingNode;
import project.engine.data.UserJob;
import project.engine.data.VOEHelper;
import project.engine.data.VOEnvironment;
import project.math.distributions.GaussianFacade;
import project.math.distributions.GaussianSettings;
import project.engine.data.environmentGenerator.EnvironmentGenerator;
import project.engine.data.environmentGenerator.EnvironmentGeneratorSettings;
import project.engine.data.environmentGenerator.EnvironmentPricingSettings;
import project.engine.data.jobGenerator.JobGenerator;
import project.engine.slot.slotProcessor.criteriaHelpers.MinFinishTimeCriteria;
import project.math.distributions.HyperGeometricFacade;
import project.math.distributions.HyperGeometricSettings;
import project.math.distributions.UniformFacade;
import project.engine.data.jobGenerator.JobGeneratorSettings;
import project.engine.scheduler.SchedulerOperations;
import project.engine.slot.slotProcessor.criteriaHelpers.MinSumCostCriteria;
import project.experiment.archive.DynamicSchedulerAndBSCompareExperiment;
import project.experiment.archive.IntersectingAlternativesExperiment;
/**
 *
 * @author Администратор
 */
public class MagicaTestForm extends javax.swing.JFrame {

    project.application.component.resourceDiagram.ModelViewPanel chart;
    DynamicProcessorHelper manualProcessor;


    // inits class variables
    private void myInit(){
        chart = new project.application.component.resourceDiagram.ModelViewPanel();
        jScrollPaneMain.setViewportView(chart);
        
    }

    private void distributionTest(){
        //HyperGeometricSettings hgset = new HyperGeometricSettings(10, 60);
        HyperGeometricSettings hgset = new HyperGeometricSettings(1, 12);
        HyperGeometricFacade hg = new HyperGeometricFacade(hgset);
        GaussianSettings gs = new GaussianSettings(-10, 0, 10);
        GaussianFacade gf = new GaussianFacade(gs);
        UniformFacade uf = new UniformFacade(1, 12);

        DistributionStats stats = new DistributionStats();
        stats.testGenerator(1000, gf);
        int b = 0;
    }

    private void myTest(){
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
        envSet.minResourceSpeed = 2;
        envSet.maxResourceSpeed = 6;
        envSet.resourceLineNum = 6;
        envSet.maxTaskLength = 60;
        envSet.minTaskLength = 10;
        envSet.occupancyLevel = 1;
        envSet.timeInterval = 600;
        //HyperGeometricSettings hgSet = new HyperGeometricSettings(1000, 60, 100, 2);
        HyperGeometricSettings hgSet = new HyperGeometricSettings(2, 10);
        GaussianSettings gsSet = new GaussianSettings(10, 15, 30);
        envSet.perfGenerator = new HyperGeometricFacade(hgSet);
        //envSet.perfGenerator = new GaussianFacade(gsSet);
        EnvironmentGenerator envGen = new EnvironmentGenerator();
        EnvironmentPricingSettings epc = new EnvironmentPricingSettings();
        epc.priceQuotient = 0.8;
        epc.priceMutationFactor = 0.5;

        lines = envGen.generateResourceTypes(envSet);
        VOEnvironment env = new VOEnvironment();
        env = envGen.generate(envSet, lines);
        env.applyPricing(epc);

        // Creating job batch
//        ResourceRequest R1 = new ResourceRequest(0,2,400, 3, 2);
//        ResourceRequest R2 = new ResourceRequest(1,4,130, 2, 2);          //get resources with speed 2 for time 130 (if faster than faster), volume = 130*2 = 260, price is 2 for speed 2)
//        ResourceRequest R3 = new ResourceRequest(2,3,250, 2, 2);        //get resources with speed 1 for time 150 (if faster than faster), volume = 150*1 = 150, price is 1.1 for speed 1)
//        ResourceRequest R4 = new ResourceRequest(3,3,160, 2, 2);
//        R1.name = "Job 1";
//        R2.name = "Job 2";
//        R3.name = "Job 3";
//        R4.name = "Job 4";
//        R1.criteria = new MinRunTimeCriteria();
//
//        ArrayList<ResourceRequest> requests = new ArrayList<ResourceRequest>();
//        requests.add(R1);
//        requests.add(R2);
//        requests.add(R3);
//        requests.add(R4);

        // Creating job batch2

        JobGenerator jg = new JobGenerator();
        JobGeneratorSettings jgs = new JobGeneratorSettings();
        jgs.taskNumber = 10;

        gsSet = new GaussianSettings(1, 2, 4);
        jgs.cpuNumGen = new GaussianFacade(gsSet);
        jgs.useSpeedPriceFactor = true;

        gsSet = new GaussianSettings(120, 300, 600);
        jgs.timeGen = new GaussianFacade(gsSet);

        gsSet = new GaussianSettings(1, 2, 3);
        jgs.perfGen = new GaussianFacade(gsSet);

        gsSet = new GaussianSettings(2, 3, 5);
        jgs.maxPriceGen = new GaussianFacade(gsSet);

        ArrayList<UserJob> jobs = jg.generate(jgs);

        //Slot Processor
        /*SlotProcessor proc = new SlotProcessor();
        SlotProcessorSettings prSet = new SlotProcessorSettings();
        prSet.algorithmType = "MODIFIED";
        prSet.cycleLength = 400;
        prSet.cycleStart = 0;
        prSet.clean = true;


        VOEHelper.updateSlots(env);
        VOEHelper.trimSlots(env, 0, 400);
        proc.findAlternatives(requests, env, prSet);
        VOEHelper.nameBatchAlternatives(requests);*/

        //Batch Slicer

        VOEHelper.updateSlots(env);
        VOEHelper.trimSlots(env, 0, 600);

        BatchSlicer bc = new BatchSlicer();
        BatchSlicerSettings bcs = new BatchSlicerSettings();
        bcs.periodStart = 0;
        bcs.periodEnd = 600;
        bcs.slicesNum = 2;
        bcs.asSettings = new AlternativeSolverSettings();
        bcs.asSettings.optType = "MIN";
        bcs.asSettings.optimizedVar = 1; //COST
        bcs.asSettings.limitedVar = 0; //TIME
        bcs.asSettings.limitCalculationType = 0;    //Average limit
        bcs.asSettings.limitCountData = new LimitCountData(40, 40);
        bcs.asSettings.optimalOnly = true;
        bcs.spConceptType = "EXTREME";
        bcs.spAlgorithmType = "MODIFIED";
        bcs.spCriteriaHelper = new MinSumCostCriteria();
        bc.solve(bcs, env, jobs);

//        SlotProcessorV2 sp2 = new SlotProcessorV2();
//        SlotProcessorSettings sps = new SlotProcessorSettings();
//        sps.algorithmType = "MODIFIED";
//        sps.clean = false;
//        sps.countStats = false;
//        sps.algorithmConcept = "COMMON";
//        sps.check4PreviousAlternatives = true;
//        sps.alternativesMinDistance = 0.1;
//        sps.cycleStart = 0;
//        sps.cycleLength = 600;
//
//        sp2.findAlternatives(requests, env, sps);

        SchedulerOperations.nameBatchAlternatives(jobs);

        for(Iterator it = jobs.iterator();it.hasNext();){               //Apply alternatives to VOE
            UserJob job = (UserJob)it.next();
            if(job.alternatives.isEmpty())
                continue;

            if(job.bestAlternative>=0){ //if we found suitable alternative, otherwise by default it equals -1
                Alternative a = job.alternatives.get(job.bestAlternative);
                //VOEHelper.addAlternativeToVOE(env, a, job.name);
            }
        }

        chart.LoadJobAlternatives(jobs, false);
        chart.SetEnvironment(env);


//
//
//        //experiment namba 2
//
//        bcs.slicesNum = 3;
//        bcs.asSettings.limitCalculationType = 2;
//        bcs.asSettings.limitCountData.externalJobs = requests;
//
//        ArrayList requests2 = VOEHelper.copyJobBatchList(requests);
//        bc = new BatchSlicer();
//        bc.solve(bcs, env2, requests2);
//
//        int d=0;


        //experiment namba 3

//        BackfillSettings bfs = new BackfillSettings();
//        bfs.aggressive = true;
//        bfs.backfillMetric = "COSTMIN";
//        bfs.cycleLength = 600;
//        bfs.policy = "BESTFIT";
//        bfs.priorityPolicy = "NONE";
//        Backfill bf = new Backfill();
//        bf.Backfill(requests, env, bfs);
//        VOEHelper.nameBatchAlternatives(requests);
//        chart.SetEnvironment(env);
//        int a = 0;
        
    }

    private void example(){
        BatchSlicer bs;
        BatchSlicerSettings bss;
        bs = new BatchSlicer();
        bss = new BatchSlicerSettings();

        bss.periodStart = 0;
        bss.periodEnd = 600;
        bss.sliceAlgorithm = 0;
        bss.spAlgorithmType = "MODIFIED";
        bss.spConceptType = "EXTREME";
        bss.slicesNum = 1;
        bss.shiftAlternatives = false;
        bss.asSettings = new AlternativeSolverSettings();
        bss.asSettings.usePareto = false;
        bss.asSettings.limitedVar = AlternativeSolverSettings.COST;      //Time
        bss.asSettings.optimizedVar = AlternativeSolverSettings.TIME;    //Cost
        bss.asSettings.optType = "MIN";
        bss.asSettings.optimalOnly = true;
        bss.asSettings.limitCalculationType = 0;    //average
        
        //Creating resources
        ArrayList<ComputingNode> lines = new ArrayList<ComputingNode>();

        EnvironmentGeneratorSettings envSet = new EnvironmentGeneratorSettings();
        envSet.minResourceSpeed = 2;
        envSet.maxResourceSpeed = 7;
        envSet.resourceLineNum = 7;
        envSet.maxTaskLength = 50;
        envSet.minTaskLength = 10;
        //envSet.occupancyLevel = 1;
        HyperGeometricSettings hgSet = new HyperGeometricSettings(1000, 150, 30, 0, 10, 0, 2);
        envSet.occupGenerator = new HyperGeometricFacade(hgSet);
        envSet.timeInterval = 600;
        //envSet.hgPerfSet = new HyperGeometricSettings(1000, 60, 100, 1);   //mean = 6.0 e = 2.254125347242491
        EnvironmentGenerator envGen = new EnvironmentGenerator();
        EnvironmentPricingSettings epc = new EnvironmentPricingSettings();
        epc.priceQuotient = 1;
        epc.priceMutationFactor = 0.6;
        epc.speedExtraCharge = 0.02;

        lines = envGen.generateResourceTypes(envSet);

        //creating environment
        VOEnvironment env = new VOEnvironment();
        env = envGen.generate(envSet, lines);
        env.applyPricing(epc);
        
        
        JobGenerator jg = new JobGenerator();
        JobGeneratorSettings jgs = new JobGeneratorSettings();
        jgs.taskNumber = 5;

        jgs.minPrice = 2.5;
        jgs.maxPrice = 2.5;
        jgs.useSpeedPriceFactor = true;

        jgs.minTime = 250;
        jgs.maxTime = 250;

        jgs.minSpeed = 1;
        jgs.maxSpeed = 1;

        jgs.minCPU = 2;
        jgs.maxCPU = 2;

        ArrayList<UserJob> batch = jg.generate(jgs);
        //rg.setRandomBatchCriterias(batch);
        batch.get(0).resourceRequest.criteria = new MinFinishTimeCriteria();
        batch.get(1).resourceRequest.criteria = new MinSumCostCriteria();
        batch.get(2).resourceRequest.criteria = new MinFinishTimeCriteria();
        batch.get(3).resourceRequest.criteria = new MinFinishTimeCriteria();
        batch.get(4).resourceRequest.criteria = new MinFinishTimeCriteria();
        //batch.get(1).criteria = new MinSumCostCriteria();
        //batch.get(2).criteria = new MinRunTimeCriteria();
        //batch.get(3).criteria = new MinSumTimeCriteria();
        //batch.get(4).criteria = null;
        
        bs.solve(bss, env, batch);
        
        drawExample(batch, env, 0);
    }
    
    private void drawExample(ArrayList<UserJob> batch, VOEnvironment env, int modelTime){
        SchedulerOperations.nameBatchAlternatives(batch);
        chart.setModelTime(modelTime);
        chart.SetEnvironment(env);
        chart.LoadJobAlternatives(batch, false);
        
    }

    private void makeExperiment(){
        //ExtremeExperiment exp = new ExtremeExperiment();
        //AlternativesStatsExperiment exp = new AlternativesStatsExperiment();
        //SchedullingExperiment exp = new SchedullingExperiment();
        //SingleExtremeInBSExperiment exp = new SingleExtremeInBSExperiment();
        //ExtremeCriteriasInBSExperiment exp = new ExtremeCriteriasInBSExperiment();
        //BatchSliceExperiment exp = new BatchSliceExperiment();
        //BSFExperiment exp = new BSFExperiment();
        //ResourcePriceExperiment exp = new ResourcePriceExperiment();
        //ShiftingExperiment exp = new ShiftingExperiment();
        //ShiftingExperiment2 exp = new ShiftingExperiment2();
        //ShiftingExperimentWithUsers exp = new ShiftingExperimentWithUsers();
        //DomainExperiment exp = new DomainExperiment();
        //DynamicSchedulerExperiment exp = new DynamicSchedulerExperiment();
        IntersectingAlternativesExperiment exp = new IntersectingAlternativesExperiment();
        
        exp.performExperiments(1);
        System.out.println(exp.getData());
        if((exp.batchToShow!=null)&&(exp.envToShow!=null)){
            drawExample(exp.batchToShow, exp.envToShow, 0); 
        }
        int b = 0;
    }
    
    public void DynamicModelling(){
        //DynamicSchedulerExperiment exp = new DynamicSchedulerExperiment();
        DynamicSchedulerAndBSCompareExperiment exp = new DynamicSchedulerAndBSCompareExperiment();
        exp.performExperiments(1);
        
        manualProcessor = new DynamicProcessorHelper();
        manualProcessor.setScheduler(exp.getDs());
        chart.setManualProcessor(manualProcessor);
        
        System.out.println(exp.getData());
    }
    
    
    /** Creates new form MagicaTestForm */
    public MagicaTestForm() {
        initComponents();
        myInit();
        //example();
        
        DynamicModelling();
        //makeExperiment();
        
        //myTest();
        //distributionTest();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPaneMain = new javax.swing.JScrollPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneMain, javax.swing.GroupLayout.DEFAULT_SIZE, 757, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneMain, javax.swing.GroupLayout.DEFAULT_SIZE, 535, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MagicaTestForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPaneMain;
    // End of variables declaration//GEN-END:variables

}
