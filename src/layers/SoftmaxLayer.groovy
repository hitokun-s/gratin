package layers

import components.Neuron
import util.Weight

import static util.Util.*

/**
 * SoftMax Layer
 * FullyConnectedLayerと分離する実装の方が望ましいかもしれないが、
 * ここではわかりやすさのために一体型で実装しておく
 * 分離型と一体型を両者を実装してみて比べることで、より良い設計が見えてくるかもしれない
 * TODO そのへん整理できたら名前変えよう
 */
class SoftmaxLayer {

    // バイアスもないようなSoftmaxの計算にニューロンを登場させるのは、大げさすぎる？？
    Weight w
    List<Neuron> inputs = []
    List<Neuron> outputs = []

    public SoftmaxLayer(int inCnt, int outCnt) {
        inCnt.times {
            inputs << new Neuron(bias: Math.random(), idx: it)
        }
        outCnt.times {
            outputs << new Neuron(bias: Math.random(), idx: it)
        }
        w = new Weight(inputs, outputs)
    }

    /**
     * クラスkのニューロンへ接続する、データベクトルと重みベクトルの内積
     * ボルツマンマシンとのアナロジーにより便宜的にEnergyと呼んでおく
     */
    public double getEnergy(List<Double> data, int k) {
        Neuron outN = outputs[k]
        inputs.sum { Neuron inN ->
            w[inN, outN] * inN.value
        }
    }

    /**
     * 正規化定数を返す。各種参考文献では、Z(Θ)になっている。「z」でアクセスできるのは便利だけど、微妙かも
     * @return
     */
    public double getZ() {
        def Z = 0 // 正規化定数
        outputs.size().times { idx ->
            Z += Math.exp(getEnergy(inputValues, idx))
        }
        Z
    }

    /**
     * 与えた入力データがクラスk（k:クラスインデックス）である確率（＝Softmax関数の出力値）
     */
    public double getProbability(List<Double> data, int k) {
        inputValues = data // 規格化定数を"z"で呼べるようにするには、事前に入力をセットしないといけない。これはちょっと危険。
        Math.exp(getEnergy(data, k)) / z
    }

    /**
     * @return 重み更新量（勾配）
     */
    public double updateWeights(Neuron inN, Neuron outN, List<Map> data) {
        // 全重みを周回ごとにランダムな順に並び替えて、巡回更新していくのが良いらしい？
        // その話と確率的降下法（＝１データのみについて更新）との関係はどうなんだろう？
        def k = outN.idx
        def gradW = data.sum {
            inputValues = it.input
            (getProbability(it.input, k) - it.output[k]) * inN.value
        }
        println "gradW:$gradW"
        w[inN, outN] -= gradW
        gradW
    }

    /**
     * @param List < [ input:List < Double > , output:List<Double>]>
     */
    public void train(List<Map> data) {

        // 超手抜き

        Neuron inN = getRandom(inputs)
        Neuron outN = getRandom(outputs)

        while (Math.abs(updateWeights(inN, outN, data)) > 0.01) {
            inN = getRandom(inputs)
            outN = getRandom(outputs)
        }
    }

    /**
     * とりあえずクラスを示すインデックスを返すようにする
     * @param input
     * @return
     */
    public int predict(List<Double> input) {
        inputValues = input
        outputs.max { Neuron outN ->
            inputs.sum { Neuron inN ->
                w[inN, outN] * inN.value
            }
        }.idx
        // maxとなる確率値＝自信度として取得してもいいかも？まあ尤度でいいか。。。
    }

    /**
     * 尤度（負の対数尤度）を返す。実装が正しいかのテスト用。学習すれば減るはず。
     * @param List < [ input:List < Double > , output:List<Double>]>
     */
    public double getLikelihood(List<Map> data) {
        // 分類クラスが1～KまでK個あるとして、１サンプルデータがクラスkに分類されるときの尤度は直接計算すると、
        // （1 - クラス1の確率）* （1 - クラス2の確率）* ... *（クラスkの確率）* ... * (1 - クラスKの確率）
        // という確率積になる。さらにこれが、サンプルデータ分だけ掛け合わされる
        // 対数を取ると、シグマが2回登場することになる
        -data.sum { sample ->
            outputs.sum { Neuron outN ->
                def k = outN.idx
                if (sample.output[k] == 1) {
                    return Math.log(getProbability(sample.input, k))
                } else {
                    return Math.log(1 - getProbability(sample.input, k))
                }
            }
        }
    }

    public List<Double> getInputValues() {
        inputs*.value
    }

    public void setInputValues(List<Double> values) {
        inputs.eachWithIndex { n, i ->
            n.value = values[i]
        }
    }

    public List<Double> getOutputValues() {
        outputs*.value
    }
}