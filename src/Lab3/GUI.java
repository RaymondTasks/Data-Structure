package Lab3;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

/**
 * 压缩文件储存的信息依次为：压缩后大小，原大小，原文件名，Huffman树，编码后文件流
 */
public class GUI extends JFrame implements ActionListener {

    private final int bufferSize = 8 * 1024 * 1024;
    private JButton open = new JButton("Open");
    private JButton unzip = new JButton("Unzip");
    private JButton zip = new JButton("Zip");
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

    private JFileChooser openFileChooser, saveZippedFileChooser, saveUnzippedFileChooser;

    private long zippedLength;
    private long originalLength;
    private String originalName;

    private BufferedInputStream unzippedInput;

    private void open() throws IOException {
        if (openFileChooser == null) {
            openFileChooser = new JFileChooser();
            openFileChooser.setDialogTitle("Open A File to Zip or Unzip");
            openFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            openFileChooser.setMultiSelectionEnabled(false);
            openFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
                    "Huffman Coding Zipped File (*.huff)", "huff"));
        }
        openFileChooser.showOpenDialog(GUI.this);
        openedFile = openFileChooser.getSelectedFile();
        if (openedFile == null || !openedFile.exists()) {
            return;
        }
        //显示文件信息
        var name = openedFile.getName();
        zip.setEnabled(true);
        if (name.substring(name.lastIndexOf('.') + 1).equals("huff")) {
            //压缩文件
            unzip.setEnabled(true);
            unzippedInput = new BufferedInputStream(new FileInputStream(openedFile), bufferSize);
            var dataInput = new DataInputStream(unzippedInput);
            zippedLength = dataInput.readLong();
            originalLength = dataInput.readLong();
            originalName = dataInput.readUTF();

            //log
            System.out.println("压缩文件信息：");
            System.out.println("压缩文件名： " + openedFile.getName());
            System.out.println("原文件名： " + originalName);
            System.out.println("原大小： " + originalLength + " bytes");
            System.out.println("压缩后大小： " + openedFile.length() + " bytes");
            System.out.println("压缩后有效数据流大小： " + zippedLength / 8 + " bytes");
            System.out.println("压缩率： " + (100.0 * openedFile.length() / originalLength) + "%");
            System.out.println();
            //log

            HT = new HuffmanCodingTree();
            HT.restoreFromFile(dataInput);
        } else {
            unzip.setEnabled(false);
            //log
            System.out.println("文件信息：");
            System.out.println("文件名: " + openedFile.getName());
            System.out.println("文件大小：" + openedFile.length() + " bytes");
            //log
        }

    }

    private void zip() throws IOException {
        if (saveZippedFileChooser == null) {
            saveZippedFileChooser = new JFileChooser();
            saveZippedFileChooser.setDialogTitle("Save Zipped File");
            saveZippedFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            saveZippedFileChooser.setMultiSelectionEnabled(false);
            saveZippedFileChooser.setAcceptAllFileFilterUsed(false);
            saveZippedFileChooser.setFileFilter(new FileNameExtensionFilter(
                    "Huffman Coding Zipped File (*.huff)", "huff"));
        }
        saveZippedFileChooser.setSelectedFile(new File(openedFile.getAbsolutePath() + ".huff"));
        saveZippedFileChooser.showSaveDialog(GUI.this);
        savedFile = saveZippedFileChooser.getSelectedFile();
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

        dataOutput.writeLong(0);  //预留压缩后大小的位置
        dataOutput.writeLong(openedFile.length());
        dataOutput.writeUTF(openedFile.getName());
        HT.saveToFile(dataOutput);

        input = new BufferedInputStream(new FileInputStream(openedFile), bufferSize);
        long length = HT.encode(input, output);
        input.close();
        output.close();
        dataOutput.close();

        var randomAccessFile = new RandomAccessFile(savedFile, "rw");
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
        System.out.println("压缩率： " + (100.0 * savedFile.length() / openedFile.length()) + "%");
        System.out.println("压缩时间： " + t + " ms");
        System.out.println("压缩速率： " + (1000.0 / 1024 / 1024 * openedFile.length() / t) + " MB/s");
        System.out.println();
        //log
    }

    private void unzip() throws IOException {
        if (saveUnzippedFileChooser == null) {
            saveUnzippedFileChooser = new JFileChooser();
            saveUnzippedFileChooser.setDialogTitle("Save Unzipped File");
            saveUnzippedFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            saveUnzippedFileChooser.setMultiSelectionEnabled(false);
            saveUnzippedFileChooser.setAcceptAllFileFilterUsed(false);
        }
        saveUnzippedFileChooser.setCurrentDirectory(openedFile.getParentFile());
        saveUnzippedFileChooser.setSelectedFile(new File(originalName));
        saveUnzippedFileChooser.showSaveDialog(GUI.this);
        savedFile = saveUnzippedFileChooser.getSelectedFile();
        if (savedFile.exists()) {
            savedFile.delete();
        }
        savedFile.createNewFile();
        long t = System.currentTimeMillis();
        var output = new BufferedOutputStream(new FileOutputStream(savedFile), bufferSize);
        HT.decode(unzippedInput, zippedLength, output);
        unzippedInput.close();
        output.close();

        t = System.currentTimeMillis() - t;
        //log
        System.out.println("解压信息:");
        System.out.println("解压文件名： " + savedFile.getName());
        System.out.println("解压大小： " + savedFile.length());
        System.out.println("解压时间： " + t + " ms");
        System.out.println("解压速率： " + (1000.0 / 1024 / 1024 * openedFile.length() / t) + " MB/s");
        //log
    }
}
