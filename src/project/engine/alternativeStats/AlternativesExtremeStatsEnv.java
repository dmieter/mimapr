/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project.engine.alternativeStats;

import java.util.ArrayList;
import project.engine.data.Alternative;
import project.engine.data.DistributedTask;
import project.engine.data.Slot;
import project.engine.data.VOEHelper;
import project.engine.data.VOEnvironment;
import project.engine.slot.slotProcessor.criteriaHelpers.ValuationModelAltroTime;
import project.engine.slot.slotProcessor.criteriaHelpers.ValuationModelEgoTime;

/**
 *
 * @author Magica
 */
public class AlternativesExtremeStatsEnv extends AlternativesExtremeStats {

    double egoCharacteristicAverage = 0;
    double altroCharacteristicAverage = 0;

    public void processAlternatives(ArrayList<Alternative> alternatives, VOEnvironment env, long t) {

        double maxEgoValue = Double.NEGATIVE_INFINITY;
        double maxAltroValue = Double.NEGATIVE_INFINITY;

        processAlternatives(alternatives, t);

        for (Alternative a : alternatives) {

            double curEgoVal = getEgoCharacteristic(a, env);
            double curAltroVal = getAltroCharacteristic(a, env);

            if (curEgoVal > maxEgoValue) {
                maxEgoValue = curEgoVal;
            }

            if (curAltroVal > maxAltroValue) {
                maxAltroValue = curAltroVal;
            }
        }

        egoCharacteristicAverage = (egoCharacteristicAverage * (experimentsNum - 1) + maxEgoValue) / (experimentsNum);
        altroCharacteristicAverage = (altroCharacteristicAverage * (experimentsNum - 1) + maxAltroValue) / (experimentsNum);
    }

    public void processAlternatives(ArrayList<Alternative> alternatives, VOEnvironment env) {
        processAlternatives(alternatives, env, 0);
    }

    protected double getEgoCharacteristic(Alternative a, VOEnvironment env) {

        double sumDist = 0;

        for (Slot s : a.window.slots) {
            double prevEventTime = VOEHelper.getPreviousEventTime(s.resourceLine.id, s.start, env);
            double nextEventTime = VOEHelper.getNextEventTime(s.resourceLine.id, s.end, env);
            //System.out.println(s.resourceLine.id+". Next Event: "+ nextEventTime + " after " + (nextEventTime - s.end));
            //System.out.println(s.resourceLine.id+". Prev Event: "+ prevEventTime + " was after " + (s.start - prevEventTime));
            sumDist += new ValuationModelEgoTime().calculateDistanceFunction(s, s.start - prevEventTime, nextEventTime - s.end);
        }

        return sumDist/a.window.slots.size();
    }

    protected double getAltroCharacteristic(Alternative a, VOEnvironment env) {

        double sumDist = 0;

        for (Slot s : a.window.slots) {
            double prevEventTime = VOEHelper.getPreviousEventTime(s.resourceLine.id, s.start, env);
            double nextEventTime = VOEHelper.getNextEventTime(s.resourceLine.id, s.end, env);
            sumDist += new ValuationModelAltroTime().calculateDistanceFunction(s, s.start - prevEventTime, nextEventTime - s.end);
        }

        return sumDist/a.window.slots.size();
    }

    public String getData() {
        String data = super.getData()
                + "Ego Characteristic: " + this.egoCharacteristicAverage + "\n"
                + "Altruistic Characteristic: " + this.altroCharacteristicAverage + "\n";

        return data;
    }
}
