package gratin.util

import gratin.components.Neuron

/**
 * NMatrix for Neuron
 * @author Hitoshi Wada
 */
class NMatrix extends ArrayList<ArrayList<Neuron>> {

    // �����s��p
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

    // �����m��ώZ
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
     * �s��̐ρidot product�j���u*�v�ɂ��邩�udot�v���A�Y�܂����Ƃ���B
     * numpy�ł́A�udot�v���̗p���A�u*�v�͗v�f���m�̐ω��Z�ɓ��ĂĂ���B
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
     * �s��S�̂̒l���A�w�肳�ꂽ�ő�l�E�ŏ��l�͈̔͂ɂȂ�悤�A�g��k�����s�ړ�����
     */
    public NMatrix translate(double max, double min) {
        ((this - this.min()) / (this.max() - this.min())) * (max - min) + min
    }

    /**
     * List���p�����Ă���̂ŁA���O��each�ɂ͂ł��Ȃ��B
     * @param �N���[�W�� �i�����́A�v�f�l�A�s�C���f�b�N�X�A��C���f�b�N�X�j
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
     * ��`�̈��؂�o��
     * �i��jradius = 2 �Ȃ�A[row][col]�𒆐S�ɂ����A5 * 5 �̗̈��Ԃ�
     * ��`�̈悪����Nmatrix����͂ݏo��ꍇ�́A0�p�f�B���O����
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

    // �uas Matrix�v �ɂ���āAvalue�������o����Matrix��Ԃ�
    Object asType(Class clazz) {
        if (clazz == Matrix) {
            return new Matrix(this.collect{List<Neuron> row ->
                row.collect{it.value}
            })
        }
        throw new RuntimeException("unsupported type conversion into:${clazz.name}")
    }
}
