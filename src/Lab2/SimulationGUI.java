package Lab2;

import javax.swing.*;
import java.awt.*;

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
    private int floorHeight;
    private int carriageHeight;
    private int carriageWidth;
    private int carriageShaftWidth;
    private int waitingAreaWidth;
    private int buttonWidth;
    private Image passengerImg;
    private int frameHeight;
    private int frameWidth;

    /**
     * @param simulator          后端模拟器
     * @param passengerImg       乘客图片
     * @param timeScale          时间粒度
     * @param floorHeight        楼层高度
     * @param carriageHeight     电梯厢高度
     * @param carriageWidth      电梯厢宽度
     * @param carriageShaftWidth 电梯井宽度
     * @param waitingAreaWidth   等待区宽度
     * @param buttonWidth        按钮区宽度
     */
    public SimulationGUI(ElevatorSimulator simulator, Image passengerImg, long timeScale,
                         int floorHeight,
                         int carriageHeight, int carriageWidth, int carriageShaftWidth,
                         int waitingAreaWidth, int buttonWidth) {
        this.simulator = simulator;
        this.timeScale = timeScale;

        //读取必要的后端数据
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
        this.passengerImg = passengerImg;
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

        mainPanel = new JPanel();
        setContentPane(mainPanel);
        mainPanel.setLayout(null);
        mainPanel.setSize(frameWidth, frameHeight);
        setSize(frameWidth + 14, frameHeight + 39);  //解决错位问题

        //初始化各个panel
        waitingAreaPanel = new WaitingAreaPanel[floors];
        for (int i = 0; i < floors; i++) {
            waitingAreaPanel[i] = new WaitingAreaPanel(i);
            mainPanel.add(waitingAreaPanel[i]);
            waitingAreaPanel[i].setSize(waitingAreaWidth, floorHeight);
            waitingAreaPanel[i].setLocation(frameWidth - waitingAreaWidth, (floors - 1 - i) * floorHeight);
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

    //电梯井
    class CarriageShaftPanel extends JPanel {
        CarriagePanel carriagePanel;
        ElevatorSimulator.Carriage carriage;

        public CarriageShaftPanel(ElevatorSimulator.Carriage carriage) {
            this.carriage = carriage;
            setLayout(null);
            carriagePanel = new CarriagePanel();
            add(carriagePanel);
            carriagePanel.setSize(carriageWidth, carriageHeight);
            carriagePanel.setLocation((carriageShaftWidth - 2 - carriageWidth) / 2 + 1,
                    frameHeight - floorHeight - carriageHeight);
        }

        public void refresh() {
            carriagePanel.refresh();
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            g.setColor(Color.black);
            g.drawLine(0, 0, 0, frameHeight - 1);
            g.drawLine(carriageShaftWidth - 1, 0, carriageShaftWidth - 1, frameHeight - 1);
        }

        //电梯厢
        class CarriagePanel extends JPanel {
            double openedPercent = 0.0;  //门开的比例
            double nowFloor = carriage.nowFloor;

            public void refresh() {
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
                setLocation(getX(), frameHeight - (int) (nowFloor * floorHeight) - carriageHeight);
                carriagePanel.repaint();
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
        private int floor;

        public WaitingAreaPanel(int floor) {
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
            g.fillRect(waitingAreaWidth - signWidth, 1, signWidth, signHeight);

            //绘制等待队列
            var iter = passengerQueues[floor].iterator();
            int x = 10;
            while (iter.hasNext()) {
                var p = iter.next();
                g.setColor(p.color);
                g.fillOval(x, floorHeight - 15, 15, 15);
                x += 10;
            }
        }
    }

    //按钮区域
    class ButtonPanel extends JPanel {
        private int floor;

        public ButtonPanel(int floor) {
            this.floor = floor;
        }

        public void refresh() {
            repaint();
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            g.setColor(Color.black);
            g.drawLine(0, 0, 0, floorHeight - 1);
            g.drawLine(0, 0, buttonWidth - 1, 0);
            g.drawLine(0, floorHeight - 1, buttonWidth - 1, floorHeight - 1);

            if (floor != floors) {
                g.setColor(callingUp[floor] ? Color.red : Color.black);
                //todo 绘制上箭头
            }
            if (floor != 0) {
                g.setColor(callingDown[floor] ? Color.red : Color.black);
                //todo 绘制下箭头
            }


        }

    }
}


