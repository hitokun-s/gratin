package gratin.classifier

import gratin.components.Net

/**
 * Multi-Layer Perceptron
 * hiddenLayerCnt = 0 とすれば、ただのパーセプトロンになる
 * ただのNetのラッパーになっている。Netの継承クラスにした方がいいかな？
 *
 * @author Hitoshi Wada
 */
class Perceptron {

    Net net

    Perceptron(int inputCnt, int outputCnt, int hiddenLayerCnt = 0) {
        // TODO option引数をつけて、中間層のユニット数を指定できるようにするか？
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
