package util

import components.Neuron
import spock.lang.Specification

/**
 * @author Hitoshi Wada
 */
class WeightSpec extends Specification {

    def "constructor for Fully Connected Network"() {
        given:
            def n1 = new Neuron(idx: 1)
            def n2 = new Neuron(idx: 2)
            def n3 = new Neuron(idx: 3)
            def n4 = new Neuron(idx: 4)
            def n5 = new Neuron(idx: 5)
        when:
            def w = new Weight([n1, n2, n3, n4, n5])
        then:
            w.size() == 15 // = 5C2 + 5(self connection)
            w[n1, n3] == w[n3, n1]
    }

    def "constructor for bipartite graph"() {
        given:
            def n1 = new Neuron(idx: 1)
            def n2 = new Neuron(idx: 2)
            def n3 = new Neuron(idx: 3)
            def n4 = new Neuron(idx: 4)
            def n5 = new Neuron(idx: 5)
        when:
            def w = new Weight(
                [n1, n2],
                [n3, n4, n5]
            )
        then:
            w.size() == 6
            w[n1, n3] == w[n3, n1]
    }

    // 加算代入、減算代入、乗算代入、除算代入ができる
    def "assignment operations are possible"() {
        given:
            def n1 = new Neuron(idx: 1)
            def n2 = new Neuron(idx: 2)
            def n3 = new Neuron(idx: 3)
            def n4 = new Neuron(idx: 4)
            def n5 = new Neuron(idx: 5)
            def w = new Weight(
                [n1, n2],
                [n3, n4, n5]
            )
            w[n1, n3] = (0.12 as Double)
            w[n1, n4] = (0.12 as Double)
            w[n2, n3] = (0.12 as Double)
            w[n2, n4] = (0.12 as Double)
        when:
            w[n1, n3] += 0.1
            w[n1, n4] -= 0.06
            w[n2, n3] *= 0.1
            w[n2, n4] /= 2
        then:
            w[n1, n3] == 0.22
            w[n1, n4] == 0.06
            w[n2, n3] == 0.012
            w[n2, n4] == 0.06
    }

    def "throw AssertionError when access invalid connection"() {
        given:
            def n1 = new Neuron(idx: 1)
            def n2 = new Neuron(idx: 2)
            def n3 = new Neuron(idx: 3)
            def n4 = new Neuron(idx: 4)
            def n5 = new Neuron(idx: 5)
            def w = new Weight(
                [n1, n2],
                [n3, n4, n5]
            )
        when:
            w[n1, n2] = (0.1 as Double)
        then:
            thrown(AssertionError)
    }

    def "pairs()"() {
        given:
            def n1 = new Neuron(idx: 1)
            def n2 = new Neuron(idx: 2)
            def n3 = new Neuron(idx: 3)
            def w = new Weight([n1, n2, n3])
        when:
            def pairs = w.pairs()
        then:
            pairs.size() == 6
            [n1, n1] in pairs
            [n1, n2] in pairs || [n2, n1] in pairs
            [n1, n3] in pairs || [n3, n1] in pairs
            [n2, n2] in pairs
            [n2, n3] in pairs || [n3, n2] in pairs
            [n3, n3] in pairs
    }

}