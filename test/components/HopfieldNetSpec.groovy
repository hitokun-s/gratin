package components

import spock.lang.Specification

class HopfieldNetSpec extends Specification{

    def "update"(){
        given:
            def net = new HopfieldNet(8)
            net.memorize([
                [1,-1,-1,1,-1,1,1,1,-1,-1],
                [1,1,-1,1,-1,1,-1,-1,-1,1]
            ])
        when:
            net.setValues([1,1,1,1,1,1,1,1,1,1]) // �K���ȓ��͒l
            def eng_before = net.energy
            10.times{
                net.update()
                println net.energy
            }
            def eng_after = net.energy
        then:
        eng_after < eng_before
    }

    def "setValues,getValues"(){
        given:
            def net = new HopfieldNet(5)
        when:
            net.setValues([0,1,-1,-1,1])
            def res = net.getValues()
        then:
            res == [0.0, 1.0, -1.0, -1.0, 1.0]
    }

    def "recall"(){
        given:
            def net = new HopfieldNet(13)
            net.memorize([
                [1,-1,-1,1,-1,1,1,1,-1,-1,1,-1,1],
                [1,1,-1,1,-1,1,-1,-1,-1,1,-1,1,-1]
            ])
        when:
            // �m�C�Y�Ƃ��āA10�Ԗڂ�-1�ɕς��Ă݂�
            def res = net.recall([1,-1,-1,1,-1,1,1,1,-1,1,1,-1,1])
        then:
            // ���̃p�^�[�����z�N�����
            res == [1,-1,-1,1,-1,1,1,1,-1,-1,1,-1,1]
    }

}
