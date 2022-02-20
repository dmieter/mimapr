package project.engine.slot.slotProcessor;

import com.dmieter.algorithm.opt.knapsack.knapsack01.multiweights.FixedItemsNumberKnapsackSolverOpt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import project.engine.data.Alternative;
import project.engine.data.ComputingResourceLine;
import project.engine.data.Slot;
import project.engine.data.UserJob;
import project.engine.data.VOEHelper;
import project.engine.data.Window;
import project.engine.slot.slotProcessor.criteriaHelpers.IReduceComplexity;
import project.math.utils.MathUtils;

/**
 *
 * @author dmieter
 */
public class SimpleSquareWindowFinder extends SquareWindowFinder {

    protected double bestCriteriaValue = Double.NEGATIVE_INFINITY;
    protected Window bestWindow = null;
    protected int retrieveWindowCallsCnt = 0;

    protected boolean findSquareWindowLimited(UserJob job, List<Slot> slots, WindowSearchParameters searchParams) {

        boolean windowFound = false;

        /* init class variables for next window search */
        double localBestCriterionValue = Double.NEGATIVE_INFINITY;
        double localWindowStartTime = 0;
        Window localBestWindow = null;

        ArrayList<Slot> extendedWindow = new ArrayList<>();

        for (int i = 0; i < slots.size(); i++) {
            Slot slot = slots.get(i);
        //for (Slot slot : slots) {
            localWindowStartTime = slot.start;
            
            int newPerformanceSlotsCnt = 0;

            /* checking only for slots with performance > minPerformnace and aenoufg length */
            if (slot.resourceLine.resourceType.getPerformance() >= searchParams.minPerformance
                    && slot.getLength() >= searchParams.length) {
                extendedWindow.add(slot);

                /* checking current extended slots for new start time */
                for (Iterator<Slot> itSlot = extendedWindow.iterator(); itSlot.hasNext();) {
                    Slot potentialSlot = itSlot.next();
                    if (localWindowStartTime + searchParams.length > potentialSlot.end) {
                        /* can't foem window with new start time */
                        itSlot.remove();

                    } else if (potentialSlot.resourceLine.resourceType.getPerformance() >= searchParams.minPerformance
                            && (searchParams.prevMinPerformance == null
                            || potentialSlot.resourceLine.resourceType.getPerformance() < searchParams.prevMinPerformance)) {
                        /* counting slots with performance between prev and current min 
                        (new slots comparing to previous performance iteration) */
                        newPerformanceSlotsCnt++;
                    }
                }
            } else {
                /* performing window search only if current slot was really added */
 /* maybe we should introduce more events to start the search, not only after new slot is added */
                //continue;
            }
            
            /* skipping window search if next i+1 slot has the same start time
               in this case we must trigger window search when all slots with the same start time are checked */
            if((i < slots.size() - 1) && (slots.get(i+1).start == slot.start)){
                continue;   // next i+1 slot has the same start time, we may first add it and then search for window
            }

            /* window search */
            if (extendedWindow.size() >= job.resourceRequest.resourceNeed /* we have some minimum size */
                    && newPerformanceSlotsCnt > 0) {
                /* we need new in terms of performance resources in the window list */

                retrieveWindowCallsCnt++;
                Window curWindow = selectBestWindow(job, extendedWindow, localWindowStartTime, searchParams.length);
                if (curWindow != null) {
                    double curCriterionValue = job.resourceRequest.criteria.getCriteriaValue(curWindow);

                    /* here sometimes we don't need to go to end of the scehduling interval.. for example when we minimize start time */
                    if (curCriterionValue > localBestCriterionValue) {
                        localBestCriterionValue = curCriterionValue;
                        localBestWindow = curWindow;
                        windowFound = true;
                        //System.out.println("New Best for " + localBestWindow.getTotalVolumeCost() + " out of " + localBestWindow.resourceRequest.getMaxCostInt());
                        if (job.resourceRequest.isFirstFit) {
                            break;  // first window found should be enough
                        }
                    } else if (job.resourceRequest.criteria instanceof IReduceComplexity) {
                        if (((IReduceComplexity) job.resourceRequest.criteria).stopConditionByCriterion(localBestCriterionValue, curCriterionValue)) {
                            break;  // we may stop here to reduce further computations
                        }
                    }
                }
            }
        }

        if (windowFound) {
            searchParams.window = localBestWindow;
            searchParams.criterionValue = localBestCriterionValue;
        }

        return windowFound;
    }

    protected List<Double> retrievePerformanceList(List<ComputingResourceLine> nodes) {

        Set<Double> performanceSet = new HashSet<>();

        for (ComputingResourceLine node : nodes) {
            performanceSet.add(node.resourceType.getPerformance());
        }

        List<Double> performanceList = new ArrayList<>(performanceSet);
        Collections.sort(performanceList, Collections.reverseOrder());

        return performanceList;
    }

