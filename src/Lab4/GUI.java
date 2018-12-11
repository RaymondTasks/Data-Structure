package Lab4;

import Graph.MatrixGraph;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Scanner;

public class GUI extends JFrame implements ActionListener {

    private JPanel buttonPanel, argsPanel, resultPanel;
    private JButton open, reset, dijkstra;
    private JTextField from, to;
    private JLabel result;

    private int[] path;

    public GUI() {
        setTitle("Dijkstra");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(350, 450);
        setResizable(false);
        setLayout(new GridLayout(3, 1));
        buttonPanel = new JPanel();
        argsPanel = new JPanel();
        resultPanel = new JPanel();
        add(buttonPanel);
        add(argsPanel);
        add(resultPanel);
        initButtonPanel();
        initArgsPanel();
        initResultPanel();
    }

    private void initButtonPanel() {
        buttonPanel.setLayout(null);
        open = new JButton("OPEN");
        reset = new JButton("RESET");
        reset.setEnabled(false);
        open.addActionListener(this);
        reset.addActionListener(this);
        open.setBounds(50, 50, 100, 50);
        reset.setBounds(200, 50, 100, 50);
        buttonPanel.setSize(350, 150);
        buttonPanel.add(open);
        buttonPanel.add(reset);
    }

    private void initArgsPanel() {
        argsPanel.setLayout(null);
        JLabel l1 = new JLabel("From:");
        JLabel l2 = new JLabel("To:");
        l1.setBounds(30, 30, 70, 30);
        l2.setBounds(30, 90, 370, 30);
        argsPanel.add(l1);
        argsPanel.add(l2);
        from = new JTextField();
        to = new JTextField();
        from.setBounds(100, 30, 70, 30);
        to.setBounds(100, 90, 70, 30);
        argsPanel.add(from);
        argsPanel.add(to);
        dijkstra = new JButton("DIJKSTRA");
        dijkstra.setEnabled(false);
        dijkstra.addActionListener(this);
        dijkstra.setBounds(220, 30, 90, 90);
        argsPanel.add(dijkstra);
    }

    private void initResultPanel() {
        resultPanel.setLayout(null);
        JLabel msg = new JLabel("Shortest Path Length:");
        msg.setBounds(30, 50, 150, 30);
        result = new JLabel();
        result.setBounds(180, 50, 100, 30);
        resultPanel.add(msg);
        resultPanel.add(result);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == open) {
                open();
            } else if (e.getSource() == reset) {
                reset();
            } else if (e.getSource() == dijkstra) {
                dijkstra();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private MatrixGraph graph;
    private JFileChooser fileChooser;
    private ShowFrame showFrame;

    private void open() throws IOException {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select The File to Build Graph");
            fileChooser.setFileFilter(new FileNameExtensionFilter("TEXT File", "txt"));
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.setMultiSelectionEnabled(false);
        }
        fileChooser.showOpenDialog(GUI.this);
        var fin = fileChooser.getSelectedFile();
        if (fin != null) {
            Scanner sc = new Scanner(new BufferedReader(new FileReader(fin)));
            int n = sc.nextInt();
            var vexs = new String[n];
            for (int i = 0; i < n; i++) {
                vexs[i] = "V" + i;
            }
            var arcs = new int[n][n];
            for (int i = 0; i < n; i++) {
                arcs[i][i] = 0;
                for (int j = 0; j < n; j++) {
                    arcs[i][j] = sc.nextInt();
                }
                for (int j = 0; j < i; j++) {
                    arcs[i][j] = arcs[j][i];
                }
            }
            graph = new MatrixGraph(vexs, arcs);
            graph.resetLastPath();
            reset.setEnabled(true);
            dijkstra.setEnabled(true);
            showGraph();
        }
    }

    private void reset() throws IOException {
        graph.resetLastPath();
        showGraph();
    }

    private void dijkstra() throws IOException {
        int a = Integer.valueOf(from.getText());
        int b = Integer.valueOf(to.getText());
        if (a < 0 || a >= graph.getVexNumber() || b < 0 || b >= graph.getVexNumber()) {
            //非法输入
            JOptionPane.showConfirmDialog(GUI.this,
                    "Please input right start anf end vex.",
                    "Illegal Input!", JOptionPane.OK_OPTION);
            return;
        }
        path = graph.Dijkstra(a, b);
        if (path == null) {
            graph.resetLastPath();
            result.setText("Not Connected");
        } else {
            int len = 0;
            var arcs = graph.getArcs();
            for (int i = 0; i < path.length - 1; i++) {
                len += arcs[path[i]][path[i + 1]];
            }
            result.setText(String.valueOf(len));
        }
        showGraph();
    }

    private void showGraph() throws IOException {
        var str = graph.toDotLanguage();
        var temp_dir = System.getProperty("java.io.tmpdir");
        File dot = new File(temp_dir + "\\graphviz.dot");
        if (dot.exists()) {
            dot.delete();
        }
        var out = new FileWriter(dot);
        out.write(str);
        out.close();

        String cmd[] = {"dot", "-T", "png", "-Gdpi=100", "-o", temp_dir + "\\graphviz.png", dot.getAbsolutePath()};
        try {
            Runtime.getRuntime().exec(cmd).waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //显示图片
        if (showFrame == null) {
            showFrame = new ShowFrame("Graph");
        }
        showFrame.setImg(temp_dir + "\\graphviz.png");
        showFrame.setVisible(true);
    }

}

class ShowFrame extends JFrame {
    public ShowFrame(String title) {
        super(title);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
    }

    private BufferedImage img;

    public void setImg(String fileName) throws IOException {
        img = ImageIO.read(new File(fileName));
        setSize(img.getWidth() + 14, img.getHeight() + 39);
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.drawImage(img, 7, 30, null);
    }
}
