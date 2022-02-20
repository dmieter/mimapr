package project.engine.data;

/**
 * represents a new slot cut out from older slot
 * we need it to evaluate window criterion when slots already trimmed to a window start and length
 * SlotCut maintains original slot start and finish time
 *
 * @author dmieter
 */
public class SlotCut extends Slot {

    protected Double previousStart;
    protected Double previousEnd;

    /* creating SlotCut out of old slot */
    public SlotCut(Slot slot, Double newStart, Double newEnd) {
        super(newStart, newEnd, slot.resourceLine);
        this.previousStart = slot.start;
        this.previousEnd = slot.end;
        this.id = slot.id;
        this.q = slot.q;
    }

    /**
     * @return the previousStart
     */
    public Double getPreviousStart() {
        return previousStart;
    }

    /**
     * @return the previousEnd
     */
    public Double getPreviousEnd() {
        return previousEnd;
    }

}
