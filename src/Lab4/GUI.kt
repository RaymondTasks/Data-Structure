package Lab4

import Graph.MatrixGraph
import java.awt.Graphics
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.image.BufferedImage
import java.io.*
import java.util.*
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

class GUI : JFrame(), ActionListener {

    private val buttonPanel: JPanel
    private val argsPanel: JPanel
    private val resultPanel: JPanel

    private lateinit var open: JButton
    private lateinit var reset: JButton
    private lateinit var dijkstra: JButton
    private lateinit var from: JTextField
    private lateinit var to: JTextField
    private lateinit var result: JLabel

    private lateinit var graph: MatrixGraph

    private var fileChooser: JFileChooser? = null
    private var path: IntArray? = null
    private var showFrame: ShowFrame? = null

    init {
        title = "Dijkstra"
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        setSize(350, 450)
        isResizable = false
        layout = GridLayout(3, 1)
        buttonPanel = JPanel()
        argsPanel = JPanel()
        resultPanel = JPanel()
        add(buttonPanel)
        add(argsPanel)
        add(resultPanel)
        initButtonPanel()
        initArgsPanel()
        initResultPanel()
        fileChooser = JFileChooser()

    }

    private fun initButtonPanel() {
        buttonPanel.layout = null
        open = JButton("OPEN")
        reset = JButton("RESET")
        reset.isEnabled = false
        open.addActionListener(this)
        reset.addActionListener(this)
        open.setBounds(50, 50, 100, 50)
        reset.setBounds(200, 50, 100, 50)
        buttonPanel.setSize(350, 150)
        buttonPanel.add(open)
        buttonPanel.add(reset)
    }

    private fun initArgsPanel() {
        argsPanel.layout = null
        val l1 = JLabel("From:")
        val l2 = JLabel("To:")
        l1.setBounds(30, 30, 70, 30)
        l2.setBounds(30, 90, 370, 30)
        argsPanel.add(l1)
        argsPanel.add(l2)
        from = JTextField()
        to = JTextField()
        from.setBounds(100, 30, 70, 30)
        to.setBounds(100, 90, 70, 30)
        argsPanel.add(from)
        argsPanel.add(to)
        dijkstra = JButton("DIJKSTRA")
        dijkstra.isEnabled = false
        dijkstra.addActionListener(this)
        dijkstra.setBounds(220, 30, 90, 90)
        argsPanel.add(dijkstra)
    }

    private fun initResultPanel() {
        resultPanel.layout = null
        val msg = JLabel("Shortest Path Length:")
        msg.setBounds(30, 50, 150, 30)
        result = JLabel()
        result.setBounds(180, 50, 100, 30)
        resultPanel.add(msg)
        resultPanel.add(result)
    }


    override fun actionPerformed(e: ActionEvent) {
        try {
            when (e.source) {
                open -> open()
                reset -> reset()
                dijkstra -> dijkstra()
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

    }

    @Throws(IOException::class)
    private fun open() {
        if (fileChooser == null) {
            fileChooser = JFileChooser()
            fileChooser!!.dialogTitle = "Select The File to Build Graph"
            fileChooser!!.fileFilter = FileNameExtensionFilter("TEXT File", "txt")
            fileChooser!!.isAcceptAllFileFilterUsed = false
            fileChooser!!.isMultiSelectionEnabled = false
        }
        fileChooser!!.showOpenDialog(this@GUI)
        val fin = fileChooser!!.selectedFile
        if (fin != null) {
            //fin是储存信息的文件
            val sc = Scanner(BufferedReader(FileReader(fin)))
            val n = sc.nextInt()
            val vexs = arrayOfNulls<String>(n)
            //顶命名，此处用默认命名
            for (i in 0 until n) {
                vexs[i] = "V$i"
            }
            val arcs = Array(n) { IntArray(n) }
            for (i in 0 until n) {
                //读取矩阵
                for (j in 0 until n) {
                    arcs[i][j] = sc.nextInt()
                }
                //对角化
                for (j in 0 until i) {
                    arcs[i][j] = arcs[j][i]
                }
            }
            graph = MatrixGraph(vexs, arcs)
            graph.resetLastPath()
            reset.isEnabled = true
            dijkstra.isEnabled = true
            showGraph()
        }
    }

    @Throws(IOException::class)
    private fun reset() {
        graph.resetLastPath()
        showGraph()
    }

    @Throws(IOException::class)
    private fun dijkstra() {
        val a = Integer.valueOf(from.text)
        val b = Integer.valueOf(to.text)
        if (a < 0 || a >= graph.vexNumber || b < 0 || b >= graph.vexNumber) {
            //非法输入
            JOptionPane.showConfirmDialog(this@GUI,
                    "Please input right start and end vertexs.",
                    "Illegal Input!", JOptionPane.OK_OPTION)
            return
        }
        path = graph.Dijkstra(a, b)
        if (path == null) {
            graph.resetLastPath()
            result.text = "Not Connected"
        } else {
            var len = 0
            val arcs = graph.arcs
            for (i in 0 until path!!.size - 1) {
                len += arcs[path!![i]][path!![i + 1]]
            }
            result.text = len.toString()
        }
        showGraph()
    }

    @Throws(IOException::class)
    private fun showGraph() {
        val str = graph.toDotLanguage()
        val tempdir = System.getProperty("java.io.tmpdir")
        val dot = File("$tempdir\\graphviz.dot")
        if (dot.exists()) {
            dot.delete()
        }
        val out = FileWriter(dot)
        out.write(str)
        out.close()

        //生成png文件，写入到系统临时目录下
        val cmd = arrayOf("dot", "-T", "png", "-Gdpi=100", "-o", "$tempdir\\graphviz.png", dot.absolutePath)
        try {
            Runtime.getRuntime().exec(cmd).waitFor()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        //显示图片
        if (showFrame == null) {
            showFrame = ShowFrame("Graph")
        }
        showFrame!!.setImg("$tempdir\\graphviz.png")
        showFrame!!.isVisible = true
    }

}

internal class ShowFrame(title: String) : JFrame(title) {

    private var img: BufferedImage? = null

    init {
        defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE
    }

    @Throws(IOException::class)
    fun setImg(fileName: String) {
        img = ImageIO.read(File(fileName))
        setSize(img!!.width + 14, img!!.height + 39)
        repaint()
    }

    override fun paint(g: Graphics?) {
        super.paint(g)
        g!!.drawImage(img, 7, 30, null)
    }
}
