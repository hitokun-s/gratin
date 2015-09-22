package layers

import components.Neuron
import util.Weight

/**
 * Fully Connected Layer
 */
class FullyConnLayer extends Layer {

    Weight w

    public FullyConnLayer(List<Neuron> inputs, List<Neuron> outputs) {
        super(inputs, outputs)
        w = new Weight(inputs, outputs)
    }

    def forward() {
        outputs.each { Neuron outN ->
            outN.value = inputs.sum { Neuron inN ->
                w[inN, outN] * inN.value
            }
        }
    }

    def backward() {
        inputs.each { Neuron inN ->
            inN.delta = outputs.sum { Neuron outN ->
                w[inN, outN] * outN.delta
            }
        }
    }

    @Override
    public void updateWeights() {
        inputs.each { Neuron inN ->
            outputs.each { outN ->
                def gradW = outN.delta * inN.value
                w[inN,outN] -= gradW
            }
        }
    }
}
