package gratin.components

import gratin.layers.ConvLayer
import gratin.layers.MinSquaredLayer
import gratin.layers.PoolingLayer
import gratin.util.Matrix4D
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
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
        defs.eachWithIndex { Map df, int idx ->
            def outputs = neurons(df.outputCount)
            def layer = getLayer(df.name, inputs, outputs, df.opt)
            layer.idx = idx
            layers << layer
            log.debug("${df.name} created!!")
            inputs = outputs // share reference between 2 layers
        }
    }

    /**
     * construct from json file
     * TODO you must die for these disgusting code
     */
    public Net(String fileName){
        List<Map> layerInfos = readParams(fileName)
        def inputs = neurons(((List)layerInfos[0].inputs).size())
        layerInfos.eachWithIndex { Map layerInfo, int idx ->
            def outputs = neurons(((List)layerInfo.outputs).size())
            def layer = getLayerForJson(layerInfo.name, inputs, outputs, layerInfo) // layerInfo can play as 'opt' in normal constructor
            layer.idx = layerInfo.layerIdx
            layers << layer
            log.debug("${layerInfo.name} created!!")
            inputs = outputs // share reference between 2 layers
            layer.reflect(layerInfo)
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
     * @return averaged cost
     */
    public double train(List<Map> teachers, int epochCnt = 1500) {

        log.info "let's train!"

        // TODO Totally ugly, You must die.

        double thresholdContinue = 0.05 // TODO これを使って、epochCntによらない収束判定もできるようにしたい
        boolean toContinue = true

        int epoch = 0
        double cost = 0
        // batch learning
        while (toContinue) {
            epoch++
//            toContinue = false
            def totalError = 0
            teachers.eachWithIndex { Map teacher, int idx ->
                if(idx > 0 && idx % 50 == 0){
                    log.debug("processed data count up to:$idx")
                }
                forward(teacher.in)
                def err = layers.last().getError(teacher.out) // TODO ConvNetJSのように、forwardの戻り値にするという案も
                totalError += err
                backward(teacher.out)
            }
            layers*.update(teachers.size())
//            while(retries){
//                def tmp = []
//                log.debug "retry! retry layer count:${retries.size()}"
//                retries.each{Map teacher ->
//                    forward(teacher.in)
//                    def err = layers.last().getError(teacher.out)
//                    if(err > 3){
//                        tmp << teacher
//                    }
//                    backward(teacher.out)
//                }
//                layers*.update(retries.size())
//                retries = tmp
//            }
            cost = totalError / teachers.size()
            log.debug "epoch:$epoch, avg cost:$cost"
            if (epoch >= epochCnt) {
                toContinue = false
            }
//            if (epochCnt % 5 == 0) {
//                saveParams("${new Date().format('yyyy-MM-dd-HH-mm-ss')}.json")
//            }
        }
        cost
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

    // TODO f**k! abandon this ugly method as soon as possible!
    def Layer getLayerForJson(String name, List<Neuron> inputs, List<Neuron> outputs, Map opt = [:]) {
        switch (name) {
            case "fc": new FullyConnLayer(inputs, outputs); break
            case "sm": new SoftmaxLayer(inputs, outputs); break
            case "ms": new MinSquaredLayer(inputs, outputs); break
            case "si": new SigmoidLayer(inputs, outputs); break
            case "cv":
                opt.channelCount = opt.channelSize
                opt.height = opt.width = 28
                opt.filters = new Matrix4D(opt.filters)
                new ConvLayer(inputs, outputs, opt)
                break
            case "pl":
                opt.channelCount = opt.channelSize
                opt.in = [height:28, width:28]
                opt.out = [height:7, width:7]
                new PoolingLayer(inputs, outputs, opt)
                break
            default: throw new RuntimeException("Invalid Layer Def!")
        }
    }

    /**
     * save parameters in JSON file
     * refs : http://stackoverflow.com/questions/19522919/reading-json-object-from-txt-file-in-groovy
     */
    def saveParams(String fileName = "gratin.json") {
        def saveFile = new File(fileName)
        def infos = layers.collect {
            it.getInfo()
        }
        def json = JsonOutput.toJson(infos)
//        saveFile.text = JsonOutput.prettyPrint(json) // this makes too many lines
        saveFile.text = json
    }

    /**
     * read parameters in JSON file
     */
    List<Map> readParams(String fileName){
        def inputFile = new File(fileName)
        new JsonSlurper().parseText(inputFile.text)
    }

    /**
     * PCA（正答率、correct answer rate）を返す
     * @param data:List<[in:[...], out:[...]]> and 'in' must be normalized already, and 'out' must be one-hot vector
     * @return PCA（正答率）
     */
    public double getPCA(List data){
        int cnt = 0
        def trueOrFalse = data.collect {
            if(cnt++ % 50 == 0){
                log.debug "finished test count:$cnt"
            }
            it.out.findIndexOf { it == 1.0 } == predict(it.in)
        }
        trueOrFalse.count { it } / trueOrFalse.size() // 正解率（PCA）を返す
    }
}
