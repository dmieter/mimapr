package project.engine.former.deadline;

import project.engine.data.ComputingResourceLine;
import project.engine.data.Slot;
import project.engine.data.VOEnvironment;

import java.util.List;

/**
 * Created by Petrukha on 09.05.2016.
 */
public class DeadlineFormerVoeUtils {

    public static double getEnvironmentSpecificSlotLength(VOEnvironment environment, int cycleLength) {
        return getEnvironmentSpecificSlotLength(environment, 0, cycleLength);
    }

    public static double getEnvironmentSpecificSlotLength(VOEnvironment environment, int periodStart, int cycleLength) {
        double result = 0;
        for (ComputingResourceLine line : environment.resourceLines) {
            result += getSumSlotLength(line, periodStart, cycleLength) * line.getPerformance();
        }
        return result;
    }

    public static double getSumSlotLength(ComputingResourceLine line, int cycleLength){
        return getSumSlotLength(line, 0, cycleLength);
    }

    public static double getSumSlotLength(ComputingResourceLine line, int periodStart, int cycleLength){
        double result = 0;
        for (Slot s : line.getSlots()) {
            result += findSlotLengthInCycle(s, periodStart, cycleLength);
        }
        return result;
    }

    public static double getSumSlotLength(VOEnvironment environment, int cycleLength){
        double sLength = 0;
        for (ComputingResourceLine line : environment.resourceLines) {
            sLength += getSumSlotLength(line, cycleLength);
        }
        return sLength;
    }

    public static double getAverageSlotSpecificLength(List<ComputingResourceLine> lines, int cycleLength) {
        return getAverageSlotSpecificLength(lines, 0, cycleLength);
    }

    public static double getAverageSlotSpecificLength(List<ComputingResourceLine> lines, int periodStart, int cycleLength) {
        double result = 0;
        for (ComputingResourceLine line : lines) {
            int i = 0;
            double currentSlotLength = 0;
            for (Slot s : line.getSlots()) {
                currentSlotLength += findSlotLengthInCycle(s, periodStart, cycleLength) * line.getPerformance();
                if (currentSlotLength > 0) {
                    result = (result * i + currentSlotLength) / (i + 1);
                    i++;
                }
            }
        }
        return result;
    }

    private static double findSlotLengthInCycle(Slot slot, int periodStart, int cycleLength) {
        double start = slot.start;
        double end = slot.end;
        if (start < periodStart)
            start = periodStart;
        if (end > periodStart + cycleLength)
            end = periodStart + cycleLength;
        return (end - start >= 0) ? (end - start) : 0;
    }

}
