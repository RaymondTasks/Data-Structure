package Lab5

import Table.HashTable
import Table.HashTableLinearDetection
import Table.HashTableLinkedList
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.util.*
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.collections.ArrayList

fun main(args: Array<String>) {
    GUI().isVisible = true
}

/**
 * 从文件中读取数据构建hash表
 */
class GUI : JFrame("Hash Table"), ActionListener {

    private val open = JButton("Open")
    private val calcASL = JButton("Calculate ASL")
    private val search = JButton("Search")

    //HT1是用线性探测法解决冲突的hash表
    private lateinit var HT1: HashTableLinearDetection<String, Int>
    //HT2是用链表法解决冲突的hash表
    private lateinit var HT2: HashTableLinkedList<String, Int>

    //文件选择框
    private val fileChooser = JFileChooser()

    //显示ASL计算结果
    private val ASLInfo1 = JLabel()
    private val ASLInfo2 = JLabel()

    //显示查找输入的key的比较次数
    private val SLInfo1 = JLabel()
    private val SLInfo2 = JLabel()

    //输入key的文本框
    private val keyInput = JTextField()

    init {
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        isResizable = false
        layout = GridLayout(4, 1)
        setSize(500, 600)

        //打开和计算ASL按钮的面板
        add(JPanel(null).apply {
            add(open.apply {
                isEnabled = true
                addActionListener(this@GUI)
                setBounds(50, 40, 100, 70)
            })
            add(calcASL.apply {
                isEnabled = false
                addActionListener(this@GUI)
                setBounds(200, 40, 100, 70)
            })
        })

        //显示ASL信息的面板
        add(JPanel(null).apply {
            add(ASLInfo1.apply {
                setBounds(25, 45, 450, 30)
            })
            add(ASLInfo2.apply {
                setBounds(25, 75, 450, 30)
            })
        })

        //输入key的面板
        add(JPanel(null).apply {
            add(JLabel("Input key:").apply {
                setBounds(25, 45, 100, 30)
            })
            add(keyInput.apply {
                setBounds(25, 75, 150, 30)
            })
            add(search.apply {
                isEnabled = false
                addActionListener(this@GUI)
                setBounds(200, 45, 100, 60)
            })
        })

        //输出搜索信息的面板
        add(JPanel(null).apply {
            add(SLInfo2.apply {
                setBounds(25, 45, 450, 30)
            })
            add(SLInfo1.apply {
                setBounds(25, 75, 450, 30)
            })
        })

        fileChooser.apply {
            dialogTitle = "Open Data File"
            isMultiSelectionEnabled = false
            isAcceptAllFileFilterUsed = false
            fileFilter = FileNameExtensionFilter("Text File (*.txt)",
                    "txt")
        }

    }

    override fun actionPerformed(e: ActionEvent) {
        when (e.source) {
            open -> open()
            search -> search()
            calcASL -> calcASL()
        }
    }

    //用于计算查找成功的ASL
    private val keyList = ArrayList<String>()

    /**
     * 打开文件，生成HT1和HT2
     */
    private fun open() {
        val fin = fileChooser.apply {
            showOpenDialog(this@GUI)
        }.selectedFile ?: return

        if (!fin.exists()) {
            //文件不存在
            return
        }

        val sc = Scanner(BufferedInputStream(FileInputStream(fin), 4096))
        //默认容量是100
        HT1 = HashTableLinearDetection()
        HT2 = HashTableLinkedList()
        keyList.clear()
        while (sc.hasNext()) {
            val key = sc.next()
            val element = sc.nextInt()
            keyList.add(key)
            HT1.put(key, element)
            HT2.put(key, element)
        }
        calcASL.isEnabled = true
        search.isEnabled = true
    }

    /**
     * 根据输入的key查找，显示查找长度
     */
    private fun search() {
        val key = keyInput.text
        try {
            //查找成功
            SLInfo1.text = "线性探测法：查找成功，element=${HT1.get(key)}，SL=${HT1.SL}"
        } catch (e: Exception) {
            //查找失败
            SLInfo1.text = "线性探测法：查找失败，SL=${HT1.SL}"
        }
        try {
            //查找成功
            SLInfo2.text = "拉链法：查找成功，element=${HT2.get(key)}，SL=${HT2.SL}"
        } catch (e: Exception) {
            //查找失败
            SLInfo2.text = "拉链法：查找失败，SL=${HT2.SL}"
        }
    }

    /**
     * 计算过两种解决冲突办法的ASL
     */
    private fun calcASL() {
        ASLInfo1.text = "线性探测法：查找成功ASL=${calcSuccessASL(HT1)}，" +
                "失败ASL=${calcFailedASL(HT1)}"
        ASLInfo2.text = "拉链法：查找成功ASL=${calcSuccessASL(HT2)}，" +
                "失败ASL=${calcFailedASL(HT2)}"
    }

    /**
     * 计算查找成功的ASL
     */
    private fun calcSuccessASL(table: HashTable<String, Int>): Double {
        var SL = 0
        keyList.forEach {
            table.get(it)
            SL += table.SL
        }
        return SL.toDouble() / keyList.size
    }

    /**
     * 随机生成不在keyList中的字符串100个
     * 计算查找失败的ASL
     */
    private fun calcFailedASL(table: HashTable<String, Int>): Double {
        var SL = 0
        val random = java.util.Random()
        val all = 100
        for (i in 1..all) {
            lateinit var key: String
            do {
                key = random.nextInt().toString()
            } while (key in keyList)
            try {
                table.get(key)
            } catch (e: Exception) {
                //key不存在
            } finally {
                SL += table.SL
            }
        }
        return SL.toDouble() / all
    }
}