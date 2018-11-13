package Lab2;

import List.LinkedList;
import Queue.OrderedLinkedQueue;

import java.awt.*;
import java.util.Comparator;
import java.util.Random;

public class ElevatorSimulator {

    private boolean logEnable = false;

    private int upAndDownTime;
    private int openAndCloseTime;
    private int detectGap;
    private int enterAndExitTime;
    private int berthTime;

    private int floors, base;
    private int parallelNumber, maxLoad;
    private int minArrivingGap, minAbortingGap;
    private int maxArrivingGap, maxAbortingGap;
    private Carriage[] carriages;
    private boolean[] callingUp, callingDown;
    private PassengerLinkedQueue passengerQueues[];

    //事件队列
    private OrderedLinkedQueue<Event> events = new OrderedLinkedQueue<>(new Comparator<Event>() {
        @Override
        public int compare(Event o1, Event o2) {
            return o1.occurTime - o2.occurTime;
        }
    });
    //全局时钟
    private int time = 0;

    public class Passenger {  //乘客类
        PassengerState state;
        Direction direction;
        int arrivingTime;
        int abortingTime;
        int inFloor, outFloor;

        Passenger(int arrivingTime, int abortingTime, int inFloor, int outFloor) {
            this.arrivingTime = arrivingTime;
            this.abortingTime = abortingTime;
            this.inFloor = inFloor;
            this.outFloor = outFloor;
            direction = outFloor > inFloor ? Direction.up : Direction.down;
            ID = random.nextInt();
            color = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat());
        }

        public void setState(PassengerState state) {
            this.state = state;
        }

