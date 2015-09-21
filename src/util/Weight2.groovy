package util

import components.Neuron

/**
 * 理想として、重みへのアクセスは、
 * Weight2[a][b]
 * Weight2[a,b]（[a,b]というListがMapのキーになっている）
 * というような簡潔な書き方が好ましい
 *
 * 相互結合（重み共有）なのか方向別結合なのかによって、設計が全然違ってくる
 *
 * 本クラスは、Weight2[a][b] と Weight2[b][a] を別の箱として用意する場合。
 *
 * 重み共有の場合は、本来必要な倍の箱が無駄に作られ、しかも更新時も同期が必要になり、いろいろ無駄ではあるけれども、
 * 方向別重みが基本で、たまたま重み共有にしている、と捉えれば自然な実装かもしれない
 *
 * ただし存在しない結合に対して重みを設定できてしまう、という欠点がある
 * （例）[a,b]と[c,d,e]の間の二部グラフをつくったとして、
 * Weight2[a][b] = 0.3 という処理がエラーにならずに通ってしまう。
 * ドロップアウトのように、動的に結合が生滅することを考えると、それは自然な形であるし、
 * 利用する側のソースで、想定外の結合ができないように注意すれば済む話、ではある。
 *
 * @author Hitoshi Wada
 */
class Weight2 extends HashMap<Neuron, HashMap<Neuron, Double>>{

    /**
     * 2集団間の連結、つまり二部グラフを作る
     * @param list1
     * @param list2
     */
    public Weight2(List<Neuron> list1, List<Neuron> list2){
        [list1,list2].eachCombination { List pair ->
            def w = Math.random() // 重みは乱数で初期化
            if(!this[pair[0]]){
                this[pair[0]] = new HashMap()
            }
            this[pair[0]][pair[1]] = w
            if(!this[pair[1]]){
                this[pair[1]] = new HashMap()
            }
            this[pair[1]][pair[0]] = w
        }
    }
}