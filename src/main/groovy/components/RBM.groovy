package components

import groovy.util.logging.Log4j
import util.Weight

import static util.Util.*

/**
 * Restricted Boltzmann Machine
 *
 * @author Hitoshi Wada
 */
@Log4j
class RBM {

    List<Neuron> visibleNeurons = []
    List<Neuron> hiddenNeurons = []

    Weight w // ユニット間相互結合の重み

    double lr = 0.1 // 勾配上昇法の学習率
    int T = 1 // CD法での反復回数

    public RBM(int visibleUnitCnt, int hiddenUnitCnt) {
        visibleUnitCnt.times {
            visibleNeurons << new Neuron(bias: Math.random(), idx: it)
        }
        hiddenUnitCnt.times {
            hiddenNeurons << new Neuron(bias: Math.random(), idx: it)
        }
        w = new Weight(visibleNeurons, hiddenNeurons)
    }

    /**
     * あるサンプルデータについて、CD法により重みとバイアスを更新する
     * @return [weightGrads : [], biasGrads : []] 勾配量の配列（勾配上昇法の収束判定用）
     */
    public Map updateWeightsAndBiases(List<Double> values) {

        def weightGrads = []
        def biasGrads = []

        setVisibleValues(values)

        // 初期値を一時保存
        def v0 = visibleValues
        def p0 = hiddenNeurons.collect { getConditionedProbability(it) } // 隠れ素子の（値＝１となる）確率値の配列
        updateHiddenValues()

        T.times {
            updateVisibleValues()
            updateHiddenValues()
        }

        def vT = visibleValues
        def pT = hiddenNeurons.collect { getConditionedProbability(it) }

        [visibleNeurons, hiddenNeurons].combinations().each { List pair ->
            Neuron v = pair[0]
            Neuron h = pair[1]
            def gradW = lr * (v0[v.idx] * p0[h.idx] - vT[v.idx] * pT[h.idx])
            weightGrads << gradW
            w[v, h] += gradW
        }

        visibleNeurons.each {
            def biasGrad = lr * (v0[it.idx] - vT[it.idx])
            biasGrads << biasGrad
            it.bias += biasGrad
        }
        hiddenNeurons.each {
            def biasGrad = lr * (p0[it.idx] - pT[it.idx])
            biasGrads << biasGrad
            it.bias += biasGrad
        }
        [weightsGrads: weightGrads, biasGrads: biasGrads]
    }

    /**
     * 与えられた全サンプルデータについて、CD法により尤度を最大化する
     */
    def memorize(List<List<Double>> patterns) {
        def TH = 0.00001
        boolean shouldContinue = true

        // 全ての勾配が閾値以下になるまで、サンプルデータを巡回して重みとバイアスを更新する
        while (shouldContinue) {
            println "Let's go to update cycle!"
            patterns.each { List<Double> pattern ->
                def grads = updateWeightsAndBiases(pattern)
                println grads
                shouldContinue = grads.weightGrads.findAll { Math.abs(it) > TH }.size() > 0 || grads.biasGrads.findAll {
                    Math.abs(it) > TH
                }.size() > 0
            }
        }
    }

    /**
     * 隠れ層が与えられたときの、ある可視素子の（値＝１の）条件付確率を返す、または、
     * 可視層が与えられたときの、ある隠れ素子の（値＝１の）条件付確率を返す
     * RBMの性質上、これらは完全に対称なので１メソッドにまとめた。
     */
    def getConditionedProbability(Neuron n) {
        def partners = visibleNeurons.contains(n) ? hiddenNeurons : visibleNeurons
        // TODO 惰性で命名してしまった。ほんとにエネルギー変化分になるかは要検証
        double energyDiff = n.bias + partners.sum { Neuron p ->
            w[n, p] * p.value
        }
        // ロジスティック関数
        sigma(energyDiff)
    }

    public double getEnergy(List<Double> pattern) {
        -visibleNeurons.sum { Neuron n ->
            n.bias * n.value
        } - hiddenNeurons.sum { Neuron n ->
            n.bias * n.value
        } - [visibleNeurons, hiddenNeurons].combinations().sum { List<Neuron> pair ->
            if (pair[0] == pair[1]) return 0
            w[pair[0], pair[1]] * pair[0].value * pair[1].value
        }
    }

