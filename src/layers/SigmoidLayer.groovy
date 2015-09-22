package layers

import components.Neuron
import static util.Util.*

/**
 * input neurons : output neurons = 1 : 1
 * In other words, one input neuron is connected with one output neuron.
 *
 * @author Hitoshi Wada
 */
class SigmoidLayer extends Layer{

    int pairCnt

    public SigmoidLayer(List<Neuron> inputs, List<Neuron> outputs) {
        super(inputs, outputs)
        assert inputs.size() == outputs.size()
        pairCnt = inputs.size()
    }

    @Override
    def forward() {
        pairCnt.times{ idx ->
            outputs[idx].value = sigma(inputs[idx].value)
        }
    }

    @Override
    def backward() {
        pairCnt.times{ idx ->
            inputs[idx].delta = sigmad(outputs[idx].delta)
        }
    }
}
