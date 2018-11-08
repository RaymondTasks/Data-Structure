package Lab2;

public class Main {

    public static void main(String args[]) {
        var sim = new ElevatorSimulator(5, 1, 3, 10,
                0, 100, 200, 1000,
                100, 20, 25,
                40, 300);
        sim.setLogEnable(true);
        var GUI = new SimulationGUI(sim, null, 100,
                100,
                90, 70, 80,
                500,
                50);
        sim.startSimulation();
        for (; ; ) {
            sim.nextEvent();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            GUI.refresh();
        }
    }
}
