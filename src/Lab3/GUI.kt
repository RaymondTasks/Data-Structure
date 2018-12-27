package Lab3

import Tree.HuffmanCodingTree
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.*
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.WindowConstants
import javax.swing.filechooser.FileNameExtensionFilter

fun main(args: Array<String>) {
    GUI().isVisible = true
}

/**
 * 压缩文件储存的信息依次为：文件头，压缩后大小(bit)，原文件名，原大小(byte)，Huffman树，编码后文件流
 */
class GUI : JFrame("Huffman Coding Zipper"), ActionListener {

    private val open = JButton("Open")
    private val unzip = JButton("Unzip")
    private val zip = JButton("Zip")
    private val fileChooser = JFileChooser()

    private lateinit var openedFile: File
    private lateinit var savedFile: File
    private lateinit var HT: HuffmanCodingTree

    private var zippedLength: Long = 0
    private var originalLength: Long = 0
    private lateinit var originalName: String

    private lateinit var zippedInput: DataInputStream

    private val bufferSize = 8 * 1024 * 1024
    private val huffFileHeader = 0x23333333  //文件头

    init {
        isResizable = true
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        setSize(300, 100)
        layout = GridLayout(1, 3)

        add(open.apply {
            isEnabled = true
            addActionListener(this@GUI)
        })
        add(zip.apply {
            isEnabled = false
            addActionListener(this@GUI)
        })
        add(unzip.apply {
            isEnabled = false
            addActionListener(this@GUI)
        })

        fileChooser.apply {
            fileSelectionMode = JFileChooser.FILES_ONLY
            isMultiSelectionEnabled = false
        }
    }

    override fun actionPerformed(e: ActionEvent) {
        when (e.source) {
            open -> open()
            zip -> zip()
            unzip -> unzip()
        }
    }

    /**
     * 打开文件
     */
    private fun open() {

        openedFile = fileChooser
                .apply {
                    dialogTitle = "Open A File to Zip or Unzip"
                    resetChoosableFileFilters()
                    addChoosableFileFilter(FileNameExtensionFilter(
                            "Huffman Coding Zipped File (*.huff)", "huff"))
                    showOpenDialog(this@GUI)
                }
                .selectedFile ?: return

        if (!openedFile.exists()) {
            //未选中文件或者选中的文件不存在
            return
        }
        zip.isEnabled = true

        zippedInput = DataInputStream(
                BufferedInputStream(
                        FileInputStream(openedFile), bufferSize))
        //分析文件信息
        if (zippedInput.readInt() == huffFileHeader) {  //huff压缩文件
            unzip.isEnabled = true
            //读取压缩文件的信息
            zippedInput.apply {
                zippedLength = readLong()
                originalName = readUTF()
                originalLength = readLong()
                //从压缩文件恢复Huffman Tree
                HT = HuffmanCodingTree()
                HT.restoreFromFile(this)
            }
            //log
            println("压缩文件信息：")
            println("压缩文件名： ${openedFile.name}")
            println("原文件名： $originalName")
            println("原大小： $originalLength bytes")
            println("压缩后大小： ${openedFile.length()} bytes")
            println("压缩后有效数据流大小： ${zippedLength / 8} bytes")
            println("压缩率： ${100.0 * openedFile.length() / originalLength} %")
            println()
            //log end

        } else {  //一般文件
            unzip.isEnabled = false
            zippedInput.close()
            //log
            println("文件信息：")
            println("文件名： ${openedFile.name}")
            println("文件大小： ${openedFile.length()} bytes")
            println()
            //log end

        }

    }

    /**
     * 压缩文件
     */
    private fun zip() {

        savedFile = fileChooser
                .apply {
                    dialogTitle = "Save Zipped File"
                    resetChoosableFileFilters()
                    isAcceptAllFileFilterUsed = true
                    fileFilter = FileNameExtensionFilter(
                            "Huffman Coding Zipped File (*.huff)", "huff")
                    currentDirectory = openedFile.parentFile
                    selectedFile = File("${openedFile.name}.huff")
                    showSaveDialog(this@GUI)
                }
                .selectedFile ?: return

        var t = System.currentTimeMillis()

        //创建HuffmanCodingTree
        DataInputStream(
                BufferedInputStream(
                        FileInputStream(openedFile), bufferSize)).apply {
            HT = HuffmanCodingTree(this)
            close()
        }

        val output = DataOutputStream(
                BufferedOutputStream(
                        FileOutputStream(savedFile), bufferSize)).apply {
            writeInt(huffFileHeader)    //文件头
            writeLong(0)             //预留压缩后大小的位置
            writeUTF(openedFile.name)   //原文件名
            writeLong(openedFile.length())  //原大小
            HT.saveToFile(this)     //储存Huffman树
        }

        var length: Long
        DataInputStream(
                BufferedInputStream(
                        FileInputStream(openedFile), bufferSize)).apply {
            length = HT.encode(this, output)       //输出压缩文件
            close()
        }
        output.close()

        //写入压缩数据流的bit长度
        RandomAccessFile(savedFile, "rw").apply {
            skipBytes(4)
            writeLong(length)
            close()
        }

        t = System.currentTimeMillis() - t
        //log
        println("压缩信息：")
        println("原文件名： ${openedFile.name}")
        println("原大小： ${openedFile.length()} bytes")
        println("压缩后文件名： ${savedFile.name}")
        println("压缩后大小： ${savedFile.length()} bytes")
        println("压缩后有效数据流大小： ${length / 8} bytes")
        println("压缩率： ${100.0 * savedFile.length() / openedFile.length()} %")
        println("压缩时间： $t ms")
        println("压缩速率： ${1000.0 / 1024.0 / 1024.0 * openedFile.length() / t} MB/s")
        println()
        //log end
    }

    /**
     * 解压文件
     */
    private fun unzip() {

        savedFile = fileChooser
                .apply {
                    dialogTitle = "Save Unzipped File"
                    resetChoosableFileFilters()
                    currentDirectory = openedFile.parentFile
                    selectedFile = File(originalName)
                    showSaveDialog(this@GUI)
                }
                .selectedFile ?: return

        var t = System.currentTimeMillis()

        DataOutputStream(
                BufferedOutputStream(
                        FileOutputStream(savedFile), bufferSize)).apply {
            HT.decode(zippedInput, zippedLength, this)
            close()
        }
        zippedInput.close()

        t = System.currentTimeMillis() - t
        //log
        println("解压信息：")
        println("解压文件名： ${savedFile.name}")
        println("解压大小： ${savedFile.length()} bytes")
        println("解压时间： $t ms")
        println("解压速率： ${1000.0 / 1024.0 / 1024.0 * openedFile.length() / t} MB/s")
        println()
        //log end
    }

}
