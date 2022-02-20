package project.engine.slot.slotProcessor.criteriaHelpers;

import project.engine.data.Slot;
import project.math.utils.MathUtils;

/**
 *
 * @author dmieter
 */
public class ValuationModelCustom extends ValuationModelTimeDistance {
    
    public static long lengthCounter = 0l;

    protected CustomSlotValuationInterface slotCustomFunction;

    public ValuationModelCustom(CustomSlotValuationInterface slotCustomFunction) {
        this.slotCustomFunction = slotCustomFunction;
    }

    @Override
    public Double getSlotValue(Slot s, double startTime, double length) {
        return slotCustomFunction.evaluateSlot(s, startTime, length, startTime - getPrevousTaskEnd(s), getNextTaskStart(s) - startTime - length);
    }

    public static Double complexValuation1(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length - 0.1 * s.resourceLine.getPerformance();

        return val;
    }
    
    public static Double complexValuation12(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length - 0.01 * s.resourceLine.getPerformance();

        return val;
    }
    
    public static Double complexValuation13(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length - 0.001 * s.resourceLine.getPerformance();

        return val;
    }
    
    public static Double complexValuation2(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length - 0.1 * s.resourceLine.getPerformance();

        return val;
    }

    public static Double complexValuation4(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length;// - 0.1 * s.resourceLine.getPerformance();    // choose low-performace nodes (penalty for performance)

        if (nextDistance <= length * 0.03) {
            val += 0.5;
            lengthCounter++;
        }else if (nextDistance > length * 0.2 && nextDistance <= length * 0.3) {
            val -= 0.005*nextDistance;
            lengthCounter++;
        }
        
        return val;
    }

    public static Double complexValuation42(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length - 0.1 * s.resourceLine.getPerformance();    // choose low-performace nodes (penalty for performance)

        if (nextDistance <= length * 0.03) {
            val *= 1.01;
            lengthCounter++;
        }else if (nextDistance > length * 0.2 && nextDistance <= length * 0.3) {
            val *= 0.995;
            lengthCounter++;
        }
        
        return val;
    }
    
    public static Double complexValuationSiegel(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length - 0.1 * s.resourceLine.getPerformance();    // choose low-performace nodes (penalty for performance)

        /* minimizing new slots count */
        if (nextDistance == 0) {
            val += 1;
        }
        if (prevDistance == 0) {
            val += 1;
        }
        
        /* minimizing next distance */
        val -= 0.0001 * nextDistance;
        
        return val;
    }
    
    public static Double complexValuationSiegelSimple(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime-length;

        /* minimizing new slots count */
        if (nextDistance == 0) {
            val += 0.5;
        }
        if (prevDistance == 0) {
            val += 0.5;
        }
        
        /* minimizing next distance */
        val -= 0.0001 * nextDistance;
        
        return val;
    }
    
    public static Double complexValuation5(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length - 0.1 * s.resourceLine.getPerformance();    // choose low-performace nodes (penalty for performance)

        if (nextDistance <= length * 0.03) {
            val += 0.5;
        }else if (nextDistance > length * 0.2 && nextDistance <= length * 0.35) {
            val -= 0.5;
        }else if (nextDistance > length){
            val += 0.1;
        }
        
        if (prevDistance <= length * 0.03) {
            val += 0.2;
        }else if (prevDistance > length * 0.2 && prevDistance <= length * 0.35) {
            val -= 0.2;
        }
        
        return val;
    }
    
    public static Double copValuation(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length - 0.1 * s.resourceLine.getPerformance();    // choose low-performace nodes (penalty for performance)

        if (nextDistance <= length * 0.03) {
            val += 0.5;
        }else if (nextDistance > length * 0.2 && nextDistance <= length * 0.35) {
            val -= 0.5;
        }else if (nextDistance > length){
            val += 0.1;
        }
        
        if (prevDistance <= length * 0.03) {
            val += 0.2;
        }else if (prevDistance > length * 0.2 && prevDistance <= length * 0.35) {
            val -= 0.2;
        }
        
        return val;
    }
    
    public static Double copValuation2(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length - 10 * s.getPerformance()*length/40;    // choose low-performace nodes (penalty for performance)

        if (nextDistance <= length * 0.03) {
            val += 0.5;
        }else if (nextDistance > length * 0.2 && nextDistance <= length * 0.35) {
            val -= 0.5;
        }else if (nextDistance > length){
            val += 0.1;
        }
        
        if (prevDistance <= length * 0.03) {
            val += 0.2;
        }else if (prevDistance > length * 0.2 && prevDistance <= length * 0.35) {
            val -= 0.2;
        }
        
        return val;
    }
    
    public static Double minProctime4(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        return -startTime - length -10*s.getPerformance()*length/40;
    }
    
    public static Double randomValuation(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length - 0.01 * MathUtils.getUniform(0, 1);
        return val;
    }
    
    public static Double minFinishValuation(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length;

        return val;
    }
    
    public static Double minStartValuation(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime;

        return val;
    }
    
    public static Double leastEffectiveValuation(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length + 0.1*s.getPrice()/s.getPerformance();  // take less effective, more expensive

        return val;
    }
    
    public static Double minCostValuation1(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length - 0.01*s.getPrice()*length/40;

        return val;
    }
    
    public static Double minCostValuation2(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length - 0.1*s.getPrice()*length/40;

        return val;
    }
    
    public static Double minCostValuation3(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length - 1*s.getPrice()*length/40;

        return val;
    }
    
    public static Double minCostValuation4(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length - 10*s.getPrice()*length/40;

        return val;
    }
    
    public static Double minCostValuation5(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length - 100*s.getPrice()*length/40;

        return val;
    }
    
    public static Double minCostValuation55(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length - 1000*s.getPrice()*length/40;

        return val;
    }
    
    public static Double minCostValuation6(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -s.getPrice()*length/40;

        return val;
    }
    
    public static Double maxCostValuation1(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length + 0.01*s.getPrice()*length/40;

        return val;
    }
    
    public static Double maxCostValuation2(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length + 0.1*s.getPrice()*length/40;

        return val;
    }
    
    public static Double maxCostValuation3(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length + 1*s.getPrice()*length/40;

        return val;
    }
    
    public static Double maxCostValuation4(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length + 10*s.getPrice()*length/40;

        return val;
    }
    
    public static Double maxCostValuation5(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length + 100*s.getPrice()*length/40;

        return val;
    }
    
    public static Double maxCostValuation55(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length + 1000*s.getPrice()*length/40;

        return val;
    }
    
    public static Double maxCostValuation6(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = s.getPrice()*length/40;

        return val;
    }
    
    public static Double maxPerformanceValuation1(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length + 0.01*s.resourceLine.getPerformance();

        return val;
    }
    
    public static Double maxPerformanceValuation2(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length + 0.1*s.resourceLine.getPerformance();

        return val;
    }
    
    public static Double maxPerformanceValuation3(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length + 1*s.resourceLine.getPerformance();

        return val;
    }
    
    public static Double maxPerformanceValuation4(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length + 10*s.resourceLine.getPerformance();

        return val;
    }
    
    public static Double maxPerformanceValuation5(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length + 100*s.resourceLine.getPerformance();

        return val;
    }
    
    public static Double maxPerformanceValuation55(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = -startTime - length + 1000*s.resourceLine.getPerformance();

        return val;
    }
    
    public static Double maxPerformanceValuation6(Slot s, Double startTime, Double length, Double prevDistance, Double nextDistance) {

        Double val = s.resourceLine.getPerformance();

        return val;
    }

}
