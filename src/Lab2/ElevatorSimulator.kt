package Lab2

import Queue.OrderedLinkedQueue
import java.awt.Color
import kotlin.random.Random


class ElevatorSimulator
constructor(
        private val floors: Int = 5,
        private val base: Int = 1,

        private val parallelNumber: Int = 1,
        private val maxLoad: Int = 10,

        private val upAndDownTime: Int = 100,
        private val openAndCloseTime: Int = 20,
        private val enterAndExitTime: Int = 40,

        private val detectTime: Int = 40,
        private val berthTime: Int = 300,

        private val minArrivingGap: Int,
        private val maxArrivingGap: Int,

        private val minAbortingTime: Int,
        private val maxAbortingTime: Int,

        private val logEnable: Boolean = false
) {
    enum class Direction {
        Up,
        Down,
        None
    }

    private class Passenger(
            val arrivalTime: Int,
            val abortedTime: Int,
            val inFloor: Int,
            val outFloor: Int
    ) {
        var state = State.Waiting
        val direction =
                if (outFloor > inFloor) {
                    Direction.Up
                } else if (outFloor < inFloor) {
                    Direction.Down
                } else {
                    throw Exception()
                }

        val id = Random.nextInt()
        val color = Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat())

        enum class State {
            Waiting,                //等待电梯
            WaitingWontAbort,     //即将进入电梯，此时abort事件不生效
            Aborted,                //已放弃
            Leaved,                 //已离开
            Entering,               //正在进入电梯
            Exiting,                //正在离开电梯
            InCabin              //在电梯里
        }
    }

    private class Cabin(
            private val maxLoad: Int,
            private val base: Int
    ) {
        private var state = State.Idling
        var direction = Direction.None
        var nowFloor = base
        private val isCalling = BooleanArray(maxLoad) { false }

        val load = Array<Passenger?>(maxLoad) { null }
        private var nowLoad = 0
        var passengerInOrOut: Passenger? = null

        fun isFull() = nowLoad == maxLoad

        fun add(p: Passenger) {
            if (isFull()) {
                throw Exception()
            }
            isCalling[p.outFloor] = true
            nowLoad++
            load.forEachIndexed { i, passenger ->
                if (passenger == null) {
                    load[i] = p
                    return
                }
            }
        }

        fun delete(p: Passenger) {
            load.forEachIndexed { i, passenger ->
                if (passenger == p) {
                    load[i] = null
                    return
                }
            }
            throw Exception()
        }

        enum class State {
            Idling,         //正在待命
            Opening,        //正在开门
            Detecting,      //处于检测间隔中
            Closing,        //正在关门
            Upping,         //正在上升
            Downing,        //正在下降
            Berthing,       //正在停泊
            IdleUpping,    //正在前往待命状态的上升过程
            IdleDowning    //正在前往待命状态的下降过程
        }
    }


    private abstract class Event(val occurTime: Int)

    /**
     * 乘客事件
     */
    private class PassengerEvent(
            occurTime: Int,
            val type: Type,
            val passenger: Passenger,
            val cabin: Cabin
    ) : Event(occurTime) {
        enum class Type {
            Arrive,                         //到达
            Abort,                          //放弃
            SwitchToWaitingWontAbort,   //转为电梯来临时的等待
            SwitchToWaiting,              //转为正常等待
            StartEntering,                 //开始进入电梯
            EndEntering,                   //完全进入
            StartExiting,                  //开始走出电梯
            EndExiting,                    //完全走出
            Leave                           //离开
        }
    }

    /**
     * 轿厢事件
     */
    private class CabinEvent(
            occurTime: Int,
            val type: Type,
            val cabin: Cabin
    ) : Event(occurTime) {
        enum class Type {
            StartUp,           //开始上升
            EndUp,             //完成上升
            StartDown,         //开始下降
            EndDown,           //完成下降
            StartOpen,         //开始开门
            EndOpen,           //完成开门
            Detect,             //检测是否有人进出
            StartClose,        //开始关门
            EndClose,          //完成关门，只在closing状态生效
            DecideDirection,   //完成关门或者到达新的一层后，决定电梯方向
            StartUpInHalf,      //idle_upping或者idle_downing中途变为upping
            StartDownInHalf,    //idle_upping或者idle_downing中途变为downing
            StartBerth,        //开始停泊
            EndBerth,          //结束停泊，只在berthing状态生效
            StartUpToIdling,      //开始上升去往待命层
            EndUpToIdling,        //到达，只在idle_upping状态生效
            StartDownToIdling,    //开始下降去往待命层
            EndDownToIdling,      //到达，只在idle_downing状态生效
            StartIdling,         //开始待命
        }
    }

    /**
     * 调度事件
     */
    private class ScheduleEvent(
            occurTime: Int,
            val triggerFloor: Int,
            val triggerDirection: Direction
    ) : Event(occurTime)

    //事件队列
    private val events = OrderedLinkedQueue<Event>(
            Comparator<Event> { o1, o2 ->
                o1.occurTime - o2.occurTime
            }
    )

    private fun addPassengerEvent(
            occurTime: Int,
            type: PassengerEvent.Type,
            passenger: Passenger,
            cabin: Cabin
    ) {
        events.add(PassengerEvent(occurTime, type, passenger, cabin))
    }

    private fun addCabinEvent(
            occurTime: Int,
            type: CabinEvent.Type,
            cabin: Cabin
    ) {
        events.add(CabinEvent(occurTime, type, cabin))
    }

    private fun addScheduleEvent(
            occurTime: Int,
            triggerFloor: Int,
            triggerDirection: Direction
    ) {
        events.add(ScheduleEvent(occurTime, triggerFloor, triggerDirection))
    }

    private var time = 0;

    fun nextEvent() {
        if (events.isEmpty) {
            throw Exception()
        } else {
            val e = events.get()
            time = e.occurTime;
            when (e) {
                is PassengerEvent -> {

                }
                is CabinEvent -> {

                }
                is ScheduleEvent -> {

                }
            }
        }
    }


}