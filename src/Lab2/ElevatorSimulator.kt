package Lab2

import Queue.OrderedLinkedQueue
import java.awt.Color
import java.util.*
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

    private enum class Direction {
        Up,
        Down,
        None
    }

    private enum class PassengerState {
        Waiting,                //等待电梯
        WaitingWontAbort,     //即将进入电梯，此时abort事件不生效
        Aborted,                //已放弃
        Leaved,                 //已离开
        Entering,               //正在进入电梯
        Exiting,                //正在离开电梯
        InCabin              //在电梯里
    }

    private enum class CabinState {
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

    private enum class PassengerEventType {
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

    private enum class CabinEventType {
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

    /**
     * 事件
     */
    private interface Event {
        val time: Int
    }

    /**
     * 乘客事件
     */
    private data class PassengerEvent(
            override val time: Int,
            val type: PassengerEventType,
            val passenger: Passenger,
            val cabin: Cabin?
    ) : Event

    /**
     * 电梯厢事件
     */
    private data class CabinEvent(
            override val time: Int,
            val type: CabinEventType,
            val cabin: Cabin
    ) : Event

    /**
     * 调度事件
     */
    private data class ScheduleEvent(
            override val time: Int,
            val triggerFloor: Int,
            val triggerDirection: Direction
    ) : Event

    /**
     * 乘客类
     */
    private inner class Passenger(
            val arrivalTime: Int,
            val abortedTime: Int,
            val inFloor: Int,
            val outFloor: Int
    ) {
        var state = PassengerState.Waiting
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

        fun arrive() {
            if (logEnable) {
                //todo
            }
            val nextp = getRandomPassenger()
            addPassengerEvent(
                    time = nextp.arrivalTime,
                    type = PassengerEventType.Arrive,
                    passenger = nextp
            )

            //是否因为电梯满员不能按按钮
            var canPressButton = true

            //寻找开着的，方向正确的，未满的电梯
            for (c in cabins) {
                if (c.nowFloor == inFloor && c.direction == direction
                        && c.state == CabinState.Detecting) {
                    if (c.isFull()) {
                        canPressButton = false
                    } else {
                        if (c.passengerInOrOut == null) {
                            //电梯没有人进出，直接进入电梯
                            addPassengerEvent(
                                    time = time,
                                    passenger = this,
                                    cabin = c,
                                    type = PassengerEventType.StartEntering
                            )
                        } else {
                            //有人进出中，排队
                            state = PassengerState.WaitingWontAbort
                            queues[inFloor].addLast(this)
                        }
                        return
                    }
                }
            }

            //寻找方向正确的正在开门的电梯
            for (c in cabins) {
                if (c.nowFloor == inFloor && c.direction == direction
                        && c.state == CabinState.Opening) {
                    state = PassengerState.WaitingWontAbort
                    queues[inFloor].addLast(this)
                    return
                }
            }

            //寻找方向正确的正在关门的电梯
            for (c in cabins) {
                if (c.nowFloor == inFloor && c.direction == direction
                        && c.state == CabinState.Closing) {
                    if (c.isFull()) {
                        canPressButton = false
                    } else {
                        state = PassengerState.WaitingWontAbort
                        queues[inFloor].addLast(this)
                        // 取消电梯关门事件
                        c.reopen()
                        return
                        //todo 可能有一些问题
                    }
                }
            }

            //以上各种情况都不存在
            state = PassengerState.Waiting
            queues[inFloor].addLast(this)
            if (canPressButton) {
                when (direction) {
                    Direction.Up -> {
                        if (!callingUp[inFloor]) {
                            callingUp[inFloor] = true
                            addScheduleEvent(
                                    time = time,
                                    triggerFloor = inFloor,
                                    triggerDirection = Direction.Up
                            )
                        }
                    }
                    Direction.Down -> {
                        if (!callingDown[inFloor]) {
                            callingDown[inFloor] = true
                            addScheduleEvent(
                                    time = time,
                                    triggerFloor = inFloor,
                                    triggerDirection = Direction.Down
                            )
                        }
                    }
                    else -> throw Exception()
                }
            }
        }

        fun abort() {
            if (state == PassengerState.Waiting) {
                if (logEnable) {
                    //todo
                }
                state = PassengerState.Aborted
                queues[inFloor].remove(this)
                addPassengerEvent(
                        time = time,
                        passenger = this,
                        type = PassengerEventType.Leave
                )
            }
        }

        fun switchToWaiting(willAbort: Boolean = true) {
            if (willAbort) {
                if (time > arrivalTime + abortedTime) {
                    addPassengerEvent(
                            time = time,
                            passenger = this,
                            type = PassengerEventType.Abort
                    )
                } else {
                    state = PassengerState.Waiting
                }
            } else {
                state = PassengerState.WaitingWontAbort
            }
        }

        fun startEntering(c: Cabin) {
            if (logEnable) {
                //todo
            }
            state = PassengerState.Entering
            c.passengerInOrOut = this
            queues[inFloor].remove(this)
            addPassengerEvent(
                    time = time + enterAndExitTime,
                    passenger = this,
                    cabin = c,
                    type = PassengerEventType.EndEntering
            )
        }

        fun endEntering(c: Cabin) {
            state = PassengerState.InCabin
            c.add(this)
            c.passengerInOrOut = null
            val nextp = c.getNextForEntering()
            if (nextp != null) {
                addPassengerEvent(
                        time = time,
                        passenger = nextp,
                        cabin = c,
                        type = PassengerEventType.StartEntering
                )
            }
        }

        fun startExiting(c: Cabin) {
            if (logEnable) {
                //todo
            }
            state = PassengerState.Exiting
            c.delete(this)
            c.passengerInOrOut = this
            addPassengerEvent(
                    time = time + enterAndExitTime,
                    passenger = this,
                    cabin = c,
                    type = PassengerEventType.EndExiting
            )
        }

        fun endExiting(c: Cabin) {
            c.passengerInOrOut = null
            addPassengerEvent(
                    time = time,
                    passenger = this,
                    type = PassengerEventType.Leave
            )
            var nextp = c.getNextForExiting()
            if (nextp == null) {
                nextp = c.getNextForEntering()
                if (nextp != null) {
                    addPassengerEvent(
                            time = time,
                            passenger = nextp,
                            cabin = c,
                            type = PassengerEventType.StartEntering
                    )
                }
            } else {
                addPassengerEvent(
                        time = time,
                        passenger = nextp,
                        cabin = c,
                        type = PassengerEventType.StartExiting
                )
            }
        }

        fun leave() {
            if (logEnable) {
                //todo
            }
            state = PassengerState.Leaved
        }
    }

    /**
     * 电梯厢类
     */
    private inner class Cabin {
        var state = CabinState.Idling
        var direction = Direction.None
        var nowFloor = base
        val isCalling = BooleanArray(floors) { false }

        val load = Array<Passenger?>(maxLoad) { null }
        var nowLoad = 0
        var passengerInOrOut: Passenger? = null

        fun isFull() = nowLoad == maxLoad

        /**
         * 增加乘客
         */
        fun add(p: Passenger) {
            if (isFull()) {
                throw Exception()   //todo
            }
            isCalling[p.outFloor] = true
            for (i in load.indices) {
                if (load[i] == null) {
                    load[i] = p
                    nowLoad++
                    return
                }
            }
        }

        /**
         * 删除乘客
         */
        fun delete(p: Passenger) {
            for (i in load.indices) {
                if (load[i] == p) {
                    load[i] = null
                    nowLoad--
                    return
                }
            }
            throw Exception()   //todo
        }

        /**
         * 获得下一个要出去的乘客
         */
        fun getNextForExiting(): Passenger? {
            load.forEach { p ->
                if (p != null && p.outFloor == nowFloor) {
                    return p
                }
            }
            return null
        }

        /**
         * 获得下一个要进入电梯的乘客
         */
        fun getNextForEntering(): Passenger? {
            if (isFull()) {
                return null
            }
            queues[nowFloor].forEach { p ->
                if (p.direction == direction) {
                    return p
                }
            }
            return null
        }

        var startingTimeOpenOrClose: Int = 0  //用于中断关门后生成新的开门事件
        var startingTimeUpOrDown: Int = 0  //用于中断前往待命状态的移动事件

        fun reopen() {

        }

        /**
         * 获得浮点表示的具体位置
         */
        fun getPosition() = when (state) {
            CabinState.Upping, CabinState.IdleUpping ->
                nowFloor.toDouble() +
                        (time - startingTimeUpOrDown).toDouble() / upAndDownTime
            CabinState.Downing, CabinState.IdleDowning ->
                nowFloor.toDouble() -
                        (time - startingTimeUpOrDown).toDouble() / upAndDownTime
            else -> nowFloor.toDouble()
        }

    }

    //事件队列
    private val events = OrderedLinkedQueue<Event>(
            Comparator { o1, o2 ->
                o1.time - o2.time
            }
    )

    /**
     * 增加一个乘客事件
     */
    private fun addPassengerEvent(
            time: Int,
            type: PassengerEventType,
            passenger: Passenger,
            cabin: Cabin? = null
    ) {
        events.add(PassengerEvent(
                time = time,
                type = type,
                passenger = passenger,
                cabin = cabin))
    }

    /**
     * 增加一个电梯厢事件
     */
    private fun addCabinEvent(
            time: Int,
            type: CabinEventType,
            cabin: Cabin
    ) {
        events.add(CabinEvent(
                time = time,
                type = type,
                cabin = cabin))
    }

    /**
     * 增加一个调度事件
     */
    private fun addScheduleEvent(
            time: Int,
            triggerFloor: Int,
            triggerDirection: Direction
    ) {
        events.add(ScheduleEvent(
                time = time,
                triggerDirection = triggerDirection,
                triggerFloor = triggerFloor))
    }

    // 所有电梯厢
    private val cabins = Array(parallelNumber) { Cabin() }
    // 所有乘客队列
    private val queues = Array(floors) { LinkedList<Passenger>() }
    // 上升按钮
    private val callingUp = BooleanArray(floors) { false }
    // 下降按钮
    private val callingDown = BooleanArray(floors) { false }

    // 全局时钟
    private var time = 0

    // 获得随机乘客
    private fun getRandomPassenger(): Passenger {
        val inFloor = Random.nextInt(floors)
        var outFloor: Int
        do {
            outFloor = Random.nextInt(floors)
        } while (inFloor == outFloor)
        return Passenger(
                arrivalTime = time + Random.nextInt(minArrivingGap, maxArrivingGap + 1),
                abortedTime = Random.nextInt(minAbortingTime, maxAbortingTime + 1),
                inFloor = inFloor,
                outFloor = outFloor
        )
    }

    // 开始
    fun start() {
        val p = getRandomPassenger()
        addPassengerEvent(
                time = p.arrivalTime,
                type = PassengerEventType.Arrive,
                passenger = p
        )
    }

    //下一个事件
    fun nextEvent() {
        if (events.isEmpty) {
            throw Exception()   //todo
        } else {
            val e = events.get()
            time = e.time
            when (e) {
                is PassengerEvent -> {
                    val p = e.passenger
                    val c = e.cabin
                    when (e.type) {
                        PassengerEventType.Arrive -> p.arrive()
                        PassengerEventType.Abort -> p.abort()
                        PassengerEventType.SwitchToWaiting -> p.switchToWaiting()
                        PassengerEventType.SwitchToWaitingWontAbort ->
                            p.switchToWaiting(false)
                        PassengerEventType.StartEntering -> p.startEntering(c!!)
                        PassengerEventType.EndEntering -> p.endEntering(c!!)
                        PassengerEventType.StartExiting -> p.startExiting(c!!)
                        PassengerEventType.EndExiting -> p.endExiting(c!!)
                        PassengerEventType.Leave -> p.leave()
                    }
                }
                is CabinEvent -> {
                    val c = e.cabin
                }
                is ScheduleEvent -> {

                }
            }
        }
    }

}