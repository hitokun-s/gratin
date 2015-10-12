package gratin.image

import gratin.util.Matrix

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
    Matrix exec(Matrix m){
        def res = m.clone()
        m.forEach {double v,int i,int j ->
            res[i][j] = (m.partial(i,j,window.size()) * window).sum()
        }
        res
    }
}
