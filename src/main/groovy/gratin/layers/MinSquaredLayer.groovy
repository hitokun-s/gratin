package gratin.layers

import gratin.components.Neuron

/**
 * TODO think about the name later
 * Å¬“ñæŒë·
 * @author Hitoshi Wada
 */
class MinSquaredLayer extends Layer{

    int pairCnt

    MinSquaredLayer(List<Neuron> inputs, List<Neuron> outputs) {
        super(inputs, outputs)
        assert inputs.size() == outputs.size()
        pairCnt = inputs.size()
    }

    @Override
    def forward() {
        // just transfer values
        pairCnt.times{
            outputs[it].value = inputs[it].value
        }
    }

    @Override
    def backward() {
        // as same as the case of Softmax Layer!
        pairCnt.times{ int idx ->
            inputs[idx].delta = outputs[idx].value - teacher[idx]
        }
    }

    public double getError(List<Integer> teachers) {
        double logSum = 0
        pairCnt.times{ int idx ->
            def diff = outputs[idx].value - teachers[idx]
            logSum += diff * diff
        }
        logSum / 2
    }

    @Override
    int predict() {
        //TODO what can I do ?
    }
}
