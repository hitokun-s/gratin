package gratin.util

import groovy.util.logging.Log4j

/**
 * refs : http://www.lifewithpython.com/2014/11/python-use-matrix-operations.html
 *
 * @author Hitoshi Wada
 */
@Log4j
class Matrix extends ArrayList<ArrayList> {

    // 正方行列用
    Matrix(int size) {
        this(size, size)
    }

    public Matrix(int row, int col) {
        // Is there more elegant way?
        row.times {
            this << new double[col] as ArrayList
        }
    }

    public Matrix(List<List<Number>> source) {
        super(source)
    }

    Matrix plus(Matrix m) {
        def res = clone()
        rowCount.times { row ->
            colCount.times { col ->
                res[row][col] += m[row][col]
            }
        }
        res
    }

    Matrix plus(double d) {
        def res = clone()
        rowCount.times { row ->
            colCount.times { col ->
                res[row][col] += d
            }
        }
        res
    }

    Matrix minus(Matrix m) {
        def res = clone()
        rowCount.times { row ->
            colCount.times { col ->
                res[row][col] -= m[row][col]
            }
        }
        res
    }

    Matrix minus(double d) {
        def res = clone()
        rowCount.times { row ->
            colCount.times { col ->
                res[row][col] -= d
            }
        }
        res
    }

    // 項同士を積算
    Matrix multiply(Matrix m) {
        def res = clone()
        rowCount.times { row ->
            colCount.times { col ->
                res[row][col] *= m[row][col]
            }
        }
        res
    }

    Matrix multiply(double d) {
        def res = clone()
        rowCount.times { row ->
            colCount.times { col ->
                res[row][col] *= d
            }
        }
        res
    }

    Matrix div(double d) {
        def res = clone()
        rowCount.times { row ->
            colCount.times { col ->
                res[row][col] /= d
            }
        }
        res
    }

    /**
     * 行列の積（dot product）を「*」にするか「dot」か、悩ましいところ。
     * numpyでは、「dot」を採用し、「*」は要素同士の積演算に当てている。
     * refs : http://www.lifewithpython.com/2014/11/python-use-matrix-operations.html
     */
    Matrix dotProduct(Matrix m) {
        // check condition for multiply
        assert this.colCount == m.rowCount
        def res = new Matrix(this.rowCount, m.colCount)
        res.rowCount.times { rowIdx ->
            res.colCount.times { colIdx ->
                res[rowIdx][colIdx] = dotProduct(this.row(rowIdx), m.col(colIdx))
            }
        }
        res
    }

    // Matrix[i] is enough ? Is this not necessary?
    List row(int i) {
        this[i]
    }

    // ideal access way must be col[i] ...
    // or more ideally, Matrix[:][i] or Matrix[:,i] is preferable as numpy do. but impossible in Groovy...
    List col(int i) {
        this.collect { it[i] }
    }

    // This override is not necessary
//    @Override
//    public boolean equals(Object m){
//        rowCount.times{ row ->
//            colCount.times{ col ->
//                if(this[row][col] != m[row][col]){
//                    return false
//                }
//            }
//        }
//        this
//    }

    public int getRowCount() {
        this.size()
    }

    public int getColCount() {
        this[0].size()
    }

    @Override
    public Matrix clone() {
        def res = []
        this.each { row ->
            res << row.clone()
        }
        new Matrix(res)
    }

    def dotProduct = { List x, List y ->
        assert x && y && x.size() == y.size()
        [x, y].transpose().collect { xx, yy -> xx * yy }.sum()
    }

    /**
     * 全要素の和
     */
    public double sum() {
        ((List)this).sum {
            ((List)it).sum()
        }
    }

    public double max() {
        ((List)this).collect {
            ((List)it).max()
        }.max()
    }

    public double min() {
        ((List)this).collect {
            ((List)it).min()
        }.min()
    }

    public Matrix translate(double max, double min) {
        ((this - this.min()) / (this.max() - this.min())) * (max - min) + min
    }

    /**
     * Listを継承しているので、名前をeachにはできない。
     * @param クロージャ （引数は、要素値、行インデックス、列インデックス）
     */
    public Matrix forEach(Closure cls) {
        def res = clone()
        rowCount.times { row ->
            colCount.times { col ->
                res[row][col] = cls(this[row][col], row, col)
            }
        }
        res
    }

    /**
     * 矩形領域を切り出す
     * （例）radius = 2 なら、[row][col]を中心にした、5 * 5 の領域を返す
     * 矩形領域が元のMatrixからはみ出る場合は、0パディングする
     */
    public Matrix partial(int row, int col, int size) {
        def res = new Matrix(size)
        def radius = (size - 1) / 2
        int i = row - radius, j = col - radius
        for (int m = 0; m < size; m++) {
            for (int n = 0; n < size; n++) {
                res[m][n] = (i + m < 0 || i + m >= rowCount || j + n < 0 || j + n >= colCount) ? 0 : this[i + m][j + n] as double // 0 padding
            }
        }
        res
    }
}
