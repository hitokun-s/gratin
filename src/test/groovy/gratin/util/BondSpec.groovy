package gratin.util

import gratin.components.Neuron
import spock.lang.Specification


/**
 * @author Hitoshi Wada
 */
class BondSpec extends Specification {

    def "findAllByNeuron"() {
        given:
            def n1 = new Neuron()
            def n2 = new Neuron()
            1000.times{
                new Bond(n1, new Neuron())
                new Bond(n2, new Neuron())
            }
        when:
            def res = Bond.findAllByS(n1)
        then:
            res.size() == 1000
    }
}