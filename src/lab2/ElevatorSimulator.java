package lab2;

import Queue.OrderedLinkedQueue;
import List.LinkedList;

import java.util.Comparator;
import java.util.Random;

public class ElevatorSimulator {

    protected class Passenger {  //乘客类
        PassengerState state;
        int arrivingTime;
        int abortingTime;
        int inFloor, outFloor;

        Passenger(int arrivingTime, int abortingTime, int inFloor, int outFloor) {
            this.arrivingTime = arrivingTime;
            this.abortingTime = abortingTime;
            this.inFloor = inFloor;
            this.outFloor = outFloor;
        }

        public boolean isForUp() {
            return outFloor > inFloor;
        }

        public void setState(PassengerState state) {
            this.state = state;
        }
    }

    protected class Carriage {  //电梯厢类
        CarriageState state;
        boolean[] isCalling;
        boolean isForUp;
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
        }

        public boolean isFull() {
            return nowLoad == content.length;
        }

        public void setState(CarriageState state) {
            this.state = state;
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
                if (content[i].outFloor == nowFloor) {
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
            var iter = passengerQueue[nowFloor].iterator();
            while (iter.hasNext()) {
                p = iter.next();
                if (p.isForUp() == isForUp) {
                    passengerQueue[nowFloor].delete(p);
                    return p;
                }

            }
            return null;
        }  //找到下一个要进入的乘客

        public int lastStartClosingTime;  //用于中断关门后生成新的开门事件

    }

    protected abstract class Event {  //抽象事件
        int occurTime;
        Carriage carriage;
    }  //抽象事件

    protected class PassengerEvent extends Event {  //乘客事件
        PassengerEventType type;
        Passenger passenger;

        PassengerEvent(Passenger passenger, Carriage carriage, int occurTime, PassengerEventType type) {
            this.passenger = passenger;
            this.carriage = carriage;
            this.occurTime = occurTime;
            this.type = type;
        }
    }  //乘客事件

    protected class CarriageEvent extends Event {  //电梯厢事件
        CarriageEventType type;

        CarriageEvent(Carriage carriage, int occurTime, CarriageEventType type) {
            this.carriage = carriage;
            this.occurTime = occurTime;
            this.type = type;
        }
    }  //电梯厢事件

    //状态枚举
    private enum PassengerState {
        waiting,                //等待电梯
        waiting_wont_abort,     //即将进入电梯，此时abort事件不生效
        aborted,                //已放弃
        leaved,                 //已离开
        entering,               //正在进入电梯
        exiting,                //正在离开电梯
        InCarriage              //在电梯里
    }  //乘客状态

    private enum CarriageState {  //电梯厢状态
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
    private enum PassengerEventType {  //乘客事件类型
        arrive,         //到达
        abort,          //放弃
        enter_start,    //开始进入电梯
        enter_end,      //完全进入
        exit_start,     //开始走出电梯
        exit_end,       //完全走出
        leave           //离开
    }  //乘客事件类型

    private enum CarriageEventType {  //电梯厢事件类型
        up_start,           //开始上升
        up_end,             //完成上升
        down_start,         //开始下降
        down_end,           //完成下降
        open_start,         //开始开门
        open_end,           //完成开门
        detect,             //检测是否有人进出
        close_start,        //开始关门
        close_end,          //完成关门，只在closing状态生效
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

