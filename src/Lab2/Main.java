package Lab2;

public class Main {

    public static void main(String args[]) {


        var sim = new ElevatorSimulator(10, 1, 2, 10,
                0, 1000, 1000, 10000,
                100, 20, 25,
                40, 300);
        sim.setLogEnable(true);

        var GUI = new SimulationGUI(sim, null, 3,
                100,
                90, 70, 80,
                500,
                50, 10);
        sim.startSimulation();
        for (; ; ) {
            sim.nextEvent();
            GUI.refresh();
        }
    }
}
