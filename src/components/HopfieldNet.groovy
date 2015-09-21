package components

import util.Weight

import static util.Util.getRandom
import static util.Util.pairs

// TODO とりあえず書いてみる。あとで整理する。ネットワークのタイプごとにまとめなおす。
/**
 * Hopfield Network
 * 連想記憶ができるまで実装。deep learningのBolzmann machineの基礎という意味では自己相関記憶が重要？
 * 参考：http://ipr20.cs.ehime-u.ac.jp/~kinoshita/exp3/pdf_file/neuro2.pdf
 *
 * @author Hitoshi Wada
 */
class HopfieldNet {

    List<Neuron> neurons = [] // ユニットの状態は、{+1, -1}
    Weight w // ユニット間相互結合の重み

    // この回数だけupdateしてもエネルギー関数が減少しなければ、極小値と判定する
    // TODO 理論的に決定する方法を調べてコンストラクタに実装
    int recallThreshHold = 100

    public HopfieldNet(int unitCnt) {
        unitCnt.times {
            neurons << new Neuron(idx: it)
        }
        w = new Weight(neurons)
    }

    /**
     * 非同期更新：ランダムに選んだ１ユニットのみを更新する
     * TODO 同期更新（全ユニットをまとめて更新）も実装しておくべき？
     */
    public void update() {
        Neuron neuron = getRandom(neurons)
        // 重み付け総和
        double weightedSum = neurons.sum { Neuron n ->
            w[n, neuron] * n.value
        } - neuron.bias
        // いわゆるsign関数
        neuron.value = weightedSum >= neuron.bias ? 1 : -1
    }

    /**
     * エネルギーを返す。式に注意。すべてのユニット組み合わせ（対角線）だけ加算するのではなく、
     * 総当たり重複組み合わせ（ユニットi->jをみた組み合わせ≠ユニットjからiをみた組み合わせは別物としてカウント）
     * そうしないとエネルギー関数が単調減少にならない
     * @return
     */
    public double getEnergy() {
        -pairs(neurons, true).sum { List<Neuron> pair ->
            if (pair[0] == pair[1]) return 0 // 自己結合はカウントしない
            w[pair] * pair[0].value * pair[1].value
        } / 2 + neurons.sum { Neuron neuron ->
            neuron.value * neuron.bias
        }
    }

    /**
     * （複数の）パターンを記憶させる。具体的には重みを設定する
     */
    public void memorize(List<List<Integer>> patterns) {
        patterns.each { List<Integer> pattern ->
            setValues(pattern)
            pairs(neurons, true).each { List<Neuron> pair ->
                w[pair] += pair[0].value * pair[1].value
            }
        }
    }

    /**
     * テストでも便利なはず
     * @param values
     */
    public void setValues(List<Double> values) {
        neurons.eachWithIndex { n, i ->
            n.value = values[i]
        }
    }

    public double[] getValues() {
        neurons*.value as double[]
    }

    /**
     * 想起する
     * @return
     */
    public int[] recall(List<Double> input) {
        setValues(input)
        double prevE, currE
        int keepCnt = 0 // エネルギーが変化しなかった回数
        while (keepCnt < recallThreshHold) {
            prevE = getEnergy()
            update()
            currE = getEnergy()
            assert currE <= prevE // 減少しないとおかしい
            if (currE == prevE) {
                keepCnt++
            } else {
                keepCnt = 0
            }
        }
        getValues()
    }
}