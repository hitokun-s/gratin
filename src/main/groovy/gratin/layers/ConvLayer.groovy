package gratin.layers

import gratin.components.Neuron
import gratin.image.Filter
import gratin.util.Bond
import gratin.util.GMatrix4D
import gratin.util.Matrix
import gratin.util.Matrix3D
import gratin.util.Matrix4D
import gratin.util.NMatrix3D

/**
 * Neuron方式での畳み込み層
 * 唯一最大のメリットは、実装が人間にとってシンプルで直観的であること。
 * 結合関係を、Neuron同士のマップとして記憶してしまえば、forward／backwardでは、何も考えなくてよい。
 * @author Hitoshi Wada
 */
class ConvLayer {

    // 基本方針
    // フィルタは４つの次元、フィルタ種類、入力チャネル、x座標、y座標、を持つ。
    // フィルタ重み共有は、hoge[フィルタ種類][入力チャネル][x座標][y座標]に、bondリストをセットすることで実現する
    // hogeの実体は、四次元行列でも、四深度マップでも、二深度マップ×二元行列でも、何でもいい。

    NMatrix3D inputs
    NMatrix3D outputs

    Matrix4D filters
    GMatrix4D sharedWeights // filtersと同じ形の4D行列。そこに重み共有しているBondリストを登録していく
    int filterTypeCount
    int filterSize
    int stride

    ConvLayer(NMatrix3D inputs, NMatrix3D outputs, Map opt = []) {
        this.inputs = inputs
        this.outputs = outputs

        filterTypeCount = outputs.depth
        int channelSize = inputs.depth

        if(opt.filters){
            filters = opt.filters
            filterSize = filters.row
        }else{
            filterSize = opt.filterSize ?: 11
            filters = new Matrix4D(filterTypeCount, channelSize, filterSize, filterSize)
        }
        stride = opt.stride ?: 4

        // TODO ugly! stupid! f**k!
        sharedWeights = new GMatrix4D(filterTypeCount, channelSize, filterSize, filterSize) // 初期値は空配列
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
        shareWeight() // どこで呼ぶか？
        outputs.forEach { Neuron outN ->
            outN.value = Bond.findAllByE(outN).sum { Bond b ->
                b.w * b.s.value
            } + outN.bias
        }
    }

    def backward() {
        inputs.forEach { Neuron inN ->
            inN.delta = Bond.findAllByS(inN).sum { Bond b ->
                b.w * b.e.delta // TODO こう書くのならば、wはすでに重み共有処理されているべき！
            }
        }
        // TODO Can I do this here? Funny?
        outputs.forEach { Neuron outN ->
            outN.bias -= 0.1 * outN.delta
        }
    }

    /**
     * フィルター重みを、それを共有している各重みに転写する
     */
    public void shareWeight(){
        sharedWeights.forEachWithIndex {List<Bond> bonds, int fIdx, int cIdx, int row, int col ->
            bonds*.w = filters[fIdx][cIdx][row][col]
        }
    }
}
