package gratin.layers

import gratin.components.Neuron
import groovy.util.logging.Log4j
import spock.lang.Specification

import static gratin.util.Util.neurons

/**
 * @author Hitoshi Wada
 */
@Log4j
class SigmoidLayerSpec extends Specification {

    def "forward"(){
        given:
            List<Neuron> inputs = neurons(3)
            List<Neuron> outputs = neurons(3)
            def layer = new SigmoidLayer(inputs, outputs)
            inputs[1].value = 5
        when:
            layer.forward()
        then:
            outputs[1].value == 1 / (1 + Math.exp(-5)) // value transferred through sigmoid(logistic) function
    }
}