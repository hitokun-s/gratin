package components

/**
 * Unit�ł��A�Ăі��͉��ł�����
 *
 * @author Hitoshi Wada
 */
class Neuron {

    // ��ԕϐ� internal state value
    public double value = 0

    double bias = 0

    // �w�̃����o�[�ɂȂ����Ƃ��̃C���f�b�N�X
    // TODO ugly
    int idx

    @Override
    public String toString(){
        "Neuron:$idx"
    }
}
