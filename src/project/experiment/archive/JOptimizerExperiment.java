package project.experiment.archive;

//import com.joptimizer.exception.JOptimizerException;
//import com.joptimizer.optimizers.LPOptimizationRequest;
//import com.joptimizer.optimizers.LPPrimalDualMethod;

import project.experiment.Experiment;

/**
 *
 * @author dmieter
 */

public class JOptimizerExperiment extends Experiment {

    String solution = "";
    
    @Override
    public void performExperiments(int expNum) {
        for (int i = 0; i < expNum; i++) {
            performExperiment();
        }
    }

    protected void performExperiment() {
        /*try {
            //Objective function
            double[] c = new double[]{1., 1., 1., 8., 9., 6.};

            //Inequalities constraints
            double[][] G = new double[][]{{1., 2., 2., 3., 4., 8.}};
            double[] h = new double[]{7.};
            
            //Equalities constraints
            double[][] A = new double[][]{{1.,1.,1.,1.,1.,1.}};
            double[] b = new double[]{3.};

            //Bounds on variables
            double[] lb = new double[]{0, 0, 0, 0, 0, 0};
            double[] ub = new double[]{1, 1, 1, 1, 1, 1};

            //optimization problem
            LPOptimizationRequest or = new LPOptimizationRequest();
            or.setC(c);
            or.setG(G);
            or.setH(h);
            or.setA(A);
            or.setB(b);
            or.setLb(lb);
            or.setUb(ub);
            or.setDumpProblem(true);

            //optimization
            LPPrimalDualMethod opt = new LPPrimalDualMethod();

            opt.setLPOptimizationRequest(or);
            opt.optimize();
            
            double[] sol = opt.getOptimizationResponse().getSolution();
            double val = sol[0]*c[0]+sol[1]*c[1]+sol[2]*c[2]+sol[3]*c[3]+sol[4]*c[4]+sol[5]*c[5];
            double csum = sol[0]*1+sol[1]*2+sol[2]*2+sol[3]*3+sol[4]*4+sol[5]*8;
            solution = ""+val+" "+csum +" "+sol[0]+" "+sol[1]+" "+sol[2]+" "+sol[3]+" "+sol[4]+" "+sol[5];
            
        } catch (JOptimizerException e) {
            throw new RuntimeException(e);
        }*/
    }

    @Override
    public String getData() {
        return "JOptimizerExperiment finished: "+solution;
    }

}
