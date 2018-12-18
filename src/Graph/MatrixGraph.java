package Graph;

import java.util.ArrayList;

/**
 * 邻接矩阵表示的无向图
 */
public class MatrixGraph {

    private static final int Infinity = -1;     //表示不连接

    private String vexs[];      //顶的名称
    private int arcs[][];       //边长度

    public String[] getVexs() {
        return vexs;
    }

    public int[][] getArcs() {
        return arcs;
    }

    public int getVexNumber() {
        return vexs.length;
    }

    /**
     * 构造函数
     *
     * @param vexs 顶信息
     * @param arcs 边长度，已化为对称矩阵
     */
    public MatrixGraph(String vexs[], int arcs[][]) {
        this.vexs = vexs;
        this.arcs = arcs;
    }

    /**
     * 用Dijkstra算法求最短路径
     *
     * @param a 起始点的序号
     * @param b 终点的讯号
     * @return 最短路径经过的顶，有序排列
     */
    public int[] Dijkstra(int a, int b) {
        if (a == b) {
            lastPath = new ArrayList<>();
            lastPath.add(a);
            int[] p = new int[1];
            p[0] = a;
            return p;
        }

        //path是储存a到各顶点的已知最短路径
        var path = new ArrayList<ArrayList<Integer>>();
        for (int i = 0; i < vexs.length; i++) {
            path.add(new ArrayList<>());
        }

        //D是从a到其他顶的已知最短路径长度
        int D[] = new int[vexs.length];
        for (int i = 0; i < vexs.length; i++) {
            D[i] = arcs[a][i];
            if (D[i] != Infinity) {
                var initpath = new ArrayList<Integer>();
                initpath.add(a);
                if (i != a) {
                    initpath.add(i);
                }
                path.set(i, initpath);
            }
        }

        //S是已求得最短路径的顶的集合
        var S = new ArrayList<Integer>();
        S.add(a);

        //S中包括所有的顶前一直循环
        while (S.size() < vexs.length) {
            //找到新的最近节点
            int min = Integer.MAX_VALUE;
            int minIndex = -1;
            for (int i = 0; i < vexs.length; i++) {
                if (!S.contains(i) && D[i] != Infinity && D[i] < min) {
                    min = D[i];
                    minIndex = i;
                }
            }
            if (minIndex == -1) {  //非连通图
                return null;
            }

            //更新S
            S.add(minIndex);

            //已找到到达b的最短路径
            if (minIndex == b) {
                lastPath = path.get(minIndex);
                int ret[] = new int[lastPath.size()];
                int len = 0;
                for (var p : lastPath) {
                    ret[len++] = p;
                }
                return ret;
            }

            //更新D和path
            for (int i = 0; i < vexs.length; i++) {
                if (!S.contains(i) && arcs[minIndex][i] != Infinity) {
                    int tmp = D[minIndex] + arcs[minIndex][i];
                    if (D[i] == Infinity || tmp < D[i]) {
                        D[i] = tmp;
                        var pi = new ArrayList<>(path.get(minIndex));
                        pi.add(i);
                        path.set(i, pi);
                    }
                }
            }
        }

        return null; //没有找到b，可能是b不存在或者与a不连通v
    }

    private ArrayList<Integer> lastPath;

    public void resetLastPath() {
        lastPath = new ArrayList<>();
    }

    /**
     * 转化成dot语言用于Graphviz绘图
     *
     * @return dot语言字符串
     */
    public String toDotLanguage() {
        var sb = new StringBuilder();
        sb.append("graph{\n\tnode[shape=circle]\n");
        //添加顶
        for (int i = 0; i < vexs.length; i++) {
            sb.append("\t" + vexs[i]);
            //经过的顶显示为红色
            if (lastPath.contains(i)) {
                sb.append(" [color=red,fontcolor=red]");
            }
            sb.append("\n");

        }
        //添加边
        for (int i = 0; i < vexs.length; i++) {
            for (int j = i + 1; j < vexs.length; j++) {
                if (arcs[i][j] != Infinity) {
                    sb.append("\t" + vexs[i] + " -- " + vexs[j] + " [");
                    var indexi = lastPath.indexOf(i);
                    var indexj = lastPath.indexOf(j);
                    //经过的边显示为红色
                    if (indexi != -1 && indexj != -1 && Math.abs(indexi - indexj) == 1) {
                        sb.append("color=red,fontcolor=red,");
                    }
                    sb.append("label=" + arcs[i][j] + "]\n");
                }
            }
        }
        sb.append("}\n");
        return sb.toString();
    }


}
