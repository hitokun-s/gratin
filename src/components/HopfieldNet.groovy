package components

import util.Relations

import static util.Util.*

// TODO とりあえず書いてみる。あとで整理する。ネットワークのタイプごとにまとめなおす。
/**
 * Hopfield Network
 * 連想記憶ができるまで実装。deep learningのBolzmann machineの基礎という意味では自己相関記憶が重要？
 * 参考：http://ipr20.cs.ehime-u.ac.jp/~kinoshita/exp3/pdf_file/neuro2.pdf
 */
class HopfieldNet {

    List<Neuron> neurons = [] // ユニットの状態は、{+1, -1}
    Relations weights // ユニット間相互結合の重み

    // この回数だけupdateしてもエネルギー関数が減少しなければ、極小値と判定する
    // TODO 理論的に決定する方法を調べてコンストラクタに実装
    int recallThreshHold = 100

    public HopfieldNet(int unitCnt) {
        unitCnt.times{
            neurons << new Neuron()
        }
        weights = new Relations(neurons, 0)
    }

    /**
     * 非同期更新：ランダムに選んだ１ユニットのみを更新する
     */
    public void update(){
        Neuron neuron = getRandom(neurons)
        // TODO ugly code
        // 重み付け総和
        double weightedSum = weights.getFriends(neuron).sum {Neuron friend ->
            weights.get(friend, neuron) * friend.value
        } - neuron.theta
        neuron.value = weightedSum >= 0 ? 1 : -1
    }

    public double getEnergy(){
        - weights.combinations.sum{ List<Neuron> pair ->
            weights.get(pair[0], pair[1]) * pair[0].value * pair[1].value
        } / 2 + neurons.sum{ Neuron neuron ->
            neuron.value * neuron.theta
        }
    }

    /**
     * （複数の）パターンを記憶させる。具体的には重みを設定する
     */
    public void memorize(List<List<Integer>> patterns){
        patterns.each{List<Integer> pattern ->
            setValues(pattern)
//            neurons.eachWithIndex{ Neuron neuron, int i ->
//                neuron.value = pattern.get(i) as double
//            }
            weights.combinations.each{ List<Neuron> pair ->
                // TODO too ugly, you must die
                weights.set(pair[0], pair[1], weights.get(pair[0], pair[1]) + pair[0].value * pair[1].value)
            }
        }
    }

    /**
     * テストでも便利なはず
     * @param values
     */
    public void setValues(List<Double> values){
        neurons.eachWithIndex{n,i ->
            n.value = values[i]
        }
    }

    public double[] getValues(){
        neurons*.value as double[]
    }

    /**
     * 想起する
     * @return
     */
    public int[] recall(List<Double> input){
        setValues(input)
        double prevE,currE
        int keepCnt = 0 // エネルギーが変化しなかった回数
        while(keepCnt < recallThreshHold){
            prevE = getEnergy()
            update()
            currE = getEnergy()
            assert currE <= prevE // 減少しないとおかしい
            if(currE == prevE){
                keepCnt++
            }else{
                keepCnt = 0
            }
        }
        getValues()
    }
}