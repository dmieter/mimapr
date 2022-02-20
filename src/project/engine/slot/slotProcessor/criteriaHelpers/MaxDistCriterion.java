/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project.engine.slot.slotProcessor.criteriaHelpers;

import project.engine.data.Slot;
import project.engine.data.Window;

/**
 *
 * @author Magica
 */
public class MaxDistCriterion implements ICriteriaHelper {

    protected ValuationModel valuationModel;
    
    public MaxDistCriterion() {
        valuationModel = new ValuationModel();
    }
    
    public MaxDistCriterion(ValuationModel valuationModel) {
        if (valuationModel != null) {
            this.valuationModel = valuationModel;
        } else {
            this.valuationModel = new ValuationModel();
        }
    }
    
    public double getCriteriaValue(Window w) {
        double sumDist = 0;
        w.sortSlotsByCost();
        if (!w.squareWindow) {
            throw new UnsupportedOperationException("MaxAdditiveUrerValuationCriterion supports only Square windows");
        }

        for (Slot slot : w.slots) {
            sumDist += this.valuationModel.getSlotValue(slot, w.startTime, w.length);
        }

        return sumDist;
    }

    public String getDescription() {
        return "Max Dist";
    }
}
