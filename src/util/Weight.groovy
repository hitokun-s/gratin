package util

import components.Neuron

/**
 * あるユニットとあるユニットをつなぐ結合の重みが１つしかない場合
 * Weight[a][b]（＝Weight[b][a]）
 * の形で重みにアクセスできる
 */
class Weight extends HashMap<List<Neuron>, Double>{

    /**
     * 2集団間の連結、つまり二部グラフを作る
     * @param list1
     * @param list2
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
        assert super.get(key)
        super.put(key, value)
    }

    // get(List<Neuron> key)と書きたいが、そうするとOverrideとみなされずにコンパイルエラーになってしまう
    @Override
    public Double get(Object key){
        if(!super.get(key)){
            key = (key as List).reverse()
        }
        assert super.get(key)
        super.get(key)
    }
}
