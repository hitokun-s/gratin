package gratin.layers

import gratin.components.Neuron
import groovy.util.logging.Log4j
import spock.lang.Specification
import static gratin.util.Util.*

/**
 * @author Hitoshi Wada
 */
@Log4j
class FullyConnLayerSpec extends Specification {

    def "constructor"() {
        given:
            List<Neuron> inputs = neurons(3)
            List<Neuron> outputs = neurons(2)
        when:
            def layer = new FullyConnLayer(inputs, outputs)
        then:
            layer.inputs == inputs
            layer.outputs == outputs
            layer.w.pairs().size() == 6 // 2 * 3
    }
}