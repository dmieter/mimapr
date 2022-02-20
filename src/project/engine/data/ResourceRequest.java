/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project.engine.data;

import project.engine.slot.slotProcessor.criteriaHelpers.ICriteriaHelper;
import project.math.utils.MathUtils;

public class ResourceRequest {

    //maximum allowed price of resource
    public double maxNodePrice;

    //number of resources needed
    public int resourceNeed;

    //minimum resource speed    
    public double minNodePerformance;

    //volume: time required to process job on a node with performance = 1
    public double volume;

    // deadline before this job must be completed
    public long deadLine;

    //alternative searching criteria
    public ICriteriaHelper criteria;

    //alternative searching criteria
    public Class initialCriteria;
    
    // is the first fit window found (i.e. with minimized start time) is ok or should we search through the whole slot list
    public boolean isFirstFit = false;

    public StorageRequest storageRequest;

    public ResourceRequest() {

    }

    //Back-compatibility
    public ResourceRequest(int cpuNum, double volume, double cash, double resSpeed) {
        this.resourceNeed = cpuNum;
        this.volume = volume;
        this.maxNodePrice = cash;
        this.minNodePerformance = resSpeed;
        this.deadLine = 0;
    }

    //Back-compatibility
    public ResourceRequest(int cpuNum, double volume, double cash, double resSpeed, long deadLine) {
        this.resourceNeed = cpuNum;
        this.volume = volume;
        this.maxNodePrice = cash;
        this.minNodePerformance = resSpeed;
        this.deadLine = deadLine;
    }

    public double getVolume() {
        return volume;
    }

    public Double getMaxCost() {
        return resourceNeed * volume * maxNodePrice;
    }

    public int getMaxCostInt() {
        return (int) MathUtils.nextUp(getMaxCost());
    }

    @Override
    public ResourceRequest clone() {
        ResourceRequest rr = new ResourceRequest(this.resourceNeed, this.volume, this.maxNodePrice,
                this.minNodePerformance, this.deadLine);
        rr.criteria = this.criteria;
        rr.isFirstFit = this.isFirstFit;
        
        if (storageRequest != null) {
            rr.storageRequest = storageRequest.clone();
        }
        return rr;
    }
}
