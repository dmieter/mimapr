/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project.engine.slot.slotProcessor;

import com.dmieter.algorithm.opt.knapsack.Item;
import com.dmieter.algorithm.opt.knapsack.knapsack01.multiweights.FixedItemsNumberKnapsackProblem;
import com.dmieter.algorithm.opt.knapsack.knapsack01.multiweights.FixedItemsNumberKnapsackSolver;
import com.dmieter.algorithm.opt.knapsack.knapsack01.multiweights.FixedItemsNumberKnapsackSolverOpt;
import com.dmieter.algorithm.opt.knapsack.knapsack01.multiweights.IKnapsack01MultiWeightsSolver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import project.engine.data.ComputingResourceLine;
import project.engine.data.Slot;
import project.engine.data.SlotCut;
import project.engine.data.UserJob;
import project.engine.data.VOEHelper;
import project.engine.data.Window;
import project.engine.slot.slotProcessor.criteriaHelpers.MaxAdditiveUserValuationCriterion;
import project.engine.slot.slotProcessor.criteriaHelpers.MaxAdditiveUserValuationUnlimCCriterion;
import project.engine.slot.slotProcessor.criteriaHelpers.ValuationModel;

/**
 *
 * @author dmieter
 */
public abstract class SquareWindowFinder extends SlotProcessorV2 {

    //FixedItemsNumberKnapsackSolverOpt solver = new FixedItemsNumberKnapsackSolverOpt();
    //FixedItemsNumberKnapsackSolver solver = new FixedItemsNumberKnapsackSolver();
    protected IKnapsack01MultiWeightsSolver solver = new FixedItemsNumberKnapsackSolver();

    abstract public Window findSquareWindow(UserJob job, List<Slot> slots);

    abstract public Window findSquareWindow(UserJob job, List<Slot> slots, List<ComputingResourceLine> nodes);

    abstract public Window findSquareWindow(UserJob job, double volume, int nodesNum, List<Slot> slots, List<Double> performanceList);

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

        if (job.resourceRequest.criteria instanceof MaxAdditiveUserValuationCriterion) {
            /* max cost limit inside */
            if (!pickSlotsByKnapsack(job, startTime, length, windowList, bestSlots)) {
                return null;
            }
        } else if (job.resourceRequest.criteria instanceof MaxAdditiveUserValuationUnlimCCriterion) {
            /* unlimited by cost scenario */
            if (!pickSlotsGreedy(job, startTime, length, windowList, bestSlots)) {
                return null;
            }
        } else {
            /* retrieving best square window for a trivial criteria: minCost, minFinish, etc. */
 /* this call sets first slotsNeed slots inside tempW as best */
            job.resourceRequest.criteria.getCriteriaValue(tempW);

            /* we should have max Cost check here */
            if (checkCostForWindow(tempW)) {
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

    public boolean pickSlotsByKnapsack(UserJob job, double startTime, double length,
            final List<Slot> extendedList,
            final List<Slot> bestSlots) {

        MaxAdditiveUserValuationCriterion jobCriterion = (MaxAdditiveUserValuationCriterion) job.resourceRequest.criteria;
        ValuationModel valuation = jobCriterion.getValuationModel();

        FixedItemsNumberKnapsackProblem problem = new FixedItemsNumberKnapsackProblem();
        List<Item> items = new ArrayList<>(extendedList.size());

        for (Slot s : extendedList) {
            Item item = new Item((int) s.id, valuation.getSlotWeightInt(s, length), valuation.getSlotValue(s, startTime, length));
            item.setRefObject(s);
            items.add(item);
        }

        problem.setItems(items);
        problem.setItemsRequiredNumber(job.resourceRequest.resourceNeed);
        problem.setMaxWeight(job.resourceRequest.getMaxCostInt());

        FixedItemsNumberKnapsackSolverOpt.dpOperations.addIteration();

        if (solver.solve(problem)) {
//            System.out.println("\n\nIteration: " + FixedItemsNumberKnapsackSolverOpt.dpOperations.getIterations()
//                    + "\nInit Operations: " + FixedItemsNumberKnapsackSolverOpt.dpOperations.getInitOperations()
//                    + "\nMain Operations: " + FixedItemsNumberKnapsackSolverOpt.dpOperations.getOperations());
            for (Item item : problem.getSelectedItems()) {
                bestSlots.add((Slot) item.getRefObject());
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean pickSlotsGreedy(UserJob job, double startTime, double length,
            final List<Slot> extendedList,
            final List<Slot> bestSlots) {

        MaxAdditiveUserValuationUnlimCCriterion jobCriterion = (MaxAdditiveUserValuationUnlimCCriterion) job.resourceRequest.criteria;
        ValuationModel userValuation = jobCriterion.getValuationModel();
        List<Slot> sortedList = new ArrayList<>(extendedList);
        // sorting slots by user valuation

        Collections.sort(sortedList, (Slot a, Slot b)
                -> (userValuation.getSlotValue(b, startTime, length)
                        .compareTo(userValuation.getSlotValue(a, startTime, length))));

        bestSlots.addAll(sortedList.subList(0, job.resourceRequest.resourceNeed));

        return true;
    }

    @Override
    protected boolean checkCostForWindow(Window w) {
        if (w.slots.size() < w.resourceRequest.resourceNeed) {
            return false;
        }

        double s = 0;
        for (int i = 0; i < w.resourceRequest.resourceNeed; i++) {
            s += w.slots.get(i).getLengthCost(w.length);
        }

        return s <= w.resourceRequest.getMaxCost();
    }

    public void useOptimizedImplementation(boolean use) {
        flush();
        if (use) {
            this.solver = new FixedItemsNumberKnapsackSolverOpt();
        } else {
            this.solver = new FixedItemsNumberKnapsackSolver();
        }
    }

    public void flush() {
        solver.flush();
    }

}
