package project.engine.slot.slotProcessor.criteriaHelpers;

/**
 *
 * @author dmieter
 */
public class ValuationModelEgoTime extends ValuationModelTimeDistance {

    // maximizing distance to a nearest task
    @Override
    protected double calculateDistanceFunction(double prevDistance, double nextDistance) {

        if (prevDistance < 0 || nextDistance < 0) {
            throw new IllegalStateException("Incorrect distance values");
        }
        
        //return Math.min(prevDistance, MAX_DISTANCE) +  Math.min(nextDistance, MAX_DISTANCE);
        return Math.min(Math.min(nextDistance, prevDistance), MAX_DISTANCE);
    }
    
}
