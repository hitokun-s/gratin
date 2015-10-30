package gratin.components

import gratin.layers.ConvLayer
import gratin.layers.MinSquaredLayer
import gratin.layers.PoolingLayer
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

    List<Layer> layers = []

    /**
     * ex. args
     * [
     *    [ name : 'fc', inputCount : 4, outputCount : 5 ],
     *    [ name : 'si'],
     *    [ name : 'fc', outputCount : 5 ],
     *    [ name : 'sm', outputCount : 5]
     * ]
     * @param defs
     */
    public Net(List<Map> defs) {
        completeDefs(defs) // 一部プロパティ（inputCount、outputCountなど）がなければ自動計算。前後の層とも整合を取る。
        assert defs.every { Map df -> df.inputCount && df.outputCount }

        def inputs = neurons(defs[0].inputCount)
        defs.each { Map df ->
            def outputs = neurons(df.outputCount)
            layers << getLayer(df.name, inputs, outputs, df.opt)
            inputs = outputs // share reference between 2 layers
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
    public void train(List<Map> teachers, int epochCnt = 1500) {

        log.info "let's train!"

        // TODO Totally ugly, You must die.

        double thresholdContinue = 0.05 // TODO これを使って、epochCntによらない収束判定もできるようにしたい
        boolean toContinue = true

        int epoch = 0

        // batch learning
        while (toContinue) {
            epoch++
            log.debug "epoch:$epoch, cost:${getError(teachers)}"
//            toContinue = false
            teachers.each { Map teacher ->
                forward(teacher.in)
                backward(teacher.out)
            }
            layers*.update()
            if (epochCnt && epoch > epochCnt) {
                toContinue = false
            }
//            lr *= 0.99 // 各Layer.update()へ移行
        }
    }

    public List<Double> product(List<Double> data) {
        forward(data)
        layers.last().outputValues
    }

    public int predict(List<Double> data) {
        forward(data)
        layers.last().predict()
    }

    /**
     * 例えば、畳み込み層では、filterTypeCountやchannelCountから、入出力ユニット数は自動決定できる。
     * また、前LayerのoutputCount == 次LayerのinpputCount という関係からも自動決定できる。
     * 最終的に「すべてのLayer定義が、inputCount、outputCountを持つ」ようにすることが責務
     * 一応、最低限の情報だけ定義すればよいという、親切機能のつもり。。。
     */
    def completeDefs(List<Map> defs) {
        // How dangerous is this logic...how can I guarantee this can work perfectly?

        int loopCnt = 0
        while (defs.any { !it.inputCount || !it.outputCount }) {
            if (loopCnt++ > defs.size()) break
            defs.eachWithIndex { Map df, int i ->
                if (df.inputCount && df.outputCount) return // もうセット済なら用なし

                // inputCountがなければ、前の層のoutputCountにする
                if (!df.inputCount && (i > 0 && defs[i - 1].outputCount)) {
                    df.inputCount = defs[i - 1].outputCount
                }
                // outputCountがなければ、次の層のinputCountにする
                if (!df.outputCount && (i < defs.size() - 1 && defs[i + 1].inputCount)) {
                    df.outputCount = defs[i + 1].inputCount
                }
                switch (df.name) {
                    case "fc":
                        break
                    case "sm":
                    case "si":
                    case "ms":
                        // Sigmoid層、Softmax層、MinSquared層、は、入力ユニット数＝出力ユニット数
                        if (!df.inputCount && df.outputCount) {
                            df.inputCount = df.outputCount
                        }
                        if (!df.outputCount && df.inputCount) {
                            df.outputCount = df.inputCount
                        }
                        break
                    case "cv":
                        if (!df.inputCount || !df.outputCount) {
                            ConvLayer.setInputAndOutputCount(df)
                        }
                        assert df.inputCount && df.outputCount
                        break
                    case "pl":
                        if (!df.inputCount || !df.outputCount) {
                            PoolingLayer.setInputAndOutputCount(df)
                        }
                        break
                    default:
                        throw new RuntimeException("Invalid Layer Def!:$df")
                }
            }
        }
        if (defs.any { !it.inputCount || !it.outputCount }) {
            throw new RuntimeException("Something wrong in layer defs!")
        }
    }

    def Layer getLayer(String name, List<Neuron> inputs, List<Neuron> outputs, Map opt = [:]) {
        switch (name) {
            case "fc": new FullyConnLayer(inputs, outputs); break
            case "sm": new SoftmaxLayer(inputs, outputs); break
            case "ms": new MinSquaredLayer(inputs, outputs); break
            case "si": new SigmoidLayer(inputs, outputs); break
            case "cv": new ConvLayer(inputs, outputs, opt); break
            case "pl": new PoolingLayer(inputs, outputs, opt); break
            default: throw new RuntimeException("Invalid Layer Def!")
        }
    }
}
