package gratin.util

/**
 * もうこうなると、４要素のListをキーにしたMapともう違いはないような気もしてくる。。。
 * @author Hitoshi Wada
 */
class Matrix4D extends ArrayList<Matrix3D>{

    // 引数順は、アクセス順（Matrix4D[depth2][depth][row][col]）と一致させること。
    Matrix4D(int depth2, int depth, int row, int col) {
        depth2.times{
            this << new Matrix3D(row, col, depth)
        }
    }

    int getDepth2(){
        this.size()
    }

    // 全要素数
    public int getTotalCount(){
        this.depth2 * this[0].totalCount
    }

    // 指定座標の総インデックス
    public int getTotalIndex(int depth2, int depth, int row, int col){
        this[0].totalCount * depth2 + this[0].getTotalIndex(depth, row, col)
    }

    public int ti(int depth2, int depth, int row, int col){
        this[0].totalCount * depth2 + this[0].getTotalIndex(depth, row, col)
    }
}