        passengerQueue = new PassengerLinkedQueue[floors];
        events = new OrderedLinkedQueue<>(new Comparator<Event>() {
            @Override
            public int compare(Event o1, Event o2) {
                if (o1.occurTime == o2.occurTime) {  //电梯事件优先执行
                    boolean b1 = o1 instanceof CarriageEvent;
                    boolean b2 = o1 instanceof CarriageEvent;
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
    private PassengerLinkedQueue passengerQueue[];


    //事件队列
    private OrderedLinkedQueue<Event> events;
    //全局时钟
    private int time = 0;


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


    //读取一个事件，更新状态，生成下一个事件
    public void nextEvent() {
        if (!events.isEmpty()) {
            var e = events.get();
            time = e.occurTime;  //设定时钟

            if (e instanceof PassengerEvent) {  //乘客事件
                var p = ((PassengerEvent) e).passenger;
                var c = ((PassengerEvent) e).carriage;

                switch (((PassengerEvent) e).type) {
                    case arrive:

                        //todo 待完成,需要重构
                        if (p.isForUp()) {
                            callingUp[p.inFloor] = true;
                        } else {
                            callingDown[p.inFloor] = true;
                        }

                        break;
                    case abort:
                        if (p.state == PassengerState.waiting) {
                            p.setState(PassengerState.aborted);
                            passengerQueue[p.inFloor].delete(p);
                            events.add(new PassengerEvent(p, null, time,
                                    PassengerEventType.leave));
                        }
                        break;
                    case enter_start:
                        p.setState(PassengerState.entering);
                        c.IOPassenger = p;
                        passengerQueue[c.nowFloor].delete(p);
                        events.add(new PassengerEvent(p, c, time + enterAndExitTime,
                                PassengerEventType.enter_end));
                        break;
                    case enter_end:
                        p.setState(PassengerState.InCarriage);
                        c.IOPassenger = null;
                        c.add(p);
                        var nextp = c.getNextPassengerForEntering();
                        if (nextp != null) {
                            events.add(new PassengerEvent(nextp, c, time,
                                    PassengerEventType.enter_start));
                        }
                        break;
                    case exit_start:
                        p.setState(PassengerState.exiting);
                        c.IOPassenger = p;
                        c.delete(p);
                        events.add(new PassengerEvent(p, c, time + enterAndExitTime,
                                PassengerEventType.leave));
                        break;
                    case exit_end:
                        c.IOPassenger = null;
                        events.add(new PassengerEvent(p, c, time, PassengerEventType.leave));
                        var nextp2 = c.getNextPassengerForExiting();
                        if (nextp2 == null) {
                            nextp2 = c.getNextPassengerForEntering();
                            if (nextp2 != null) {
                                events.add(new PassengerEvent(nextp2, c, time,
                                        PassengerEventType.enter_start));
                            }
                        } else {
                            events.add(new PassengerEvent(nextp2, c, time,
                                    PassengerEventType.exit_start));
                        }
                        break;
                    case leave:
                        p.setState(PassengerState.leaved);
                        break;
                }

            } else {
                var c = ((CarriageEvent) e).carriage;
                switch (((CarriageEvent) e).type) {
                    case open_start:
                        c.setState(CarriageState.opening);
                        c.isCalling[c.nowFloor] = false;
                        events.add(new CarriageEvent(c, time + openAndCloseTime,
                                CarriageEventType.open_end));
                        if (c.isForUp) {
                            callingUp[c.nowFloor] = false;
                        } else {
                            callingDown[c.nowFloor] = false;
                        }
                        var iter = passengerQueue[c.nowFloor].iterator();
                        while (iter.hasNext()) {
                            var ptmp = iter.next();
                            if (ptmp.isForUp() == c.isForUp) {
                                ptmp.setState(PassengerState.waiting_wont_abort);  //存在已到达电梯则不会放弃等待
                            }
                        }
                        break;
                    case open_end:
                        c.setState(CarriageState.detecting);
                        events.add(new CarriageEvent(c, time + detectGap,
                                CarriageEventType.detect));
                        var nextp = c.getNextPassengerForExiting();
                        if (nextp == null) {
                            nextp = c.getNextPassengerForEntering();
                            if (nextp != null) {
                                events.add(new PassengerEvent(nextp, c, time,
                                        PassengerEventType.enter_start));
                            }
                        } else {
                            events.add(new PassengerEvent(nextp, c, time,
                                    PassengerEventType.exit_start));
                        }
                        break;
                    case detect:
                        if (c.IOPassenger != null) {  //有乘客进出
                            events.add(new CarriageEvent(c, time + detectGap,
                                    CarriageEventType.detect));
                        } else {
                            events.add(new CarriageEvent(c, time,
                                    CarriageEventType.close_start));
                        }
                        break;
                    case close_start:
                        c.setState(CarriageState.closing);
                        c.lastStartClosingTime = time;
                        events.add(new CarriageEvent(c, time + openAndCloseTime,
                                CarriageEventType.close_end));
                        boolean existOtherAppropriateCarriage = false;
                        //检验是否还有其他可替代的电梯
                        for (var ctmp : carriages) {
                            if (ctmp != c && ctmp.nowFloor == c.nowFloor &&
                                    (ctmp.state == CarriageState.opening ||
                                            ctmp.state == CarriageState.detecting)) {
                                existOtherAppropriateCarriage = true;
                            }
                        }
                        //未能上电梯的乘客重新变回等待状态
                        if (!existOtherAppropriateCarriage) {
                            var iter2 = passengerQueue[c.nowFloor].iterator();
                            while (iter2.hasNext()) {
                                var ptmp = iter2.next();
                                if (ptmp.isForUp() == c.isForUp) {
                                    ptmp.setState(PassengerState.waiting);  //重新变回等待状态
                                    int maxTime = time > (ptmp.arrivingTime + ptmp.abortingTime) ?
                                            time : (ptmp.arrivingTime + ptmp.abortingTime);
                                    events.add(new PassengerEvent(ptmp, null,
                                            maxTime, PassengerEventType.abort));
                                }
                            }
                        }

                        break;
                    case close_end:
                        if (c.state == CarriageState.closing) {
                            //todo 更新本层电梯的按钮


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
                            //todo 判断是否需要另外调度电梯
                            if (c.isForUp) {
                                if (existUpCalling) {
                                    events.add(new CarriageEvent(c, time,
                                            CarriageEventType.up_start));
                                } else if (existDownCalling) {
                                    c.isForUp = false;
                                    events.add(new CarriageEvent(c, time,
                                            CarriageEventType.down_start));
                                } else {
                                    events.add(new CarriageEvent(c, time,
                                            CarriageEventType.berth_start));
                                }

                            } else {
                                if (existDownCalling) {
                                    events.add(new CarriageEvent(c, time,
                                            CarriageEventType.down_start));
                                } else if (existUpCalling) {
                                    c.isForUp = true;
                                    events.add(new CarriageEvent(c, time,
                                            CarriageEventType.up_start));
                                } else {
                                    events.add(new CarriageEvent(c, time,
                                            CarriageEventType.berth_start));
                                }
                            }

                        }
                        break;
                    case berth_start:
                        c.setState(CarriageState.berthing);
                        events.add(new CarriageEvent(c, time + berthTime,
                                CarriageEventType.berth_end));
                        break;
                    case berth_end:
                        if (c.state == CarriageState.berthing) {
                            if (c.nowFloor == base) {
                                events.add(new CarriageEvent(c, time,
                                        CarriageEventType.idle_start));
                            } else if (c.nowFloor > base) {
                                events.add(new CarriageEvent(c, time,
                                        CarriageEventType.idle_down_start));
                            } else {
                                events.add(new CarriageEvent(c, time,
                                        CarriageEventType.idle_up_start));
                            }
                        }
                        break;

                    case up_start:
                        c.setState(CarriageState.upping);
                        events.add(new CarriageEvent(c, time + upAndDownTime,
                                CarriageEventType.up_end));
                        break;
                    case up_end:
                        //todo
                        break;
                    case down_start:
                        c.setState(CarriageState.downing);
                        events.add(new CarriageEvent(c, time + upAndDownTime,
                                CarriageEventType.down_end));
                        break;
                    case down_end:
                        //todo
                        break;
                    case idle_up_start:
                        c.setState(CarriageState.idle_upping);
                        events.add(new CarriageEvent(c, time + upAndDownTime * (base - c.nowFloor),
                                CarriageEventType.idle_up_end));
                        break;
                    case idle_up_end:
                        if (c.state == CarriageState.idle_upping) {
                            events.add(new CarriageEvent(c, time, CarriageEventType.idle_start));
                        }
                        break;
                    case idle_down_start:
                        c.setState(CarriageState.idle_downing);
                        events.add(new CarriageEvent(c, time + upAndDownTime * (c.nowFloor - base),
                                CarriageEventType.idle_down_end));
                        break;
                    case idle_down_end:
                        if (c.state == CarriageState.idle_downing) {
                            events.add(new CarriageEvent(c, time, CarriageEventType.idle_start));
                        }
                        break;
                    case idle_start:
                        c.setState(CarriageState.idling);
                        break;
                }
            }
        }
    }


}

class PassengerLinkedQueue extends LinkedList<ElevatorSimulator.Passenger> {
    public void delete(ElevatorSimulator.Passenger p) {
        var q = head;
        while (q.getNext() != null) {
            if (q.getNext().getData() == p) {
                q.setNext(q.getNext().getNext());
                break;
            }
            q = q.getNext();
        }
    }
}
