package gratin.util

import gratin.components.Neuron
import spock.lang.Specification


/**
 * @author Hitoshi Wada
 */
class NmatrixSpec extends Specification {

    def "sum"() {
        given:
            def m = new NMatrix([
                [1, 2, 3],
                [4, 5, 6],
                [7, 8, 9]
            ])
        when:
            def res = m.sum()
        then:
            res == 45
    }

    def "max"() {
        given:
            def m = new NMatrix([
                [1, 2, 3],
                [4, 9, 6],
                [7, 8, 5]
            ])
        when:
            def res = m.max()
        then:
            res == 9
    }

    def "min"() {
        given:
            def m = new NMatrix([
                [5, 2, 3],
                [4, 1, 6],
                [7, 8, 9]
            ])
        when:
            def res = m.min()
        then:
            res == 1
    }

    def "forEach"() {
        given:
            def m = new NMatrix([
                [5, 2, 3],
                [4, 10, 6],
                [7, 8, 9]
            ])
        when:
            m.forEach { Neuron n ->
                n.value = n.value * n.value
            }
        then:
            m[1][1].value == 100
    }

    def "forEachWithIndexByStride"() {
        given:
            NMatrix m1 = new NMatrix(5, 5)
            NMatrix m2 = new NMatrix(5, 5)
        when:
            m1.forEachWithIndexByStride(2){Neuron v,int i,int j,int strideX, int strideY ->
                v.value = 1
            }
            m2.forEachWithIndexByStride(3){Neuron v,int i,int j,int strideX, int strideY ->
                v.value = 1
            }
        then:
            m1 as Matrix == [
                [1,0,1,0,1],
                [0,0,0,0,0],
                [1,0,1,0,1],
                [0,0,0,0,0],
                [1,0,1,0,1]
            ]
            m2 as Matrix == [
                [1,0,0,1,0],
                [0,0,0,0,0],
                [0,0,0,0,0],
                [1,0,0,1,0],
                [0,0,0,0,0]
            ]
    }
}