        int ID;
        Color color;  //颜色
    }

    public class Carriage {  //电梯厢类
        CarriageState state;
        Direction direction;
        boolean[] isCalling;
        int nowFloor;
        int nowLoad;

        Passenger[] content;  //所载乘客
        Passenger IOPassenger;  //当前处于进出状态的乘客

        Carriage(int maxLoad) {
            isCalling = new boolean[floors];
            nowFloor = base;
            nowLoad = 0;
            state = CarriageState.idling;
            content = new Passenger[maxLoad];
            direction = Direction.none;
        }

        public boolean isFull() {
            return nowLoad == content.length;
        }

        public void setState(CarriageState state) {
            this.state = state;
        }

        public void setDirection(Direction direction) {
            this.direction = direction;
        }

        public void add(Passenger p) {
            isCalling[p.outFloor] = true;
            nowLoad++;
            for (int i = 0; i < content.length; i++) {
                if (content[i] == null) {
                    content[i] = p;
                    break;
                }
            }
        }

        public void delete(Passenger p) {
            nowLoad--;
            for (int i = 0; i < content.length; i++) {
                if (content[i] == p) {
                    content[i] = null;
                    break;
                }
            }
        }

        public Passenger getNextPassengerForExiting() {  //找到下一个要离开的乘客
            for (int i = 0; i < content.length; i++) {
                if (content[i] != null && content[i].outFloor == nowFloor) {
                    return content[i];
                }
            }
            return null;
        }  //找到下一个要离开的乘客

        public Passenger getNextPassengerForEntering() {  //找到下一个要进入的乘客
            if (nowLoad == maxLoad) {
                return null;
            }
            var iter = passengerQueues[nowFloor].iterator();
            while (iter.hasNext()) {
                var p = iter.next();
                if (direction == p.direction) {
                    return p;
                }

            }
            return null;
        }  //找到下一个要进入的乘客

        int lastStartOpenOrCloseTime;  //用于中断关门后生成新的开门事件
        int lastStartUpOrDownTime;  //用于中断前往待命状态的移动事件

        public double getPosition() {
            switch (state) {
                case upping:
                case idle_upping:
                    return (double) nowFloor + (double) (time - lastStartUpOrDownTime) / upAndDownTime;
                case downing:
                case idle_downing:
                    return (double) nowFloor - (double) (time - lastStartUpOrDownTime) / upAndDownTime;
                default:
                    return (double) nowFloor;
            }
        }

    }

    private abstract class Event {
        int occurTime;
    }  //抽象事件

    protected class PassengerEvent extends Event {
        PassengerEventType type;
        Passenger passenger;
        Carriage carriage;

        PassengerEvent(Passenger passenger, Carriage carriage, int occurTime, PassengerEventType type) {
            this.passenger = passenger;
            this.carriage = carriage;
            this.occurTime = occurTime;
            this.type = type;
        }
    }  //乘客事件

    protected class CarriageEvent extends Event {
        CarriageEventType type;
        Carriage carriage;

        CarriageEvent(Carriage carriage, int occurTime, CarriageEventType type) {
            this.carriage = carriage;
            this.occurTime = occurTime;
            this.type = type;
        }
    }  //电梯厢事件

    protected class ScheduleEvent extends Event {
        int triggerFloor;
        Direction triggerDirection;

        ScheduleEvent(int occurTime, int triggerFloor, Direction triggerDirection) {
            this.occurTime = occurTime;
            this.triggerFloor = triggerFloor;
            this.triggerDirection = triggerDirection;
        }
    }

    public enum Direction {
        up,
        none,
        down
    }

    //状态枚举
    public enum PassengerState {
        waiting,                //等待电梯
        waiting_wont_abort,     //即将进入电梯，此时abort事件不生效
        aborted,                //已放弃
        leaved,                 //已离开
        entering,               //正在进入电梯
        exiting,                //正在离开电梯
        in_carriage              //在电梯里
    }  //乘客状态

    public enum CarriageState {  //电梯厢状态
        idling,         //正在待命
        opening,        //正在开门
        detecting,      //处于检测间隔中
        closing,        //正在关门
        upping,         //正在上升
        downing,        //正在下降
        berthing,       //正在停泊
        idle_upping,    //正在前往待命状态的上升过程
        idle_downing    //正在前往待命状态的下降过程
    }  //电梯厢状态

    //事件类型枚举
    public enum PassengerEventType {  //乘客事件类型
        arrive,                         //到达
        abort,                          //放弃
        switch_to_waiting_wont_abort,   //转为电梯来临时的等待
        switch_to_waiting,              //转为正常等待
        enter_start,                    //开始进入电梯
        enter_end,                      //完全进入
        exit_start,                     //开始走出电梯
        exit_end,                       //完全走出
        leave                           //离开
    }  //乘客事件类型

    public enum CarriageEventType {  //电梯厢事件类型
        up_start,           //开始上升
        up_end,             //完成上升
        down_start,         //开始下降
        down_end,           //完成下降
        open_start,         //开始开门
        open_end,           //完成开门
        detect,             //检测是否有人进出
        close_start,        //开始关门
        close_end,          //完成关门，只在closing状态生效
        decide_direction,   //完成关门或者到达新的一层后，决定电梯方向
        half_up_start,
        half_down_start,
        berth_start,        //开始停泊
        berth_end,          //结束停泊，只在berthing状态生效
        idle_up_start,      //开始上升去往待命层
        idle_up_end,        //到达，只在idle_upping状态生效
        idle_down_start,    //开始下降去往待命层
        idle_down_end,      //到达，只在idle_downing状态生效
        idle_start,         //开始待命
    }  //电梯厢事件类型

    /**
     * @param floors           层数
     * @param base             待命层
     * @param parallelNumber   电梯数
     * @param maxLoad          单电梯厢最大负载
     * @param minArrivingGap   乘客到达最小间隔
     * @param maxArrivingGap   乘客到达最大间隔
     * @param minAbortingGap   最小放弃时间
     * @param maxAbortingGap   最大放弃时间
     * @param upAndDownTime    电梯上升下降一层时间
     * @param openAndCloseTime 电梯开关门时间
     * @param enterAndExitTime 乘客进出电梯时间
     * @param detectGap        电梯检测时间间隔
     * @param berthTime        电梯无动作时停泊时间
     */
    public ElevatorSimulator(int floors, int base, int parallelNumber, int maxLoad,
                             int minArrivingGap, int maxArrivingGap, int minAbortingGap, int maxAbortingGap,
                             int upAndDownTime, int openAndCloseTime, int enterAndExitTime,
                             int detectGap, int berthTime) {

        this.floors = floors;                   //总层数
        this.base = base;                       //待命层
        this.parallelNumber = parallelNumber;   //联动电梯数
        this.maxLoad = maxLoad;                 //每个电梯厢最大负载
        this.minArrivingGap = minArrivingGap;   //最小到达间隔
        this.minAbortingGap = minAbortingGap;   //最小放弃间隔
        this.maxArrivingGap = maxArrivingGap;   //最大到达间隔时间
        this.maxAbortingGap = maxAbortingGap;   //最大放弃间隔时间

        this.upAndDownTime = upAndDownTime;         //电梯上下一层时间
        this.openAndCloseTime = openAndCloseTime;   //电梯开关门时间
        this.enterAndExitTime = enterAndExitTime;   //乘客进出时间
        this.detectGap = detectGap;                 //电梯开门状态时的检测间隔
        this.berthTime = berthTime;                 //前往待命状态前的停泊时间

        carriages = new Carriage[parallelNumber];
        for (int i = 0; i < parallelNumber; i++) {
            carriages[i] = new Carriage(maxLoad);
        }

        callingUp = new boolean[floors];
        callingDown = new boolean[floors];

        passengerQueues = new PassengerLinkedQueue[floors];
        for (int i = 0; i < floors; i++) {
            passengerQueues[i] = new PassengerLinkedQueue();
        }

    }

    private Random random = new Random();

    public Passenger getRandomPassenger() {    //获得随机乘客
        int inFloor = random.nextInt(floors);
        int outFloor;
        do {
            outFloor = random.nextInt(floors);
        } while (inFloor == outFloor);
        return new Passenger(time + minArrivingGap + random.nextInt(maxArrivingGap - minArrivingGap),
                minAbortingGap + random.nextInt(maxAbortingGap - minAbortingGap), inFloor, outFloor);
    }    //获得随机乘客

    public void startSimulation() {
        var p = getRandomPassenger();
        events.add(new PassengerEvent(p, null, p.arrivingTime, PassengerEventType.arrive));
    }

    //todo 待完善

    /**
     * 检测本层是否存在未满且detecting且方向正确的电梯
     * 若不存在，检测本层是否存在opening且方向正确的电梯的电梯
     * 若不存在，检测是否存在未满且closing且方向正确的电梯，如果存在，取消close_end事件
     * 以上三种状态，都不需要更新按钮，只需把乘客置为wait_wont_abort状
     * 如果都不存在，更新按钮，更新电梯调度
     * 乘客选择优先级：开着的>正在开的>正在关的
     */
    private void arrive(Passenger p) {

        if (logEnable) {
            if (p.inFloor == 0) {
                System.out.println(time + "\t: Passenger " + p.ID + " arrive B1");
            } else {
                System.out.println(time + "\t: Passenger " + p.ID + " arrive F" + p.inFloor);
            }

        }

        var newp = getRandomPassenger();  //下一个乘客
        events.add(new PassengerEvent(newp, null, newp.arrivingTime, PassengerEventType.arrive));

        boolean cantPressButton = false;  //是否因为电梯满员不能按按钮

        for (var ctmp : carriages) {  //寻找开着的，方向正确的，未满的电梯
            if (ctmp.nowFloor == p.inFloor && ctmp.direction == p.direction &&
                    ctmp.state == CarriageState.detecting) {
                if (ctmp.isFull()) {
                    cantPressButton = true;
                } else {
                    if (ctmp.IOPassenger == null) {   //电梯没有人进出，直接进入电梯
                        events.add(new PassengerEvent(p, ctmp, time, PassengerEventType.enter_start));
                    } else {
                        p.setState(PassengerState.waiting_wont_abort);
                        passengerQueues[p.inFloor].add(p);
                    }
                    return;
                }
            }
        }

        for (var ctmp : carriages) {
            if (ctmp.nowFloor == p.inFloor && ctmp.direction == p.direction &&
                    ctmp.state == CarriageState.opening) {  //寻找方向正确的正在开门的电梯
                p.setState(PassengerState.waiting_wont_abort);
                passengerQueues[p.inFloor].add(p);
                return;
            }
        }

        for (var ctmp : carriages) {
            if (ctmp.nowFloor == p.inFloor && ctmp.direction == p.direction &&
                    ctmp.state == CarriageState.closing) {  //寻找方向正确的正在关门的电梯
                if (ctmp.isFull()) {
                    cantPressButton = true;
                } else {
                    p.setState(PassengerState.waiting_wont_abort);
                    passengerQueues[p.inFloor].add(p);
                    ctmp.setState(CarriageState.opening);  //取消电梯关门事件
                    ctmp.lastStartOpenOrCloseTime = time - (openAndCloseTime - (time - ctmp.lastStartOpenOrCloseTime));
                    events.add(new CarriageEvent(ctmp, ctmp.lastStartOpenOrCloseTime + openAndCloseTime, CarriageEventType.open_end));
                    return;
                }
            }
        }

        //以上各种情况都不存在
        p.setState(PassengerState.waiting);
        passengerQueues[p.inFloor].add(p);
        events.add(new PassengerEvent(p, null, time + p.abortingTime, PassengerEventType.abort));
        if (!cantPressButton) {
            if (p.direction == Direction.up) {
                if (!callingUp[p.inFloor]) {
                    events.add(new ScheduleEvent(time, p.inFloor, Direction.up));
                }
            } else {
                if (!callingDown[p.inFloor]) {
                    events.add(new ScheduleEvent(time, p.inFloor, Direction.down));
                }
            }
        }
        //其他情况无需按按钮，因为电梯关门结束时会更新按钮状态
    }

    private void abort(Passenger p) {
        if (p.state == PassengerState.waiting) {
            if (logEnable) {
                System.out.println(time + "\t: Passenger " + p.ID + " abort.");
            }
            p.setState(PassengerState.aborted);
            passengerQueues[p.inFloor].delete(p);
            events.add(new PassengerEvent(p, null, time, PassengerEventType.leave));
        }
    }

    private void switch_to_waiting_wont_abort(Passenger p) {
        p.setState(PassengerState.waiting_wont_abort);
    }

    private void switch_to_waiting(Passenger p) {
        if (time >= p.arrivingTime + p.abortingTime) {
            events.add(new PassengerEvent(p, null, time, PassengerEventType.abort));
        } else {
            p.setState(PassengerState.waiting);
        }

    }

    private void enter_start(Passenger p, Carriage c) {
        if (logEnable) {
            System.out.println(time + "\t: Passenger " + p.ID + " enter carriage.");
        }
        p.setState(PassengerState.entering);
        c.IOPassenger = p;
        passengerQueues[c.nowFloor].delete(p);
        events.add(new PassengerEvent(p, c, time + enterAndExitTime, PassengerEventType.enter_end));
    }

    private void enter_end(Passenger p, Carriage c) {
        p.setState(PassengerState.in_carriage);
        c.IOPassenger = null;
        c.add(p);
        var nextp = c.getNextPassengerForEntering();
        if (nextp != null) {
            events.add(new PassengerEvent(nextp, c, time, PassengerEventType.enter_start));
        }
    }

    private void exit_start(Passenger p, Carriage c) {
        if (logEnable) {
            System.out.println(time + "\t: Passenger " + p.ID + " exit carriage.");
        }
        p.setState(PassengerState.exiting);
        c.IOPassenger = p;
        c.delete(p);
        events.add(new PassengerEvent(p, c, time + enterAndExitTime, PassengerEventType.exit_end));
    }

    private void exit_end(Passenger p, Carriage c) {
        c.IOPassenger = null;
        events.add(new PassengerEvent(p, c, time, PassengerEventType.leave));
        var nextp = c.getNextPassengerForExiting();
        if (nextp == null) {
            nextp = c.getNextPassengerForEntering();
            if (nextp != null) {
                events.add(new PassengerEvent(nextp, c, time, PassengerEventType.enter_start));
            }
        } else {
            events.add(new PassengerEvent(nextp, c, time, PassengerEventType.exit_start));
        }
    }

    private void leave(Passenger p) {
        if (logEnable) {
            System.out.println(time + "\t: Passenger " + p.ID + " leave.");
        }
        p.setState(PassengerState.leaved);
    }


    private void open_start(Carriage c) {
        c.setState(CarriageState.opening);
        c.lastStartOpenOrCloseTime = time;
        c.isCalling[c.nowFloor] = false;  //更新按钮
        if (c.direction == Direction.up) {
            callingUp[c.nowFloor] = false;
        } else {
            callingDown[c.nowFloor] = false;
        }
        events.add(new CarriageEvent(c, time + openAndCloseTime, CarriageEventType.open_end));
        var iter = passengerQueues[c.nowFloor].iterator();
        while (iter.hasNext()) {
            var ptmp = iter.next();
            if (c.direction == ptmp.direction) {
                //存在已到达电梯则不会放弃等待
                events.add(new PassengerEvent(ptmp, null, time, PassengerEventType.switch_to_waiting_wont_abort));
            }
        }
    }

    private void open_end(Carriage c) {
        c.setState(CarriageState.detecting);
        events.add(new CarriageEvent(c, time + detectGap, CarriageEventType.detect));
        var nextp = c.getNextPassengerForExiting();
        if (nextp == null) {
            nextp = c.getNextPassengerForEntering();
            if (nextp != null) {
                events.add(new PassengerEvent(nextp, c, time, PassengerEventType.enter_start));
            }
        } else {
            events.add(new PassengerEvent(nextp, c, time, PassengerEventType.exit_start));
        }
    }

    private void detect(Carriage c) {
        if (c.IOPassenger == null) {  //无乘客进出
            events.add(new CarriageEvent(c, time, CarriageEventType.close_start));
        } else {
            events.add(new CarriageEvent(c, time + detectGap, CarriageEventType.detect));
        }
    }

    private void close_start(Carriage c) {
        c.setState(CarriageState.closing);
        c.lastStartOpenOrCloseTime = time;
        events.add(new CarriageEvent(c, time + openAndCloseTime, CarriageEventType.close_end));

        //更新乘客等待状态
        var iter = passengerQueues[c.nowFloor].iterator();
        while (iter.hasNext()) {
            var ptmp = iter.next();
            boolean existOtherAppropriateCarriage = false;
            if (ptmp.direction == Direction.up) {
                for (var ctmp : carriages) {
                    if (ctmp.nowFloor == c.nowFloor && ctmp.direction == Direction.up &&
                            (ctmp.state == CarriageState.opening || ctmp.state == CarriageState.detecting)) {
                        existOtherAppropriateCarriage = true;
                        break;
                    }
                }
                if (!existOtherAppropriateCarriage) {
                    events.add(new PassengerEvent(ptmp, null, time, PassengerEventType.switch_to_waiting));
                }
            } else {
                for (var ctmp : carriages) {
                    if (ctmp.nowFloor == c.nowFloor && ctmp.direction == Direction.down &&
                            (ctmp.state == CarriageState.opening || ctmp.state == CarriageState.detecting)) {
                        existOtherAppropriateCarriage = true;
                        break;
                    }
                }
                if (!existOtherAppropriateCarriage) {
                    events.add(new PassengerEvent(ptmp, null, time, PassengerEventType.switch_to_waiting));
                }
            }
        }
    }

    private void close_end(Carriage c) {
        if (c.state == CarriageState.closing) {
            //更新上行按钮
            boolean cantPressButton = false;
            for (var ctmp : carriages) {
                if (ctmp.nowFloor == c.nowFloor && ctmp.direction == Direction.up &&
                        (ctmp.state == CarriageState.opening || ctmp.state == CarriageState.detecting
                                || ctmp.state == CarriageState.closing)) {
                    cantPressButton = true;
                    break;
                }
            }
            if (!cantPressButton) {
                var iter = passengerQueues[c.nowFloor].iterator();
                while (iter.hasNext()) {
                    if (iter.next().direction == Direction.up && !callingUp[c.nowFloor]) {
                        events.add(new ScheduleEvent(time, c.nowFloor, Direction.up));
                        break;
                    }
                }
            }
            //更新下行按钮
            cantPressButton = false;
            for (var ctmp : carriages) {
                if (ctmp.nowFloor == c.nowFloor && ctmp.direction == Direction.down &&
                        (ctmp.state == CarriageState.opening || ctmp.state == CarriageState.detecting
                                || ctmp.state == CarriageState.closing)) {
                    cantPressButton = true;
                    break;
                }
            }
            if (!cantPressButton) {
                var iter = passengerQueues[c.nowFloor].iterator();
                while (iter.hasNext()) {
                    if (iter.next().direction == Direction.down && !callingDown[c.nowFloor]) {
                        events.add(new ScheduleEvent(time, c.nowFloor, Direction.down));
                        break;
                    }
                }
            }

            //必须把这个放在后面，防止出现错误
            events.add(new CarriageEvent(c, time, CarriageEventType.decide_direction));

        }
    }

    private double FindPosition(Carriage c, Direction direction, Direction searchDirection) {
        //向上的电梯找最低
        //向下的电梯找最高
        double position = direction == Direction.up ? Double.MAX_VALUE : Double.MIN_VALUE;
        for (var ctmp : carriages) {
            if (ctmp != c && ctmp.direction == direction) {
                double realFloor = ctmp.getPosition();
                if (searchDirection == Direction.up && realFloor > c.getPosition() ||
                        searchDirection == Direction.down && realFloor < c.getPosition()) {
                    if (direction == Direction.up && realFloor < position ||
                            direction == Direction.down && realFloor > position) {
                        position = realFloor;
                    }
                }
            }
        }
        return position;
    }

    private boolean searchUp(Carriage c) {
        //找到c以上向上的最低电梯
        double min = FindPosition(c, Direction.up, Direction.up);
        for (int i = c.nowFloor + 1; i < floors && i < min; i++) {
            if (callingUp[i] || callingDown[i]) {
                return true;
            }
        }
        //找到c以上向下的最高电梯
//        double max = FindPosition(c, Direction.down, Direction.up);
//        for (int i = floors - 1; i > max && i >= 0; i--) {
//            if (callingDown[i]) {
//                return true;
//            }
//        }
        return false;
    }

    private boolean searchDown(Carriage c) {
        //找到c以下向下的最高电梯
        double max = FindPosition(c, Direction.down, Direction.down);
        for (int i = c.nowFloor - 1; i >= 0 && i > max; i--) {
            if (callingUp[i] || callingDown[i]) {
                return true;
            }
        }
        //找到c以下向上的最低电梯
//        double min = FindPosition(c, Direction.up, Direction.down);
//        for (int i = 0; i < min && i < floors; i++) {
//            if (callingUp[i]) {
//                return true;
//            }
//        }
        return false;
    }

    /**
     * 决定电梯方向原则：
     * 如果有更近的电梯可以到达目标
     * 那么本电梯不前往目标
     * <p>
     * 实际方法，以向上为例
     * 找到c以上同方向最低的电梯d
     * 检测c d间的楼层
     * 如果按钮被按下
     * 则c继续向上
     */
    //todo 待改进
    private void decide_direction(Carriage c) {
        if (c.direction == Direction.up) {
            for (int i = c.nowFloor + 1; i < floors; i++) {
                if (c.isCalling[i]) {
                    events.add(new CarriageEvent(c, time, CarriageEventType.up_start));
                    return;
                }
            }
            if (searchUp(c)) {
                events.add(new CarriageEvent(c, time, CarriageEventType.up_start));
                return;
            }
            //转向
            if (callingDown[c.nowFloor]) {
                c.setDirection(Direction.down);
                c.setState(CarriageState.downing);
                events.add(new CarriageEvent(c, time, CarriageEventType.open_start));
                return;
            }
            if (searchDown(c)) {
                events.add(new CarriageEvent(c, time, CarriageEventType.down_start));
                return;
            }
        } else {
            for (int i = c.nowFloor - 1; i >= 0; i--) {
                if (c.isCalling[i]) {
                    events.add(new CarriageEvent(c, time, CarriageEventType.down_start));
                    return;
                }
            }
            if (searchDown(c)) {
                events.add(new CarriageEvent(c, time, CarriageEventType.down_start));
                return;
            }
            //转向
            if (callingUp[c.nowFloor]) {
                c.setDirection(Direction.up);
                c.setState(CarriageState.upping);
                events.add(new CarriageEvent(c, time, CarriageEventType.open_start));
                return;
            }
            if (searchUp(c)) {
                events.add(new CarriageEvent(c, time, CarriageEventType.up_start));
                return;
            }
        }
        events.add(new CarriageEvent(c, time, CarriageEventType.berth_start));
    }

    private void half_up_start(Carriage c) {
        c.setDirection(Direction.up);
        c.setState(CarriageState.upping);
        if (c.state == CarriageState.idle_downing) {
            c.nowFloor--;
            c.lastStartUpOrDownTime = time - (upAndDownTime - (time - c.lastStartUpOrDownTime));
        }
        events.add(new CarriageEvent(c, c.lastStartUpOrDownTime + upAndDownTime, CarriageEventType.up_end));
    }

    private void half_down_start(Carriage c) {
        c.setDirection(Direction.down);
        c.setState(CarriageState.downing);
        if (c.state == CarriageState.idle_upping) {
            c.nowFloor++;
            c.lastStartUpOrDownTime = time - (upAndDownTime - (time - c.lastStartUpOrDownTime));
        }
        events.add(new CarriageEvent(c, c.lastStartUpOrDownTime + upAndDownTime, CarriageEventType.down_end));
    }

    private void up_start(Carriage c) {
        c.setState(CarriageState.upping);
        c.setDirection(Direction.up);
        c.lastStartUpOrDownTime = time;
        events.add(new CarriageEvent(c, time + upAndDownTime, CarriageEventType.up_end));
    }

    private void up_end(Carriage c) {
        c.nowFloor++;
        if (c.nowFloor == floors - 1) {  //到顶转向
            c.setDirection(Direction.down);
            c.setState(CarriageState.downing);
        }
        if (c.isCalling[c.nowFloor] ||
                c.direction == Direction.up && callingUp[c.nowFloor] ||
                c.direction == Direction.down && callingDown[c.nowFloor]) {
            events.add(new CarriageEvent(c, time, CarriageEventType.open_start));
        } else {
            events.add(new CarriageEvent(c, time, CarriageEventType.decide_direction));
        }
    }

    private void down_start(Carriage c) {
        c.setState(CarriageState.downing);
        c.setDirection(Direction.down);
        c.lastStartUpOrDownTime = time;
        events.add(new CarriageEvent(c, time + upAndDownTime, CarriageEventType.down_end));
    }

    private void down_end(Carriage c) {
        c.nowFloor--;
        if (c.nowFloor == 0) {
            c.setDirection(Direction.up);
            c.setState(CarriageState.upping);
        }
        if (c.isCalling[c.nowFloor] ||
                c.direction == Direction.up && callingUp[c.nowFloor] ||
                c.direction == Direction.down && callingDown[c.nowFloor]) {
            events.add(new CarriageEvent(c, time, CarriageEventType.open_start));
        } else {
            events.add(new CarriageEvent(c, time, CarriageEventType.decide_direction));
        }
    }

    private void berth_start(Carriage c) {
        c.setState(CarriageState.berthing);
        c.setDirection(Direction.none);
        events.add(new CarriageEvent(c, time + berthTime, CarriageEventType.berth_end));
    }

    private void berth_end(Carriage c) {
        if (c.state == CarriageState.berthing) {
            if (c.nowFloor == base) {
                events.add(new CarriageEvent(c, time, CarriageEventType.idle_start));
            } else if (c.nowFloor > base) {
                events.add(new CarriageEvent(c, time, CarriageEventType.idle_down_start));
            } else {
                events.add(new CarriageEvent(c, time, CarriageEventType.idle_up_start));
            }
        }
    }

    private void idle_up_start(Carriage c) {
        c.setState(CarriageState.idle_upping);
        c.setDirection(Direction.none);
        c.lastStartUpOrDownTime = time;
        events.add(new CarriageEvent(c, time + upAndDownTime, CarriageEventType.idle_up_end));
    }

    private void idle_up_end(Carriage c) {
        if (c.state == CarriageState.idle_upping) {
            c.nowFloor++;
            if (c.nowFloor == base) {
                events.add(new CarriageEvent(c, time, CarriageEventType.idle_start));
            } else {
                events.add(new CarriageEvent(c, time, CarriageEventType.idle_up_start));
            }
        }
    }

    private void idle_down_start(Carriage c) {
        c.setState(CarriageState.idle_downing);
        c.setDirection(Direction.none);
        c.lastStartUpOrDownTime = time;
        events.add(new CarriageEvent(c, time + upAndDownTime, CarriageEventType.idle_down_end));
    }

    private void idle_down_end(Carriage c) {
        if (c.state == CarriageState.idle_downing) {
            c.nowFloor--;
            if (c.nowFloor == base) {
                events.add(new CarriageEvent(c, time, CarriageEventType.idle_start));
            } else {
                events.add(new CarriageEvent(c, time, CarriageEventType.idle_down_start));
            }
        }
    }

    private void idle_start(Carriage c) {
        c.setState(CarriageState.idling);
        c.setDirection(Direction.none);
    }

