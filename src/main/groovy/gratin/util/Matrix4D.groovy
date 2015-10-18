package gratin.util

/**
 * もうこうなると、４要素のListをキーにしたMapともう違いはないような気もしてくる。。。
 * @author Hitoshi Wada
 */
class Matrix4D extends ArrayList<Matrix3D>{

    // 引数順は、アクセス順（Matrix4D[depth2][depth][row][col]）と一致させること。
    Matrix4D(int depth2, int depth, int row, int col) {
        depth2.times{
            this << new Matrix3D(depth, row, col)
        }
    }

    public Matrix4D(Matrix m){
        this << new Matrix3D(m)
    }

    int getDepth2(){
        this.size()
    }

    int getDepth(){
        this[0].depth
    }

    int getRow(){
        this[0].row
    }

    int getCol(){
        this[0].col
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
        getTotalIndex(depth2, depth, row, col)
    }

    /**
     * getTotalIndex の逆
     * @return [depth2, depth, row, col]
     */
    public List getEachIndex(int totalIndex){
        [(int)(totalIndex / this[0].totalCount)] + this[0].getEachIndex(totalIndex % this[0].totalCount)
    }
}
