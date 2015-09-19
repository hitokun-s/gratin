package util

import components.Neuron
import spock.lang.Specification

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
}