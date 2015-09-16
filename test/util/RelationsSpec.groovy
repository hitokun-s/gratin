package util

import components.Neuron
import spock.lang.Specification

class RelationsSpec extends Specification {

    def "constructor"(){
        when:
            def relations = new Relations([1,2,3,4])
        then:
            relations.getAll().size() == 6 + 4 // 4C2の順列組合せ　＋　自己結合
    }

    def "getter,setter"(){
        given:
            def relations = new Relations([1,2,3,4])
            Date d = new Date() // なんでも良い
        when:
            relations.set(2,4,d)
        then:
            relations.get(2,4) == d
            relations.get(4,2) == d
            relations.get(1,4) == null
    }

    def "get"(){
        given:
            def relations = new Relations([1,2,3,4])
            relations.set(1,1,'a')
            relations.set(1,2,'b')
            relations.set(1,3,'c')
            relations.set(1,4,'d')
            relations.set(2,2,'e')
            relations.set(2,3,'f')
            relations.set(2,4,'g')
            relations.set(3,3,'h')
            relations.set(3,4,'i')
            relations.set(4,4,'j')
        when:
            List res = relations.get(2)
        then:
            res == ['b','e','f','g']
    }

    def "getFriends"(){
        def relations = new Relations([1,2,3,4])
        when:
        List res = relations.getFriends(2)
        then:
        res == [1,3,4]
    }

    // 二部グラフの場合
    def "getFriends for bipartite graph"(){
        given:
            List neurons1 = []
            4.times{
                neurons1 << new Neuron()
            }
            List neurons2 = []
            5.times{
                neurons2 << new Neuron()
            }
            assert neurons1.size() == 4
            def relations = new Relations(neurons1, neurons2)
        when:
            def friends = relations.getFriends(neurons1[2]) // 適当に選んだ
        then:
            friends.sort() == neurons2.sort()
    }

}