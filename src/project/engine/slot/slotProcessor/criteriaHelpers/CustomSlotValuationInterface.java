
package project.engine.slot.slotProcessor.criteriaHelpers;

import project.engine.data.Slot;

/**
 *
 * @author emelyanov
 */
@FunctionalInterface
public interface CustomSlotValuationInterface {
    public Double evaluateSlot(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance);
}
