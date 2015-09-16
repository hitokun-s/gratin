package layers

import spock.lang.Specification

class SoftmaxLayer2Spec extends Specification {

    def "updateWeights decrease negative log likelihood"(){
        given:
            def layer = new SoftmaxLayer2(4,2)
            def sample = [
                    [input:[0.2,0.2,0.3,0.3], output:[0,1]]
            ]
        when:
            def l1 = layer.getLikelihood(sample)
            layer.updateWeights(layer.inputs[1], layer.outputs[0], sample) // 適当
            def l2 = layer.getLikelihood(sample)
            layer.updateWeights(layer.inputs[3], layer.outputs[1], sample) // 適当
            def l3 = layer.getLikelihood(sample)
        then:
            l1 > l2
            l2 > l3
    }

    def "train decrease negative log likelihood"(){
        given:
            def layer = new SoftmaxLayer2(4,2)
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
        def layer = new SoftmaxLayer2(4,2)
        def sample = [
                [input:[0.1,0.2,0.3,0.4], output:[0,1]], // 上昇サンプルはクラス1
                [input:[0.4,0.3,0.2,0.1], output:[1,0]]  // 下降サンプルはクラス2
        ]
        layer.train(sample)
        when:
            def res1 = layer.predict([0.1,0.2,0.2,0.5]) // 適当な上昇サンプル -> クラス1になってほしい
            def res2 = layer.predict([0.3,0.3,0.3,0.1]) // 適当な下降サンプル -> クラス2になってほしい
        then:
            res1 == 1
            res2 == 0
    }
}