package gratin.layers

import gratin.components.Neuron
import gratin.image.Filter
import gratin.util.*

/**
 * Neuron方式での畳み込み層
 * 唯一最大のメリットは、実装が人間にとってシンプルで直観的であること。
 * 結合関係を、Neuron同士のマップとして記憶してしまえば、forward／backwardでは、何も考えなくてよい。
 * @author Hitoshi Wada
 */
class ConvLayer extends Layer {

    String name = 'cv'

    // [基本方針]
    // フィルタは４つの次元、フィルタ種類、入力チャネル、x座標、y座標、を持つ。
    // フィルタ重み共有は、sharedWeights[フィルタ種類][入力チャネル][x座標][y座標]に、bondリストをセットすることで実現する

    static def defOpts = [
        stride    : 4, // TODO 未使用となっている
        windowSize: 5
    ]

    NMatrix3D inputs
    NMatrix3D outputs

    Matrix4D filters
    GMatrix4D sharedWeights // filtersと同じ形の4D行列。そこに重み共有しているBondリストを登録していく
    int filterTypeCount
    int windowSize
    int stride
    int channelSize

    ConvLayer(List<Neuron> inputs, List<Neuron> outputs, Map opt = [:]) {

        super(inputs, outputs)

        assert inputs[0] instanceof Neuron && outputs[0] instanceof Neuron

        this.inputs = new NMatrix3D(inputs, opt.channelCount, opt.height, opt.width)
        this.outputs = new NMatrix3D(outputs, opt.filterTypeCount, opt.height, opt.width)

        filterTypeCount = opt.filterTypeCount
        channelSize = opt.channelCount

        if (opt.filters) {
            filters = opt.filters
            windowSize = filters.row
        } else {
            windowSize = opt.windowSize ?: defOpts.windowSize
            filters = new Matrix4D(filterTypeCount, channelSize, windowSize, windowSize)
        }
        stride = opt.stride ?: defOpts.stride

        // TODO ugly! stupid! f**k!
        sharedWeights = new GMatrix4D(filterTypeCount, channelSize, windowSize, windowSize) // 初期値は空配列
        createBond()
    }

    def createBond() {
        filters.eachWithIndex { Matrix3D filter, int fIdx -> // filter index
            filter.eachWithIndex { Matrix m, int cIdx -> // channel index
                new Filter(filter[cIdx]).eachConnection(inputs[cIdx]) { int p, int q, int si, int sj, int ei, int ej ->
                    sharedWeights[fIdx][cIdx][p][q] << new Bond(inputs[cIdx][si][sj], outputs[fIdx][ei][ej])
                }
            }
        }
    }

    def forward() {
        outputs.forEach { Neuron outN ->
            outN.value = Bond.findAllByE(outN).sum { Bond b ->
                b.w * b.s.value
            } + outN.bias
        }
    }

    def backward() {
        // やってることはFullyConnLayerと全く同じ！
        inputs.forEach { Neuron inN ->
            inN.delta = Bond.findAllByS(inN).sum { Bond b ->
                b.wd += b.e.delta * inN.value // accumlate weight gradient for batch learning
                b.w * b.e.delta
            }
        }
        // TODO Can I do this here? Funny?
        outputs.forEach { Neuron outN ->
            outN.bias -= 0.1 * outN.delta
        }
    }

    @Override
    def update(int cnt) {
        sharedWeights.forEachWithIndex { List<Bond> bonds, int fIdx, int cIdx, int row, int col ->
            def gradH = bonds.sum { Bond b -> b.wd }/cnt // フィルター重み勾配 = それを共有している仮重み勾配の総和
            def h = filters[fIdx][cIdx][row][col]
            def decay = 0.0001 * h
            h -= lr * (gradH + decay) // フィルター重みを更新
            filters[fIdx][cIdx][row][col] = h
            bonds*.w = h // フィルター重みを共有仮重みへ反映
            bonds*.wd = 0 // 仮重みを初期化
        }
    }

    /**
     * df.optをもとに、df.inputCount, df.outputCountをセットする
     */
    static def setInputAndOutputCount(Map df) {
        assert df.opt
        Map opt = df.opt
        assert opt.channelCount && opt.height && opt.width && opt.filterTypeCount
        df.inputCount = opt.channelCount * opt.height * opt.width
        df.outputCount = opt.filterTypeCount * opt.height * opt.width
    }

    @Override
    Map getInfo() {
        Map res = super.getInfo()
        res.filters = filters
//        res.sharedWeights = sharedWeights
        def sw = []
        sharedWeights.forEachWithIndex { List<Bond> bonds, int fIdx, int cIdx, int row, int col ->
            sw << [bonds: bonds.collect{
                    [s:it.s.idx, e:it.e.idx,w:it.w]
                    },
                   fIdx: fIdx,
                   cIdx: cIdx,
                   row: row,
                   col: col
            ]
        }
        res.sw = sw
        res.filterTypeCount = filterTypeCount
        res.windowSize = windowSize
        res.stride = stride
        res.channelSize = channelSize
        res
    }

    // reflect parameters
    @Override
    public reflect(Map info){
        super.reflect(info)
        sharedWeights.forEachWithIndex { List<Bond> bonds, int fIdx, int cIdx, int row, int col ->
            bonds.clear()
            List bondsInfo = info.sw.find{
                it.fIdx == fIdx && it.cIdx == cIdx && it.row == row && it.col == col
            }.bonds
            bondsInfo.each { Map map ->
                def bond = new Bond(inputs.find{it.idx == map.s}, outputs.find{it.idx == map.e})
                bond.w = map.w
                bonds << bond
            }
        }
    }

}