    protected List<Double> retrieveSlotsPerformanceList(List<Slot> slots) {

        Set<Double> performanceSet = new HashSet<>();

        for (Slot slot : slots) {
            performanceSet.add(slot.resourceLine.resourceType.getPerformance());
        }

        List<Double> performanceList = new ArrayList<>(performanceSet);
        Collections.sort(performanceList, Collections.reverseOrder());

        return performanceList;
    }

    @Override
    public Window findSquareWindow(UserJob job, double volume, int nodesNum, List<Slot> slots, List<Double> performanceList) {

        bestCriteriaValue = Double.NEGATIVE_INFINITY;
        bestWindow = null;
        retrieveWindowCallsCnt = 0;

        List<Double> performancesSorted = new ArrayList<>();
        performancesSorted.addAll(performanceList);
        Collections.sort(performancesSorted, Collections.reverseOrder());

        Double prevPerformance = Double.POSITIVE_INFINITY;

        for (Double performance : performancesSorted) {
            Double windowLength = MathUtils.nextUp(volume / performance);

            /* we will search for window among slots with performnace >= performance */
 /* prevPerformance shows what slots were already considered in previous iterations and
            thus should be considerd as new in current iteration  */
            WindowSearchParameters params = new WindowSearchParameters(performance, prevPerformance, windowLength);

            boolean success = findSquareWindowLimited(job, slots, params);
            if (success && params.criterionValue > bestCriteriaValue) {
                bestCriteriaValue = params.criterionValue;
                bestWindow = params.window;
            }

            prevPerformance = performance;
        }

        return bestWindow;

    }

    @Override
    public Window findSquareWindow(UserJob job, List<Slot> slots, List<ComputingResourceLine> nodes) {

        List<Double> performanceList = retrievePerformanceList(nodes);

        return findSquareWindow(job,
                job.resourceRequest.getVolume(),
                job.resourceRequest.resourceNeed,
                slots,
                performanceList);
    }

    /**
     * @return the bestCriterionValue
     */
    public double getBestCriterionValue() {
        return bestCriteriaValue;
    }

    /**
     * @return the bestWindow
     */
    public Window getBestWindow() {
        return bestWindow;
    }

    /**
     * @return the windowStartTime
     */
    public Double getWindowStartTime() {
        return bestWindow == null ? null : bestWindow.startTime;
    }

    /**
     * @return the retrieveWindowCallsCnt
     */
    public int getRetrieveWindowCallsCnt() {
        return retrieveWindowCallsCnt;
    }

    @Override
    public Window findSquareWindow(UserJob job, List<Slot> slots) {
        return findSquareWindow(job,
                job.resourceRequest.getVolume(),
                job.resourceRequest.resourceNeed,
                slots,
                retrieveSlotsPerformanceList(slots));
    }

    public class WindowSearchParameters {

        public Window window;
        public Double criterionValue = Double.NEGATIVE_INFINITY;
        public Double minPerformance;
        public Double prevMinPerformance = null;
        public Double length;

        public WindowSearchParameters(Double minPerformance, Double length) {
            this.minPerformance = minPerformance;
            this.length = length;
        }

        public WindowSearchParameters(Double minPerformance, Double prevMinPerformance, Double length) {
            this.minPerformance = minPerformance;
            this.prevMinPerformance = prevMinPerformance;
            this.length = length;
        }

    }

    @Override
    public SlotProcessorResult findAlternatives(ArrayList<UserJob> jobs, ArrayList<Slot> slots, SlotProcessorSettings settings, int maxAlternatives) {

        List<UserJob> jobsForSearch = new ArrayList<>();
        jobsForSearch.addAll(jobs);

        ArrayList<Slot> slotsToUse = VOEHelper.copySlotList(slots);

        FixedItemsNumberKnapsackSolverOpt.dpOperations.resetAllOperations();

        while (!jobsForSearch.isEmpty()) {
            for (Iterator<UserJob> it = jobsForSearch.iterator(); it.hasNext();) {
                UserJob job = it.next();
                List<Double> performanceList = retrieveSlotsPerformanceList(slotsToUse);

                //System.out.println(job.name);
                Window w = findSquareWindow(job,
                        job.resourceRequest.volume,
                        job.resourceRequest.resourceNeed,
                        slotsToUse,
                        performanceList);
                if (w != null) {
                    subtractWindowFromSlots(w, slotsToUse);
                    Alternative a = new Alternative(w);
                    job.addAlternative(a);
                    if (job.alternatives.size() >= maxAlternatives) {
                        it.remove();
                    }
                } else {
                    /* found no window for a job - removing it from search list */
                    it.remove();
                    System.out.println("wow, no window for job " + job.name);
                }
            }
        }

        FixedItemsNumberKnapsackSolverOpt.dpOperations.resetAllOperations();

        return null;
    }

    public void flush() {
        super.flush();
        this.bestCriteriaValue = Double.NEGATIVE_INFINITY;
        this.bestWindow = null;
        this.retrieveWindowCallsCnt = 0;
    }
}
