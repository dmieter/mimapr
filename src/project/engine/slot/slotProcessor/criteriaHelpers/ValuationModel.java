package project.engine.slot.slotProcessor.criteriaHelpers;

import project.engine.data.Slot;
import project.engine.data.UserJob;
import project.engine.data.VOEnvironment;
import project.math.utils.MathUtils;

/**
 *
 * @author emelyanov
 */
public class ValuationModel {

    protected UserJob job;
    protected VOEnvironment env;

    public ValuationModel() {
        this.job = null;
        this.env = null;
    }

    public ValuationModel(UserJob job, VOEnvironment env) {
        this.job = job;
        this.env = env;
    }

    public Double getSlotValue(Slot s, double startTime, double length) {
        return s.q;
    }

    public Double getSlotWeight(Slot s, double length) {
        return s.getLengthCost(length);
    }

    public int getSlotWeightInt(Slot s, double length) {
        return (int) MathUtils.nextUp(getSlotWeight(s, length));
    }

}
