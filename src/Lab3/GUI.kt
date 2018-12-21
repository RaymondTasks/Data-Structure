package Lab3

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
    private var openedFile: File? = null
    private var savedFile: File? = null
    private lateinit var HT: HuffmanCodingTree

    private var zippedLength: Long = 0
    private var originalLength: Long = 0
    private lateinit var originalName: String

    private var zippedInput: DataInputStream? = null

    init {
        isResizable = true
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        setSize(300, 100)

        open.addActionListener(this)
        zip.addActionListener(this)
        unzip.addActionListener(this)
        open.isEnabled = true
        zip.isEnabled = false
        unzip.isEnabled = false
        layout = GridLayout(1, 3)
        add(open)
        add(zip)
        add(unzip)

        fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
        fileChooser.isMultiSelectionEnabled = false
    }

    override fun actionPerformed(e: ActionEvent) {
        try {
            when (e.source) {
                open -> open()
                zip -> zip()
                unzip -> unzip()
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

    }

    /**
     * 打开文件
     */
    @Throws(IOException::class)
    private fun open() {
        fileChooser.dialogTitle = "Open A File to Zip or Unzip"
        fileChooser.resetChoosableFileFilters()
        fileChooser.addChoosableFileFilter(FileNameExtensionFilter(
                "Huffman Coding Zipped File (*.huff)", "huff"))
        fileChooser.showOpenDialog(this@GUI)
        openedFile = fileChooser.selectedFile
        if (openedFile == null || !openedFile!!.exists()) {
            //未选中文件或者选中的文件不存在
            return
        }
        zip.isEnabled = true

        //显示文件信息
        zippedInput = DataInputStream(BufferedInputStream(FileInputStream(openedFile), bufferSize))

        if (zippedInput!!.readInt() == huffFileHeader) {  //huff压缩文件
            unzip.isEnabled = true
            //读取压缩文件的信息
            zippedLength = zippedInput!!.readLong()
            originalName = zippedInput!!.readUTF()
            originalLength = zippedInput!!.readLong()
            //从压缩文件恢复Huffman Tree
            HT = HuffmanCodingTree()
            HT.restoreFromFile(zippedInput!!)
            //log
            println("压缩文件信息：")
            println("压缩文件名： ${openedFile!!.name}")
            println("原文件名： $originalName")
            println("原大小： $originalLength bytes")
            println("压缩后大小： ${openedFile!!.length()} bytes")
            println("压缩后有效数据流大小： ${zippedLength / 8} bytes")
            println("压缩率： ${100.0 * openedFile!!.length() / originalLength} %")
            println()
            //log end
        } else {  //一般文件
            unzip.isEnabled = false
            zippedInput!!.close()
            zippedInput = null
            //log
            println("文件信息：")
            println("文件名： ${openedFile!!.name}")
            println("文件大小： ${openedFile!!.length()} bytes")
            println()
            //log end
        }
    }

    /**
     * 压缩文件
     */
    @Throws(IOException::class)
    private fun zip() {
        fileChooser.dialogTitle = "Save Zipped File"
        fileChooser.resetChoosableFileFilters()
        fileChooser.isAcceptAllFileFilterUsed = true
        fileChooser.fileFilter = FileNameExtensionFilter(
                "Huffman Coding Zipped File (*.huff)", "huff")
        fileChooser.currentDirectory = openedFile!!.parentFile
        fileChooser.selectedFile = File("${openedFile!!.name}.huff")
        fileChooser.showSaveDialog(this@GUI)
        savedFile = fileChooser.selectedFile
        if (savedFile == null) {
            return
        }
        if (savedFile!!.exists()) {
            savedFile!!.delete()
        }
        savedFile!!.createNewFile()

        var t = System.currentTimeMillis()

        //创建HuffmanCodingTree
        var input = DataInputStream(BufferedInputStream(FileInputStream(openedFile!!), bufferSize))
        HT = HuffmanCodingTree(input)
        input.close()

        val output = DataOutputStream(BufferedOutputStream(FileOutputStream(savedFile!!), bufferSize))

        output.writeInt(huffFileHeader)  //文件头
        output.writeLong(0)  //预留压缩后大小的位置
        output.writeUTF(openedFile!!.name)  //原文件名
        output.writeLong(openedFile!!.length())  //原大小
        HT.saveToFile(output)  //储存Huffman树

        input = DataInputStream(BufferedInputStream(FileInputStream(openedFile!!), bufferSize))
        val length = HT.encode(input, output)
        input.close()
        output.close()

        val randomAccessFile = RandomAccessFile(savedFile!!, "rw")
        randomAccessFile.skipBytes(4)
        randomAccessFile.writeLong(length)
        randomAccessFile.close()

        t = System.currentTimeMillis() - t
        //log
        println("压缩信息：")
        println("原文件名： ${openedFile!!.name}")
        println("原大小： ${openedFile!!.length()} bytes")
        println("压缩后文件名： ${savedFile!!.name}")
        println("压缩后大小： ${savedFile!!.length()} bytes")
        println("压缩后有效数据流大小： ${length / 8} bytes")
        println("压缩率： ${100.0 * savedFile!!.length() / openedFile!!.length()} %")
        println("压缩时间： $t ms")
        println("压缩速率： ${1000.0 / 1024.0 / 1024.0 * openedFile!!.length() / t} MB/s")
        println()
        //log end
    }

    /**
     * 解压文件
     */
    @Throws(IOException::class)
    private fun unzip() {
        fileChooser.dialogTitle = "Save Unzipped File"
        fileChooser.resetChoosableFileFilters()
        fileChooser.currentDirectory = openedFile!!.parentFile
        fileChooser.selectedFile = File(originalName)
        fileChooser.showSaveDialog(this@GUI)
        savedFile = fileChooser.selectedFile
        if (savedFile!!.exists()) {
            savedFile!!.delete()
        }
        savedFile!!.createNewFile()

        var t = System.currentTimeMillis()

        val output = DataOutputStream(BufferedOutputStream(FileOutputStream(savedFile!!), bufferSize))
        HT.decode(zippedInput!!, zippedLength, output)
        zippedInput!!.close()
        output.close()

        t = System.currentTimeMillis() - t
        //log
        println("解压信息：")
        println("解压文件名： ${savedFile!!.name}")
        println("解压大小： ${savedFile!!.length()} bytes")
        println("解压时间： $t ms")
        println("解压速率： ${1000.0 / 1024.0 / 1024.0 * openedFile!!.length() / t} MB/s")
        println()
        //log end
    }

    private val bufferSize = 8 * 1024 * 1024
    private val huffFileHeader = 0x23333333  //文件头

}
