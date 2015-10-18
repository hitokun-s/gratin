package gratin.util

/**
 * もうこうなると、４要素のListをキーにしたMapともう違いはないような気もしてくる。。。
 * Matrixをgenericsで統一できないものかなぁ。。。
 * @author Hitoshi Wada
 */
class GMatrix4D extends ArrayList<GMatrix3D>{

    // 引数順は、アクセス順（Matrix4D[depth2][depth][row][col]）と一致させること。
    GMatrix4D(int depth2, int depth, int row, int col, Object defValue = null) {
        depth2.times{
            this << new GMatrix3D(depth, row, col, defValue)
        }
    }

    int getDepth2(){
        this.size()
    }

    // cls(value, depth2, depth, row, col)
    public void forEachWithIndex(Closure cls) {
        depth2.times { depth2 ->
            this[depth2].forEachWithIndex {v,depth,row,col ->
                cls(v, depth2, depth, row, col)
            }
        }
    }

    @Override
    public String toString(){
        "depth2:${this.size()}, depth:${this[0].size()}, row:${this[0][0].rowCount}, col:${this[0][0].colCount}}"
    }
}