//todo

    /**
     * 调度事件处理
     * 按钮事件触发调度
     * 如果存在直接赶来的电梯，则什么都不做
     * 反之则调用空闲电梯
     * 调度时默认
     *
     * @param triggerFloor     按钮楼层
     * @param triggerDirection 按钮方向
     */
    private void schedule(int triggerFloor, Direction triggerDirection) {
        //先更新电梯按钮
        if (triggerDirection == Direction.up) {
            callingUp[triggerFloor] = true;
        } else {
            callingDown[triggerFloor] = true;
        }
        //有正在赶来的电梯
        if (triggerDirection == Direction.up) {
            for (var ctmp : carriages) {
                if (ctmp.direction == Direction.up && ctmp.nowFloor < triggerFloor) {
                    return;
                }
            }
        } else {
            for (var ctmp : carriages) {
                if (ctmp.direction == Direction.down && ctmp.nowFloor > triggerFloor) {
                    return;
                }
            }
        }
        //没有正在赶来的电梯，调用最近的空闲电梯
        Carriage nearest = null;
        double min = Double.MAX_VALUE;
        for (var ctmp : carriages) {
            if (ctmp.direction == Direction.none) {
                double delta = Math.abs(ctmp.getPosition() - triggerFloor);
                if (delta < min) {
                    min = delta;
                    nearest = ctmp;
                }
            }
        }
        if (nearest != null) {
            switch (nearest.state) {
                case berthing:
                case idling:
                    if (nearest.nowFloor > triggerFloor) {
                        events.add(new CarriageEvent(nearest, time, CarriageEventType.down_start));
                    } else if (nearest.nowFloor == triggerFloor) {
                        nearest.setDirection(triggerDirection);
                        events.add(new CarriageEvent(nearest, time, CarriageEventType.open_start));
                    } else {
                        events.add(new CarriageEvent(nearest, time, CarriageEventType.up_start));
                    }
                    break;
                case idle_upping:
                case idle_downing:
                    if (nearest.getPosition() > triggerFloor) {
                        events.add(new CarriageEvent(nearest, time, CarriageEventType.half_down_start));
                    } else {
                        events.add(new CarriageEvent(nearest, time, CarriageEventType.half_up_start));
                    }
                    break;
            }
        }
    }

    public void nextEvent() {
        if (!events.isEmpty()) {
            var e = events.get();
            time = e.occurTime;
            if (e instanceof PassengerEvent) {  //乘客事件
                var p = ((PassengerEvent) e).passenger;
                var c = ((PassengerEvent) e).carriage;
                switch (((PassengerEvent) e).type) {
                    case arrive:
                        arrive(p);
                        break;
                    case abort:
                        abort(p);
                        break;
                    case switch_to_waiting_wont_abort:
                        switch_to_waiting_wont_abort(p);
                        break;
                    case switch_to_waiting:
                        switch_to_waiting(p);
                        break;
                    case enter_start:
                        enter_start(p, c);
                        break;
                    case enter_end:
                        enter_end(p, c);
                        break;
                    case exit_start:
                        exit_start(p, c);
                        break;
                    case exit_end:
                        exit_end(p, c);
                        break;
                    case leave:
                        leave(p);
                        break;
                }
            } else if (e instanceof CarriageEvent) {  //电梯事件
                var c = ((CarriageEvent) e).carriage;
                switch (((CarriageEvent) e).type) {
                    case open_start:
                        open_start(c);
                        break;
                    case open_end:
                        open_end(c);
                        break;
                    case detect:
                        detect(c);
                        break;
                    case close_start:
                        close_start(c);
                        break;
                    case close_end:
                        close_end(c);
                        break;
                    case decide_direction:
                        decide_direction(c);
                        break;
                    case half_up_start:
                        half_up_start(c);
                        break;
                    case half_down_start:
                        half_down_start(c);
                        break;
                    case up_start:
                        up_start(c);
                        break;
                    case up_end:
                        up_end(c);
                        break;
                    case down_start:
                        down_start(c);
                        break;
                    case down_end:
                        down_end(c);
                        break;
                    case berth_start:
                        berth_start(c);
                        break;
                    case berth_end:
                        berth_end(c);
                        break;
                    case idle_up_start:
                        idle_up_start(c);
                        break;
                    case idle_up_end:
                        idle_up_end(c);
                        break;
                    case idle_down_start:
                        idle_down_start(c);
                        break;
                    case idle_down_end:
                        idle_down_end(c);
                        break;
                    case idle_start:
                        idle_start(c);
                        break;
                }
            } else {  //调度事件
                schedule(((ScheduleEvent) e).triggerFloor, ((ScheduleEvent) e).triggerDirection);
            }
        }
    }

    public int getParallelNumber() {
        return parallelNumber;
    }

    public int getFloors() {
        return floors;
    }

    public int getNowTime() {
        return time;
    }

    public int getNextTime() {
        return events.getHead().occurTime;
    }

    public Carriage[] getCarriages() {
        return carriages;
    }

    public PassengerLinkedQueue[] getPassengerQueues() {
        return passengerQueues;
    }

    public boolean[] getCallingUp() {
        return callingUp;
    }

    public boolean[] getCallingDown() {
        return callingDown;
    }

    public int getMaxLoad() {
        return maxLoad;
    }

    public int getUpAndDownTime() {
        return upAndDownTime;
    }

    public int getEnterAndExitTime() {
        return enterAndExitTime;
    }

    public int getOpenAndCloseTime() {
        return openAndCloseTime;
    }

    public void setLogEnable(boolean logEnable) {
        this.logEnable = logEnable;
    }

}

class PassengerLinkedQueue extends LinkedList<ElevatorSimulator.Passenger> {
    public void delete(ElevatorSimulator.Passenger p) {
        var q = head;
        while (q.getNext() != null) {
            if (q.getNext().getData() == p) {
                q.setNext(q.getNext().getNext());
                if (q.getNext() == null) {  //删除的是尾节点
                    tail = q;
                }
                break;
            }
            q = q.getNext();
        }
    }

    public void add(ElevatorSimulator.Passenger p) {
        insertTail(p);
    }
}
