/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project.application.forms;

import java.io.IOException;
import java.io.PrintWriter;

import project.experiment.Experiment;
import project.experiment.archive.IntersectingAlternativesExperiment;
import project.utils.export.ExcelExport;

/**
 *
 * @author emelyanov
 */
public class SimpleMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //makeExperiment();
        test();
    }

    private static void makeExperiment() {
        //ExtremeExperiment exp = new ExtremeExperiment();
        //AlternativesStatsExperiment exp = new AlternativesStatsExperiment();
        //SchedullingExperiment exp = new SchedullingExperiment();
        //RegressionSchedullingExperiment exp = new RegressionSchedullingExperiment();
        //SingleExtremeInBSExperiment exp = new SingleExtremeInBSExperiment();
        //ExtremeCriteriasInBSExperiment exp = new ExtremeCriteriasInBSExperiment();
        //BatchSliceExperiment exp = new BatchSliceExperiment();
        //BSFExperiment exp = new BSFExperiment();
        //ResourcePriceExperiment exp = new ResourcePriceExperiment();
        //ShiftingExperiment exp = new ShiftingExperiment();
        //ShiftingExperiment2 exp = new ShiftingExperiment2();
        //DomainExperiment exp = new DomainExperiment();
        //DynamicSchedulerExperiment exp = new DynamicSchedulerExperiment();
        //AlternativeSolverV2RegressionExperiment exp = new AlternativeSolverV2RegressionExperiment();
        //Experiment exp = new AlternativeSolverV2UserExperiment();
        //Experiment exp = new AlternativeSolverV2UserPercentaleExperiment();
        //Experiment exp = new DynamicSchedulerAndBSCompareExperiment();
        //Experiment exp = new UserCriteriaRelationsExperiment();
        Experiment exp = new IntersectingAlternativesExperiment();
        //Experiment exp = new BatchSlicerUserPercentaleExperiment();

        exp.performExperiments(5);
        System.out.println(exp.getData());
        //saveResults(exp.getData(), "exp_results.txt");

        int b = 0;
    }

    private static void test() {
        try {
            ExcelExport.exportToExcel("D:/temp/export.xls", null);
        } catch (IOException e) {
            System.err.println("Error exporting to Excel: " + e.getMessage());
        }
    }

    protected static void saveResults(String results, String fileName) {
        try {
            PrintWriter out = new PrintWriter(fileName);
            out.println(results);
            out.close();
        } catch (Exception e) {
            System.out.println("Can't save experiment results to " + fileName + "; cause: " + e);

        }
    }
}
