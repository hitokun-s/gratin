package layers

import components.Neuron
import util.Weight

/**
 * Fully Connected Layer
 */
class FullyConnLayer extends Layer {

    public FullyConnLayer(List<Neuron> inputs, List<Neuron> outputs) {
        super(inputs, outputs)
        w = new Weight(inputs, outputs)
        wd = new Weight(inputs, outputs)
    }

    def forward() {
        outputs.each { Neuron outN ->
            outN.value = inputs.sum { Neuron inN ->
                w[inN, outN] * inN.value
            }
        }
    }

    def backward() {
        // calculate and save input neuron's delta based on output neuron's delta(This is the back propagation!!)
        inputs.each { Neuron inN ->
            inN.delta = outputs.sum { Neuron outN ->
                w[inN, outN] * outN.delta
            }
        }
    }
}
