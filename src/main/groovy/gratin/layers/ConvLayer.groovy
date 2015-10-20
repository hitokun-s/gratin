package gratin.layers

import gratin.components.Neuron
import gratin.image.Filter
import gratin.util.*

/**
 * Neuron�����ł̏�ݍ��ݑw
 * �B��ő�̃����b�g�́A�������l�ԂɂƂ��ăV���v���Œ��ϓI�ł��邱�ƁB
 * �����֌W���ANeuron���m�̃}�b�v�Ƃ��ċL�����Ă��܂��΁Aforward�^backward�ł́A�����l���Ȃ��Ă悢�B
 * @author Hitoshi Wada
 */
class ConvLayer extends Layer{

    // [��{���j]
    // �t�B���^�͂S�̎����A�t�B���^��ށA���̓`���l���Ax���W�Ay���W�A�����B
    // �t�B���^�d�݋��L�́AsharedWeights[�t�B���^���][���̓`���l��][x���W][y���W]�ɁAbond���X�g���Z�b�g���邱�ƂŎ�������

    NMatrix3D inputs
    NMatrix3D outputs

    Matrix4D filters
    GMatrix4D sharedWeights // filters�Ɠ����`��4D�s��B�����ɏd�݋��L���Ă���Bond���X�g��o�^���Ă���
    int filterTypeCount
    int windowSize
    int stride

    ConvLayer(List<Neuron> inputs, List<Neuron> outputs, Map opt = [:]) {

        super(inputs, outputs)

        // TODO �ݒ�璷�����I
        this.inputs = new NMatrix3D(inputs, opt.channelCount, opt.in.height, opt.in.width)
        this.outputs = new NMatrix3D(inputs, opt.filterTypeCount, opt.out.height, opt.out.width)

        filterTypeCount = opt.filterTypeCount
        int channelSize = opt.channelCount

        if(opt.filters){
            filters = opt.filters
            windowSize = filters.row
        }else{
            windowSize = opt.windowSize ?: 11
            filters = new Matrix4D(filterTypeCount, channelSize, windowSize, windowSize)
        }
        stride = opt.stride ?: 4

        // TODO ugly! stupid! f**k!
        sharedWeights = new GMatrix4D(filterTypeCount, channelSize, windowSize, windowSize) // �����l�͋�z��
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
        // ����Ă邱�Ƃ�FullyConnLayer�ƑS�������I
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
    def update(){
        sharedWeights.forEachWithIndex {List<Bond> bonds, int fIdx, int cIdx, int row, int col ->
            def gradH = bonds.sum{Bond b -> b.wd} //
            def h = filters[fIdx][cIdx][row][col] // �t�B���^�[�d�݌��z = ��������L���Ă��鉼�d�݌��z�̑��a
            def decay = 0.0001 * h
            h -= lr * (gradH + decay) // �t�B���^�[�d�݂��X�V
            filters[fIdx][cIdx][row][col] = h
            bonds*.w = h // �t�B���^�[�d�݂����L���d�݂֔��f
            bonds*.wd = 0 // ���d�݂�������
        }
    }

}
