package components

import groovy.util.logging.Log4j
import util.Weight
import static util.Util.*

/**
 * ボルツマンマシン
 *
 * @author Hitoshi Wada
 */
@Log4j
class BoltzmannMachine {

    List<Neuron> neurons = [] // ユニットの出力値は、{0,1}
    Weight w // ユニット間相互結合の重み

    double T = 5 // 温度

    public BoltzmannMachine(int unitCnt) {
        unitCnt.times {
            neurons << new Neuron(bias: Math.random())
        }
        w = new Weight(neurons)
    }

    /**
     * 経験分布の平均値と、gibbsSampling値から計算した真の分布の期待値を用いて、
     * 勾配降下法における、重みとバイアスの勾配を計算し、両者を更新する
     */
    def updateWeights() {

    }

    // アルゴリズムの中では直接的には使われないが、
    // エネルギーがこの公式で計算されるという前提で、条件付き確率（遷移確率）の公式が導出される
    // 遷移によって減少する（もしくは維持される）はずなので、
    // 遷移を繰り返して定常分布になったかどうかの判定や、アルゴリズムが正しいかのテストに使えるはず
    public double getEnergy(List<Double> pattern) {
        def tmpValues // 現在値を保存
        if (pattern) {
            tmpValues = getValues()
            setValues(pattern)
        }
        def res = -pairs(neurons, true).sum { List<Neuron> pair ->
            if (pair[0] == pair[1]) return 0 // 自己結合はカウントしない
            w[pair] * pair[0].value * pair[1].value
        } - neurons.sum { Neuron neuron ->
            neuron.value * neuron.bias
        }
        if (pattern) {
            setValues(tmpValues as List<Double>) // 現在値を元に戻しておく
        }
        res
    }

    /**
     * ある１ユニットの値を、確率的に更新する
     * すなわち「[0,1]で生成した乱数 < 遷移確率（条件付き確率）」なら更新する
     * ギブスサンプリングでも、記憶の想起でも使われる
     */
    public void update(Neuron target) {
        // 対称ニューロンの値が0→1に変化したときのエネルギー変化量
        double energyDiff = neurons.sum { Neuron n ->
            w[target, n] * n.value
        } + target.bias
        double p = 1 / (1 + Math.exp(-energyDiff / T)) // 出力値が１である条件付き確率＝遷移確率
        target.value = Math.random() < p ? 1 : 0
    }

    /**
     * ギブスサンプリングによって、所定の重みにおけるボルツマン分布（真の分布）に従うデータをサンプルする
     * ボルツマンマシンではこのサンプルデータを基に、真の分布における特定値の期待値を計算し、
     * それを用いてパラメータ（重みと内部状態）を更新し、対数尤度を最大化するパラメータを決定する
     * ギブスサンプリングはあくまで期待値を少ない計算負担で得る工夫であり、
     * ボルツマンマシン自体の本質というわけではない
     * ユニット数が非常に小さければ、期待値をまともに計算することもできる
     */
    def gibbsSampling() {

    }

    /**
     * （複数の）パターンを記憶させる。
     * サンプルデータがあるボルツマン分布から得られたものだと仮定して、
     * その対数尤度を最大化するため、勾配上昇法により、重みとバイアスを更新する
     */
    public void memorize(List<List<Integer>> patterns) {

        def allPattern = getAllPattern(patterns[0].size())

        def grads = [false]
        def gradsB = [false]
        // すべての重みの勾配の絶対値 < 0.01 になるまで続行する
        def cycle = 1
        while (grads.findAll { it == false }.size() > 0 || gradsB.findAll { it == false }.size() > 1) {
            println "weights update cycle:$cycle"
            cycle++
            grads = []
            gradsB = []
            w.pairs().each { List<Neuron> pair ->

                def sampleAverage = patterns.sum { List<Integer> pattern ->
                    setValues(pattern)
                    pair[0].value * pair[1].value
                } / patterns.size()

                // 真の分布での期待値。全パターンについて、対象統計量 * ボルツマン確率　の和を取る
                // 本当はここでgibbsSamplingの出番だが、今回はまともにやってみる

                def originalE = allPattern.sum { List<Integer> pattern ->
                    setValues(pattern)
                    pair[0].value * pair[1].value * Math.exp(-getEnergy()) // 生起確率の分子だけ先にかけておく
                } / allPattern.sum { List<Integer> pattern ->
                    setValues(pattern)
                    Math.exp(-getEnergy())
                }// 正規化定数で割る

                def gradW = sampleAverage - originalE

                // 重みを更新
                w[pair] += gradW

                grads << (Math.abs(gradW) < 0.01)
            }
            // bias
            neurons.each { neuron ->
                def sampleAverage = patterns.sum { List<Integer> pattern ->
                    setValues(pattern)
                    neuron.value
                } / patterns.size()

                def originalE = allPattern.sum { List<Integer> pattern ->
                    setValues(pattern)
                    neuron.value * Math.exp(-getEnergy()) // 生起確率の分子だけ先にかけておく
                } / allPattern.sum { List<Integer> pattern ->
                    setValues(pattern)
                    Math.exp(-getEnergy())
                }// 正規化定数で割る

                def gradB = sampleAverage - originalE

                // バイアスを更新
                neuron.bias += gradB

                gradsB << (Math.abs(gradB) < 0.05)
            }
            println "likelihood:${getLikelihood(patterns)}"
        }
    }

    // 引数のデータが、現在のボルツマンマシンによって作成された対数尤度を返す
    // 勾配上昇法で重みやバイアスを更新すれば、対数尤度は増加するはず
    public double getLikelihood(List<List<Integer>> patterns) {
        def allPattern = getAllPattern(patterns[0].size())

        def lnZtheta = Math.log(allPattern.sum { List<Integer> pattern ->
            setValues(pattern)
            Math.exp(-getEnergy())
        })

        patterns.sum { List<Integer> pattern ->
            -getEnergy(pattern) - lnZtheta
        }
    }

    /**
     * ボルツマンマシンにおける「記憶の想起」
     * ある初期値を与え、
     * gibbsSamplingと同じ方法で１ユニットずつ更新し、定常状態のデータを得る
     * より生成確率の高いデータに更新されていくはず
     * もし初期値が、経験データにノイズを加えたものであれば、経験データが復元されるはず
     * Simulated Annealing で実行する
     */
    def recall(List<Double> pattern) {
        setValues(pattern)
        T = 3
        double af = 0.95 // annealing factor
        def mfp

        while (T > 0.2) {
            def observedMap = [:]
            10000.times {
                neurons.each { n ->
                    update(n)
                }
                if (it % 20 == 0) {
                    observedMap[values] = observedMap[values] ? observedMap[values] + 1 : 1
                }
            }

            T *= af
            println "Temprature is set to:$T"
            // このサイクル内での最頻パターン（Most Frequent Pattern）
            mfp = observedMap.max { it.value }.key
            println "most frequent pattern:$mfp"
        }
        mfp
    }

    public void setValues(List<Double> values) {
        neurons.eachWithIndex { n, i ->
            n.value = values[i]
        }
    }

    public double[] getValues() {
        neurons*.value as double[]
    }

}
