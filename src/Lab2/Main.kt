package Lab2

fun main(args: Array<String>) {
    //数据维护部份
    val sim = ElevatorSimulation.getBuilder()
            .setLogEnable(true)
            .setArrivingGap(0, 300)
            .setAbortingGap(1000, 10000)
            .build()
//    val sim = ElevatorSimulation(10, 1, 2, 10,
//            0, 300, 1000, 10000,
//            100, 20, 25,
//            40, 300)
//    val sim = ElevatorSimulator(
//            minArrivingGap = 0,
//            maxArrivingGap = 300,
//            minAbortingTime = 1000,
//            maxAbortingTime = 10000,
//            logEnable = true
//    )
    //启用log
//    sim.setLogEnable(true)
    //GUI部分
    val GUI = GUI(sim, 5,
            100,
            90, 70, 80,
            500,
            50, 10)
    sim.startSimulation()  //启动模拟
    while (true) {
        sim.nextEvent()  //下一个事件
        GUI.refresh()  //更新GUI
    }
}

