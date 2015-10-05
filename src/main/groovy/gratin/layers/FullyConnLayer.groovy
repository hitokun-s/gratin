package gratin.layers

import gratin.components.Neuron
import groovy.util.logging.Log4j
import gratin.util.Weight

/**
 * Fully Connected Layer
 */
@Log4j
class FullyConnLayer extends Layer {

    public FullyConnLayer(List<Neuron> inputs, List<Neuron> outputs) {
        super(inputs, outputs)
        // set bias as random
        outputs.each{Neuron outN ->
            outN.bias = Math.random()
        }
        w = new Weight(inputs, outputs)
        wd = new Weight(inputs, outputs)
    }

    def forward() {
        outputs.each { Neuron outN ->
            outN.value = inputs.sum { Neuron inN ->
                w[inN, outN] * inN.value
            } + outN.bias
        }
    }

    def backward() {
        // calculate and save input neuron's delta based on output neuron's delta(This is the back propagation!!)
        inputs.each { Neuron inN ->
            inN.delta = outputs.sum { Neuron outN ->
                w[inN, outN] * outN.delta
            }
        }
        // TODO Can I do this here? Funny?
        outputs.each{Neuron outN ->
            outN.bias -= 0.1 * outN.delta
        }
    }
}
