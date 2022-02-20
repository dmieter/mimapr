package project.engine.data.jobGenerator;

import java.util.*;
import project.engine.data.ResourceRequest;
import project.engine.slot.slotProcessor.criteriaHelpers.MinFinishTimeCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinRunTimeCriteria;
import project.engine.slot.slotProcessor.criteriaHelpers.MinSumCostCriteria;
import project.math.utils.MathUtils;

/**
 * Created by IntelliJ IDEA.
 * User: Rookie
 * Date: 16.11.2009
 * Time: 15:44:44
 * To change this template use File | Settings | File Templates.
 */
public class RequestGenerator {

    private ArrayList<ResourceRequest> requests;

    public ArrayList<ResourceRequest> getRequests()
    {
        if (requests == null)
        {
            requests = generate(null);
        }
        return requests;
    }

    

    public ArrayList<ResourceRequest> generate(JobGeneratorSettings settings)
    {
        ArrayList<ResourceRequest> results = new ArrayList<ResourceRequest>();
        if (settings == null)
        {
            return results;
        }
        for (int i=0; i<settings.taskNumber; i++)
        {

            ResourceRequest req = new ResourceRequest();

            //resource need
            if(settings.cpuNumGen!=null){
                req.resourceNeed = (int)settings.cpuNumGen.getRandom();
            }
            else if(settings.useGaussianCPU)
            {
                req.resourceNeed = (int)MathUtils.getGaussian((double)settings.minCPU, (double)settings.maxCPU, (double)settings.avgCPU);
            }
            else
            {
                req.resourceNeed = MathUtils.getUniform(settings.minCPU, settings.maxCPU);
            }
            //time
            if(settings.timeGen!=null){
                req.volume = settings.timeGen.getRandom();
            }
            else if (settings.useGaussianTime)
            {
                req.volume = MathUtils.getGaussian(settings.minTime, settings.maxTime, settings.avgTime);
            }
            else
            {
                req.volume = MathUtils.getUniform(settings.minTime, settings.maxTime);
            }

            //speed
            if(settings.perfGen!=null){
                req.minNodePerformance = settings.perfGen.getRandom();
            }
            else if (settings.useGaussianSpeed)
            {
                req.minNodePerformance = MathUtils.getGaussian(settings.minSpeed, settings.maxSpeed, settings.avgSpeed);
            }
            else
            {
                req.minNodePerformance = MathUtils.getUniform(settings.minSpeed, settings.maxSpeed);
            }
            
            //price
            if(settings.maxPriceGen!=null){
                req.maxNodePrice = settings.maxPriceGen.getRandom();
            }
            else if (settings.useGaussianPrice)
            {
                req.maxNodePrice = MathUtils.getGaussian(settings.minPrice, settings.maxPrice, settings.avgPrice);
            }
            else
            {
                req.maxNodePrice = MathUtils.getUniform(settings.minPrice, settings.maxPrice);
            }
            if(settings.useSpeedPriceFactor){
                req.maxNodePrice*=req.minNodePerformance;
            }
/*            //timestamp
            if (settings.bufferLength != 0 && settings.absoluteStart != null)
            {
                double offset = getUniform(0, settings.bufferLength);
                req.timestamp = new Date();
                req.timestamp.setTime(settings.absoluteStart.getTime() + Math.round(offset*1000));
            }*/
            results.add(req);
        }
        if (settings.sortBy != 0)
        {
            switch (settings.sortBy)
            {
                case 2: //priceMax
                   Collections.sort(results,new Comparator<ResourceRequest>()
                   {
                        public final int compare ( ResourceRequest a, ResourceRequest b )
                        {
                            return Double.compare(a.maxNodePrice, b.maxNodePrice);
                        }
                    });
                    break;
                case 3: //Time
                   Collections.sort(results,new Comparator<ResourceRequest>()
                   {
                        public final int compare ( ResourceRequest a, ResourceRequest b )
                        {
                            return Double.compare(a.volume,  b.volume);
                        }
                    });
                    break;
                case 4: //CPUNeed
                   Collections.sort(results,new Comparator<ResourceRequest>()
                   {
                        public final int compare ( ResourceRequest a, ResourceRequest b )
                        {
                            return Double.compare(a.resourceNeed, b.resourceNeed);
                        }
                    });
                    break;
                case 5: //speed
                   Collections.sort(results,new Comparator<ResourceRequest>()
                   {
                        public final int compare ( ResourceRequest a, ResourceRequest b )
                        {
                            return Double.compare(a.minNodePerformance, b.minNodePerformance);
                        }
                    });
                    break;
            }
        }
        return results;
    }

}
