package gratin.components

import gratin.util.TestUtil
import gratin.util.Util
import groovy.util.logging.Log4j
import org.apache.log4j.Level
import org.apache.log4j.Logger
import spock.lang.Specification

/**
 * @author Hitoshi Wada
 */
@Log4j
class NetSpec extends Specification {

    void setup() {
        // TODO これは、gradleの設定ですべき
        // set log level to debug
        Logger.getLogger("gratin").level = Level.DEBUG
    }

    def "train can decrease Error"() {
        given:
            def defs = [
                [name: 'fc', count: 4],
                [name: 'sm', count: 4]
            ]
            def net = new Net(defs, 4)
            def sample = [
                [in: [1, 2, 3, 4], out: [0, 1, 0, 0]],
                [in: [1, 2, 1, 2], out: [1, 0, 0, 0]],
                [in: [4, 3, 2, 1], out: [0, 0, 0, 1]]
            ]
        when:
            def error1 = net.getError(sample)
            net.train(sample)
            def error2 = net.getError(sample)
            println net.predict([1, 2, 3, 4])
            println net.predict([1, 2, 1, 2])
            println net.predict([4, 3, 2, 1])
        then:
            error1 > error2
    }

    def "predict"() {
        given:
            def defs = [
                [name: 'fc', count: 4],
                [name: 'sm', count: 4]
            ]
            def net = new Net(defs, 4)
            def sample = [
                [in: [1, 2, 3, 4], out: [0, 1, 0, 0]],
                [in: [1, 2, 1, 2], out: [1, 0, 0, 0]],
                [in: [4, 3, 2, 1], out: [0, 0, 0, 1]]
            ]
            net.train(sample)
        when:
            def res1 = net.predict([1, 2, 3, 4])
            def res2 = net.predict([1, 2, 1, 2])
            def res3 = net.predict([4, 3, 2, 1])
        then:
            res1 == 1
            res2 == 0
            res3 == 3
    }

    def "multi layer perceptron"() {
        given:
            def defs = [
                [name: 'fc', count: 4],
                [name: 'si', count: 4],
                [name: 'fc', count: 4],
                [name: 'sm', count: 4]
            ]
            def net = new Net(defs, 4)
            def sample = [
                [in: [1, 2, 3, 4], out: [0, 1, 0, 0]],
                [in: [1, 2, 1, 2], out: [1, 0, 0, 0]],
                [in: [4, 3, 2, 1], out: [0, 0, 0, 1]]
            ]
        when:
            def error1 = net.getError(sample)
            net.train(sample)
            def error2 = net.getError(sample)
            println error1
            println error2
        then:
            error1 > error2
    }

    def "multi layer perceptron for IRIS data"(){
        given:
            def defs = [
                [name: 'fc', count: 4],
                [name: 'si', count: 4],
                [name: 'fc', count: 3],
                [name: 'sm', count: 3]
            ]
            def net = new Net(defs, 4)
            def sample = TestUtil.getIris()
        when:
            def error1 = net.getError(sample)
            net.train(sample)
            def error2 = net.getError(sample)
            println error1
            println error2
        then:
            error1 > error2
    }

    /**
     * 重み勾配の差分近似（difference approximation）により実装が正しいか確認する
     * TODO Net.diagnose()みたいに、メソッド化する方が良いかな？
     * TODO いずれにしても、モーメントや減衰も導入されてくるし、バッチ法／ミニバッチ法／SGDと種類も分かれるので、
     * TODO diagnoseしやすいように、設計する必要がある
     */
    def "diagnose correctness of backprop implementation by difference approximation around weight gradient"() {
        given:
            def defs = [
                [name: 'fc', count: 4],
                [name: 'si', count: 4],
                [name: 'fc', count: 4],
                [name: 'sm', count: 4]
            ]
            def net = new Net(defs, 4)
            def sample = [
                [in: [1, 2, 3, 4], out: [0, 1, 0, 0]],
                [in: [1, 2, 1, 2], out: [1, 0, 0, 0]],
                [in: [4, 3, 2, 1], out: [0, 0, 0, 1]]
            ]
        when:
            net.train(sample, 1) // 適当にtrainして止める
            // set target
            def layer = net.layers[0]
            def inN = layer.inputs[1]
            def outN = layer.outputs[2]

            // target gradient
            def gradW = layer.wd[inN, outN]

            def epsilon = Util.eps * Math.abs(layer.w[inN, outN])

            def beforeE = net.getError(sample)
            layer.w[inN, outN] += epsilon
            def afterE = net.getError(sample)

            def approxGradW = (afterE - beforeE) / epsilon
            println approxGradW
        then:
            true

    }
}