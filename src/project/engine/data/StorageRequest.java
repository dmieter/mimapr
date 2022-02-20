
package project.engine.data;

import project.engine.slot.slotProcessor.criteriaHelpers.ICriteriaHelper;

/**
 *
 * @author emelyanov
 */
public class StorageRequest {
    private int requiredVolume;
    private int maxDrives = 100;
    private ICriteriaHelper criterion;
    
    public StorageRequest(int requiredVolume, ICriteriaHelper criterion, int maxDrives){
        this.requiredVolume = requiredVolume;
        this.maxDrives = maxDrives;
        this.criterion = criterion;
    }
    
    public StorageRequest(int requiredVolume, ICriteriaHelper criterion){
        this.requiredVolume = requiredVolume;
        this.criterion = criterion;
    }
    
    public StorageRequest(int requiredVolume){
        this.requiredVolume = requiredVolume;
    }
    
    public StorageRequest clone(){
        return new StorageRequest(requiredVolume, criterion, maxDrives);
    }

    /**
     * @return the requiredVolume
     */
    public int getRequiredVolume() {
        return requiredVolume;
    }

    /**
     * @return the maxDrives
     */
    public int getMaxDrives() {
        return maxDrives;
    }

    /**
     * @return the criterion
     */
    public ICriteriaHelper getCriterion() {
        return criterion;
    }
    
}
