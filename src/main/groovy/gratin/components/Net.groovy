package gratin.components

import groovy.util.logging.Log4j
import gratin.layers.FullyConnLayer
import gratin.layers.Layer
import gratin.layers.SigmoidLayer
import gratin.layers.SoftmaxLayer

import static gratin.util.Util.*

/**
 * @author Hitoshi Wada
 */
@Log4j
class Net {

    double lr = 0.1 // learning Rate

    List<Layer> layers = []

    /**
     * ex. args
     * [
     *    [ name : 'fc', count : 5 ],
     *    [ name : 'si', count : 5 ],
     *    [ name : 'fc', count : 5 ],
     *    [ name : 'sm', count : 5]
     * ]
     * TODO should I create LayerDef Class?
     * @param defs
     */
    public Net(List<Map> defs, int inputCnt) {
        def inputs = neurons(inputCnt)
        defs.each { Map df ->
            def outputs = neurons(df.count)
            // layers << getLayerConstructor(df.name).newInstance(inputs, outputs)
            layers << getLayer(df.name, inputs, outputs)
            inputs = outputs // share reference
        }
    }

    public void forward(List<Double> data) {
        layers.first().inputValues = data
        layers*.forward()
    }

    public void backward(List<Double> teacher) {
        layers.last().teacher = teacher
        layers.reverse()*.backward()
    }

    public double getError(List<Map> teachers) {
        teachers.sum { teacher ->
            forward(teacher.in)
            layers.last().getError(teacher.out)
        }
    }

    /**
     * for each Layer, update weights using deltas, if the Layer has weights to be updated.
     *
     * This method may be implemented inside Layer or Trainer(new class).
     * TODO Should be inside Layer? Because "layer.*" is annoying...
     *
     * @param teachers List<[in:double[], out.double]>
     */
    public void train(List<Map> teachers, int epochCnt = 0) {

        log.info "let's train!"

        // TODO Totally ugly, You must die.

        double thresholdContinue = 0.1
        boolean toContinue = true

        int epoch = 0

        // batch learning
        while (toContinue) {
            epoch++
            log.debug "epoch:$epoch, cost:${getError(teachers)}"
            toContinue = false
            teachers.each { Map teacher ->
                forward(teacher.in)
                backward(teacher.out)
                layers.findAll { it.w }.each { Layer layer ->
                    layer.inputs.each { inN ->
                        layer.outputs.each { outN ->
                            def gradW = outN.delta * inN.value
                            layer.wd[inN, outN] += gradW // accumlate weight gradient for batch learning
                            if (Math.abs(gradW) > thresholdContinue) toContinue = true
                        }
                    }
                }
            }
            layers.findAll { it.w }.each { Layer layer ->
                layer.inputs.each { Neuron inN ->
                    layer.outputs.each { outN ->
                        layer.w[inN, outN] -= lr * layer.wd[inN, outN]
                        // layer.wd[inN, outN] = 0 // this cause weird error, I don,t know why
                        layer.wd[inN, outN] = 0.000000000000000000000000000000000000000000000001 as double
                    }
                }
            }
            if(epochCnt && epoch > epochCnt){
                toContinue = false
            }
            lr *= 0.99
        }
    }

    public int predict(List<Double> data) {
        forward(data)
        layers.last().predict()
    }

//    Constructor getLayerConstructor(String name) {
//        Class c
//        switch (name) {
//            case "fc": c = FullyConnLayer.class; break
//            case "sm": c = SoftmaxLayer.class; break
//            default: throw new RuntimeException()
//        }
//        c.getDeclaredConstructor([List.class, List.class] as Class[])
//    }

    // TODO which is better, above or this?
    def Layer getLayer(String name, List<Neuron> inputs, List<Neuron> outputs) {
        switch (name) {
            case "fc": new FullyConnLayer(inputs, outputs); break
            case "sm": new SoftmaxLayer(inputs, outputs); break
            case "si": new SigmoidLayer(inputs, outputs); break
            default: throw new RuntimeException()
        }
    }
}
