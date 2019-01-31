package Lab4

import Graph.MatrixGraph
import java.awt.Graphics
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

fun main(args: Array<String>) {
    GUI().isVisible = true
}

class GUI : JFrame("dijkstra"), ActionListener {

    private val open = JButton("Open")
    private val reset = JButton("Reset")
    private val dijkstra = JButton("dijkstra")

    private val fileChooser = JFileChooser()
    private val showFrame = ShowFrame()

    private val from = JTextField()
    private val to = JTextField()
    private val result = JLabel()

    private lateinit var graph: MatrixGraph

    private lateinit var path: IntArray

    init {
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        setSize(350, 450)
        isResizable = false
        layout = GridLayout(3, 1)

        //初始化按钮面板
        add(JPanel(null).apply {
            add(open.apply {
                addActionListener(this@GUI)
                setBounds(50, 50, 100, 50)
            })
            add(reset.apply {
                isEnabled = false
                addActionListener(this@GUI)
                setBounds(200, 50, 100, 50)
            })
        })

        //参数输入面板
        add(JPanel(null).apply {
            add(JLabel("From:").apply {
                setBounds(30, 30, 70, 30)
            })
            add(JLabel("To:").apply {
                setBounds(30, 90, 370, 30)
            })
            add(from.apply {
                setBounds(100, 30, 70, 30)
            })
            add(to.apply {
                setBounds(100, 90, 70, 30)
            })
            add(dijkstra.apply {
                isEnabled = false
                addActionListener(this@GUI)
                setBounds(220, 30, 90, 90)
            })
        })

        //结果显示面板
        add(JPanel(null).apply {
            add(JLabel("Shortest Path Length:").apply {
                setBounds(30, 50, 150, 30)
            })
            add(result.apply {
                setBounds(180, 50, 100, 30)
            })
        })

        fileChooser.apply {
            dialogTitle = "Select The File to Build Graph"
            fileFilter = FileNameExtensionFilter("Text File", "txt")
            isAcceptAllFileFilterUsed = false
            isMultiSelectionEnabled = false
        }
    }

    override fun actionPerformed(e: ActionEvent) {
        when (e.source) {
            open -> open()
            reset -> reset()
            dijkstra -> dijkstra()
        }
    }

    private fun open() {

        val fin = fileChooser.apply {
            showOpenDialog(this@GUI)
        }.selectedFile ?: return

        //fin是储存信息的文件
        val sc = Scanner(BufferedReader(FileReader(fin)))
        //顶数量
        val n = sc.nextInt()
        //顶信息，使用默认命名
        val vexs = Array(n) { i -> "V$i" }
        //边长度
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

        reset.isEnabled = true
        dijkstra.isEnabled = true
        showGraph()

    }

    private fun reset() {
        graph.resetLastPath()
        showGraph()
    }

    private fun dijkstra() {
        val a = from.text.toInt()
        val b = to.text.toInt()

        if (a in 0 until graph.getVexNumber() && b in 0 until graph.getVexNumber()) {
            path = graph.dijkstra(a, b)
            if (path.isEmpty()) {
                result.text = "Not Connected"
            } else {
                var len = 0
                val arcs = graph.arcs
                for (i in 0 until path.size - 1) {
                    len += arcs[path[i]][path[i + 1]]
                }
                result.text = len.toString()
            }
            showGraph()
        } else {
            //非法输入
            JOptionPane.showConfirmDialog(this@GUI,
                    "Please input right start and end vertexs.",
                    "Illegal Input!", JOptionPane.YES_OPTION)
        }

    }

    private fun showGraph() {
        val tempdir = System.getProperty("java.io.tmpdir")

        FileWriter("$tempdir\\graphviz.dot").apply {
            write(graph.toDotLanguage())
            close()
        }

        //生成png文件，写入到系统临时目录下
        Runtime.getRuntime().exec(
                arrayOf("dot", "-T", "png", "-Gdpi=100",
                        "-o", "$tempdir\\graphviz.png",
                        "$tempdir\\graphviz.dot")
        ).waitFor()

        //显示图片
        showFrame.apply {
            isVisible = true
            setImg("$tempdir\\graphviz.png")
        }
    }

}

internal class ShowFrame : JFrame("Graph") {

    private var img: BufferedImage? = null

    init {
        defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE
    }

    fun setImg(fileName: String) {
        img = ImageIO.read(File(fileName))
        setSize(img!!.width + 14, img!!.height + 39)
        repaint()
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        g.drawImage(img, 7, 30, null)
    }
}
