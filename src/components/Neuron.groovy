package components

/**
 * Unitでも、呼び名は何でもいい
 *
 * @author Hitoshi Wada
 */
class Neuron {

    // 状態変数 internal state value
    public double value = 0

    double bias = 0

    // 層のメンバーになったときのインデックス
    // TODO ugly
    int idx

    @Override
    public String toString(){
        "Neuron:$idx"
    }
}
