package Lab2;

import javax.swing.*;
import java.awt.*;

/**
 * 乘客画个火柴人
 */

public class SimulationGUI extends JFrame {

    private ElevatorSimulator simulator;
    private long timeScale;
    private int floors;
    private int upAndDownTime;
    private int enterAndExitTime;
    private int openAndCloseTime;
    private ElevatorSimulator.Carriage carriages[];
    private PassengerLinkedQueue passengerQueues[];
    private boolean callingUp[];
    private boolean callingDown[];
    private int carriageMaxLoad;
    private WaitingAreaPanel waitingAreaPanel[];
    private CarriageShaftPanel carriageShaftPanel[];
    private ButtonPanel buttonPanel[];
    private int parallelNumber;
    private Container mainPanel;

    //绘图细节参数
    private int floorHeight;        //楼层高度
    private int carriageHeight;     //电梯高度
    private int carriageWidth;      //电梯宽度
    private int carriageShaftWidth; //电梯井宽度
    private int waitingAreaWidth;   //等待区宽度
    private int buttonWidth;        //按钮宽度
    private Image passengerImg;
    private int frameHeight;
    private int frameWidth;

    public SimulationGUI(ElevatorSimulator simulator, Image passengerImg, long timeScale,
                         int floorHeight,
                         int carriageHeight, int carriageWidth, int carriageShaftWidth,
                         int waitingAreaWidth, int buttonWidth) {
        this.simulator = simulator;
        this.timeScale = timeScale;
        this.passengerImg = passengerImg;

        //读取必要的参数
        this.floors = simulator.getFloors();
        this.carriages = simulator.getCarriages();
        this.passengerQueues = simulator.getPassengerQueues();
        this.parallelNumber = simulator.getParallelNumber();
        this.callingUp = simulator.getCallingUp();
        this.callingDown = simulator.getCallingDown();
        this.carriageMaxLoad = simulator.getMaxLoad();
        this.upAndDownTime = simulator.getUpAndDownTime();
        this.enterAndExitTime = simulator.getEnterAndExitTime();
        this.openAndCloseTime = simulator.getOpenAndCloseTime();

        //界面参数设置
        this.floorHeight = floorHeight;
        this.carriageHeight = carriageHeight;
        this.carriageWidth = carriageWidth;
        this.carriageShaftWidth = carriageShaftWidth;
        this.waitingAreaWidth = waitingAreaWidth;
        this.buttonWidth = buttonWidth;
        frameHeight = floorHeight * floors;
        frameWidth = carriageShaftWidth * parallelNumber + buttonWidth + waitingAreaWidth;

        setTitle("Elevator Simulation");
        setResizable(false);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainPanel = new MainPanel();  //解决错位问题
        setContentPane(mainPanel);
        mainPanel.setLayout(null);
        mainPanel.setSize(frameWidth, frameHeight);
        setSize(frameWidth + 14, frameHeight + 39);

        //初始化各个panel
        waitingAreaPanel = new WaitingAreaPanel[floors];
        for (int i = 0; i < floors; i++) {
            waitingAreaPanel[i] = new WaitingAreaPanel(passengerQueues[i], i);
            mainPanel.add(waitingAreaPanel[i]);
            waitingAreaPanel[i].setSize(waitingAreaWidth, floorHeight);
            waitingAreaPanel[i].setLocation(frameWidth - waitingAreaWidth, i * floorHeight);
        }
        carriageShaftPanel = new CarriageShaftPanel[parallelNumber];
        for (int i = 0; i < parallelNumber; i++) {
            carriageShaftPanel[i] = new CarriageShaftPanel(carriages[i]);
            mainPanel.add(carriageShaftPanel[i]);
            carriageShaftPanel[i].setSize(carriageShaftWidth, frameHeight);
            carriageShaftPanel[i].setLocation(i * carriageShaftWidth, 0);
        }
        buttonPanel = new ButtonPanel[floors];
        for (int i = 0; i < floors; i++) {
            buttonPanel[i] = new ButtonPanel(i);
            mainPanel.add(buttonPanel[i]);
            buttonPanel[i].setSize(buttonWidth, floorHeight);
            buttonPanel[i].setLocation(carriageShaftWidth * parallelNumber, i * floorHeight);
        }

    }

