package Lab2;

import javax.swing.*;
import java.awt.*;

public class GUI extends JFrame {

    private ElevatorSimulation simulator;
    private long timeScale;
    private int floors;
    private int upAndDownTime;
    private int enterAndExitTime;
    private int openAndCloseTime;
    private ElevatorSimulation.Cabin cabins[];
    private PassengerLinkedQueue passengerQueues[];
    private boolean callingUp[];
    private boolean callingDown[];
    private int carMaxLoad;
    private WaitingAreaPanel waitingAreaPanel[];
    private CarShaftPanel carShaftPanel[];
    private ButtonPanel buttonPanel[];
    private int parallelNumber;
    private Container mainPanel;

    //绘图细节参数
    private int floorHeight;
    private int carHeight;
    private int carWidth;
    private int carShaftWidth;
    private int waitingAreaWidth;
    private int buttonWidth;
    private int frameHeight;
    private int frameWidth;
    private int passengerRadius;

    /**
     * SimulationGUI构造函数
     *
     * @param simulator        后端模拟器
     * @param timeScale        时间粒度
     * @param floorHeight      楼层高度
     * @param carHeight        电梯厢高度
     * @param carWidth         电梯厢宽度
     * @param carShaftWidth    电梯井宽度
     * @param waitingAreaWidth 等待区宽度
     * @param buttonWidth      按钮区宽度
     */
    public GUI(ElevatorSimulation simulator, long timeScale,
               int floorHeight,
               int carHeight, int carWidth, int carShaftWidth,
               int waitingAreaWidth, int buttonWidth,
               int passengerRadius) {
        this.simulator = simulator;
        this.timeScale = timeScale;

        //读取必要的后端数据
        this.floors = simulator.getFloors();
        this.cabins = simulator.getCabins();
        this.passengerQueues = simulator.getQueues();
        this.parallelNumber = simulator.getParallelNumber();
        this.callingUp = simulator.getCallingUp();
        this.callingDown = simulator.getCallingDown();
        this.carMaxLoad = simulator.getMaxLoad();
        this.upAndDownTime = simulator.getUpAndDownTime();
        this.enterAndExitTime = simulator.getEnterAndExitTime();
        this.openAndCloseTime = simulator.getOpenAndCloseTime();

        //界面参数设置
        this.floorHeight = floorHeight;
        this.carHeight = carHeight;
        this.carWidth = carWidth;
        this.carShaftWidth = carShaftWidth;
        this.waitingAreaWidth = waitingAreaWidth;
        this.buttonWidth = buttonWidth;
        this.passengerRadius = passengerRadius;
        frameHeight = floorHeight * floors;
        frameWidth = carShaftWidth * parallelNumber + buttonWidth + waitingAreaWidth;

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
        carShaftPanel = new CarShaftPanel[parallelNumber];
        for (int i = 0; i < parallelNumber; i++) {
            carShaftPanel[i] = new CarShaftPanel(cabins[i]);
            mainPanel.add(carShaftPanel[i]);
            carShaftPanel[i].setSize(carShaftWidth, frameHeight);
            carShaftPanel[i].setLocation(i * carShaftWidth, 0);
        }
        buttonPanel = new ButtonPanel[floors];
        for (int i = 0; i < floors; i++) {
            buttonPanel[i] = new ButtonPanel(i);
            mainPanel.add(buttonPanel[i]);
            buttonPanel[i].setSize(buttonWidth, floorHeight);
            buttonPanel[i].setLocation(carShaftWidth * parallelNumber, (floors - 1 - i) * floorHeight);
        }

    }

