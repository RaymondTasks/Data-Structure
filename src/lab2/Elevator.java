package lab2;

import List.LinkedList;
import Queue.LinkedQueue;
import Queue.OrderedLinkedQueue;

import java.util.Comparator;
import java.util.Random;

public class Elevator {

    private abstract class Event {
        int occurTime = 0;
        public int type = 0;
    }

    private class CarriageEvent extends Event {
        public static final int GoUp = 0;
        public static final int GoDown = 1;
        public static final int Open = 2;
        public static final int Close = 3;
        public static final int Detect = 4;
        public static final int GoWaiting = 5;
        public static final int GoIdling = 6;

        public Carriage carriage;

        public CarriageEvent(Carriage carriage, int occurTime, int type) {
            this.carriage = carriage;
            this.occurTime = occurTime;
            this.type = type;
        }

    }

    private class PassengerEvent extends Event {
        public static final int Arrive = 0;
        public static final int Enter = 2;
        public static final int GiveUp = 3;
        public static final int Leave = 4;
        public Passenger passenger;

        public PassengerEvent(Passenger passenger, int occurTime, int type) {
            this.passenger = passenger;
            this.occurTime = occurTime;
            this.type = type;
        }
    }

    public class Carriage {
        public int nowFloor;
        public int state;
        private int load;
        private int maxLoad;
        public int last_Start_Opening_Or_Closing_Time;
        public int last_Start_Upping_Or_Downing_Time;
        public boolean is_Open_Or_Close_Canceled;
        private boolean isDestination[];
        public int calledDestination;

        public static final int State_Idling = 0;

        public static final int State_Upping = 1;
        public static final int State_Downing = 2;

        public static final int State_Opening_Up = 4;
        public static final int State_Opening_Down = 5;

        public static final int State_Closing_Up = 7;
        public static final int State_Closing_Down = 8;

        public static final int State_IOing_Up = 9;
        public static final int State_IOing_Down = 10;

        public static final int State_Waiting = 11;

        private LinkedList<Passenger> passengers;


        public Carriage(int maxLoad) {
            nowFloor = 1;
            state = State_Idling;
            load = 0;
            this.maxLoad = maxLoad;
            last_Start_Opening_Or_Closing_Time = -1;
            is_Open_Or_Close_Canceled = false;
            isDestination = new boolean[floors];
            calledDestination = -1;
            passengers = new LinkedList<Passenger>();
        }

        public void add(Passenger p) {
            passengers.insertTail(p);
            load++;
        }

        public boolean isFull() {
            return load == maxLoad;
        }

    }

    public class Passenger {
        public boolean willGiveUp;//为false时giveup事件无效
        public int inFloor;
        public int outFloor;
        public int arrivedTime;
        public int giveUpTime;
        public int state;

        public Passenger(int inFloor, int outFloor, int arrivedTime, int giveUpTime) {
            this.inFloor = inFloor;
            this.outFloor = outFloor;
            this.arrivedTime = arrivedTime;
            this.giveUpTime = giveUpTime;
            willGiveUp = true;
        }

        public static final int State_Waiting = 0;
        public static final int State_Waiting_Extended = 1;
        public static final int State_In_Carriage = 2;
        public static final int State_GaveUp = 3;


    }


    private int time = 0;

    private int floors, amount;
    private int
    int maxArrivedTimeGap, maxGiveUpTime;

    //    private boolean[] CallUp, CallDown;
    private LinkedQueue<Passenger>[] upQueue, downQueue;
    private boolean[] upCalled, downCalled;

    private Carriage carriage[];


    //电梯移动速度100t/层
    public Elevator(int floors, int amount, int maxLoad,
                    int maxArrivedTimeGap, int maxGiveUpTime) {
        this.floors = floors;
        this.amount = amount;
        this.maxArrivedTimeGap = maxArrivedTimeGap;
        this.maxGiveUpTime = maxGiveUpTime;

        carriage = new ElevatorCarriage[floors];
        for (int i = 0; i < amount; i++) {
            PassengerList[i] = new LinkedList<>();
            carriage[i] = new ElevatorCarriage(maxLoad);
        }

        EventList = new OrderedLinkedQueue<>(new Comparator<Event>() {
            @Override
            public int compare(Event o1, Event o2) {
                return o1.occurTime - o2.occurTime;
            }
        });

    }

    private void refreshGUI() {

    }

    //原子化事件
    private OrderedLinkedQueue<Event> EventList;
    private Random random;

