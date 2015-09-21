package layers

import spock.lang.Specification
import static util.Util.*

/**
 * @author Hitoshi Wada
 */
class SoftmaxLayerSpec extends Specification {

    def "updateWeights decrease negative log likelihood"(){
        given:
            def layer = new SoftmaxLayer(4,2)
            def sample = [
                    [input:[0.2,0.2,0.3,0.3], output:[0,1]]
            ]
        when:
            def l1 = layer.getLikelihood(sample)
            layer.updateWeights(layer.inputs[1], layer.outputs[0], sample) // �K��
            def l2 = layer.getLikelihood(sample)
            layer.updateWeights(layer.inputs[3], layer.outputs[1], sample) // �K��
            def l3 = layer.getLikelihood(sample)
        then:
            l1 > l2
            l2 > l3
    }

    def "train decrease negative log likelihood"(){
        given:
            def layer = new SoftmaxLayer(4,2)
            def sample = [
                [input:[0.2,0.2,0.3,0.3], output:[0,1]]
            ]
        when:
            def l1 = layer.getLikelihood(sample)
            layer.train(sample)
            def l2 = layer.getLikelihood(sample)
        then:
            l1 > l2
    }

    def "predict"(){
        given:
        def layer = new SoftmaxLayer(4,2)
        def sample = [
                [input:[0.1,0.2,0.3,0.4], output:[0,1]], // �㏸�T���v���̓N���X1
                [input:[0.4,0.3,0.2,0.1], output:[1,0]]  // ���~�T���v���̓N���X2
        ]
        layer.train(sample)
        when:
            def res1 = layer.predict([0.1,0.2,0.2,0.5]) // �K���ȏ㏸�T���v�� -> �N���X1�ɂȂ��Ăق���
            def res2 = layer.predict([0.3,0.3,0.3,0.1]) // �K���ȉ��~�T���v�� -> �N���X2�ɂȂ��Ăق���
        then:
            res1 == 1
            res2 == 0
    }

    def "IRIS test can predict with probability larger than 50 %"(){
        given:
            // TODO ��������B�f�[�^�O���������������炱���ɔ��f����
            def sample = []
            new File("data/iris.data.txt").eachLine { line ->
                def output
                switch(line.split(",")[-1]){
                    case "Iris-setosa" : output = [1.0,0.0,0.0];break;
                    case "Iris-versicolor" : output = [0.0,1.0,0.0];break;
                    case "Iris-virginica" : output = [0.0,0.0,1.0];break;
                    default:assert false
                }
                sample << [input : line.split(",")[0..-2].collect {Double.parseDouble(it) / 10}, output:output]
            }
            def layer = new SoftmaxLayer(4,3)
            layer.train(sample)
        when:
            int successCnt = 0
            100.times{
                def aSample = getRandom(sample)
                if(aSample.output[layer.predict(aSample.input)] == 1){
                    successCnt++
                }
            }
            def p = successCnt / 100 // �I���m��
            println p
        then:
            p > 0.5
    }
}