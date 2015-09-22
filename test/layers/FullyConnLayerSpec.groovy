package layers

import components.Neuron
import spock.lang.Specification
import static util.Util.*

/**
 * @author Hitoshi Wada
 */
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