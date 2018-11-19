package Lab2;

public class Main {
    public static void main(String args[]) {
        //数据维护部份
        var sim = new ElevatorSimulator(10, 1, 2, 10,
                0, 300, 1000, 10000,
                100, 20, 25,
                40, 300);
        //启用log
        sim.setLogEnable(true);
        //GUI部分
        var GUI = new SimulationGUI(sim, 5,
                100,
                90, 70, 80,
                500,
                50, 10);
        sim.startSimulation();  //启动模拟
        for (; ; ) {
            sim.nextEvent();  //下一个事件
            GUI.refresh();  //更新GUI
        }
    }
}
