package gratin.util

import gratin.components.Neuron

/**
 * 立体行列
 * 要素アクセスは、Matrix3D[depth][row][col]の順序。
 * Matrix3D[depth]だと、その深さにあるMatrixが返る。
 * @author Hitoshi Wada
 */
class NMatrix3D extends ArrayList<NMatrix>{

    // 引数順は、アクセス順（Matrix3D[depth][row][col]）と一致させること。
    NMatrix3D(int depth, int row, int col) {
        depth.times{
            this << new NMatrix(row, col)
        }
    }

    // 深さ１で作成
    public NMatrix3D(NMatrix m){
        this << m
    }

    public NMatrix3D(List<Object> source) {
        if(source[0] instanceof NMatrix){
            source.each {
                this << it
            }
        }else{
            source.each { List<List<Number>> list ->
                this << new NMatrix(list)
            }
        }
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

    Object sumWithDepth(Closure cls){
        def res
        this.eachWithIndex{v,i ->
            def tmp = cls(v,i)
            res = res ? res + tmp : tmp
        }
        res
    }

    // eachをオーバーライドする方が呼び出し元では綺麗かもしれないが、他メソッド内でeachを使うかもしれないし、
    // 外部からeachを呼ぶ可能性がある（入力をチャネル別に処理するとか）ので、一旦オーバーライドしないでおく。
    void forEach(Closure cls){
        this.each{NMatrix m ->
            m.forEach(cls)
        }
    }

//    // 全要素数
//    public int getTotalCount(){
//        this.depth * this[0].totalCount
//    }
//
//    // 左角（？）から数えたインデックス
//    public int getTotalIndex(int depth, int row, int col){
//        this[0].totalCount * depth + this[0].getTotalIndex(row, col)
//    }
//
//    public int ti(int depth, int row, int col){
//        getTotalIndex(depth, row, col)
//    }
//
//    /**
//     * getTotalIndex の逆
//     * @return [depth, row, col]
//     */
//    public List getEachIndex(int totalIndex){
//        [(int)(totalIndex / this[0].totalCount)] + this[0].getEachIndex(totalIndex % this[0].totalCount)
//    }
}
