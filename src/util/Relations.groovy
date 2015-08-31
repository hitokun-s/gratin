package util

/**
 * 2�̃��j�b�g�ɂ���Ē�`�������̗p�B�T�^�I�ɂ͏d�݁B
 * �ԗւ̍Ĕ����Ƃ������A�����Ƃ悢���@�Ƃ����C�u���������肻���B�B
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
                map[pair[0]][pair[1]] = defaultValue
            }
            if(pair[0] != pair[1]){
                combinations << pair
            }
        }
    }

    // ���z�I�ɂ́A�eRelation�ɗ��[�����������������A���p���₷���͂�
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
     * key�ɂЂ��Â��S�Ă�Relation�����X�g�ŕԂ��B�����Ǐd�݂����Ԃ��Ă��g�������Ȃ������ȁB
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
     * key��Neuron��z��B������������X�g�ŕԂ�
     * @param key
     * @return
     */
    public List getFriends(key){
        List res = []
        map.each{ Object k1, Map v1 ->
            v1.each{ k2, v2 ->
                if(k1 == key && k2 == key) return
                def friend =  k1 == key ? k2 : k1
                res << friend
            }
        }
        res.unique()
    }

}