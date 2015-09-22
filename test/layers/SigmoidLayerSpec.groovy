package layers

import components.Neuron
import spock.lang.Specification

import static util.Util.neurons
import static util.Util.neurons

/**
 * @author Hitoshi Wada
 */
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