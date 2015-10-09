package gratin.util

/**
 * Multiple Matrix
 * @author Hitoshi Wada
 */
class NeuronMatrices extends ArrayList<NeuronMatrix>{

    // row == vertical size, col == horizontal size
    NeuronMatrices(int row, int col, int depth) {
        depth.times{
            this << new NeuronMatrix(row, col)
        }
    }
}
