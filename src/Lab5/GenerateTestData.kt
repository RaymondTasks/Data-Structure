package Lab5

import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

fun main(args: Array<String>) {
    //生成90个 String Int 对写入文件

    DataOutputStream(
            FileOutputStream(
                    File("C:\\Users\\rayomnd\\Desktop\\lab5_data.txt")))
            .apply {
                for (i in 1..90) {
                    writeUTF("${Random.nextLong()}\t\t${Random.nextInt()}")
                }
            }

}