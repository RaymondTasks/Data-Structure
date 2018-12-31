package Lab5

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import kotlin.random.Random

fun main(args: Array<String>) {
    //生成90个 String Int 对写入文件
    val out = BufferedOutputStream(
            FileOutputStream(
                    File("C:\\Users\\rayomnd\\Desktop\\lab5_data.txt")
            ))
    System.setOut(PrintStream(out))
    for (i in 1..90) {
        println("${Random.nextLong()}\t\t${Random.nextInt()}")
    }
    out.close()
}