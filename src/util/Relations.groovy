package util

/**
 * 2つのユニットによって定義されるもの用。典型的には重み。
 * 車輪の再発明というか、もっとよい方法とかライブラリがありそう。。
 */
class Relations {

    HashMap map = new HashMap()
    List combinations = []

    public Relations(List list, Object defaultValue = null){
        [list,list].eachCombination { List pair ->
            if(pair[0].hashCode() >= pair[1].hashCode()){
                if(!map.containsKey(pair[0])){
                    map[pair[0]] = new HashMap()
                }
                map[pair[0]][pair[1]] = Math.random()
            }
            if(pair[0] != pair[1]){
                combinations << pair
            }
        }
    }

    /**
     * 2集団間の連結、つまり二部グラフを作る
     * @param list1
     * @param list2
     * @param defaultValue
     */
    public Relations(List list1, List list2, Object defaultValue = null){
        [list1,list2].eachCombination { List pair ->
            boolean b = pair[0].hashCode() >= pair[1].hashCode()
            def elm1 = b ? pair[0] : pair[1]
            def elm2 = b ? pair[1] : pair[0]
            if(!map.containsKey(elm1)){
                map[elm1] = new HashMap()
            }
            map[elm1][elm2] = Math.random() // 重みは乱数で初期化
            if(elm1 != elm2){
                combinations << pair
            }
        }
    }

    // 理想的には、各Relationに両端情報も持たせた方が、コードがわかりやすくなるはず
    // そのためにはRelationを数値ではなくオブジェクトにして、value, edge1, edge2 みたいなプロパティを与える必要
    // オブジェクトとしてはあくまで、Wij ≠ Wji　ということに注意！
    public List getAll(){
        List res = []
        map.each {Object key, Map value ->
            res.addAll value.values()
        }
        res
    }

    public Object get(key1, key2){
        key1.hashCode() >= key2.hashCode() ? map[key1][key2] : map[key2][key1]
    }

    public void set(key1, key2, obj){
        if(key1.hashCode() >= key2.hashCode()){
            map[key1][key2] = obj
        }else{
            map[key2][key1] = obj
        }
    }

    /**
     * keyにひもづく全てのRelationをリストで返す。だけど重みだけ返しても使い道がなさそうな。
     * @param key
     * @return
     */
    public List get(key){
        List res = []
        map.each{ Object k1, Map v1 ->
            v1.each{ k2, v2 ->
                if(k1 == key || k2 == key){
                    res << v2
                }
            }
        }
        res
    }

    /**
     * keyはNeuronを想定。結合相手をリストで返す
     * @param key
     * @return
     */
    public List getFriends(key){
        List res = []
        map.each{ Object k1, Map v1 ->
            v1.each{ k2, v2 ->
                if(k1 == key && k2 == key) return
                if(k1 == key || k2 == key){
                    def friend =  k1 == key ? k2 : k1
                    res << friend
                }
            }
        }
        res.unique()
    }

}
