package Lab2;

import Queue.OrderedLinkedQueue;

import javax.swing.*;

public class SimulationGUI {

    private ElevatorSimulator simulator;
    JFrame mainFrame;
    OrderedLinkedQueue events;

    public SimulationGUI(ElevatorSimulator simulator) {
        this.simulator = simulator;
        events = simulator.getEvents();

        //主窗口
        mainFrame = new JFrame();
        mainFrame.setTitle("Elevator Simulation");
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //todo 根据层数和电梯数初始化界面

    }

    private int floorGap;   //每层间隔

    public void refresh() {

    }
}
