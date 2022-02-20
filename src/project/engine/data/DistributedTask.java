package project.engine.data;

import java.io.Serializable;
import project.engine.data.ComputingNode;

/**
 * Created by IntelliJ IDEA. User: unco Date: 22.03.2009 Time: 20:50:41 To
 * change this template use File | Settings | File Templates.
 */
public class DistributedTask implements Serializable {

    public double startTime = 0;
    public double endTime = 0;
    public ComputingNode resource;
    public String taskName = "";

    public DistributedTask(String name, double start, double end) {
        taskName = name;
        startTime = start;
        endTime = end;
        resource = null;
    }

    public DistributedTask() {
        startTime = 0;
        endTime = 0;
        taskName = "";
    }

    @Override
    public DistributedTask clone() {
        DistributedTask newDT = new DistributedTask(taskName, startTime, endTime);
        newDT.resource = resource;
        return newDT;
    }

}
