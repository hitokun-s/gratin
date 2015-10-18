package gratin.util

/**
 * 立体行列
 * 要素アクセスは、Matrix3D[depth][row][col]の順序。
 * Matrix3D[depth]だと、その深さにあるMatrixが返る。
 * @author Hitoshi Wada
 */
class GMatrix3D extends ArrayList<GMatrix>{

    // 引数順は、アクセス順（Matrix3D[depth][row][col]）と一致させること。
    GMatrix3D(int depth, int row, int col, Object defValue = null) {
        depth.times{
            this << new GMatrix(row, col, defValue)
        }
    }

    GMatrix3D(List<GMatrix> source){
        super(source)
    }

    int getDepth(){
        this.size()
    }

    int getRow(){
        this[0].size()
    }

    int getCol(){
        this[0][0].size()
    }

    // cls(value, depth, row, col)
    public void forEachWithIndex(Closure cls) {
        depth.times { depth ->
            this[depth].forEachWithIndex {v,row,col ->
                cls(v, depth, row, col)
            }
        }
    }
}
