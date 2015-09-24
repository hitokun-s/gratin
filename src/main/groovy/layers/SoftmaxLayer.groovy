package layers

import components.Neuron
import groovy.util.logging.Log4j

/**
 * @author Hitoshi Wada
 */
@Log4j
class SoftmaxLayer extends Layer{

    int pairCnt

    // normalization factor
    double Z = 0

    def SoftmaxLayer(List<Neuron> inputs, List<Neuron> outputs) {
        super(inputs, outputs)
        assert inputs.size() == outputs.size()
        pairCnt = inputs.size()
    }

    @Override
    def forward() {
        Z = 0
        pairCnt.times{ int idx ->
            Z += (outputs[idx].value = Math.exp(inputs[idx].value))
        }
        outputs.each{it.value /= Z}
    }

    /**
     * teacher data should be set before calling backward()
     * @param teacher (should be array of 0/1)
     */
    @Override
    def backward() {
        pairCnt.times{ int idx ->
            inputs[idx].delta = outputs[idx].value - teacher[idx]
        }
    }

    /**
     * a.k.a. Cost
     * calculate negative log likelihood based on current outputs and teacher data
     * to be used to make sure implementation is correct
     */
    public double getError(List<Integer> teachers) {
        double logSum = 0
        pairCnt.times{ int idx ->
            logSum += (teachers[idx] == 1) ? Math.log(outputs[idx].value) : Math.log(1 - outputs[idx].value)
        }
        - logSum
    }

    @Override
    int predict() {
        outputs.max{it.value}.idx
    }
}
