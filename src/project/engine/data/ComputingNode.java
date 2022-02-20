package project.engine.data;

public class ComputingNode extends Resource {
    private double performance;

    public ComputingNode(int id, String name, double speed) {
        super(id, name);
        this.performance = speed;
    }
    
    public ComputingNode(String name, double speed) {
        super(0, name);
        this.performance = speed;
    }

    public double getPerformance() {
        return performance;
    }

    public void setPerformance(double speed) {
        this.performance = speed;
    }
}
