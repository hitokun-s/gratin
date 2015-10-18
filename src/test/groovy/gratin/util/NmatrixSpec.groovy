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
            m.forEach {Neuron n ->
                n.value = n.value * n.value
            }
        then:
            m[1][1].value == 100
    }
}