    public void refresh() {
        int nowTime = simulator.getNowTime();
        int nextTime = simulator.getNextTime();
        for (int i = nowTime; i < nextTime; i++) {

            for (var tmp : carShaftPanel) {
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
    class CarShaftPanel extends JPanel {
        CarPanel carPanel;
        ElevatorSimulation.Cabin cabin;

        public CarShaftPanel(ElevatorSimulation.Cabin cabin) {
            this.cabin = cabin;
            setLayout(null);
            carPanel = new CarPanel();
            add(carPanel);
            carPanel.setSize(carWidth, carHeight);
            carPanel.setLocation((carShaftWidth - 2 - carWidth) / 2 + 1,
                    frameHeight - floorHeight - carHeight);
        }

        public void refresh() {
            carPanel.refresh();
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            g.setColor(Color.black);
            g.drawLine(0, 0, 0, frameHeight - 1);
            g.drawLine(carShaftWidth - 1, 0, carShaftWidth - 1, frameHeight - 1);
        }

        //电梯厢
        class CarPanel extends JPanel {
            double openedPercent = 0.0;  //门开的比例
            double position = cabin.getNowFloor();

            public void refresh() {
                switch (cabin.getState()) {
                    case idling:
                    case berthing:
                    case detecting:
                        position = cabin.getNowFloor();
                        break;
                    case upping:
                    case idle_upping:
                        position += 1.0 / upAndDownTime;
                        break;
                    case downing:
                    case idle_downing:
                        position -= 1.0 / upAndDownTime;
                        break;
                    case opening:  //更新门的状态
                        openedPercent += 1.0 / openAndCloseTime;
                        break;
                    case closing:
                        openedPercent -= 1.0 / openAndCloseTime;
                        break;
                }
                setLocation(getX(), frameHeight - (int) (position * floorHeight) - carHeight);
                carPanel.repaint();
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);

                //电梯边框
                g.setColor(Color.black);
                g.drawLine(0, 0, 0, carHeight - 1);
                g.drawLine(0, 0, carWidth - 1, 0);
                g.drawLine(carWidth - 1, carHeight - 1, 0, carHeight - 1);
                g.drawLine(carWidth - 1, carHeight - 1, carWidth - 1, 0);

                //电梯门
                int showedWidth = (int) ((carWidth - 2) / 2 * (1.0 - openedPercent));
                g.setColor(new Color(Color.gray.getRed(), Color.gray.getGreen(), Color.gray.getBlue(), 128));  //半透明灰色
                g.fillRect(1, 1, showedWidth, carHeight - 2);
                g.fillRect(carWidth - 1 - showedWidth, 1, showedWidth, carHeight - 2);

                //todo 电梯内乘客
                int n = 1 + (int) Math.sqrt(carMaxLoad);
                int raw = 0;
                int column = 0;
                for (var ptmp : cabin.getLoad()) {
                    if (ptmp != null) {
                        g.setColor(ptmp.getColor());
                        g.fillOval(2 + column * (2 * passengerRadius + 1), carHeight - 1 - (raw + 1) * (2 * passengerRadius + 1),
                                2 * passengerRadius, 2 * passengerRadius);
                        column++;
                        if (column == n - 1) {
                            column = 0;
                            raw++;
                        }
                    }

                }
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
//            g.setColor(new Color(Color.gray.getRed(),Color.gray.getGreen(),Color.gray.getBlue(),128));
//            int signHeight = floorHeight / 3;
//            int signWidth = 2 * floorHeight / 3;
//            g.fillRect(waitingAreaWidth - signWidth, 1, signWidth, signHeight);

            g.setColor(Color.black);
            g.setFont(new Font("思源黑体", Font.PLAIN, 35));
            g.drawString(floor == 0 ? ("B1") : ("F" + floor), waitingAreaWidth - 50, 30);

            //绘制等待队列
            var iter = passengerQueues[floor].iterator();
            int x = 0;
            while (iter.hasNext()) {
                var p = iter.next();
                g.setColor(p.getColor());
                g.fillOval(x, floorHeight - 2 * passengerRadius - 1, 2 * passengerRadius, 2 * passengerRadius);
                x += 2 * passengerRadius + 5;
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
            Graphics2D g2 = (Graphics2D) g;
            super.paint(g2);
            g2.setColor(Color.black);
            g2.drawLine(0, 0, 0, floorHeight - 1);
            g2.drawLine(0, 0, buttonWidth - 1, 0);
            g2.drawLine(0, floorHeight - 1, buttonWidth - 1, floorHeight - 1);

            g2.setStroke(new BasicStroke(2));
            if (floor != floors - 1) {
                g2.setColor(callingUp[floor] ? Color.red : Color.black);
                g2.drawLine(buttonWidth / 2, floorHeight * 3 / 8, buttonWidth / 2, floorHeight / 8);
                g2.drawLine(buttonWidth / 2, floorHeight / 8, buttonWidth / 4, floorHeight / 4);
                g2.drawLine(buttonWidth / 2, floorHeight / 8, buttonWidth * 3 / 4, floorHeight / 4);
            }
            if (floor != 0) {
                g2.setColor(callingDown[floor] ? Color.red : Color.black);
                g2.drawLine(buttonWidth / 2, floorHeight * 5 / 8, buttonWidth / 2, floorHeight * 7 / 8);
                g2.drawLine(buttonWidth / 2, floorHeight * 7 / 8, buttonWidth / 4, floorHeight * 3 / 4);
                g2.drawLine(buttonWidth / 2, floorHeight * 7 / 8, buttonWidth * 3 / 4, floorHeight * 3 / 4);
            }

        }

    }
}