    /**
     * 引数のデータが、現在のボルツマンマシンによって作成された対数尤度を返す
     * 勾配上昇法で重みやバイアスを更新すれば、対数尤度は増加するはず
     * 動作チェックやテストに使う
     */
    public double getLikelihood(List<List<Integer>> patterns) {

        def allVisiblePatterns = getAllPattern(visibleNeurons.size())
        def allHiddenPatterns = getAllPattern(hiddenNeurons.size())

        // 正規化定数
        def Z = 0
        allVisiblePatterns.each { visiblePattern ->
            setVisibleValues(visiblePattern)
            allHiddenPatterns.each { hiddenPattern ->
                setHiddenValues(hiddenPattern)
                Z += Math.exp(-getEnergy())
            }
        }

        // 「v,hについてのボルツマン確率分布をhで周辺化（積算）したもの」の対数を、各サンプル毎に計算して和を取れば良い
        // のだが、ちょっと計算順序を変えている
        patterns.sum { List<Integer> pattern ->
            setVisibleValues(pattern)
            def marginAboutHidden = allHiddenPatterns.sum { List<Integer> hiddenPattern ->
                setHiddenValues(hiddenPattern)
                Math.exp(-getEnergy())
            }
            Math.log(marginAboutHidden)
        } - patterns.size() * Z
    }

    public List<Double> recall(List<Double> pattern) {
        setVisibleValues(pattern)
        hiddenNeurons.each { Neuron n ->
            n.value = getConditionedProbability(n) >= 0.5 ? 1 : 0
        }
        def allNeurons = visibleNeurons + hiddenNeurons

        boolean isChanged = true
        while (isChanged) {
            isChanged = false
            (allNeurons.size() * 20).times {
                Neuron n = getRandom(allNeurons)
                def tmp = n.value
                n.value = getConditionedProbability(n) >= 0.5 ? 1 : 0
                if (tmp != n.value) isChanged = true
            }
        }
        visibleValues
    }

    /**
     * KL情報量（Kullback-Leibler Divergence）
     * Closure渡しする抽象的なメソッドとしてUtilに切り出したい気もする
     */
    public double getKL(List<List<Double>> patterns) {

        // getlikelihood()と相当かぶっている。うまくまとめたいもの。

        def allVisiblePatterns = getAllPattern(visibleNeurons.size())
        def allHiddenPatterns = getAllPattern(hiddenNeurons.size())

        // 正規化定数
        def Z = 0
        allVisiblePatterns.each { visiblePattern ->
            setVisibleValues(visiblePattern)
            allHiddenPatterns.each { hiddenPattern ->
                setHiddenValues(hiddenPattern)
                Z += Math.exp(-getEnergy())
            }
        }

        // patterns内に重複はないと仮定
        patterns.sum { List<Integer> pattern ->
            setVisibleValues(pattern)
            // 付与データにおけるこのpatternの生起確率
            def q = 1 / patterns.size()
            // 可視素子がpatternになる確率＝ボルツマン確率を隠れ素子の全パターンで周辺化
            def marginAboutHidden = allHiddenPatterns.sum { List<Integer> hiddenPattern ->
                setHiddenValues(hiddenPattern)
                Math.exp(-getEnergy())
            }
            // ボルツマンマシンとしての、このpatternの生起確率
            def p = Math.log(marginAboutHidden) / Z
            q * (Math.log(q) - Math.log(p))
        }
    }

    public void updateVisibleValues() {
        visibleNeurons.each { Neuron n ->
            n.value = Math.random() < getConditionedProbability(n) ? 1 : 0
        }
    }

    public void updateHiddenValues() {
        hiddenNeurons.each { Neuron n ->
            n.value = Math.random() < getConditionedProbability(n) ? 1 : 0
        }
    }

    public void setVisibleValues(List<Double> values) {
        visibleNeurons.eachWithIndex { n, i ->
            n.value = values[i]
        }
    }

    public void setHiddenValues(List<Double> values) {
        hiddenNeurons.eachWithIndex { n, i ->
            n.value = values[i]
        }
    }

    public double[] getVisibleValues() {
        visibleNeurons*.value as double[]
    }

    public double[] getHiddenValues() {
        hiddenNeurons*.value as double[]
    }
}