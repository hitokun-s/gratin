package gratin.image

import gratin.components.Neuron
import gratin.util.Matrix
import gratin.util.NMatrix

/**
 * H * Hのフィルター領域ごとに畳込み（重み積和）を行う
 * 画像処理ソースから呼び出せば、画像フィルターになる（呼び出し元で画像に変換）
 * が、画像専用にしたくないので、Matrix処理器の枠は超えないようにしたい
 * @author Hitoshi Wada
 */
class Filter {
    Matrix window

    Filter(Matrix matrix) {
        this.window = matrix
    }

    /**
     * 引数のMatrixにフィルター処理したMatrixを返す
     */
    Matrix exec(Matrix m) {
        def res = m.clone()
        m.forEach { double v, int i, int j ->
            res[i][j] = (m.partial(i, j, window.size()) * window).sumValue()
        }
        res
    }

    /**
     * Closure引数は、int p, int q, int si, int sj, int ei, int ej
     * - p,q ... フィルタwindow内の座標
     * - si,sj ... 畳み込みの入力画素座標（結合のStart端の意）
     * - ei,ej ... 畳み込みの出力画素座標（結合のEnd端の意）
     */
    void eachConnection(Matrix targetMatrix, Closure cls) {

        targetMatrix.forEach { double v, int center_i, int center_j ->
            int size = window.size()
            def radius = (size - 1) / 2
            int i = center_i - radius, j = center_j - radius
            for (int m = 0; m < size; m++) {
                for (int n = 0; n < size; n++) {
                    if (i + m < 0 || i + m >= targetMatrix.rowCount || j + n < 0 || j + n >= targetMatrix.colCount) {
                        continue // はみ出る領域については結合なしと定義する
                    }
                    cls(m, n, i + m, j + n, center_i, center_j)
                }
            }
        }
    }

    // TODO MatrixとNMatrixを抽象化して１つにまとめたい、べき！
    void eachConnection(NMatrix targetMatrix, Closure cls) {

        targetMatrix.forEachWithIndex { Neuron v, int center_i, int center_j ->
            int size = window.size()
            def radius = (size - 1) / 2
            int i = center_i - radius, j = center_j - radius
            for (int m = 0; m < size; m++) {
                if (i + m < 0 || i + m >= targetMatrix.rowCount) {
                    continue // はみ出る領域については結合なしと定義する
                }
                for (int n = 0; n < size; n++) {
                    if (j + n < 0 || j + n >= targetMatrix.colCount) {
                        continue // はみ出る領域については結合なしと定義する
                    }
                    cls(m, n, i + m, j + n, center_i, center_j)
                }
            }
        }
    }
}
