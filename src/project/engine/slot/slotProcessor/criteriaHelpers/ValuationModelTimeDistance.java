package project.engine.slot.slotProcessor.criteriaHelpers;

import project.engine.data.DistributedTask;
import project.engine.data.Slot;
import project.engine.data.SlotCut;

/**
 *
 * @author dmieter
 */
public class ValuationModelTimeDistance extends ValuationModel {

    protected static final double MAX_DISTANCE = 500;

    protected Double getPrevousTaskEnd(Slot s){
        Double prevTaskEnd = s.start;
        if (s instanceof SlotCut) {
            SlotCut slotCut = (SlotCut) s;
            prevTaskEnd = slotCut.getPreviousStart();
        }
        return prevTaskEnd;
    }
    
    protected Double getNextTaskStart(Slot s){
        Double nextTaskStart = s.end;
        if (s instanceof SlotCut) {
            SlotCut slotCut = (SlotCut) s;
            nextTaskStart = slotCut.getPreviousEnd();
        }
        return nextTaskStart;
    }
    
    @Override
    public Double getSlotValue(Slot s, double startTime, double length) {
        return calculateDistanceFunction(s, startTime - getPrevousTaskEnd(s), getNextTaskStart(s) - startTime - length);
    }

    protected double calculateDistanceFunction(double prevDistance, double nextDistance) {

        if (prevDistance < 0 || nextDistance < 0) {
            throw new IllegalStateException("Incorrect distance values");
        }

        //return Math.min(prevDistance, MAX_DISTANCE) +  Math.min(nextDistance, MAX_DISTANCE);
        //return Math.min(Math.min(nextDistance, prevDistance), MAX_DISTANCE);
        return nextDistance + prevDistance;
    }

    public double calculateDistanceFunction(Slot s, double prevDistance, double nextDistance) {
        return calculateDistanceFunction(prevDistance, nextDistance);
    }
}
