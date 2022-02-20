package project.engine.slot.slotProcessor.criteriaHelpers;

import project.engine.data.Slot;
import project.engine.data.Window;

/**
 *
 * @author emelyanov
 */
public class MaxAdditiveUserValuationUnlimCCriterion implements ICriteriaHelper {

    protected ValuationModel valuationModel;
    
    public MaxAdditiveUserValuationUnlimCCriterion(ValuationModel valuationModel) {
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
}
