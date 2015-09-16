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
                map[pair[0]][pair[1]] = Math.random()
            }
            if(pair[0] != pair[1]){
                combinations << pair
            }
        }
    }

    /**
     * 2�W�c�Ԃ̘A���A�܂�񕔃O���t�����
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
            map[elm1][elm2] = Math.random() // �d�݂͗����ŏ�����
            if(elm1 != elm2){
                combinations << pair
            }
        }
    }

    // ���z�I�ɂ́A�eRelation�ɗ��[�����������������A�R�[�h���킩��₷���Ȃ�͂�
    // ���̂��߂ɂ�Relation�𐔒l�ł͂Ȃ��I�u�W�F�N�g�ɂ��āAvalue, edge1, edge2 �݂����ȃv���p�e�B��^����K�v
    // �I�u�W�F�N�g�Ƃ��Ă͂����܂ŁAWij �� Wji�@�Ƃ������Ƃɒ��ӁI
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
                if(k1 == key || k2 == key){
                    def friend =  k1 == key ? k2 : k1
                    res << friend
                }
            }
        }
        res.unique()
    }

}
