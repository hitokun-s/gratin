package gratin.components

import groovy.util.logging.Log4j

/**
 * a.k.a Unit
 * Gratin introduces Activation Layer, so no activation happens inside Neuron.
 * This means, Neuron.value never change inside Neuron.
 * Neuron.value is set from previous layer, and taken from next layer..
 *
 * @author Hitoshi Wada
 */
@Log4j
class Neuron {

    public double value = 0

    double bias = 0 // in FullyConnLayer, bias will be shared through all outputs neurons

    // derivative of Cost(Error) at this neuron's value
    // main character in back propagation and weight update
    // often indicated delta or epsilon in back propagation formula
    double delta = 0

    // index in layer or network
    int idx

    @Override
    public String toString(){
        "Neuron:$idx"
    }
}
