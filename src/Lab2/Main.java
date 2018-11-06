package Lab2;

public class Main {

    public static void main(String args[]) {
        var sim = new ElevatorSimulator(5, 1, 4, 10,
                0, 100, 200, 1000,
                100, 20, 25, 40, 300);
        var GUI = new SimulationGUI(sim);
        sim.startSimulation();
        for (; ; ) {
            sim.nextEvent();
            System.gc();
            GUI.refresh();
        }
    }
}
