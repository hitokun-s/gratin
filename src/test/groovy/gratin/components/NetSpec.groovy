package gratin.components

import gratin.layers.ConvLayer
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
                [name: 'fc', inputCount: 4, outputCount: 4],
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
                [name: 'fc', inputCount: 4, outputCount: 4],
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
                [name: 'fc', inputCount: 4, outputCount: 4],
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
                [name: 'fc', inputCount: 4, outputCount: 4],
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
                [name: 'fc', inputCount: 4, outputCount: 4],
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
                [name: 'fc', inputCount: 4, outputCount: 3],
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

    def "vec to label"() {
        when:
            List<Map> mnist = TestUtil.getMNIST(5)
            def vecMap = Util.vecMap([0, 1, 2, 3, 4, 5, 6, 7, 8, 9])
            mnist.each { Map map ->
                map.image = ((Matrix) (map.image)).values
                map.label = vecMap[map.label]
            }
            def teachers = mnist.collect {
                [in: it.image, out: it.label]
            }
        then:
            teachers[0].out == [0, 0, 0, 0, 0, 1, 0, 0, 0, 0]
            teachers[0].out.indexOf(1 as double) == 5
    }

    def "MNIST prediction test with 3000 teacher data results in pca > 70%"() {
        given:
            List<Map> mnist = TestUtil.getMNIST(5000) // maxValue : 600000 rec
            // data example => mnist[0].image : Matrix, mnist[0].label : 5
            def defs = [
                [name: 'cv', opt: [channelCount: 1, height: 28, width: 28, filterTypeCount: 10]],
                [name: 'si'],
                [name: 'pl', opt: [channelCount: 10, in: [height: 28, width: 28]]],
                [name: 'fc'],
                [name: 'sm', outputCount: 10]
            ]
            def net = new Net(defs)
            def vecMap = Util.vecMap([0, 1, 2, 3, 4, 5, 6, 7, 8, 9])
            mnist.each { Map map ->
                map.image = ((Matrix) (map.image)).values
                map.label = vecMap[map.label]
            }
            def n = new Normalizer(mnist.collect { it.image }) // TODO ここでimageの中身も正規化されたものになる！よくない。
            def teachers = mnist.collect {
                [in: it.image, out: it.label]
            }
        when:
            1.times {
                // 最初の3000サンプルだけで訓練
                teachers[0..2999].collate(10).eachWithIndex { List miniBatch, int idx ->
                    log.debug "miniBatch idx:$idx"
                    net.train(miniBatch, 1)
                }
            }
            println "predict:${net.predict(teachers[0].in)}, answer:${teachers[0].out.indexOf(1 as double)}" // 5
            println "predict:${net.predict(teachers[1].in)}, answer:${teachers[1].out.indexOf(1 as double)}" // 0
            println "predict:${net.predict(teachers[2].in)}, answer:${teachers[2].out.indexOf(1 as double)}" // 4
            println "predict:${net.predict(teachers[3].in)}, answer:${teachers[3].out.indexOf(1 as double)}" // 1

            log.debug "Let's go to the test data with a lot of prayer..."
            def pca = net.getPCA(teachers[4000..4999]) // 未知データでテスト
            log.debug "pca:$pca"
        then:
            pca > 0.7
    }

    // パラメータ参考 : http://mochajl.readthedocs.org/en/latest/tutorial/mnist.html
    def "MNIST prediction test with 10000 teacher data results in pca > 80%"() {
        given:
            List<Map> mnist = TestUtil.getMNIST(11000) // maxValue : 600000 rec
            // data example => mnist[0].image : Matrix, mnist[0].label : 5
            def defs = [
                [name: 'cv', opt: [channelCount: 1, height: 28, width: 28, filterTypeCount: 20]],
                [name: 'si'],
                [name: 'pl', opt: [channelCount: 20, in: [height: 28, width: 28], stride: 2, windowSize: 5]],
                [name: 'fc'],
                [name: 'sm', outputCount: 10]
            ]
            def net = new Net(defs)
            def vecMap = Util.vecMap([0, 1, 2, 3, 4, 5, 6, 7, 8, 9])
            mnist.each { Map map ->
                map.image = ((Matrix) (map.image)).values
                map.label = vecMap[map.label]
            }
            def n = new Normalizer(mnist.collect { it.image }) // TODO ここでimageの中身も正規化されたものになる！よくない。
            def teachers = mnist.collect {
                [in: it.image, out: it.label]
            }
        when:
            1.times {
                teachers[0..9999].collate(20).eachWithIndex { List miniBatch, int idx ->
                    log.debug "miniBatch idx:$idx"
                    net.train(miniBatch, 1)
                }
            }
            // 【teachers[0..4999].collate(20).eachWithIndex{...での実験メモ】
            // avg costは、50周目を超えたあたりから、たまーに２を切るようになる。
            // 100周目を越えたあたりから、たまーに１を切るようになる。150を過ぎるとたいてい１を切るようになる。
            // が、200周目を超えても、たまに１以上になるときはある（平均0.7くらい？）。うーん。。。
            println "predict:${net.predict(teachers[0].in)}, answer:${teachers[0].out.indexOf(1 as double)}" // 5
            println "predict:${net.predict(teachers[1].in)}, answer:${teachers[1].out.indexOf(1 as double)}" // 0
            println "predict:${net.predict(teachers[2].in)}, answer:${teachers[2].out.indexOf(1 as double)}" // 4
            println "predict:${net.predict(teachers[3].in)}, answer:${teachers[3].out.indexOf(1 as double)}" // 1

            // 【teachers[0..9999].collate(50).eachWithIndex{...での実験メモ】
            // コストは「増えたり減ったり」ではなく、ゆっくり確実に減っていく、という印象。
            // バッチサイズを増やして平均しているのだから、それは当然。
            // 「teachers[0..4999].collate(20)」のとき、最後の方は平均コストは0.7くらいで、正解率は0.91だった。
            // ということは、その正解率を上回るには、最後の方の平均コストは、0.6とか0.5になっていないといけないはず。

            log.debug "Let's go to the test data with a lot of prayer..."
            def pca = net.getPCA(teachers[10000..10999]) // 未知データでテスト
            log.debug "pca:$pca"
            // recorded 0.91 <=  teachers[0..4999].collate(20).eachWithIndex{...
            // recorded 0.896 <=  teachers[0..9999].collate(50).eachWithIndex{...pooling : [stride:3, windowSize:3]
        then:
            pca >= 0.8
    }

    def "classification error happens about same data or not"() {
        given:
            List<Map> mnist = TestUtil.getMNIST(11000) // maxValue : 600000 rec
            def defs = [
                [name: 'cv', opt: [channelCount: 1, height: 28, width: 28, filterTypeCount: 15]],
                [name: 'si'],
                [name: 'pl', opt: [channelCount: 15, in: [height: 28, width: 28], stride: 3, windowSize: 5]],
                [name: 'fc'],
                [name: 'sm', outputCount: 10]
            ]
            def net = new Net(defs)
            def vecMap = Util.vecMap([0, 1, 2, 3, 4, 5, 6, 7, 8, 9])
            mnist.each { Map map ->
                map.image = ((Matrix) (map.image)).values
                map.label = vecMap[map.label]
            }
            def n = new Normalizer(mnist.collect { it.image }) // TODO ここでimageの中身も正規化されたものになる！よくない。
            def teachers = mnist.collect {
                [in: it.image, out: it.label, mnistIdx :it.mnistIdx]
            }
        when:
            // teachers[0..2999]で学習して[10000..10099]をテストする、というのを3回やってみたが、大体同じサンプルについてエラーになった。
            // 次の段階として、teachers[0..2999]で学習しても、{3000..5999}で学習しても、もし
            // teachers[10000..10099]について同じデータでエラーになるとしたら、そもそもそれらは学習が難しい（境界的）サンプルということになる。
            // よって、それらをあらかじめ選別して、前もって学習させたのち、従来の学習を行えば、正解率が改善される可能性がある。
            // さらにいえば、プレ学習に用いた弱点データを含むデータでテストして改善されるのは当たり前のような気がするので、
            // 理想的には、弱点データ都は別のテストデータで検証する必要がある。
            // とはいえ、弱点データは、異なる学習によって共通して生じたエラーデータ＝普遍的に弱点となるデータなので、
            // それをカバーする学習はある程度普遍性をもち、どのようなデータについても効果がある気がする。

            // プレ学習
            def preTeachers = teachers[
                10012, 10028,10038,10042,10044,10055,10059,10061,10064,10073,10089,10096
            ]
            log.debug "Let's pre-train!"
//            net.train(preTeachers, 1)

            teachers[3000..5999].collate(20).eachWithIndex { List miniBatch, int idx ->
                log.debug "miniBatch idx:$idx"
                net.train(miniBatch, 1)
//                net.train(preTeachers, 1)
            }
            log.debug "Let's go to the test data with a lot of prayer..."
            def pca = net.getPCA(teachers[9000..9099]) // 未知データ、かつプレ学習対象外のデータでテスト
            log.debug "pca:$pca"
        then:
            pca >= 0.7
    }

    def "MNIST prediction test based on ConvNetJS"() {
        given:
            List<Map> mnist = TestUtil.getMNIST(11000) // maxValue : 600000 rec
            // data example => mnist[0].image : Matrix, mnist[0].label : 5
            def defs = [
                [name: 'cv', opt: [channelCount: 1, height: 28, width: 28, filterTypeCount: 8, windowSize: 5]],
                [name: 'si'],
                [name: 'pl', opt: [channelCount: 8, in: [height: 28, width: 28], stride: 2, windowSize: 2]],
                [name: 'cv', opt: [channelCount: 8, height: 14, width: 14, filterTypeCount: 16, windowSize: 5]],
                [name: 'si'],
                [name: 'pl', opt: [channelCount: 16, in: [height: 14, width: 14], stride: 2, windowSize: 3]],
                [name: 'fc'],
                [name: 'sm', outputCount: 10]
            ]
            def net = new Net(defs)
            net.layers*.l2decay_rate = 0.001 // same as convNetJS
            def vecMap = Util.vecMap([0, 1, 2, 3, 4, 5, 6, 7, 8, 9])
            mnist.each { Map map ->
                map.image = ((Matrix) (map.image)).values
                map.label = vecMap[map.label]
            }
            def n = new Normalizer(mnist.collect { it.image }) // TODO ここでimageの中身も正規化されたものになる！よくない。
            def teachers = mnist.collect {
                [in: it.image, out: it.label]
            }
        when:
            1.times {
                teachers[0..4999].collate(20).eachWithIndex { List miniBatch, int idx ->
                    log.debug "miniBatch idx:$idx"
                    net.train(miniBatch, 1)
                }
            }
            // （トライ１）70周目くらいからavg costが、2.5に接近し、80周目で2.0に接近し、85周目で初めて２を切った。
            // （トライ２）85周目で2.5を切るようになり、107周から2を切るようになり、126周目で初めて1.5を切った。
            // 150周くらいまで、順調にコストは低下していく。
            // 100周だと、正解率は56%くらい
            // 150周だと、正解率は66%くらい
            println "predict:${net.predict(teachers[0].in)}, answer:${teachers[0].out.indexOf(1 as double)}" // 5
            println "predict:${net.predict(teachers[1].in)}, answer:${teachers[1].out.indexOf(1 as double)}" // 0
            println "predict:${net.predict(teachers[2].in)}, answer:${teachers[2].out.indexOf(1 as double)}" // 4
            println "predict:${net.predict(teachers[3].in)}, answer:${teachers[3].out.indexOf(1 as double)}" // 1

            log.debug "Let's go to the test data with a lot of prayer..."
            def pca = net.getPCA(teachers[10000..10999]) // 未知データでテスト
            log.debug "pca:$pca"
        then:
            pca >= 0.8
    }


    def "saveParams"() {
        given:
            def defs = [
                [name: 'cv', opt: [channelCount: 1, height: 28, width: 28, filterTypeCount: 10]],
                [name: 'si'],
                [name: 'pl', opt: [channelCount: 10, in: [height: 28, width: 28]]],
                [name: 'fc'],
                [name: 'sm', outputCount: 10]
            ]
            def net = new Net(defs)
        when:
            net.saveParams()
        then:
            true
    }

    def "readParams and construct"() {
        given:
            List<Map> mnist = TestUtil.getMNIST(100)
            def vecMap = Util.vecMap([0, 1, 2, 3, 4, 5, 6, 7, 8, 9])
            mnist.each { Map map ->
                map.image = ((Matrix) (map.image)).values
                map.label = vecMap[map.label]
            }
            def n = new Normalizer(mnist.collect { it.image }) // TODO ここでimageの中身も正規化されたものになる！よくない。
            def teachers = mnist.collect {
                [in: it.image, out: it.label]
            }
        when:
            def net = new Net("mnist.json")
            teachers.collate(10).eachWithIndex { List miniBatch, int idx ->
                log.debug "miniBatch idx:$idx"
                net.train(miniBatch, 5)
            }
            println net.predict(teachers[0].in) // label : 5
            println net.predict(teachers[1].in) // label : 0
            println net.predict(teachers[2].in) // label : 4
            println net.predict(teachers[3].in) // label : 1
        then:
            net.layers.size() == 5

    }
}