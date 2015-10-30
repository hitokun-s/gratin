package gratin.util

import gratin.components.Neuron
import groovy.util.logging.Log4j

/**
 * あるユニットとあるユニットをつなぐ結合の重みが１つしかない場合
 * Weight[a,b]（＝Weight[b,a]）の形で重みにアクセスできる
 *
 * [a,b]というListをキーにしたことで、重みだけでなく、pairs()によって結合全体を取得することもできる
 * （Weightという名前の趣旨からするとちょっと変だけど）
 *
 * 相互結合ネットワークの場合は、自己結合も重み0で作成するようにした
 * その方が、あるニューロン周りの結合を巡回して計算する実装がシンプルになるため。
 *
 * @author Hitoshi Wada
 */
@Log4j
class Weight extends HashMap<List<Neuron>, Double>{

    /**
     * 相互結合ネットワーク
     * Fully Connected Network
     */
    public Weight(List<Neuron> list){
        [list,list].eachCombination { List pair ->
            if(pair[0] == pair[1]){
                super.put(pair, 0) // 自己結合の重みは0とする
            }
            if(super.get(pair) || super.get(pair.reverse())) return // 登録済ならスキップ
            // superを使わないと、Overrideしている自身のget,put内の存在確認assertionで引っかかってしまう
            super.put(pair, Math.random() )// 重みは乱数で初期化
        }
    }

    /**
     * 2集団間の連結、つまり二部グラフを作る
     * bipartite graph
     */
    public Weight(List<Neuron> list1, List<Neuron> list2){
        [list1,list2].eachCombination { List pair ->
            // superを使わないと、Overrideしている自身のget,put内の存在確認assertionで引っかかってしまう
            super.put(pair, Math.random() )// 重みは乱数で初期化
        }
    }

    @Override
    public Double put(List<Neuron> key, Double value) {
        if(!super.get(key)){
            key = key.reverse()
        }
        try{
            assert super.get(key)
        }catch(AssertionError e){
            log.error(e)
        }
        super.put(key, value)
    }

    // get(List<Neuron> key)と書きたいが、そうするとOverrideとみなされずにコンパイルエラーになってしまう
    @Override
    public Double get(Object key){
        if(!super.get(key)){
            key = (key as List).reverse()
        }
        try{
            assert super.get(key)
        }catch(AssertionError e){
            log.error(e)
            super.put(key, 0)
        }
        super.get(key)
    }

    /**
     * 本当はメソッド名：getPairs()にして、外部からは、Weight.pairs でアクセスできれば良かったが、
     * 本クラスはHashMapなので、Weight.pairsとした瞬間に、Weight.get("pairs")を呼びにいってしまうので、あきらめた
     */
    public List<List<Neuron>> pairs(){
        this.keySet() as List
    }
}
