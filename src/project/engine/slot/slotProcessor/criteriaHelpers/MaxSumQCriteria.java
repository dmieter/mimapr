/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project.engine.slot.slotProcessor.criteriaHelpers;

import project.engine.data.Window;

/**
 *
 * @author Magica
 */
public class MaxSumQCriteria implements ICriteriaHelper {

    public double getCriteriaValue(Window w) {
        double sum = 0;
        w.sortSlotsByCost();
        for (int i = 0; i < w.resourceRequest.resourceNeed; i++) {
            sum += w.slots.get(i).q;
        }

        return sum;
    }

    public String getDescription() {
        return "max Q";
    }
}
