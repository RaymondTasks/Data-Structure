package Lab2;

import List.LinkedList;
import Queue.OrderedLinkedQueue;

import java.util.Comparator;
import java.util.Random;

public class ElevatorSimulator {

    protected class Passenger {  //乘客类
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
        }

        public void setState(PassengerState state) {
            this.state = state;
        }

        public int targetFloor = -1;  //用于电梯调度的一个特殊量
    }

    protected class Carriage {  //电梯厢类
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
                if (direction == p.direction) {
                    passengerQueue[nowFloor].delete(p);
                    return p;
                }

            }
            return null;
        }  //找到下一个要进入的乘客

        public int lastStartClosingTime;  //用于中断关门后生成新的开门事件
        public int lastStartUpOrDownToIdlingTime;  //用于中断前往待命状态的移动事件
        public Passenger targetPassenger;  //用于电梯调度时的的目标乘客

    }

    protected abstract class Event {
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

    private enum Direction {
        up,
        none,
        down
    }

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

        passengerQueue = new PassengerLinkedQueue[floors];
        for (int i = 0; i < floors; i++) {
            passengerQueue[i] = new PassengerLinkedQueue();
        }
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

    //todo 待完善
    private void arrive(Passenger p) {

        /* 检测本层是否存在未满且detecting且方向正确的电梯
         * 若不存在，检测本层是否存在opening且方向正确的电梯的电梯
         * 若不存在，检测是否存在未满且closing且方向正确的电梯，如果存在，取消close_end事件
         * 以上三种状态，都不需要更新按钮，只需把乘客置为wait_wont_abort状
         * 如果都不存在，更新按钮，更新电梯调度
         * 乘客选择优先级：开着的>正在开的>正在关的
         */

        for (var ctmp : carriages) {  //寻找开着的，方向正确的，未满的电梯
            if (ctmp.nowFloor == p.inFloor && ctmp.direction == p.direction &&
                    ctmp.state == CarriageState.detecting && !ctmp.isFull()) {
                if (ctmp.IOPassenger == null) {   //电梯没有人进出，直接进入电梯
                    events.add(new PassengerEvent(p, ctmp, time,
                            PassengerEventType.enter_start));
                } else {
                    p.setState(PassengerState.waiting_wont_abort);
                    passengerQueue[p.inFloor].add(p);
                }
                return;
            }
        }

        for (var ctmp : carriages) {
            if (ctmp.nowFloor == p.inFloor && ctmp.direction == p.direction &&
                    ctmp.state == CarriageState.opening) {  //寻找方向正确的正在开门的电梯
                p.setState(PassengerState.waiting_wont_abort);
                passengerQueue[p.inFloor].add(p);
                return;
            }
        }

        boolean existClosingFullCarriage = false;  //是否存在正在关门的已满电梯
        for (var ctmp : carriages) {
            if (ctmp.nowFloor == p.inFloor && ctmp.direction == p.direction &&
                    ctmp.state == CarriageState.closing) {  //寻找方向正确的正在关门的电梯
                if (ctmp.isFull()) {
                    existClosingFullCarriage = true;
                } else {
                    p.setState(PassengerState.waiting_wont_abort);
                    passengerQueue[p.inFloor].add(p);
                    ctmp.setState(CarriageState.opening);  //取消电梯关门事件
                    events.add(new CarriageEvent(ctmp,
                            time + (time - ctmp.lastStartClosingTime),
                            CarriageEventType.open_end));
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
        }
        //todo 待完善
        //存在正在关门的已满电梯，无需按按钮，因为电梯关门结束时会更新按钮状态
        p.setState(PassengerState.waiting);
        events.add(new PassengerEvent(p, null,
                time + p.abortingTime, PassengerEventType.abort));
        reschedule(p);  //重新调度
        var newp = getRandomPassenger();  //下一个乘客
        events.add(new PassengerEvent(newp, null, newp.arrivingTime,
                PassengerEventType.arrive));
    }

    private void abort(Passenger p) {
        if (p.state == PassengerState.waiting) {
            p.setState(PassengerState.aborted);
            passengerQueue[p.inFloor].delete(p);
            events.add(new PassengerEvent(p, null, time,
                    PassengerEventType.leave));
        }
    }

    private void enter_start(Passenger p, Carriage c) {
        p.setState(PassengerState.entering);
        c.IOPassenger = p;
        passengerQueue[c.nowFloor].delete(p);
        events.add(new PassengerEvent(p, c, time + enterAndExitTime,
                PassengerEventType.enter_end));
    }

    private void enter_end(Passenger p, Carriage c) {
        p.setState(PassengerState.InCarriage);
        c.IOPassenger = null;
        c.add(p);
        var nextp = c.getNextPassengerForEntering();
        if (nextp != null) {
            events.add(new PassengerEvent(nextp, c, time,
                    PassengerEventType.enter_start));
        }
    }

    private void exit_start(Passenger p, Carriage c) {
        p.setState(PassengerState.exiting);
        c.IOPassenger = p;
        c.delete(p);
        events.add(new PassengerEvent(p, c, time + enterAndExitTime,
                PassengerEventType.leave));
    }

    private void exit_end(Passenger p, Carriage c) {
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
    }

    private void leave(Passenger p) {
        p.setState(PassengerState.leaved);
    }


    private void open_start(Carriage c) {
        c.setState(CarriageState.opening);
        c.isCalling[c.nowFloor] = false;
        events.add(new CarriageEvent(c, time + openAndCloseTime,
                CarriageEventType.open_end));
        switch (c.direction) {
            case up:
                callingUp[c.nowFloor] = false;
                break;
            case down:
                callingDown[c.nowFloor] = false;
                break;
        }
        var iter = passengerQueue[c.nowFloor].iterator();
        while (iter.hasNext()) {
            var ptmp = iter.next();
            if (c.direction == ptmp.direction) {
                ptmp.setState(PassengerState.waiting_wont_abort);  //存在已到达电梯则不会放弃等待
            }
        }
    }

    private void open_end(Carriage c) {
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
    }

    private void detect(Carriage c) {
        if (c.IOPassenger != null) {  //有乘客进出
            events.add(new CarriageEvent(c, time + detectGap,
                    CarriageEventType.detect));
        } else {
            events.add(new CarriageEvent(c, time,
                    CarriageEventType.close_start));
        }
    }

    private void close_start(Carriage c) {
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
                if (ptmp.direction == c.direction) {
                    ptmp.setState(PassengerState.waiting);  //重新变回等待状态
                    int maxTime = time > (ptmp.arrivingTime + ptmp.abortingTime) ?
                            time : (ptmp.arrivingTime + ptmp.abortingTime);
                    events.add(new PassengerEvent(ptmp, null,
                            maxTime, PassengerEventType.abort));
                }
            }
        }
    }

    private void close_end(Carriage c) {
        if (c.state == CarriageState.closing) {
            events.add(new CarriageEvent(c, time,
                    CarriageEventType.decide_direction));
        }
        //剩下的乘客重新按按钮
        var iter2 = passengerQueue[c.nowFloor].iterator();
        while (iter2.hasNext()) {
            switch (iter2.next().direction) {
                case up:
                    callingUp[c.nowFloor] = true;
                    break;
                case down:
                    callingDown[c.nowFloor] = true;
                    break;
            }
        }
    }

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
        switch (c.direction) {
            case up:
                if (existUpCalling || c.targetPassenger != null) {
                    events.add(new CarriageEvent(c, time,
                            CarriageEventType.up_start));
                } else if (existDownCalling) {
                    events.add(new CarriageEvent(c, time,
                            CarriageEventType.down_start));
                } else {
                    events.add(new CarriageEvent(c, time,
                            CarriageEventType.berth_start));
                }
                break;
            case down:
                if (existDownCalling || c.targetPassenger != null) {
                    events.add(new CarriageEvent(c, time,
                            CarriageEventType.down_start));
                } else if (existUpCalling) {
                    events.add(new CarriageEvent(c, time,
                            CarriageEventType.up_start));
                } else {
                    events.add(new CarriageEvent(c, time,
                            CarriageEventType.berth_start));
                }
                break;
        }
    }

    private void up_start(Carriage c) {
        c.setState(CarriageState.upping);
        c.setDirection(Direction.up);
        events.add(new CarriageEvent(c, time + upAndDownTime,
                CarriageEventType.up_end));
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
        events.add(new CarriageEvent(c, time + upAndDownTime,
                CarriageEventType.down_end));
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
        //todo 待优化完善
        //先检查是否是被调度程序生成的电梯运动
        c.setState(CarriageState.berthing);
        c.setDirection(Direction.none);
        events.add(new CarriageEvent(c, time + berthTime,
                CarriageEventType.berth_end));
    }

    private void berth_end(Carriage c) {
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
    }

    private void idle_up_start(Carriage c) {
        c.setState(CarriageState.idle_upping);
        c.setDirection(Direction.none);
        c.lastStartUpOrDownToIdlingTime = time;
        //此处为none是因为direction不是记录真实的运动方向，是乘客预计的运动方向
        //闲置运动时没有乘客置为none
        events.add(new CarriageEvent(c, time + upAndDownTime,
                CarriageEventType.idle_up_end));
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
        c.lastStartUpOrDownToIdlingTime = time;
        events.add(new CarriageEvent(c, time + upAndDownTime * (c.nowFloor - base),
                CarriageEventType.idle_down_end));
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

    /**
     * 电梯调度时需要遵循的一些原则：
     * 触发调度更新的是等待中的乘客按下按钮，或者电梯找不到目标时
     */
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
        if (ctmp.state == CarriageState.berthing
                || ctmp.state == CarriageState.idling) {  //直接调用状态
            if (ctmp.nowFloor > triggerPassenger.inFloor) {
                ctmp.targetPassenger = triggerPassenger;
                events.add(new CarriageEvent(ctmp, time, CarriageEventType.down_start));
            } else if (ctmp.nowFloor < triggerPassenger.inFloor) {
                ctmp.targetPassenger = triggerPassenger;
                events.add(new CarriageEvent(ctmp, time, CarriageEventType.up_start));
            } else {
                events.add(new CarriageEvent(ctmp, time, CarriageEventType.open_start));
            }
        } else if (ctmp.state == CarriageState.idle_upping
                || ctmp.state == CarriageState.idle_downing) {
            double realFloor = ctmp.nowFloor;
            if (ctmp.state == CarriageState.idle_upping) {
                realFloor += (double) (time - ctmp.lastStartUpOrDownToIdlingTime) / upAndDownTime;
            } else {
                realFloor -= (double) (time - ctmp.lastStartUpOrDownToIdlingTime) / upAndDownTime;
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

    private double getAbstractDistance(Carriage c, Passenger p) {  //计算抽象距离
        double cRealFloor = c.nowFloor;
        switch (c.state) {  //运动中的电梯需要修正
            case upping:
            case idle_upping:
                cRealFloor += (double) (time - c.lastStartUpOrDownToIdlingTime) / upAndDownTime;
                break;
            case downing:
            case idle_downing:
                cRealFloor -= (double) (time - c.lastStartUpOrDownToIdlingTime) / upAndDownTime;
                break;
        }
        //berthing,idling_up or down,idling直接返回楼层差的绝对值
        //其余分8种情况讨论
        double delta = cRealFloor - p.inFloor;
        switch (p.direction) {
            case up:
                switch (c.direction) {
                    case up:
                        if (delta > 0) {
                            return 2 * (floors - 1 - p.inFloor) - delta;
                        }
                        break;
                    case down:
                        return 2 * p.inFloor + delta;
                }
                break;
            case down:
                switch (c.direction) {
                    case up:
                        return 2 * (floors - 1 - p.inFloor) - delta;
                    case down:
                        if (delta < 0) {
                            return 2 * p.inFloor + delta;
                        }
                        break;
                }
                break;
        }
        return delta < 0 ? -delta : delta;
    }

    public OrderedLinkedQueue<Event> getEvents() {
        return events;
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
