
package project.engine.alternativeStats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import project.engine.data.UserJob;
import project.utils.export.ExcelExportable;

/**
 *
 * @author emelyanov
 */
public class NamedStats implements ExcelExportable {
    
    private String name;
    private Map<String, DescriptiveStatistics> stats = new HashMap<>();
    private final DescriptiveStatistics jobsNumStats = new DescriptiveStatistics();
    
    public NamedStats(String name){
       this.name = name;
       stats.clear();
    }
    
    public void clearStats(){
        jobsNumStats.clear();
    }
    
    public void addValue(String name, Double value){
        DescriptiveStatistics varStats = stats.get(name);
        if(varStats == null){
            varStats = new DescriptiveStatistics();
            stats.put(name, varStats);
        }
        varStats.addValue(value);
    }
    
    public String getData(){
        String data = "\n";
        if(name != null){
            data += name.toUpperCase() + "\n";
        }
        
        for(Map.Entry<String,DescriptiveStatistics> entry : stats.entrySet()){
            DescriptiveStatistics varStats = entry.getValue();
            data += entry.getKey() + ": \n" + 
                    "\t" + varStats.getMean() + "(" + varStats.getN() + ")"+ "\n" +
                    "\t min: " + varStats.getMin() + "\n" +
                    "\t 25%: " + varStats.getPercentile(25) + "\n" +
                    "\t 50%: " + varStats.getPercentile(50) + "\n" +
                    "\t 75%: " + varStats.getPercentile(75) + "\n" +
                    "\t max: " + varStats.getMax() + "\n";
        }
        
        return data;
    }
    
}
