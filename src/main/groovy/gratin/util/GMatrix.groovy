package gratin.util

import groovy.util.logging.Log4j

/**
 * 汎用行列（General Matrix）
 * 汎用なので、登録、参照、変更などはできるべきだが、数値行列用を想定したplus.minusなどのメソッドは作らない
 * TODO ジェネリクス化できていないので、呼び出し元でキャストしないといけないのが難点
 *
 * @author Hitoshi Wada
 */
@Log4j
class GMatrix extends ArrayList<ArrayList> {

    // 正方行列用
    GMatrix(int size) {
        this(size, size)
    }

    public GMatrix(int row, int col, Object defValue = null) {
        // Is there more elegant way?
        row.times {
            this << (1..col).collect{defValue?:[]}
        }
    }

    public GMatrix(List<List> source) {
        super(source)
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
    public GMatrix clone() {
        def res = []
        this.each { row ->
            res << row.clone()
        }
        new GMatrix(res)
    }

    // クロージャを引数にするなら、下記メソッドは可能でもいい気がする
//    public double sumValue() {
//        ((List)this).sumValue {
//            ((List)it).sumValue()
//        }
//    }
//
//    public double maxValue() {
//        ((List)this).collect {
//            ((List)it).maxValue()
//        }.maxValue()
//    }
//
//    public double minValue() {
//        ((List)this).collect {
//            ((List)it).minValue()
//        }.minValue()
//    }

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

    @Override
    public String toString(){
        "GMatrix. row:${this.rowCount}, col:${this.colCount}"
    }
}
