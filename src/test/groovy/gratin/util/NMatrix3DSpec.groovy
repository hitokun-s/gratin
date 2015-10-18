package gratin.util

import gratin.components.Neuron
import spock.lang.Specification


/**
 * @author Hitoshi Wada
 */
class NMatrix3DSpec extends Specification {

    def "forEach"(){
        given:
            def m = new NMatrix3D(2,3,4)
        when:
            m.forEach {Neuron n ->
                n.value = 5
            }
        then:
            m[1][2][3].value == 5
    }
}