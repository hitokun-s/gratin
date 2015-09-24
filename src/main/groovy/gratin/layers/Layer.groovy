package gratin.layers

import gratin.components.Neuron
import groovy.util.logging.Log4j
import gratin.util.Weight

/**
 * Layerの責務（順伝播）：
 * - 入力ユニット群から出力データを処理をして、出力ユニット群に渡す入力データを計算すること
 *
 * （注）データを「前の層から受け取る」「次に層へ渡す」は、Layerの責務にしても良いし、しなくても良い
 * （注）Layerの責務にしない場合は、Netの責務にする
 *
 * Layerの責務（逆伝播）：
 * - 出力ユニットのδを元にして、入力ユニットのδを計算する
 * - （FullyConnLayerのみ）出力ユニットのδと、入力ユニットの出力値に基づいて、重み勾配を計算して重みを更新する
 *
 * （注）δ：誤差関数をそのユニットへの入力値で偏微分したもの。εと書かれる場合も。
 * （注）ある層の入力ユニット＝前の層の出力ユニット、ある層の出力ユニット＝次の層の入力ユニット
 *
 * @author Hitoshi Wada
 */
@Log4j
abstract class Layer {

    List<Neuron> inputs
    List<Neuron> outputs

    List<Double> teacher // only used by output layer

    Weight w // Some Layer(ex. SigmoidLayer, SoftmaxLayer) does not use weight.
    Weight wd // weight gradient TODO A little bit Confusing. Weight class shold be renamed.

    public Layer(List<Neuron> inputs, List<Neuron> outputs) {
        this.inputs = inputs
        this.outputs = outputs
    }

    abstract def forward()
    abstract def backward()

    /**
     * Only output layer should override this
     */
    public double getError(List<Integer> teachers) {
        throw new RuntimeException("This should not be executed. Please override this, or never call!")
    }

    /**
     * Only output layer should override this
     * @return class index
     */
    public int predict(){
        throw new RuntimeException("This should not be executed. Please override this, or never call!")
    }

    public void setInputValues(List<Double> values) {
        inputs.eachWithIndex { n, i ->
            n.value = values[i]
        }
    }

    public void setOutputValues(List<Double> values) {
        outputs.eachWithIndex { n, i ->
            n.value = values[i]
        }
    }

    public double[] getInputValues() {
        inputs*.value as double[]
    }

    public double[] getOutputValues() {
        outputs*.value as double[]
    }

}
