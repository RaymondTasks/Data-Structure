package Graph

/**
 * 邻接矩阵表示的无向图
 *
 * @param vexs 顶的名称
 * @param arcs 边的长度
 */
class MatrixGraph(private val vexs: Array<String>,
                  val arcs: Array<IntArray>) {

    /**
     * @return 顶的数量
     */
    fun getVexNumber(): Int = vexs.size

    private var lastPath = intArrayOf()    //上次执行dijkstra算法时得到的最短路径
    private val Infinity = -1     //表示不连接

    /**
     * 用Dijkstra算法求最短路径
     *
     * @param a 起始点的序号
     * @param b 终点的讯号
     * @return 最短路径经过的顶，有序排列
     */
    fun dijkstra(a: Int, b: Int): IntArray {
        if (a == b) {
            lastPath = intArrayOf(a)
            return lastPath
        }

        //D是从a到其他顶的已知最短路径长度
        val D = IntArray(vexs.size) { i -> arcs[a][i] }

        //path是储存a到各顶点的已知最短路径
        val path = Array(vexs.size) { i ->
            if (D[i] != Infinity) {
                if (i == a) {
                    arrayListOf(a)
                } else {
                    arrayListOf(a, i)
                }
            } else {
                arrayListOf()
            }
        }

        //S是已求得最短路径的顶的集合
        val S = arrayListOf(a)

        //S中包括所有的顶前一直循环
        while (S.size < vexs.size) {
            //找到新的最近节点
            var min = Int.MAX_VALUE
            var minIndex = -1
            vexs.forEachIndexed { i, _ ->
                if (i !in S && D[i] != Infinity && D[i] < min) {
                    min = D[i]
                    minIndex = i
                }
            }
            if (minIndex == -1) {  //非连通图
                break
            }

            //更新S
            S.add(minIndex)

            //已找到到达b的最短路径
            if (minIndex == b) {
                lastPath = path[minIndex].toIntArray()
                return lastPath
            }

            //更新D和path
            vexs.forEachIndexed { i, _ ->
                if (i !in S && arcs[minIndex][i] != Infinity) {
                    val tmp = D[minIndex] + arcs[minIndex][i]
                    if (D[i] == Infinity || tmp < D[i]) {
                        D[i] = tmp
                        val pi = ArrayList(path[minIndex])
                        pi.add(i)
                        path[i] = pi
                    }
                }
            }
        }
        lastPath = intArrayOf()
        return lastPath //没有找到b，可能是b不存在或者与a不连通v
    }

    fun resetLastPath() {
        lastPath = intArrayOf()
    }


    /**
     * 转化成dot语言用于Graphviz绘图
     *
     * @return dot语言字符串
     */
    fun toDotLanguage(): String {
        val sb = StringBuilder()
        sb.append("graph{\n\tnode[shape=circle]\n")

        //添加顶
        vexs.forEachIndexed { i, name ->
            sb.append("\t$name")
            //经过的顶显示为红色
            if (i in lastPath) {
                sb.append(" [color=red,fontcolor=red]")
            }
            sb.append("\n")
        }

        //添加边
        for (i in 0 until arcs.size) {
            for (j in i + 1 until arcs[i].size) {
                if (arcs[i][j] != Infinity) {
                    sb.append("\t${vexs[i]} -- ${vexs[j]} [")
                    val indexi = lastPath.indexOf(i)
                    val indexj = lastPath.indexOf(j)
                    //经过的边显示为红色
                    if (indexi != -1 && indexj != -1
                            && Math.abs(indexi - indexj) == 1) {
                        sb.append("color=red,fontcolor=red,")
                    }
                    sb.append("label=${arcs[i][j]}]\n")
                }
            }
        }

        sb.append("}\n")
        return sb.toString()
    }


}