    public void startSimulation() {
        random = new Random();

        //第一个乘客
        Passenger first = getRandomPassenger();
        PassengerEvent initEvent = new PassengerEvent(first,
                first.arrivedTime, PassengerEvent.Arrive);
        EventList.add(initEvent);

        //主要模拟过程
        while (!EventList.isEmpty()) {
            Event e = EventList.get();  //取事件
            time = e.occurTime;
            if (e instanceof PassengerEvent) {  //乘客事件

                Passenger p = ((PassengerEvent) e).passenger;
                switch (e.type) {
                    case PassengerEvent.Arrive:
                        /*
                        到达一个乘客时，先检测本层有没有可用的电梯，先用正在开关门的，再用待命和即将待命的
                        如果没有，再检测有没有待命或即将待命的电梯，选择最近的那个移动
                        如果什么都没欧，把乘客加到队列
                         */

                        for (var car : carriage) {

                            if (car.nowFloor == p.inFloor) {

                                //先用正在开关门的
                                if (p.inFloor < p.outFloor) {  //上升的
                                    switch (car.state) {
                                        case Carriage.State_Closing_Up:
                                            if (!car.isFull()) {
                                                car.is_Open_Or_Close_Canceled = true;//取消开关门的动作
                                                car.state = Carriage.State_Opening_Up;
                                                EventList.add(new CarriageEvent(car, time + time - car.last_Start_Opening_Or_Closing_Time
                                                        , CarriageEvent.Open));//增加开门事件
                                                upQueue[p.inFloor].add(p);
                                            }
                                        case Carriage.State_IOing_Up:
                                        case Carriage.State_Opening_Up:
                                            upQueue[p.inFloor].add(p);
                                            p.willGiveUp = false;  //不需要考虑放弃时间
                                            break;
                                        default:
                                    }
                                } else {
                                    switch (car.state) {
                                        case Carriage.State_IOing_Down:
                                        case Carriage.State_Opening_Down:
                                            downQueue[p.inFloor].add(p);
                                            p.willGiveUp = false;
                                            break;
                                        case Carriage.State_Closing_Down:
                                            if (!car.isFull()) {
                                                car.is_Open_Or_Close_Canceled = true;
                                                car.state = Carriage.State_Opening_Down;
                                                EventList.add(new CarriageEvent(car, time + time - car.last_Start_Opening_Or_Closing_Time
                                                        , CarriageEvent.Open));
                                                downQueue[p.inFloor].add(p);
                                            }
                                            p.willGiveUp = false;
                                            break;
                                        default:
                                    }
                                }

                                //再用待命和即将待命的
                                if (p.willGiveUp) {
                                    if (car.state == Carriage.State_Waiting || car.state == Carriage.State_Idling) {
                                        if (p.inFloor < p.outFloor) {
                                            car.state = Carriage.State_Opening_Up;
                                            upQueue[p.inFloor].add(p);
                                        } else {
                                            car.state = Carriage.State_Opening_Down;
                                            downQueue[p.inFloor].add(p);
                                        }
                                        EventList.add(new CarriageEvent(car, time + 100, CarriageEvent.Open));
                                        p.willGiveUp = false;
                                    }
                                }

                                if (!p.willGiveUp) {
                                    break;
                                }
                            }
                        }

                        if (p.willGiveUp) {//其他状况
                            //更新电梯运行状态
                            Carriage nearest = null;//距离最近的可用电梯
                            var comparator = new Comparator<Carriage>() {
                                @Override
                                public int compare(Carriage o1, Carriage o2) {
                                    return Math.abs(p.inFloor - o1.nowFloor) - Math.abs(p.inFloor - o2.nowFloor);
                                }
                            };
                            for (var car : carriage) {
                                if ((car.state == Carriage.State_Idling || car.state == Carriage.State_Waiting)
                                        && (nearest == null || comparator.compare(car, nearest) < 0)) {
                                    nearest = car;
                                }
                            }
                            if (nearest != null) {
                                if (p.inFloor < nearest.nowFloor) {
                                    nearest.state = Carriage.State_Downing;
                                    EventList.add(new CarriageEvent(nearest, time + 100, CarriageEvent.GoDown));
                                } else {
                                    nearest.state = Carriage.State_Upping;
                                    EventList.add(new CarriageEvent(nearest, time + 100, CarriageEvent.GoUp));
                                }
                                nearest.calledDestination = p.inFloor;
                            }

                            if (p.inFloor < p.outFloor) { //更新电梯等待队列
                                upCalled[p.inFloor] = true;
                                upQueue[p.inFloor].add(p);
                            } else {
                                downCalled[p.inFloor] = true;
                                downQueue[p.inFloor].add(p);
                            }
                            EventList.add(new PassengerEvent(p, time + p.giveUpTime,
                                    PassengerEvent.GiveUp));//增加一个放弃事件
                        }


                        break;
                    case PassengerEvent.Enter:

                        break;
                    case PassengerEvent.Leave:

                        break;
                    case PassengerEvent.GiveUp:
                        //当前电梯正在开门

                        //电梯仍未到达
                }


            } else {  //电梯事件

                Carriage c = ((CarriageEvent) e).carriage;
                switch (e.type) {
                    case CarriageEvent.GoUp:
                        c.nowFloor++;
                        if (c.isDestination[c.nowFloor] || upCalled[c.nowFloor]) {//有人需要上下电梯
                            c.state = Carriage.State_Opening_Up;
                            EventList.add(new CarriageEvent(c, time + 20, CarriageEvent.Open));
                        } else {
                            boolean isCalled
                        }
                        break;
                    case CarriageEvent.Downing:
                        c.nowFloor--;
                        if (downQueue[c.nowFloor].isEmpty()) {
                            EventList.add(new CarriageEvent(c, time + 100
                                    , CarriageEvent.Downing);
                        } else {
                            c.state = Carriage.State_Opening_Down;
                            downCalled[c.nowFloor] = false;
                            EventList.add(new CarriageEvent(c, time + 20,
                                    CarriageEvent.Opening));
                        }
                        break;
                    case CarriageEvent.
                }


            }
        }
    }

    private Passenger getRandomPassenger() {
        int inFloor = random.nextInt(floors);
        int outFloor;
        do {
            outFloor = random.nextInt(floors);
        } while (inFloor == outFloor);
        return new Passenger(inFloor, outFloor,
                time + random.nextInt(maxArrivedTimeGap)
                , random.nextInt(maxGiveUpTime));
    }

}
