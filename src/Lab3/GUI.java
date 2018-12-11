package Lab3;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

/**
 * 压缩文件储存的信息依次为：文件头，压缩后大小(bit)，原文件名，原大小(byte)，Huffman树，编码后文件流
 */
public class GUI extends JFrame implements ActionListener {

    private static final int bufferSize = 8 * 1024 * 1024;
    private static final int huffFileHeader = 0x23333333;  //文件头

    private JButton open = new JButton("Open");
    private JButton unzip = new JButton("Unzip");
    private JButton zip = new JButton("Zip");
    private JFileChooser fileChooser = new JFileChooser();
    private File openedFile, savedFile;
    private HuffmanCodingTree HT;

    public GUI() {
        setTitle("Huffman Coding Zipper");
        setResizable(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(300, 100);

        open.addActionListener(this);
        zip.addActionListener(this);
        unzip.addActionListener(this);
        open.setEnabled(true);
        zip.setEnabled(false);
        unzip.setEnabled(false);
        setLayout(new GridLayout(1, 3));
        add(open);
        add(zip);
        add(unzip);

        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == open) {
                open();
            } else if (e.getSource() == zip) {
                zip();
            } else if (e.getSource() == unzip) {
                unzip();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    private long zippedLength;
    private long originalLength;
    private String originalName;

    private BufferedInputStream zippedInput;

    private void open() throws IOException {
        fileChooser.setDialogTitle("Open A File to Zip or Unzip");
        fileChooser.resetChoosableFileFilters();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
                "Huffman Coding Zipped File (*.huff)", "huff"));
        fileChooser.showOpenDialog(GUI.this);
        openedFile = fileChooser.getSelectedFile();
        if (openedFile == null || !openedFile.exists()) {
            return;
        }
        zip.setEnabled(true);
        //显示文件信息
        var input = new BufferedInputStream(new FileInputStream(openedFile), bufferSize);
        var dataInput = new DataInputStream(input);
        int type = dataInput.readInt();

        if (type == huffFileHeader) {  //huff压缩文件
            unzip.setEnabled(true);
            zippedLength = dataInput.readLong();
            originalName = dataInput.readUTF();
            originalLength = dataInput.readLong();
            HT = new HuffmanCodingTree();
            HT.restoreFromFile(dataInput);
            zippedInput = input;
            //log
            System.out.println("压缩文件信息：");
            System.out.println("压缩文件名： " + openedFile.getName());
            System.out.println("原文件名： " + originalName);
            System.out.println("原大小： " + originalLength + " bytes");
            System.out.println("压缩后大小： " + openedFile.length() + " bytes");
            System.out.println("压缩后有效数据流大小： " + zippedLength / 8 + " bytes");
            System.out.println("压缩率： " + (100.0 * openedFile.length() / originalLength) + " %");
            System.out.println();
            //log end
        } else {  //一般文件
            unzip.setEnabled(false);
            dataInput.close();
            //log
            System.out.println("文件信息：");
            System.out.println("文件名： " + openedFile.getName());
            System.out.println("文件大小： " + openedFile.length() + " bytes");
            System.out.println();
            //log end
        }
    }

    private void zip() throws IOException {
        fileChooser.setDialogTitle("Save Zipped File");
        fileChooser.resetChoosableFileFilters();
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Huffman Coding Zipped File (*.huff)", "huff"));
        fileChooser.setCurrentDirectory(openedFile.getParentFile());
        fileChooser.setSelectedFile(new File(openedFile.getName() + ".huff"));
        fileChooser.showSaveDialog(GUI.this);
        savedFile = fileChooser.getSelectedFile();
        if (savedFile == null) {
            return;
        }
        if (savedFile.exists()) {
            savedFile.delete();
        }
        savedFile.createNewFile();

        long t = System.currentTimeMillis();

        //创建HuffmanCodingTree
        var input = new BufferedInputStream(new FileInputStream(openedFile), bufferSize);
        HT = new HuffmanCodingTree(input);
        input.close();

        var output = new BufferedOutputStream(new FileOutputStream(savedFile), bufferSize);
        var dataOutput = new DataOutputStream(output);

        dataOutput.writeInt(huffFileHeader);  //文件头
        dataOutput.writeLong(0);  //预留压缩后大小的位置
        dataOutput.writeUTF(openedFile.getName());  //原文件名
        dataOutput.writeLong(openedFile.length());  //原大小
        HT.saveToFile(dataOutput);  //储存Huffman树

        input = new BufferedInputStream(new FileInputStream(openedFile), bufferSize);
        long length = HT.encode(input, output);
        input.close();
        output.close();
        dataOutput.close();

        var randomAccessFile = new RandomAccessFile(savedFile, "rw");
        randomAccessFile.skipBytes(4);
        randomAccessFile.writeLong(length);
        randomAccessFile.close();

        t = System.currentTimeMillis() - t;
        //log
        System.out.println("压缩信息：");
        System.out.println("原文件名： " + openedFile.getName());
        System.out.println("原大小： " + openedFile.length() + " bytes");
        System.out.println("压缩后文件名： " + savedFile.getName());
        System.out.println("压缩后大小： " + savedFile.length() + " bytes");
        System.out.println("压缩后有效数据流大小： " + length / 8 + " bytes");
        System.out.println("压缩率： " + (100.0 * savedFile.length() / openedFile.length()) + " %");
        System.out.println("压缩时间： " + t + " ms");
        System.out.println("压缩速率： " + (1000.0 / 1024 / 1024 * openedFile.length() / t) + " MB/s");
        System.out.println();
        //log end
    }

    private void unzip() throws IOException {
        fileChooser.setDialogTitle("Save Unzipped File");
        fileChooser.resetChoosableFileFilters();
        fileChooser.setCurrentDirectory(openedFile.getParentFile());
        fileChooser.setSelectedFile(new File(originalName));
        fileChooser.showSaveDialog(GUI.this);
        savedFile = fileChooser.getSelectedFile();
        if (savedFile.exists()) {
            savedFile.delete();
        }
        savedFile.createNewFile();
        long t = System.currentTimeMillis();
        var output = new BufferedOutputStream(new FileOutputStream(savedFile), bufferSize);
        HT.decode(zippedInput, zippedLength, output);
        zippedInput.close();
        output.close();

        t = System.currentTimeMillis() - t;
        //log
        System.out.println("解压信息：");
        System.out.println("解压文件名： " + savedFile.getName());
        System.out.println("解压大小： " + savedFile.length() + " bytes");
        System.out.println("解压时间： " + t + " ms");
        System.out.println("解压速率： " + (1000.0 / 1024 / 1024 * openedFile.length() / t) + " MB/s");
        System.out.println();
        //log end
    }
}
