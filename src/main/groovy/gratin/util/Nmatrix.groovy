package gratin.util

import gratin.components.Neuron

/**
 * NMatrix for Neuron
 * @author Hitoshi Wada
 */
class NMatrix extends ArrayList<ArrayList<Neuron>> {

    // 正方行列用
    NMatrix(int size) {
        this(size, size)
    }

    NMatrix(int row, int col) {
        // Is there more elegant way?
        row.times {
            List tmp = []
            col.times {
                tmp << new Neuron()
            }
            this << tmp
        }
    }

    public NMatrix(List<List<Object>> source) {
        if (source[0][0] instanceof Neuron) {
            source.each { List<Neuron> list ->
                this << list
            }
        } else {
            source.size().times {
                this << source[it].collect { new Neuron(value: it) }
            }
//            ((List<List<Double>>)source).each { List<Double> list ->
//                this << list.collect { new Neuron(value: it) }
//            }
        }
    }

//    public NMatrix(List<List<Number>> values) {
//        values.collect { List<Number> list ->
//            this << list.collect { new Neuron(value: it) }
//        }
//    }

    NMatrix plus(NMatrix m) {
        NMatrix res = clone()
        rowCount.times { row ->
            colCount.times { col ->
                res[row][col].value += m[row][col].value
            }
        }
        res
    }

    NMatrix plus(double d) {
        NMatrix res = clone()
        rowCount.times { row ->
            colCount.times { col ->
                res[row][col].value += d
            }
        }
        res
    }

    NMatrix minus(NMatrix m) {
        NMatrix res = clone()
        rowCount.times { row ->
            colCount.times { col ->
                res[row][col].value -= m[row][col].value
            }
        }
        res
    }

    NMatrix minus(double d) {
        NMatrix res = clone()
        rowCount.times { row ->
            colCount.times { col ->
                res[row][col].value -= d
            }
        }
        res
    }

    // 項同士を積算
    NMatrix multiply(NMatrix m) {
        NMatrix res = clone()
        rowCount.times { row ->
            colCount.times { col ->
                res[row][col].value *= m[row][col].value
            }
        }
        res
    }

    NMatrix multiply(double d) {
        NMatrix res = clone()
        rowCount.times { row ->
            colCount.times { col ->
                res[row][col].value *= d
            }
        }
        res
    }

    NMatrix div(double d) {
        NMatrix res = clone()
        rowCount.times { row ->
            colCount.times { col ->
                res[row][col].value /= d
            }
        }
        res
    }

    /**
     * 行列の積（dot product）を「*」にするか「dot」か、悩ましいところ。
     * numpyでは、「dot」を採用し、「*」は要素同士の積演算に当てている。
     * refs : http://www.lifewithpython.com/2014/11/python-use-matrix-operations.html
     */
//    NMatrix dotProduct(NMatrix m) {
//        // check condition for multiply
//        assert this.colCount == m.rowCount
//        def res = new NMatrix(this.rowCount, m.colCount)
//        res.rowCount.times { rowIdx ->
//            res.colCount.times { colIdx ->
//                res[rowIdx][colIdx] = dotProduct(this.row(rowIdx), m.col(colIdx))
//            }
//        }
//        res
//    }

    // NMatrix[i] is enough ? Is this not necessary?
    List<Neuron> row(int i) {
        this[i]
    }

    // ideal access way must be col[i] ...
    // or more ideally, NMatrix[:][i] or NMatrix[:,i] is preferable as numpy do. but impossible in Groovy...
    List<Neuron> col(int i) {
        this.collect { it[i] }
    }

    public int getRowCount() {
        this.size()
    }

    public int getColCount() {
        this[0].size()
    }

    @Override
    public NMatrix clone() {
        def res = []
        this.each { row ->
            res << row.clone()
        }
        new NMatrix(res)
    }

    def dotProduct = { List x, List y ->
        assert x && y && x.size() == y.size()
        [x, y].transpose().collect { xx, yy -> xx * yy }.sum()
    }

    public double sum() {
        ((List) this).sum { List<Neuron> list ->
            ((List<Neuron>) list).sum { it.value }
        }
    }

    public double max() {
        ((List) this).collect { List<Neuron> list ->
            ((List) list).max { it.value }
        }.max { it.value }.value
    }

    public double min() {
        ((List) this).collect { List<Neuron> list ->
            ((List) list).min { it.value }
        }.min { it.value }.value
    }

    /**
     * 行列全体の値を、指定された最大値・最小値の範囲になるよう、拡大縮小平行移動する
     */
    public NMatrix translate(double max, double min) {
        ((this - this.min()) / (this.max() - this.min())) * (max - min) + min
    }

    /**
     * Listを継承しているので、名前をeachにはできない。
     * @param クロージャ （引数は、要素値、行インデックス、列インデックス）
     */
    public void forEachWithIndex(Closure cls) {
        rowCount.times { row ->
            colCount.times { col ->
                cls(this[row][col], row, col)
            }
        }
    }

    public void forEach(Closure cls) {
        rowCount.times { row ->
            colCount.times { col ->
                cls(this[row][col])
            }
        }
    }

    public void forEachWithIndexByStride(int stride, Closure cls) {
        int strideX = -1
        int strideY = -1
        for (int i = 0; i < rowCount; i += stride) {
            strideX++
            for (int j = 0; j < colCount; j += stride) {
                strideY++
                cls(this[i][j], i, j, strideX, strideY)
            }
            strideY = -1
        }
    }

    /**
     * 矩形領域を切り出す
     * （例）radius = 2 なら、[row][col]を中心にした、5 * 5 の領域を返す
     * 矩形領域が元のNmatrixからはみ出る場合は、0パディングする
     */
    public NMatrix partial(int row, int col, int size) {
        def res = new NMatrix(size)
        def radius = (size - 1) / 2
        int i = row - radius, j = col - radius
        for (int m = 0; m < size; m++) {
            for (int n = 0; n < size; n++) {
                res[m][n] = (i + m < 0 || i + m >= rowCount || j + n < 0 || j + n >= colCount) ? 0 : this[i + m][j + n] as double
                // 0 padding
            }
        }
        res
    }

    // 「as Matrix」 によって、valueだけ取り出してMatrixを返す
    Object asType(Class clazz) {
        if (clazz == Matrix) {
            return new Matrix(this.collect{List<Neuron> row ->
                row.collect{it.value}
            })
        }
        throw new RuntimeException("unsupported type conversion into:${clazz.name}")
    }
}