    public void refresh() {
        int nowTime = simulator.getNowTime();
        int nextTime = simulator.getNextTime();
        for (int i = nowTime; i < nextTime; i++) {

            for (var tmp : carriageShaftPanel) {
                tmp.refresh();
            }
            for (var tmp : waitingAreaPanel) {
                tmp.refresh();
            }
            for (var tmp : buttonPanel) {
                tmp.refresh();
            }

            try {
                Thread.sleep(timeScale);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    class MainPanel extends JPanel {
        @Override
        public void paint(Graphics g) {
            super.paint(g);
        }
    }


    //电梯井
    class CarriageShaftPanel extends JPanel {
        CarriagePanel carriagePanel;
        ElevatorSimulator.Carriage carriage;

        public CarriageShaftPanel(ElevatorSimulator.Carriage carriage) {
            this.carriage = carriage;
            setLayout(null);
            carriagePanel = new CarriagePanel();
            add(carriagePanel);
        }

        public void refresh() {
            carriagePanel.refresh();
            carriagePanel.move();
//            System.out.println("floor=" + carriagePanel.nowFloor + ", y=" + carriagePanel.getY());
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            g.setColor(Color.black);
            g.drawLine(0, 0, 0, frameHeight - 1);
            g.drawLine(carriageShaftWidth - 1, 0, carriageShaftWidth - 1, frameHeight - 1);
        }

        class CarriagePanel extends JPanel {
            double openedPercent = 0.0;  //门开的比例
            double nowFloor = carriage.nowFloor;

            public CarriagePanel() {
                System.out.println((carriageShaftWidth - 2 - carriageWidth) / 2 + 1);  //todo ?为什么是0

                setLocation((carriageShaftWidth - 2 - carriageWidth) / 2 + 1,
                        frameHeight - floorHeight - carriageHeight);
                setSize(carriageWidth, carriageHeight);
            }

            public void refresh() {
                System.out.println(((double) floorHeight) / upAndDownTime);
                switch (carriage.state) {
                    case idling:
                    case berthing:
                    case detecting:
                        nowFloor = carriage.nowFloor;
                        break;
                    case upping:
                    case idle_upping:
                        nowFloor += 1.0 / upAndDownTime;
                        break;
                    case downing:
                    case idle_downing:
                        nowFloor -= 1.0 / upAndDownTime;
                        break;
                    case opening:  //更新门的状态
                        openedPercent += 1.0 / openAndCloseTime;
                        break;
                    case closing:
                        openedPercent -= 1.0 / openAndCloseTime;
                        break;
                }
            }

            public void move() {
                setLocation(getX(), frameHeight - (int) (nowFloor * floorHeight) - carriageHeight);
                repaint();
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);

                //电梯边框
                g.setColor(Color.black);
                g.drawLine(0, 0, 0, carriageHeight - 1);
                g.drawLine(0, 0, carriageWidth - 1, 0);
                g.drawLine(carriageWidth - 1, carriageHeight - 1, 0, carriageHeight - 1);
                g.drawLine(carriageWidth - 1, carriageHeight - 1, carriageWidth - 1, 0);

                //电梯门
                int showedWidth = (int) ((carriageWidth - 2) / 2 * (1.0 - openedPercent));
                g.setColor(new Color(Color.gray.getRed(), Color.gray.getGreen(), Color.gray.getBlue(), 128));  //半透明灰色
                g.fillRect(1, 1, showedWidth, carriageHeight - 2);
                g.fillRect(carriageWidth - 1 - showedWidth, 1, showedWidth, carriageHeight - 2);
            }
        }
    }

    //等待区域
    class WaitingAreaPanel extends JPanel {
        PassengerLinkedQueue passengerQueue;
        int floor;

        public WaitingAreaPanel(PassengerLinkedQueue passengerQueue, int floor) {
            this.passengerQueue = passengerQueue;
            this.floor = floor;
        }

        public void refresh() {
            repaint();
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);

            g.setColor(Color.black);
            g.drawLine(0, 0, waitingAreaWidth - 1, 0);
            g.drawLine(0, floorHeight - 1, waitingAreaWidth - 1, floorHeight - 1);

            //楼层标志
            g.setColor(Color.gray);
            int signHeight = floorHeight / 3;
            int signWidth = 2 * floorHeight / 3;
            g.fillRect(waitingAreaWidth - signWidth, 0, signWidth, signHeight);

        }
    }

    //按钮区域
    class ButtonPanel extends JPanel {

        boolean up = false;
        boolean down = false;
        int floor;

        public ButtonPanel(int floor) {
            this.floor = floor;
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            g.setColor(Color.black);
            g.drawLine(0, 0, 0, floorHeight - 1);
            g.drawLine(0, 0, buttonWidth - 1, 0);
            g.drawLine(0, floorHeight - 1, buttonWidth - 1, floorHeight - 1);

        }

        public void refresh() {
            up = callingUp[floor];
            down = callingDown[floor];

            repaint();
        }

        private void drawArrowHead(Graphics g) {

        }
    }
}


