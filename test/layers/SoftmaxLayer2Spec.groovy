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
}