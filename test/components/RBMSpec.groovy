package components

import spock.lang.Specification

/**
 * @author Hitoshi Wada
 */
class RBMSpec extends Specification {

    // updateWeightsAndBiases()�͖ޓx�𑝉�������
    def "updateWeightsAndBiases() increases likelihood"(){
        given:
            def net = new RBM(3,2)
            def patterns = [
                    [0,0,1],
                    [1,1,0]
            ]
        when:
            def L1 = net.getLikelihood(patterns)
            net.updateWeightsAndBiases(patterns[0])
            def L2 = net.getLikelihood(patterns)
            println "L1:$L1"
            println "L2:$L2"
        then:
            L1 < L2
    }

    // updateWeightsAndBiases()�́AKL���ʂ����ۂ�����
    def "updateWeightsAndBiases() decrease KL divergence"(){
        given:
            def net = new RBM(3,2)
            def patterns = [
                [0,0,1],
                [1,1,0]
            ]
        when:
            def KL1 = net.getKL(patterns)
            net.updateWeightsAndBiases(patterns[0])
            def KL2 = net.getKL(patterns)
            println "KL1:$KL1"
            println "KL2:$KL2"
        then:
            KL1 > KL2
    }

    // memorize()�͖ޓx�𑝉�������
    def "memorize() increases likelihood"(){
        given:
            def net = new RBM(3,2)
            def patterns = [
                [0,0,1],
                [1,1,0]
            ]
        when:
            def L1 = net.getLikelihood(patterns)
            net.memorize(patterns)
            def L2 = net.getLikelihood(patterns)
            println "L1:$L1"
            println "L2:$L2"
        then:
            L1 < L2
    }

    // TODO �Ƃ��ǂ��z�N�Ɏ��s���Ă��܂�
    def "recall"(){
        // �L�^�\�ȃp�^�[���� �� �S�f�q���i���f�q�{�B��f�q�j* 0.14�@�ɒ��ӁI
        given:
            def net = new RBM(5,13)
            def patterns = [
                [0,0,1,0,1],
                [1,1,0,1,0]
            ]
            net.memorize(patterns)
        when:
            def res1 = net.recall([1,1,0,1,1])
            def res2 = net.recall([0,0,1,1,1])
        then:
            res1 == [1,1,0,1,0]
            res2 == [0,0,1,0,1]
    }
}