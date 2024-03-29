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

import project.experiment.CoordinatedVsMultiRandomExperiment;
import project.experiment.processor.DynamicProcessorHelper;
import project.engine.data.UserJob;
import project.engine.data.VOEnvironment;
import project.engine.scheduler.SchedulerOperations;
import project.experiment.archive.CoordinatedPreferencesUnlimitedCFastExperiment;
import project.experiment.archive.DynamicHorizontSchedulerExperiment;

/**
 *
 * @author Администратор
 */
public class VisualRunner extends javax.swing.JFrame {

    project.application.component.resourceDiagram.ModelViewPanel chart;
    DynamicProcessorHelper manualProcessor;


    // inits class variables
    private void myInit(){
        chart = new project.application.component.resourceDiagram.ModelViewPanel();
        jScrollPaneMain.setViewportView(chart);
        
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
        //IntersectingAlternativesExperiment exp = new IntersectingAlternativesExperiment();
        //IntersectingAlternativesMultipleExperiment exp = new IntersectingAlternativesMultipleExperiment();
//        UserCriteriaRelationsExperiment exp = new UserCriteriaRelationsExperiment();
//        CopySchedulerExperiment exp = new CopySchedulerExperiment();
//        IntersectingAltsVsBackfillingExperiment exp = new IntersectingAltsVsBackfillingExperiment();
        //IntersectingAltsVsBackfillingExperimentv2 exp = new IntersectingAltsVsBackfillingExperimentv2();
//        SquareWindowExperiment exp = new SquareWindowExperiment();
        //SquareWindowExtremeExperiment exp = new SquareWindowExtremeExperiment();
        //JOptimizerExperiment exp = new JOptimizerExperiment();
        //SquareWindowEgoExperiment exp = new SquareWindowEgoExperiment();
        //SquareWindowExtremeOptExperiment exp = new SquareWindowExtremeOptExperiment();
        //CoordinatedVsBackfillingExperiment exp = new CoordinatedVsBackfillingExperiment();
        //CoordinatedPreferencesExperiment exp = new CoordinatedPreferencesExperiment();
        //CoordinatedPreferencesExperimentSmall exp = new CoordinatedPreferencesExperimentSmall();
        //CoordinatedPreferencesUnlimitedCFastExperiment exp = new CoordinatedPreferencesUnlimitedCFastExperiment();
        //BackfillingErrorPriceExperiment exp = new BackfillingErrorPriceExperiment();
        //IntersectingAlternativesMultipleExperiment2 exp = new IntersectingAlternativesMultipleExperiment2();
        //EgoAltroJobFlowSchedulingExperiment exp = new EgoAltroJobFlowSchedulingExperiment();
        //CoordinatedWithBackfillingExperiment exp = new CoordinatedWithBackfillingExperiment();
        CoordinatedVsMultiRandomExperiment exp = new CoordinatedVsMultiRandomExperiment();
        
        exp.performExperiments(1);
        System.out.println(exp.getData());
        
        
        if((exp.batchToShow!=null)&&(exp.envToShow!=null)){
            drawExample(exp.batchToShow, exp.envToShow, 0);
        }
        int b = 0;
    }
    
    public void DynamicModelling(){
        //DynamicSchedulerExperiment exp = new DynamicSchedulerExperiment();
        //DynamicSchedulerAndBSCompareExperiment exp = new DynamicSchedulerAndBSCompareExperiment();
        DynamicHorizontSchedulerExperiment exp = new DynamicHorizontSchedulerExperiment();
        exp.performExperiments(1);
        
        manualProcessor = new DynamicProcessorHelper();
        manualProcessor.setScheduler(exp.getDs());
        chart.setManualProcessor(manualProcessor);
        
        System.out.println(exp.getData());
    }
    
    
    /** Creates new form MagicaTestForm */
    public VisualRunner() {
        initComponents();
        myInit();
        
        //DynamicModelling();
        makeExperiment();
        
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
                new VisualRunner().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPaneMain;
    // End of variables declaration//GEN-END:variables

}
