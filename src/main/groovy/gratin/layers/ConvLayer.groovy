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
 * Neuron�����ł̏�ݍ��ݑw
 * �B��ő�̃����b�g�́A�������l�ԂɂƂ��ăV���v���Œ��ϓI�ł��邱�ƁB
 * �����֌W���ANeuron���m�̃}�b�v�Ƃ��ċL�����Ă��܂��΁Aforward�^backward�ł́A�����l���Ȃ��Ă悢�B
 * @author Hitoshi Wada
 */
class ConvLayer {

    // ��{���j
    // �t�B���^�͂S�̎����A�t�B���^��ށA���̓`���l���Ax���W�Ay���W�A�����B
    // �t�B���^�d�݋��L�́Ahoge[�t�B���^���][���̓`���l��][x���W][y���W]�ɁAbond���X�g���Z�b�g���邱�ƂŎ�������
    // hoge�̎��̂́A�l�����s��ł��A�l�[�x�}�b�v�ł��A��[�x�}�b�v�~�񌳍s��ł��A���ł������B

    NMatrix3D inputs
    NMatrix3D outputs

    Matrix4D filters
    GMatrix4D sharedWeights // filters�Ɠ����`��4D�s��B�����ɏd�݋��L���Ă���Bond���X�g��o�^���Ă���
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
        sharedWeights = new GMatrix4D(filterTypeCount, channelSize, filterSize, filterSize) // �����l�͋�z��
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
        shareWeight() // �ǂ��ŌĂԂ��H
        outputs.forEach { Neuron outN ->
            outN.value = Bond.findAllByE(outN).sum { Bond b ->
                b.w * b.s.value
            } + outN.bias
        }
    }

    def backward() {
        inputs.forEach { Neuron inN ->
            inN.delta = Bond.findAllByS(inN).sum { Bond b ->
                b.w * b.e.delta // TODO ���������̂Ȃ�΁Aw�͂��łɏd�݋��L��������Ă���ׂ��I
            }
        }
        // TODO Can I do this here? Funny?
        outputs.forEach { Neuron outN ->
            outN.bias -= 0.1 * outN.delta
        }
    }

    /**
     * �t�B���^�[�d�݂��A��������L���Ă���e�d�݂ɓ]�ʂ���
     */
    public void shareWeight(){
        sharedWeights.forEachWithIndex {List<Bond> bonds, int fIdx, int cIdx, int row, int col ->
            bonds*.w = filters[fIdx][cIdx][row][col]
        }
    }
}
