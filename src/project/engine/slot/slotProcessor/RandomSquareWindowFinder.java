
package project.engine.slot.slotProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import project.engine.data.Slot;
import project.engine.data.SlotCut;
import project.engine.data.UserJob;
import project.engine.data.VOEHelper;
import project.engine.data.Window;

/**
 *
 * @author emelyanov
 */
// performs random secondary criteria optimization during subslot allocation procedure
public class RandomSquareWindowFinder extends SimpleSquareWindowFinder {
    
    private Integer shuffleLimit = 100;
    
    public RandomSquareWindowFinder(){
        
    }
    
    public RandomSquareWindowFinder(Integer shuffleLimit) {
        this.shuffleLimit = shuffleLimit;
    }
    
    @Override
    public Window selectBestWindow(UserJob job, final ArrayList<Slot> extendedList, double startTime, double length) {
        List<Slot> windowList = VOEHelper.copySlotList(extendedList);

        Window w = new Window(job.resourceRequest);
        w.squareWindow = true;
        w.startTime = startTime;
        w.length = length;

        Window tempW = w.clone();
        tempW.slots.addAll(windowList);
        tempW.sortSlotsByCost();
        if (!checkCostForWindow(tempW)) {
            return null;    // it's not possible to gather window out of these slot list
        }

        List<Slot> bestSlots = new ArrayList<>(w.resourceRequest.resourceNeed);

        for(int i = 0; i < shuffleLimit; i++){
            Collections.shuffle(tempW.slots);
            if (checkCostForWindow(tempW)) {
                bestSlots = tempW.slots.subList(0, w.resourceRequest.resourceNeed);
                //System.out.println("Shuffled");
                break;
            }
        }
        
        if (bestSlots.isEmpty()) {  // if no window found by shuffling, then return  min cost
            
            tempW.sortSlotsByCost();
            if (checkCostForWindow(tempW)) {
                //System.out.println("MinCosted");
                bestSlots = tempW.slots.subList(0, w.resourceRequest.resourceNeed);
            }
        }

        if (!bestSlots.isEmpty()) {
            double finishTime = startTime + length;
            for (Slot s : bestSlots) {
                SlotCut slotCut = new SlotCut(s, startTime, finishTime);
                w.slots.add(slotCut);
            }
            w.sortSlotsByCost();  // we  may want to return cheap slots first
            return w;
        } else {
            return null;
        }
    }
}
