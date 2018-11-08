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
    private OrderedLinkedQueue<Event> events;
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

        public int ID;
        public Color color;  //颜色
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
            if (nowLoad == content.length) {
                return null;
            }
            Passenger p;
            var iter = passengerQueues[nowFloor].iterator();
            while (iter.hasNext()) {
                p = iter.next();
                if (direction == p.direction) {
                    passengerQueues[nowFloor].delete(p);
                    return p;
                }

            }
            return null;
        }  //找到下一个要进入的乘客

        public int lastStartOpenOrCloseTime;  //用于中断关门后生成新的开门事件
        public int lastStartUpOrDownTime;  //用于中断前往待命状态的移动事件
        public Passenger targetPassenger;  //用于电梯调度时的的目标乘客

    }

    private abstract class Event {
        int occurTime;
        Carriage carriage;
    }  //抽象事件

    protected class PassengerEvent extends Event {
        PassengerEventType type;
        Passenger passenger;

        PassengerEvent(Passenger passenger, Carriage carriage, int occurTime, PassengerEventType type) {
            this.passenger = passenger;
            this.carriage = carriage;
            this.occurTime = occurTime;
            this.type = type;
        }
    }  //乘客事件

    protected class CarriageEvent extends Event {
        CarriageEventType type;

        CarriageEvent(Carriage carriage, int occurTime, CarriageEventType type) {
            this.carriage = carriage;
            this.occurTime = occurTime;
            this.type = type;
        }
    }  //电梯厢事件

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
        arrive,         //到达
        abort,          //放弃
        enter_start,    //开始进入电梯
        enter_end,      //完全进入
        exit_start,     //开始走出电梯
        exit_end,       //完全走出
        leave           //离开
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
        berth_start,        //开始停泊
        berth_end,          //结束停泊，只在berthing状态生效
        idle_up_start,      //开始上升去往待命层
        idle_up_end,        //到达，只在idle_upping状态生效
        idle_down_start,    //开始下降去往待命层
        idle_down_end,      //到达，只在idle_downing状态生效
        idle_start,         //开始待命
    }  //电梯厢事件类型


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
        events = new OrderedLinkedQueue<>(new Comparator<Event>() {
            @Override
            public int compare(Event o1, Event o2) {
                if (o1.occurTime == o2.occurTime) {  //电梯事件优先执行
                    boolean b1 = o1 instanceof CarriageEvent;
                    boolean b2 = o2 instanceof CarriageEvent;
                    if (b1 && !b2) {
                        return -1;
                    } else if (!b1 && b2) {
                        return 1;
                    } else {
                        return 0;
                    }
                } else {
                    return o1.occurTime - o2.occurTime;
                }

            }
        });

    }

    private Random random = new Random();

    private Passenger getRandomPassenger() {    //获得随机乘客
        int inFloor = random.nextInt(floors);
        int outFloor;
        do {
            outFloor = random.nextInt(floors);
        } while (inFloor == outFloor);
        return new Passenger(time + minArrivingGap + random.nextInt(maxArrivingGap - minArrivingGap),
                minAbortingGap + random.nextInt(maxAbortingGap - minAbortingGap),
                inFloor, outFloor);
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
            System.out.print(time + "\t: Passenger " + p.ID + " arrive ");
            if (p.inFloor == 0) {
                System.out.println("B1.");
            } else {
                System.out.println("F" + p.inFloor + ".");
            }

        }

        var newp = getRandomPassenger();  //下一个乘客
        events.add(new PassengerEvent(newp, null, newp.arrivingTime, PassengerEventType.arrive));

        for (var ctmp : carriages) {  //寻找开着的，方向正确的，未满的电梯
            if (ctmp.nowFloor == p.inFloor && ctmp.direction == p.direction &&
                    ctmp.state == CarriageState.detecting && !ctmp.isFull()) {
                if (ctmp.IOPassenger == null) {   //电梯没有人进出，直接进入电梯
                    events.add(new PassengerEvent(p, ctmp, time, PassengerEventType.enter_start));
                } else {
                    p.setState(PassengerState.waiting_wont_abort);
                    passengerQueues[p.inFloor].add(p);
                }
                return;
            }
        }

        for (var ctmp : carriages) {
            if (ctmp.nowFloor == p.inFloor && ctmp.direction == p.direction && ctmp.state == CarriageState.opening) {  //寻找方向正确的正在开门的电梯
                p.setState(PassengerState.waiting_wont_abort);
                passengerQueues[p.inFloor].add(p);
                return;
            }
        }

        boolean existClosingFullCarriage = false;  //是否存在正在关门的已满电梯
        for (var ctmp : carriages) {
            if (ctmp.nowFloor == p.inFloor && ctmp.direction == p.direction && ctmp.state == CarriageState.closing) {  //寻找方向正确的正在关门的电梯
                if (ctmp.isFull()) {
                    existClosingFullCarriage = true;
                } else {
                    p.setState(PassengerState.waiting_wont_abort);
                    passengerQueues[p.inFloor].add(p);
                    ctmp.setState(CarriageState.opening);  //取消电梯关门事件
                    events.add(new CarriageEvent(ctmp, time + (time - ctmp.lastStartOpenOrCloseTime), CarriageEventType.open_end));
                    return;
                }
            }
        }

        //以上各种情况都不存在
        if (!existClosingFullCarriage) {  //存在
            switch (p.direction) {
                case up:
                    callingUp[p.inFloor] = true;
                    break;
                case down:
                    callingDown[p.inFloor] = true;
                    break;
            }
            reschedule(p);  //触发调度
        }
        //存在正在关门的已满电梯，无需按按钮，因为电梯关门结束时会更新按钮状态
        p.setState(PassengerState.waiting);
        events.add(new PassengerEvent(p, null, time + p.abortingTime, PassengerEventType.abort));
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
        var nextp2 = c.getNextPassengerForExiting();
        if (nextp2 == null) {
            nextp2 = c.getNextPassengerForEntering();
            if (nextp2 != null) {
                events.add(new PassengerEvent(nextp2, c, time, PassengerEventType.enter_start));
            }
        } else {
            events.add(new PassengerEvent(nextp2, c, time, PassengerEventType.exit_start));
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
        c.isCalling[c.nowFloor] = false;
        events.add(new CarriageEvent(c, time + openAndCloseTime,
                CarriageEventType.open_end));
        if (c.direction == Direction.up) {
            callingUp[c.nowFloor] = false;
        } else {
            callingDown[c.nowFloor] = false;
        }
        var iter = passengerQueues[c.nowFloor].iterator();
        while (iter.hasNext()) {
            var ptmp = iter.next();
            if (c.direction == ptmp.direction) {
                ptmp.setState(PassengerState.waiting_wont_abort);  //存在已到达电梯则不会放弃等待
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
            events.add(new CarriageEvent(c, time,
                    CarriageEventType.close_start));
        } else {
            events.add(new CarriageEvent(c, time + detectGap,
                    CarriageEventType.detect));
        }
    }

    private void close_start(Carriage c) {
        c.setState(CarriageState.closing);
        c.lastStartOpenOrCloseTime = time;
        events.add(new CarriageEvent(c, time + openAndCloseTime,
                CarriageEventType.close_end));
        //检验是否还有其他可替代的电梯
        boolean existOtherAppropriateCarriage = false;
        for (var ctmp : carriages) {
            if (ctmp.nowFloor == c.nowFloor && ctmp.direction == c.direction &&
                    (ctmp.state == CarriageState.opening || ctmp.state == CarriageState.detecting)) {
                existOtherAppropriateCarriage = true;
                break;
            }
        }
        //未能上电梯的，且不存在替代电梯的乘客重新变回等待状态
        if (!existOtherAppropriateCarriage) {
            var iter = passengerQueues[c.nowFloor].iterator();
            while (iter.hasNext()) {
                var ptmp = iter.next();
                if (ptmp.direction == c.direction) {
                    ptmp.setState(PassengerState.waiting);  //重新变回等待状态
                    if (time > ptmp.arrivingTime + ptmp.abortingTime) {  //已经过了放弃时间的，直接放弃
                        events.add(new PassengerEvent(ptmp, null, time, PassengerEventType.abort));
                    }
                }
            }
        }
    }

    private void close_end(Carriage c) {
        if (c.state == CarriageState.closing) {
            events.add(new CarriageEvent(c, time, CarriageEventType.decide_direction));
            //剩下的乘客重新按按钮
            //检验是否还有其他可替代的电梯
            boolean existOtherAppropriateCarriage = false;
            for (var ctmp : carriages) {
                if (ctmp.nowFloor == c.nowFloor && ctmp.direction == c.direction &&
                        (ctmp.state == CarriageState.opening || ctmp.state == CarriageState.detecting)) {
                    existOtherAppropriateCarriage = true;
                    break;
                }
            }
            //不存在替代电梯,且存在等待的乘客，则按下按钮
            if (!existOtherAppropriateCarriage) {
                var iter = passengerQueues[c.nowFloor].iterator();
                while (iter.hasNext()) {
                    var ptmp = iter.next();
                    if (ptmp.direction == c.direction && ptmp.state == PassengerState.waiting) {
                        if (ptmp.direction == Direction.up) {
                            callingUp[ptmp.inFloor] = true;
                        } else {
                            callingDown[ptmp.inFloor] = true;
                        }
                        reschedule(ptmp);  //触发调度
                        break;
                    }
                }
            }
        }
    }

    //todo 待完善

    /**
     * 先看同方向有没有乘客要下，有的话不改变方向
     * 再看有没有被设定目标，有的话朝目标前进
     * 如果都没有且反方向有乘客要下，则转向（看看需不需要开门）（直接生成一个反向的运动end）
     * 如果都没有，进入停泊
     */
    private void decide_direction(Carriage c) {
        boolean existUpCalling = false;
        boolean existDownCalling = false;
        for (int i = c.nowFloor + 1; i < floors; i++) {
            if (c.isCalling[i]) {
                existUpCalling = true;
                break;
            }
        }
        for (int i = c.nowFloor - 1; i >= 0; i--) {
            if (c.isCalling[i]) {
                existDownCalling = true;
                break;
            }
        }

        if (c.direction == Direction.up) {
            if (existUpCalling ||
                    (c.targetPassenger != null && c.targetPassenger.inFloor > c.nowFloor)) {
                events.add(new CarriageEvent(c, time, CarriageEventType.up_start));
            } else if (existDownCalling ||
                    (c.targetPassenger != null && c.targetPassenger.inFloor < c.nowFloor)) {
                c.setDirection(Direction.down);
                c.setState(CarriageState.downing);
                if (c.isCalling[c.nowFloor] || callingDown[c.nowFloor]) {
                    events.add(new CarriageEvent(c, time, CarriageEventType.open_start));
                } else {
                    events.add(new CarriageEvent(c, time, CarriageEventType.down_start));
                }
            } else {
                events.add(new CarriageEvent(c, time, CarriageEventType.berth_start));
            }
        } else {
            if (existDownCalling ||
                    (c.targetPassenger != null && c.targetPassenger.inFloor < c.nowFloor)) {
                events.add(new CarriageEvent(c, time, CarriageEventType.down_start));
            } else if (existUpCalling ||
                    (c.targetPassenger != null && c.targetPassenger.inFloor > c.nowFloor)) {
                c.setDirection(Direction.up);
                c.setState(CarriageState.upping);
                if (c.isCalling[c.nowFloor] || callingUp[c.nowFloor]) {
                    events.add(new CarriageEvent(c, time, CarriageEventType.open_start));
                } else {
                    events.add(new CarriageEvent(c, time, CarriageEventType.up_start));
                }
            } else {
                events.add(new CarriageEvent(c, time, CarriageEventType.berth_start));
            }
        }
    }

    private void up_start(Carriage c) {
        c.setState(CarriageState.upping);
        c.setDirection(Direction.up);
        c.lastStartUpOrDownTime = time;
        events.add(new CarriageEvent(c, time + upAndDownTime, CarriageEventType.up_end));
    }

    private void up_end(Carriage c) {
        c.nowFloor++;
        if (c.targetPassenger != null && c.targetPassenger.inFloor == c.nowFloor) {
            c.targetPassenger = null;
        }
        if (c.isCalling[c.nowFloor] || callingUp[c.nowFloor]) {
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
        if (c.targetPassenger != null && c.targetPassenger.inFloor == c.nowFloor) {
            c.targetPassenger = null;
        }
        if (c.isCalling[c.nowFloor] || callingDown[c.nowFloor]) {
            events.add(new CarriageEvent(c, time, CarriageEventType.open_start));
        } else {
            events.add(new CarriageEvent(c, time, CarriageEventType.decide_direction));
        }
    }

    private void berth_start(Carriage c) {
        c.setState(CarriageState.berthing);
        c.setDirection(Direction.none);
        events.add(new CarriageEvent(c, time + berthTime, CarriageEventType.berth_end));
        //todo 寻找距离最远的乘客，触发调度
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
    }

    public void nextEvent() {
        if (!events.isEmpty()) {
            var e = events.get();
            time = e.occurTime;  //设定时钟

            var c = e.carriage;

            if (e instanceof PassengerEvent) {  //乘客事件
                var p = ((PassengerEvent) e).passenger;
                switch (((PassengerEvent) e).type) {
                    case arrive:
                        arrive(p);
                        break;
                    case abort:
                        abort(p);
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
            } else {
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
            }
        }
    }

    /**
     * 调度原则：
     * 最小绝对距离原则
     */

    //todo 待完善

    /**
     * 首先计算各个电梯的抽象距离
     * 优先调用抽象距离最小的电梯
     * <p>
     * 需要注意一些点
     * 不能改变原有电梯的targetPassenger
     * <p>
     * <p>
     * 寻找抽象距离最近的电梯
     * 如果有targetPassenger，判断是否可以调整targetPassenger
     *
     * @param triggerPassenger 触发调度的乘客
     */
    private void reschedule(Passenger triggerPassenger) {  //重新调度
        double distance[] = new double[parallelNumber];  //记录每个电梯的抽象距离
        //计算抽象距离
        for (int i = 0; i < parallelNumber; i++) {
            distance[i] = getAbstractDistance(carriages[i], triggerPassenger);
        }
        //需找距离最小的
        Carriage ctmp = null;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < parallelNumber; i++) {
            if (distance[i] < min) {
                ctmp = carriages[i];
                min = distance[i];
            }
        }
        //如果距离最小的电梯处于待命或者即将待命的状态，则调用
        if (ctmp.state == CarriageState.berthing || ctmp.state == CarriageState.idling) {  //直接调用状态
            if (ctmp.nowFloor > triggerPassenger.inFloor) {
                ctmp.targetPassenger = triggerPassenger;
                events.add(new CarriageEvent(ctmp, time, CarriageEventType.down_start));
            } else if (ctmp.nowFloor < triggerPassenger.inFloor) {
                ctmp.targetPassenger = triggerPassenger;
                events.add(new CarriageEvent(ctmp, time, CarriageEventType.up_start));
            } else {
                events.add(new CarriageEvent(ctmp, time, CarriageEventType.open_start));
            }
        } else if (ctmp.state == CarriageState.idle_upping || ctmp.state == CarriageState.idle_downing) {
            double realFloor = ctmp.nowFloor;
            if (ctmp.state == CarriageState.idle_upping) {
                realFloor += (double) (time - ctmp.lastStartUpOrDownTime) / upAndDownTime;
            } else {
                realFloor -= (double) (time - ctmp.lastStartUpOrDownTime) / upAndDownTime;
            }
            int deltaTime = (int) ((realFloor - (int) realFloor) * upAndDownTime);
            if (realFloor > triggerPassenger.inFloor) {
                ctmp.targetPassenger = triggerPassenger;
                ctmp.setState(CarriageState.downing);
                ctmp.setDirection(Direction.down);
                events.add(new CarriageEvent(ctmp, time + deltaTime, CarriageEventType.down_end));
            } else if (realFloor < triggerPassenger.inFloor) {
                ctmp.targetPassenger = triggerPassenger;
                ctmp.setState(CarriageState.upping);
                ctmp.setDirection(Direction.up);
                events.add(new CarriageEvent(ctmp, time + deltaTime, CarriageEventType.up_end));
            } else {
                events.add(new CarriageEvent(ctmp, time, CarriageEventType.open_start));
            }
        }

    }

    //todo 抽象距离的计算
    private double getAbstractDistance(Carriage c, Passenger p) {  //计算抽象距离
        double realFloor = c.nowFloor;
        switch (c.state) {  //运动中的电梯需要修正
            case upping:
            case idle_upping:
                realFloor += (double) (time - c.lastStartUpOrDownTime) / upAndDownTime;
                break;
            case downing:
            case idle_downing:
                realFloor -= (double) (time - c.lastStartUpOrDownTime) / upAndDownTime;
                break;
        }
        //berthing,idling_up or down,idling直接返回楼层差的绝对值
        //其余分8种情况讨论
        double delta = Math.abs(realFloor - p.inFloor);

        if (p.direction == Direction.up) {
            switch (c.direction) {
                case up:
                    if (p.inFloor > realFloor) {
                        return delta;
                    } else {
                        return 2 * (floors - 1 - p.inFloor) - delta;
                    }
                case down:
                    if (p.inFloor > realFloor) {
                        return 2 * p.inFloor - delta;
                    } else {
                        return 2 * p.inFloor + delta;
                    }
                case none:
                    return delta;
            }
        } else {
            switch (c.direction) {
                case up:
                    if (p.inFloor < realFloor) {
                        return 2 * (floors - 1 - p.inFloor) - delta;
                    } else {
                        return 2 * (floors - 1 - p.inFloor) + delta;
                    }
                case down:
                    if (p.inFloor < realFloor) {
                        return delta;
                    } else {
                        return 2 * p.inFloor - delta;
                    }
                case none:
                    return delta;
            }
        }
        return delta;
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

    private void log(Event e) {

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
