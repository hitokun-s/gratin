package gratin.util

import gratin.components.Neuron
import groovy.util.logging.Log4j
import spock.lang.Specification

/**
 * @author Hitoshi Wada
 */
@Log4j
class UtilSpec extends Specification {

    def "pairs"() {
        given:
            def n1 = new Neuron(idx: 1)
            def n2 = new Neuron(idx: 2)
            def n3 = new Neuron(idx: 3)
            def n4 = new Neuron(idx: 4)
            def n5 = new Neuron(idx: 5)
            def neurons = [n1, n2, n3, n4, n5]
        when:
            def pairs1 = Util.pairs(neurons) // unidirectional
            def pairs2 = Util.pairs(neurons, true) // bidirectional
        then:
            pairs1.size() == 15 // 5C2 + 5
            pairs2.size() == 25 // 5 * 5
    }

    def "neurons"() {
        when:
            def res = Util.neurons(5)
        then:
            res.size() == 5
            res[0].idx == 0
            res[1].idx == 1
            res[2].idx == 2
            res[3].idx == 3
            res[4].idx == 4
    }

    def "normalize"(){
        given:
            def data = [1, 2, 3, 4, 5] as List<Double>
        when:
            Util.normalize(data)
            println data
            def avg = data.sum()/data.size()
            def variance = data.sum{(it - avg) * (it -avg)} / data.size()
        then:
            (avg as Double).round(10) == 0 // needs rounding
            (variance as Double).round(10) == 1 // needs rounding
    }

    def "eps"(){
        when:
            def d1 = (1.0 as double) + Util.eps
            def d2 = Math.nextUp(1.0 as double)
        then:
            d1 == d2
    }

    def "dist"(){
        when:
            def res = Util.dist([1,3,5],[9,8,7])
        then:
            res == Math.sqrt(93)
    }
}