package components

import util.Relations

import static util.Util.*

// TODO �Ƃ肠���������Ă݂�B���ƂŐ�������B�l�b�g���[�N�̃^�C�v���Ƃɂ܂Ƃ߂Ȃ����B
/**
 * Hopfield Network
 * �A�z�L�����ł���܂Ŏ����Bdeep learning��Bolzmann machine�̊�b�Ƃ����Ӗ��ł͎��ȑ��֋L�����d�v�H
 * �Q�l�Fhttp://ipr20.cs.ehime-u.ac.jp/~kinoshita/exp3/pdf_file/neuro2.pdf
 */
class HopfieldNet {

    List<Neuron> neurons = [] // ���j�b�g�̏�Ԃ́A{+1, -1}
    Relations weights // ���j�b�g�ԑ��݌����̏d��

    // ���̉񐔂���update���Ă��G�l���M�[�֐����������Ȃ���΁A�ɏ��l�Ɣ��肷��
    // TODO ���_�I�Ɍ��肷����@�𒲂ׂăR���X�g���N�^�Ɏ���
    int recallThreshHold = 100

    public HopfieldNet(int unitCnt) {
        unitCnt.times{
            neurons << new Neuron()
        }
        weights = new Relations(neurons, 0)
    }

    /**
     * �񓯊��X�V�F�����_���ɑI�񂾂P���j�b�g�݂̂��X�V����
     */
    public void update(){
        Neuron neuron = getRandom(neurons)
        // TODO ugly code
        // �d�ݕt�����a
        double weightedSum = weights.getFriends(neuron).sum {Neuron friend ->
            weights.get(friend, neuron) * friend.value
        } - neuron.theta
        neuron.value = weightedSum >= 0 ? 1 : -1
    }

    public double getEnergy(){
        - weights.combinations.sum{ List<Neuron> pair ->
            weights.get(pair[0], pair[1]) * pair[0].value * pair[1].value
        } / 2 + neurons.sum{ Neuron neuron ->
            neuron.value * neuron.theta
        }
    }

    /**
     * �i�����́j�p�^�[�����L��������B��̓I�ɂ͏d�݂�ݒ肷��
     */
    public void memorize(List<List<Integer>> patterns){
        patterns.each{List<Integer> pattern ->
            setValues(pattern)
//            neurons.eachWithIndex{ Neuron neuron, int i ->
//                neuron.value = pattern.get(i) as double
//            }
            weights.combinations.each{ List<Neuron> pair ->
                // TODO too ugly, you must die
                weights.set(pair[0], pair[1], weights.get(pair[0], pair[1]) + pair[0].value * pair[1].value)
            }
        }
    }

    /**
     * �e�X�g�ł��֗��Ȃ͂�
     * @param values
     */
    public void setValues(List<Double> values){
        neurons.eachWithIndex{n,i ->
            n.value = values[i]
        }
    }

    public double[] getValues(){
        neurons*.value as double[]
    }

    /**
     * �z�N����
     * @return
     */
    public int[] recall(List<Double> input){
        setValues(input)
        double prevE,currE
        int keepCnt = 0 // �G�l���M�[���ω����Ȃ�������
        while(keepCnt < recallThreshHold){
            prevE = getEnergy()
            update()
            currE = getEnergy()
            assert currE <= prevE // �������Ȃ��Ƃ�������
            if(currE == prevE){
                keepCnt++
            }else{
                keepCnt = 0
            }
        }
        getValues()
    }
}