package gratin.util

import gratin.components.Neuron

/**
 * @author Hitoshi Wada
 */
class NeuronMatrix extends ArrayList<Neuron[]>{

    public NeuronMatrix(int row, int col) {
        // Is there more elegant way?
        row.times {
            this << new Neuron[col] as List
        }
    }

    /**
     * MatrixをNeuronMatrixに変換する感じで、NeuronMatrixを作成。もちろん値はNeuron.valueになる
     */
    public NeuronMatrix(Matrix m){
        m.rowCount.times{ rowIdx ->
            this << m.row(rowIdx).collect{
                new Neuron(value:it)
            }
        }
    }

    List<Neuron> row(int i) {
        this[i]
    }

    List<Neuron> col(int i) {
        this.collect { it[i] }
    }

    public int getRowCount() {
        this.size()
    }

    public int getColCount() {
        this[0].size()
    }

    // 指定エリアのNeuronの値を正方行列化して返す
    Matrix partial(int startRow, int startCol, int size){
        def source = []
        int iRow = startRow
        size.times{
            println row(iRow)
            println startCol + size - 1
            source << row(iRow)[startCol..(startCol + size - 1)]*.value
            iRow++
        }
        new Matrix(source)
    }
}
