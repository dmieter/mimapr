package project.engine.slot.slotProcessor.criteriaHelpers;

import java.util.function.BiPredicate;
import project.engine.data.Slot;
import project.engine.data.Window;

/**
 *
 * @author emelyanov
 */
public class MaxAdditiveUserValuationCriterion implements ICriteriaHelper, IReduceComplexity {

    protected ValuationModel valuationModel;
    protected BiPredicate<Double, Double> stopCondition = (c1,c2) -> (false);
    
    public MaxAdditiveUserValuationCriterion() {
        valuationModel = new ValuationModel();
    }
    
    public MaxAdditiveUserValuationCriterion(ValuationModel valuationModel) {
        if (valuationModel != null) {
            this.valuationModel = valuationModel;
        } else {
            this.valuationModel = new ValuationModel();
        }
    }

    @Override
    public double getCriteriaValue(Window w) {
        if (!w.squareWindow) {
            throw new UnsupportedOperationException("MaxAdditiveUrerValuationCriterion supports only Square windows");
        }

        Double sumValue = 0d;

        for (Slot slot : w.slots) {
            sumValue += getValuationModel().getSlotValue(slot, w.startTime, w.length);
        }

        return sumValue;
        /* we are maximizing */

    }

    @Override
    public String getDescription() {
        return "Additive Valuation";
    }

    /**
     * @return the valuationModel
     */
    public ValuationModel getValuationModel() {
        return valuationModel;
    }

    public void provideSearchStopCondition(BiPredicate<Double, Double> stopCondition){
        this.stopCondition = stopCondition;
    }
    
    @Override
    public boolean stopConditionByCriterion(double bestCriterionValue, double curCriterionValue) {
        return stopCondition.test(bestCriterionValue, curCriterionValue);
    }

}
