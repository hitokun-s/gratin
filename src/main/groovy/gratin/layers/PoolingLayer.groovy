package gratin.layers

import gratin.components.Neuron
import gratin.image.Filter
import gratin.util.Bond
import gratin.util.Matrix
import gratin.util.Matrix3D
import gratin.util.NMatrix3D

/**
 * Pooling Layer
 * ����Bond�̏d��w���Amax�v�[�����O�̂��߂̃t���O�i�ő匋�����P�Ƃ���j�Ƃ��Ďg���Ă���
 * @author Hitoshi Wada
 */
class PoolingLayer extends Layer{

    NMatrix3D inputs
    NMatrix3D outputs
    int windowSize
    int stride
    int channelSize

    PoolingLayer(List<Neuron> inputs, List<Neuron> outputs, Map opt = [:]) {

        super(inputs, outputs)

        this.inputs = new NMatrix3D(inputs, opt.channelCount, opt.in.height, opt.in.width)
        this.outputs = new NMatrix3D(inputs, opt.channelCount, opt.out.height, opt.out.width)

        assert this.inputs.depth == this.outputs.depth // ���̓`���l�������o�̓`���l�����A�ɂȂ�Ȃ��Ƃ�������

        channelSize = opt.channelCount
        windowSize = opt.windowSize ?: 5
        stride = opt.stride ?: 4
        assert (int) ((inputs.row - 1) / stride) + 1 == outputs.row // stride > 1�Ȃ�o�̓T�C�Y�͏k������

        createBond()
    }

    def createBond() {
        // TODO �v�[�����O�w�̏ꍇ�́A�ő�v�[�����O�ł����σv�[�����O�ł��A�v�[�����O�����̉�f�̈����Ɉʒu�ˑ������Ȃ��̂ŁA
        // �{���͂킴�킴Bond���쐬���Ȃ��Ă��ǂ��C������B�B�B
        channelSize.times { int cIdx -> // channel index
            inputs[cIdx].forEachWithIndexByStride(stride) { Neuron v, int center_i, int center_j, int strideX, int strideY ->
                def radius = (windowSize - 1) / 2
                int i = center_i - radius, j = center_j - radius
                for (int m = 0; m < windowSize; m++) {
                    if (i + m < 0 || i + m >= inputs[cIdx].rowCount) continue // �͂ݏo��̈�ɂ��Ă͌����Ȃ��ƒ�`����
                    for (int n = 0; n < windowSize; n++) {
                        if (j + n < 0 || j + n >= inputs[cIdx].colCount) continue // �͂ݏo��̈�ɂ��Ă͌����Ȃ��ƒ�`����
                        new Bond(inputs[cIdx][i + m][j + n], outputs[cIdx][strideX][strideY])
                    }
                }
            }
        }
    }

    def forward() {
        // max pooling �i�ő�v�[�����O�j
        outputs.forEach { Neuron outN ->
            def maxBond = Bond.findAllByE(outN).max { Bond b ->
                b.w = 0 // ���d�݂�������
                b.s.value
            }
            maxBond.w = 1 // backward�̂��߂̖ڈ�
            outN.value = maxBond.s.value
        }
    }

    def backward() {
        inputs.forEach { Neuron inN -> inN.delta = 0 } // �f���^��������

        // �ő�v�[�����O�����ł�backward�ł́Aforward�ōő�Ƃ��đI�΂ꂽ���̓��j�b�g�i�����j�ɁA
        // �o�̓��j�b�g�̃����̂܂܋t�`�d����
        // ����Čv�Z�������̂��߂ɁA���d�݁i0/1�j���g���āAforward�ɂ����āA
        // �ő�v�[�����O�őI�����ꂽBond�̂�w=1�ɂ��A����w=0�ɂ��Ă����B��������I
        outputs.forEach { Neuron outN ->
            def maxBond = Bond.findAllByE(outN).find { it.w == 1 }
            maxBond.s.delta += outN.delta // maxBond.s.delta = outN.delta ���ƁA�d������max�ɂȂ�ꍇ�Ƀ}�Y�C�C������B
        }
    }
}
