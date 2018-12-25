package Lab5

import HashTable.HashTable
import HashTable.HashTableLinearDetection
import HashTable.HashTableLinkedList
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.text.DecimalFormat
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

    //分别是HT1和HT2的ASL
    private var ASL1: Int = -1
    private var ASL2: Int = -1

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

        open.addActionListener(this)
        calcASL.addActionListener(this)
        calcASL.isEnabled = false
        search.addActionListener(this)
        search.isEnabled = false

        fileChooser.dialogTitle = "Open Data File"
        fileChooser.isMultiSelectionEnabled = false
        fileChooser.isAcceptAllFileFilterUsed = false
        fileChooser.fileFilter = FileNameExtensionFilter("Text File (*.txt)",
                "txt")

        //初始化布局
        layout = GridLayout(4, 1)
        setSize(500, 600)

        //打开和计算ASL按钮的面板
        val openPanel = JPanel()
        openPanel.layout = null
        open.setBounds(50, 40, 100, 70)
        calcASL.setBounds(200, 40, 100, 70)
        openPanel.add(open)
        openPanel.add(calcASL)
        add(openPanel)

        //显示ASL信息的面板
        val ASLPanel = JPanel()
        ASLPanel.layout = null
        ASLInfo1.setBounds(25, 45, 300, 30)
        ASLInfo2.setBounds(25, 75, 300, 30)
        ASLPanel.add(ASLInfo1)
        ASLPanel.add(ASLInfo2)
        add(ASLPanel)

        //输入key的面板
        val inputPanel = JPanel()
        inputPanel.layout = null
        val hint = JLabel("Input key:")
        hint.setBounds(25, 45, 100, 30)
        keyInput.setBounds(25, 75, 150, 30)
        search.setBounds(200, 45, 100, 60)
        inputPanel.add(hint)
        inputPanel.add(keyInput)
        inputPanel.add(search)
        add(inputPanel)

        //输出搜索信息的面板
        val SLPanel = JPanel()
        SLPanel.layout = null
        SLInfo1.setBounds(25, 45, 450, 30)
        SLInfo2.setBounds(25, 75, 450, 30)
        SLPanel.add(SLInfo1)
        SLPanel.add(SLInfo2)
        add(SLPanel)

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
        fileChooser.showOpenDialog(this@GUI)
        val fin = fileChooser.selectedFile
        if (fin == null || !fin.exists()) {
            //未选中或者文件不存在
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
//        val format = DecimalFormat("#.00")
        try {
            val elem = HT1.get(key)
            //查找成功
            SLInfo1.text = "线性探测法：查找成功，element=$elem，SL=${HT1.SL}"
        } catch (e: Exception) {
            SLInfo1.text = "线性探测法：查找失败，SL=${HT1.SL}"
        }
        try {
            val elem = HT2.get(key)
            //查找成功
            SLInfo2.text = "拉链法：查找成功，element=$elem，SL=${HT2.SL}"
        } catch (e: Exception) {
            SLInfo2.text = "拉链法：查找失败，SL=${HT2.SL}"
        }
    }

    /**
     * 计算过两种解决冲突办法的ASL
     */
    private fun calcASL() {
        var successASL = calcSuccessASL(HT1)
        var filedASL = calcFailedASL(HT1)
        val format = DecimalFormat("#.00")
        ASLInfo1.text = "线性探测法：查找成功ASL=${format.format(successASL)}，失败ASL=${format.format(filedASL)}"
        successASL = calcSuccessASL(HT2)
        filedASL = calcFailedASL(HT2)
        ASLInfo2.text = "拉链法：查找成功ASL=${format.format(successASL)}，失败ASL=${format.format(filedASL)}"
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