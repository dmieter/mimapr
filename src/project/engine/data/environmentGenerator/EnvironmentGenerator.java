package project.engine.data.environmentGenerator;

import project.engine.data.ComputingResourceLine;
import project.engine.data.ComputingNode;
import project.engine.data.VOEnvironment;

import java.util.ArrayList;
import java.util.Random;
import project.math.utils.MathUtils;

/**
 * Created by IntelliJ IDEA. User: Rookie Date: 30.03.2010 Time: 23:52:03 To
 * change this template use File | Settings | File Templates.
 */
public class EnvironmentGenerator {

    public String localTaskName = "task";//"local task";

    public VOEnvironment generate(EnvironmentGeneratorSettings settings, ArrayList<ComputingNode> resourceTypes) {
        if (resourceTypes == null) {
            resourceTypes = generateResourceTypes(settings);
        }

        VOEnvironment env = new VOEnvironment();
        env.resourceLines = new ArrayList<ComputingResourceLine>();
        for (ComputingNode res : resourceTypes) {
            double occupancyLevel = settings.occupancyLevel;
            if (settings.occupGenerator != null) {
                occupancyLevel = settings.occupGenerator.getRandom();   // different occupancy for each line
            }
            ComputingResourceLine rLine = new ComputingResourceLine(res);
            if (res.id == 0) {        //if in the init state
                rLine.id = (new Random()).nextInt();
            } else {
                rLine.id = res.id;
            }
            rLine.environment = env;
            rLine.q = MathUtils.getUniform(settings.minQ, settings.maxQ);
            int failure_counter;
            //dumb algorithm
            if (occupancyLevel > 0) {
                do {
                    double taskStart;
                    double taskLength;
                    failure_counter = 0;
                    do {
                        taskStart = (int) MathUtils.getUniform(0, settings.timeInterval);      //unco 04.05.2010
                        taskLength = (int) MathUtils.getGaussian(settings.minTaskLength, settings.maxTaskLength, (settings.minTaskLength + settings.maxTaskLength) / 2);
                        failure_counter++;
                    } while ((rLine.isReserved(taskStart, taskStart + taskLength)
                            || taskStart + taskLength > settings.timeInterval)
                            && failure_counter < 100000);
                    if (failure_counter < 100000) {
                        rLine.AddTask(localTaskName, taskStart, taskStart + taskLength);
                    }
                } while (rLine.getTotalOccupancyTime() / settings.timeInterval * 10 < occupancyLevel && failure_counter < 100000);
            }
            env.resourceLines.add(rLine);
        }
        return env;
    }

    public ArrayList<ComputingNode> generateResourceTypes(EnvironmentGeneratorSettings settings) {
        ArrayList<ComputingNode> resList = new ArrayList<ComputingNode>();
        for (int i = 1; i <= settings.resourceLineNum; i++) {
            double sp = 0;
            if (settings.perfGenerator != null) {
                sp = settings.perfGenerator.getRandomInteger();
            } else if (settings.minResourceSpeed != -1 && settings.maxResourceSpeed != -1) {
                sp = MathUtils.getUniform(settings.minResourceSpeed, settings.maxResourceSpeed);
            } else {
                sp = 10;
            }
            String name = String.valueOf((int) sp);
            ComputingNode r = new ComputingNode(i, "res" + i + "_" + name, (int) sp);
            resList.add(r);
        }
        return resList;
    }
}
