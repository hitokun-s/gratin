package gratin.classifier

import gratin.components.Net

/**
 * Multi-Layer Perceptron
 * hiddenLayerCnt = 0 �Ƃ���΁A�����̃p�[�Z�v�g�����ɂȂ�
 * ������Net�̃��b�p�[�ɂȂ��Ă���BNet�̌p���N���X�ɂ��������������ȁH
 *
 * @author Hitoshi Wada
 */
class Perceptron {

    Net net

    Perceptron(int inputCnt, int outputCnt, int hiddenLayerCnt = 0) {
        // TODO option���������āA���ԑw�̃��j�b�g�����w��ł���悤�ɂ��邩�H
        assert hiddenLayerCnt >= 0
        def defs = []
        hiddenLayerCnt.times{
            defs << [name: 'fc', count: inputCnt]
            defs << [name: 'si', count: inputCnt]
        }
        defs << [name: 'fc', count: outputCnt]
        defs << [name: 'sm', count: outputCnt]
        net = new Net(defs, inputCnt)
    }

    public void train(List teachers, int count){
        net.train(teachers, count)
    }

    public List predict(List data){
        net.predict(data)
    }

    public double getError(List teachers){
        net.getError(teachers)
    }
}
