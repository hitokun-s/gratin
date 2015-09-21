package layers

import components.Neuron
import util.Weight

import static util.Util.*

/**
 * SoftMax Layer
 * FullyConnectedLayer�ƕ�����������̕����]�܂�����������Ȃ����A
 * �����ł͂킩��₷���̂��߂Ɉ�̌^�Ŏ������Ă���
 * �����^�ƈ�̌^�𗼎҂��������Ă݂Ĕ�ׂ邱�ƂŁA���ǂ��݌v�������Ă��邩������Ȃ�
 * TODO ���̂ւ񐮗��ł����疼�O�ς��悤
 */
class SoftmaxLayer {

    // �o�C�A�X���Ȃ��悤��Softmax�̌v�Z�Ƀj���[������o�ꂳ����̂́A�傰��������H�H
    Weight w
    List<Neuron> inputs = []
    List<Neuron> outputs = []

    public SoftmaxLayer(int inCnt, int outCnt) {
        inCnt.times {
            inputs << new Neuron(bias: Math.random(), idx: it)
        }
        outCnt.times {
            outputs << new Neuron(bias: Math.random(), idx: it)
        }
        w = new Weight(inputs, outputs)
    }

    /**
     * �N���Xk�̃j���[�����֐ڑ�����A�f�[�^�x�N�g���Əd�݃x�N�g���̓���
     * �{���c�}���}�V���Ƃ̃A�i���W�[�ɂ��֋X�I��Energy�ƌĂ�ł���
     */
    public double getEnergy(List<Double> data, int k) {
        Neuron outN = outputs[k]
        inputs.sum { Neuron inN ->
            w[inN, outN] * inN.value
        }
    }

    /**
     * ���K���萔��Ԃ��B�e��Q�l�����ł́AZ(��)�ɂȂ��Ă���B�uz�v�ŃA�N�Z�X�ł���͕̂֗������ǁA��������
     * @return
     */
    public double getZ() {
        def Z = 0 // ���K���萔
        outputs.size().times { idx ->
            Z += Math.exp(getEnergy(inputValues, idx))
        }
        Z
    }

    /**
     * �^�������̓f�[�^���N���Xk�ik:�N���X�C���f�b�N�X�j�ł���m���i��Softmax�֐��̏o�͒l�j
     */
    public double getProbability(List<Double> data, int k) {
        inputValues = data // �K�i���萔��"z"�ŌĂׂ�悤�ɂ���ɂ́A���O�ɓ��͂��Z�b�g���Ȃ��Ƃ����Ȃ��B����͂�����Ɗ댯�B
        Math.exp(getEnergy(data, k)) / z
    }

    /**
     * @return �d�ݍX�V�ʁi���z�j
     */
    public double updateWeights(Neuron inN, Neuron outN, List<Map> data) {
        // �S�d�݂����񂲂ƂɃ����_���ȏ��ɕ��ёւ��āA����X�V���Ă����̂��ǂ��炵���H
        // ���̘b�Ɗm���I�~���@�i���P�f�[�^�݂̂ɂ��čX�V�j�Ƃ̊֌W�͂ǂ��Ȃ񂾂낤�H
        def k = outN.idx
        def gradW = data.sum {
            inputValues = it.input
            (getProbability(it.input, k) - it.output[k]) * inN.value
        }
        println "gradW:$gradW"
        w[inN, outN] -= gradW
        gradW
    }

    /**
     * @param List < [ input:List < Double > , output:List<Double>]>
     */
    public void train(List<Map> data) {

        // ���蔲��

        Neuron inN = getRandom(inputs)
        Neuron outN = getRandom(outputs)

        while (Math.abs(updateWeights(inN, outN, data)) > 0.01) {
            inN = getRandom(inputs)
            outN = getRandom(outputs)
        }
    }

    /**
     * �Ƃ肠�����N���X�������C���f�b�N�X��Ԃ��悤�ɂ���
     * @param input
     * @return
     */
    public int predict(List<Double> input) {
        inputValues = input
        outputs.max { Neuron outN ->
            inputs.sum { Neuron inN ->
                w[inN, outN] * inN.value
            }
        }.idx
        // max�ƂȂ�m���l�����M�x�Ƃ��Ď擾���Ă����������H�܂��ޓx�ł������B�B�B
    }

    /**
     * �ޓx�i���̑ΐ��ޓx�j��Ԃ��B���������������̃e�X�g�p�B�w�K����Ό���͂��B
     * @param List < [ input:List < Double > , output:List<Double>]>
     */
    public double getLikelihood(List<Map> data) {
        // ���ރN���X��1�`K�܂�K����Ƃ��āA�P�T���v���f�[�^���N���Xk�ɕ��ނ����Ƃ��̖ޓx�͒��ڌv�Z����ƁA
        // �i1 - �N���X1�̊m���j* �i1 - �N���X2�̊m���j* ... *�i�N���Xk�̊m���j* ... * (1 - �N���XK�̊m���j
        // �Ƃ����m���ςɂȂ�B����ɂ��ꂪ�A�T���v���f�[�^�������|�����킳���
        // �ΐ������ƁA�V�O�}��2��o�ꂷ�邱�ƂɂȂ�
        -data.sum { sample ->
            outputs.sum { Neuron outN ->
                def k = outN.idx
                if (sample.output[k] == 1) {
                    return Math.log(getProbability(sample.input, k))
                } else {
                    return Math.log(1 - getProbability(sample.input, k))
                }
            }
        }
    }

    public List<Double> getInputValues() {
        inputs*.value
    }

    public void setInputValues(List<Double> values) {
        inputs.eachWithIndex { n, i ->
            n.value = values[i]
        }
    }

    public List<Double> getOutputValues() {
        outputs*.value
    }
}