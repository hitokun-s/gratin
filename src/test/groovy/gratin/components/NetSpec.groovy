package gratin.components

import gratin.util.Matrix
import gratin.util.Normalizer
import gratin.util.TestUtil
import gratin.util.Util
import groovy.util.logging.Log4j
import org.apache.log4j.Level
import org.apache.log4j.Logger
import spock.lang.Specification
import static gratin.util.Util.*
import static gratin.util.TestUtil.*

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
                [name: 'fc', inputCount:4, outputCount: 4],
                [name: 'sm', outputCount: 4]
            ]
            def net = new Net(defs)
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
                [name: 'fc', inputCount: 4, outputCount: 4],
                [name: 'sm']
            ]
            def net = new Net(defs)
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
            // これだけ最低限の設定でちゃんとネットワークが完成できる！
            def defs = [
                [name: 'fc', inputCount: 4, outputCount : 4],
                [name: 'si'],
                [name: 'fc'],
                [name: 'sm', outputCount: 4]
            ]
            def net = new Net(defs)
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

    /**
     * IRISテストでPCA（正解率）をみよう！
     * TODO 過学習かどうかは別テストでチェック
     */
    def "The percentage of correct answers for whole IRIS data > 98%"() {
        given:
            def defs = [
                [name: 'fc', inputCount: 4, outputCount : 4],
                [name: 'si'],
                [name: 'fc'],
                [name: 'sm', outputCount: 3]
            ]
            def net = new Net(defs)
            def samples = TestUtil.getIris()

            def n = new Normalizer(samples.collect { it.in }) // this changed 'in' data of samples!!
            println samples
        when:
            net.train(samples, 1500)
            def trueOrFalse = samples.collect {
                it.out.findIndexOf { it == 1.0 } == net.predict(it.in)
            }
            def pca = trueOrFalse.count { it } / trueOrFalse.size()
            println pca
        then:
            pca > 0.98
    }

    def "The percentage of correct answers for IRIS cross validation check > 95%"() {
        given:
            def defs = [
                [name: 'fc', inputCount: 4, outputCount : 4],
                [name: 'si'],
                [name: 'fc'],
                [name: 'sm', outputCount: 3]
            ]
            def net = new Net(defs)
            def samples = TestUtil.getIris()
            def division = divide(samples, 0.2)
            def dataForTest = division.test
            def dataForLearn = division.learn
            def n = new Normalizer(dataForLearn.collect { it.in })

        when:
            net.train(dataForLearn, 1000)
            def trueOrFalse = dataForTest.collect {
                it.out.findIndexOf { it == 1.0 } == net.predict(n(it.in))
            }
            println "correct answer / test size = ${trueOrFalse.count { it }} / ${trueOrFalse.size()}"
            def pca = trueOrFalse.count { it } / trueOrFalse.size()
            println pca
        then:
            pca > 0.95
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
                [name: 'fc', inputCount: 4, outputCount : 4],
                [name: 'si'],
                [name: 'fc'],
                [name: 'sm', outputCount: 4]
            ]
            def net = new Net(defs)
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

    def "auto encoder"() {
        given:
            def defs = [
                [name: 'fc', inputCount: 4, outputCount:3],
                [name: 'si'],
                [name: 'fc'],
                [name: 'ms', outputCount: 4]
            ]
            def net = new Net(defs)
            def samples = [
                [in: [1, 2, 3, 4], out: [1, 2, 3, 4]],
                [in: [4, 5, 6, 7], out: [4, 5, 6, 7]],
                [in: [7, 8, 9, 10], out: [7, 8, 9, 10]]
            ]
            def n = new Normalizer(samples.collect { it.in })
            samples.each {
                it.out = it.in
            }
            println samples
        when:
            def error1 = net.getError(samples)
            net.train(samples)
            def error2 = net.getError(samples)
            println "error1:$error1"
            println "error2:$error2"
        then:
            error1 > error2
            nearlyEquals(net.product(n([1, 2, 3, 4])), n([1, 2, 3, 4]))
            nearlyEquals(net.product(n([4, 5, 6, 7])), n([4, 5, 6, 7]))
            nearlyEquals(net.product(n([7, 8, 9, 10])), n([7, 8, 9, 10]))
    }


    def "image classification test"(){
        given:
            List<Map> mnist = TestUtil.getMNIST(10000) // maxValue : 600000 rec
            // data example => mnist[0].image : Matrix, mnist[0].label : 5
            def defs = [
                [name: 'cv', opt:[channelCount:1, height: 28, width:28, filterTypeCount:10]],
                [name: 'si'],
                [name: 'pl', opt:[channelCount:10, in:[height:28, width:28]]],
                [name: 'fc'],
                [name: 'sm', outputCount:10]
            ]
            def net = new Net(defs)
            def vecMap = Util.vecMap([0,1,2,3,4,5,6,7,8,9])
            mnist.each{Map map ->
                map.image = ((Matrix)(map.image)).values
                map.label = vecMap[map.label]
            }
            def n = new Normalizer(mnist.collect { it.image }) // TODO ここでimageの中身も正規化されたものになる！よくない。
            def teachers = mnist.collect{
                [in:it.image, out:it.label]
            }
        when:
            10.times{
                teachers.collate(10).eachWithIndex{List miniBatch, int idx ->
                    log.debug "miniBatch idx:$idx"
//                    assert miniBatch.size() == 10
                    net.train(miniBatch, 3)
                    if(idx % 10 == 0){
                        log.debug "save params!"
                        net.saveParams("mnist.json") // overwrite same file
                    }
                }
            }
        then:
            vecMap[1] == [0,1,0,0,0,0,0,0,0,0]
    }

    def "saveParams"(){
        given:
            def defs = [
                [name: 'cv', opt:[channelCount:1, height: 28, width:28, filterTypeCount:10]],
                [name: 'si'],
                [name: 'pl', opt:[channelCount:10, in:[height:28, width:28]]],
                [name: 'fc'],
                [name: 'sm', outputCount:10]
            ]
            def net = new Net(defs)
        when:
            net.saveParams()
        then:
            true
    }
}