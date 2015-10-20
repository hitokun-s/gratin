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
                def gradW = outN.delta * inN.value
                wd[inN, outN] += gradW // accumlate weight gradient for batch learning
                w[inN, outN] * outN.delta
            }
        }
        // TODO Can I do this here? Funny?
        outputs.each{Neuron outN ->
            outN.bias -= 0.1 * outN.delta
        }
    }

    @Override
    def update(){
        inputs.each { Neuron inN ->
            outputs.each { outN ->
                def decay = 0.0001 * w[inN, outN]
                w[inN, outN] -= lr * (wd[inN, outN] + decay)
//                        layer.w[inN, outN] -= lr * layer.wd[inN, outN]
                // layer.wd[inN, outN] = 0 // this cause weird error, I don,t know why
                wd[inN, outN] = 0.000000000000000000000000000000000000000000000001 as double
            }
        }
        lr *= 0.99
    }
}
