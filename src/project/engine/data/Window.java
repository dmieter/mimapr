/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project.engine.data;

import java.util.*;

public class Window {

    public ResourceRequest resourceRequest;
    public double startTime;
    public double length;
    public boolean squareWindow = false;
    
    public ArrayList<Slot> slots = new ArrayList<Slot>();

    @Override
    public Window clone() {
        Window w = new Window(resourceRequest.clone());
        w.length = this.length;
        w.startTime = this.startTime;
        w.slots = VOEHelper.copySlotList(this.slots);
        w.squareWindow = this.squareWindow;

        return w;
    }

    public Window(ResourceRequest resourceRequest) {
        this.resourceRequest = resourceRequest;
    }

    public double getTotalVolumeCost() {
        double s = 0;
        for (Slot slot : slots) {
            s += slot.getVolumeCost(this);
        }
        return s;
    }

    public void sortSlotsByCost() {
        final Window thisWindow = this;

        if (squareWindow) {
            Collections.sort(slots, new Comparator<Slot>() {
                public final int compare(Slot a, Slot b) {
                    return Double.compare(a.resourceLine.price, b.resourceLine.price);
                }
            });
        } else {
            Collections.sort(slots, new Comparator<Slot>() {
                public final int compare(Slot a, Slot b) {
                    return Double.compare(a.getVolumeCost(thisWindow), b.getVolumeCost(thisWindow));
                }
            });
        }
    }

    public void sortSlotsByCostDesc() {
        final Window thisWindow = this;
        Collections.sort(slots, new Comparator<Slot>() {
            public final int compare(Slot a, Slot b) {
                return Double.compare(b.getVolumeCost(thisWindow), a.getVolumeCost(thisWindow));
            }
        });
    }

    public boolean checkForCost() {
        double sum = 0;
        for (int i = 0; i < resourceRequest.resourceNeed; i++) {
            Slot s = slots.get(i);
            sum += s.getVolumeCost(this);
        }

        return (sum <= resourceRequest.getMaxCost());
    }
}
