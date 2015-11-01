package gratin.layers

import gratin.components.Neuron
import gratin.image.Filter
import gratin.util.Bond
import gratin.util.Matrix
import gratin.util.Matrix3D
import gratin.util.NMatrix3D

/**
 * Pooling Layer
 * 結合Bondの重みwを、maxプーリングのためのフラグ（最大結合を１とする）として使っている
 * @author Hitoshi Wada
 */
class PoolingLayer extends Layer {

    String name = 'pl'

    static def defOpts = [
        stride    : 4,
        windowSize: 5
    ]

    NMatrix3D inputs
    NMatrix3D outputs
    int windowSize
    int stride
    int channelSize

    PoolingLayer(List<Neuron> inputs, List<Neuron> outputs, Map opt = [:]) {

        super(inputs, outputs)

        this.inputs = new NMatrix3D(inputs, opt.channelCount, opt.in.height, opt.in.width)
        this.outputs = new NMatrix3D(outputs, opt.channelCount, opt.out.height, opt.out.width)

        assert this.inputs.depth == this.outputs.depth // 入力チャネル数＝出力チャネル数、にならないとおかしい

        channelSize = opt.channelCount
        windowSize = opt.windowSize ?: defOpts.windowSize
        stride = opt.stride ?: defOpts.stride
        assert (int) ((this.inputs.row - 1) / stride) + 1 == this.outputs.row // stride > 1なら出力サイズは縮小する

        createBond()
    }

    def createBond() {
        // TODO プーリング層の場合は、最大プーリングでも平均プーリングでも、プーリング窓内の画素の扱いに位置依存性がないので、
        // 本当はわざわざBondを作成しなくても良い気がする。。。
        channelSize.times { int cIdx -> // channel index
            inputs[cIdx].forEachWithIndexByStride(stride) { Neuron v, int center_i, int center_j, int strideX, int strideY ->
                def radius = (windowSize - 1) / 2
                int i = center_i - radius, j = center_j - radius
                for (int m = 0; m < windowSize; m++) {
                    if (i + m < 0 || i + m >= inputs[cIdx].rowCount) continue // はみ出る領域については結合なしと定義する
                    for (int n = 0; n < windowSize; n++) {
                        if (j + n < 0 || j + n >= inputs[cIdx].colCount) continue // はみ出る領域については結合なしと定義する
                        new Bond(inputs[cIdx][i + m][j + n], outputs[cIdx][strideX][strideY])
                    }
                }
            }
        }
    }

    def forward() {
        // max pooling （最大プーリング）
        outputs.forEach { Neuron outN ->
            def maxBond = Bond.findAllByE(outN).max { Bond b ->
                b.w = 0 // 仮重みを初期化
                b.s.value
            }
            maxBond.w = 1 // backwardのための目印
            outN.value = maxBond.s.value
        }
    }

    def backward() {
        inputs.forEach { Neuron inN -> inN.delta = 0 } // デルタを初期化

        // 最大プーリング方式でのbackwardでは、forwardで最大として選ばれた入力ユニット（だけ）に、
        // 出力ユニットのδがそのまま逆伝播する
        // よって計算効率化のために、仮重み（0/1）を使って、forwardにおいて、
        // 最大プーリングで選択されたBondのみw=1にし、他はw=0にしておく。すっきり！
        outputs.forEach { Neuron outN ->
            def maxBond = Bond.findAllByE(outN).find { it.w == 1 }
            maxBond.s.delta += outN.delta // maxBond.s.delta = outN.delta だと、重複してmaxになる場合にマズイ気がする。
        }
    }

    /**
     * df.inputCount, df.outputCount など必要パラメータをセットする
     */
    static def setInputAndOutputCount(Map df) {
        assert df.opt
        Map opt = df.opt
        assert opt.channelCount && opt.in && opt.in.width && opt.in.height // 必須パラメータ
        if(!opt.stride) opt.stride = defOpts.stride
        if(!opt.windowSize) opt.windowSize = defOpts.windowSize
        if(!df.inputCount){
            df.inputCount = opt.channelCount * opt.in.height * opt.in.width
        }
        if(!opt.out){
            opt.out = [
                height : (int) ((opt.in.height - 1) / opt.stride) + 1,
                width : (int) ((opt.in.width - 1) / opt.stride) + 1
            ]
            df.outputCount = opt.channelCount * opt.out.height * opt.out.width
        }
    }

    @Override
    Map getInfo(){
        Map res = super.getInfo()
        res.windowSize = windowSize
        res.stride = stride
        res.channelSize = channelSize
        res
    }

}
