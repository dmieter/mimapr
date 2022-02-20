/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project.engine.slot.slotProcessor.criteriaHelpers;

import project.engine.data.Slot;

/**
 *
 * @author emelyanov
 */
public class ValuationModelCustomStats extends ValuationModelCustom {

    public static long costCounter = 0;
    public static long plusDistCounter = 0;
    public static long negDistCounter = 0;
    
    protected boolean richJob = false;
    private boolean shortJob = false;

    public ValuationModelCustomStats(CustomSlotValuationInterface slotCustomFunction) {
        super(slotCustomFunction);
    }

    @Override
    public Double getSlotValue(Slot s, double startTime, double length) {
        Double prevDistance = startTime - getPrevousTaskEnd(s);
        Double nextDistance = getNextTaskStart(s) - startTime - length;
        Double val = slotCustomFunction.evaluateSlot(s, startTime, length, prevDistance, nextDistance);
//        if (richJob && s.getPrice()/s.getPerformance() > 1.3) {
//            //val += 0.1*s.resourceLine.price;
//            //val *= 1.01;
//            val += 1;
//            //costCounter++;
//        }

        if (shortJob) {
            if (nextDistance <= 0.06 * length) {
                val += 1;
                //val *= 1.01;
                plusDistCounter++;
            } 
            else if (nextDistance > length*0.25 && nextDistance < length*0.5) {
                val -= 1;
                //val *= 0.99;
                negDistCounter++;
            }
            
            if (prevDistance <= 0.06 * length) {
                val += 0.5;
                //val *= 1.01;
                plusDistCounter++;
            } 
            else if (prevDistance > length*0.25 && prevDistance < length*0.5) {
                val -= 0.5;
                //val *= 0.99;
                negDistCounter++;
            }
        }

        return val;
    }

    /**
     * @return the richJob
     */
    public boolean isRichJob() {
        return richJob;
    }

    /**
     * @param richJob the expensiveJob to set
     */
    public void setRichJob(boolean richJob) {
        this.richJob = richJob;
    }

    /**
     * @return the shortJob
     */
    public boolean isShortJob() {
        return shortJob;
    }

    /**
     * @param shortJob the shortJob to set
     */
    public void setShortJob(boolean shortJob) {
        this.shortJob = shortJob;
    }

}
