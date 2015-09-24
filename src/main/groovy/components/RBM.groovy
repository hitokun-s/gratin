package components

import groovy.util.logging.Log4j
import util.Weight

import static util.Util.*

/**
 * Restricted Boltzmann Machine
 *
 * @author Hitoshi Wada
 */
@Log4j
class RBM {

    List<Neuron> visibleNeurons = []
    List<Neuron> hiddenNeurons = []

    Weight w // ���j�b�g�ԑ��݌����̏d��

    double lr = 0.1 // ���z�㏸�@�̊w�K��
    int T = 1 // CD�@�ł̔�����

    public RBM(int visibleUnitCnt, int hiddenUnitCnt) {
        visibleUnitCnt.times {
            visibleNeurons << new Neuron(bias: Math.random(), idx: it)
        }
        hiddenUnitCnt.times {
            hiddenNeurons << new Neuron(bias: Math.random(), idx: it)
        }
        w = new Weight(visibleNeurons, hiddenNeurons)
    }

    /**
     * ����T���v���f�[�^�ɂ��āACD�@�ɂ��d�݂ƃo�C�A�X���X�V����
     * @return [weightGrads : [], biasGrads : []] ���z�ʂ̔z��i���z�㏸�@�̎�������p�j
     */
    public Map updateWeightsAndBiases(List<Double> values) {

        def weightGrads = []
        def biasGrads = []

        setVisibleValues(values)

        // �����l���ꎞ�ۑ�
        def v0 = visibleValues
        def p0 = hiddenNeurons.collect { getConditionedProbability(it) } // �B��f�q�́i�l���P�ƂȂ�j�m���l�̔z��
        updateHiddenValues()

        T.times {
            updateVisibleValues()
            updateHiddenValues()
        }

        def vT = visibleValues
        def pT = hiddenNeurons.collect { getConditionedProbability(it) }

        [visibleNeurons, hiddenNeurons].combinations().each { List pair ->
            Neuron v = pair[0]
            Neuron h = pair[1]
            def gradW = lr * (v0[v.idx] * p0[h.idx] - vT[v.idx] * pT[h.idx])
            weightGrads << gradW
            w[v, h] += gradW
        }

        visibleNeurons.each {
            def biasGrad = lr * (v0[it.idx] - vT[it.idx])
            biasGrads << biasGrad
            it.bias += biasGrad
        }
        hiddenNeurons.each {
            def biasGrad = lr * (p0[it.idx] - pT[it.idx])
            biasGrads << biasGrad
            it.bias += biasGrad
        }
        [weightsGrads: weightGrads, biasGrads: biasGrads]
    }

    /**
     * �^����ꂽ�S�T���v���f�[�^�ɂ��āACD�@�ɂ��ޓx���ő剻����
     */
    def memorize(List<List<Double>> patterns) {
        def TH = 0.00001
        boolean shouldContinue = true

        // �S�Ă̌��z��臒l�ȉ��ɂȂ�܂ŁA�T���v���f�[�^�����񂵂ďd�݂ƃo�C�A�X���X�V����
        while (shouldContinue) {
            println "Let's go to update cycle!"
            patterns.each { List<Double> pattern ->
                def grads = updateWeightsAndBiases(pattern)
                println grads
                shouldContinue = grads.weightGrads.findAll { Math.abs(it) > TH }.size() > 0 || grads.biasGrads.findAll {
                    Math.abs(it) > TH
                }.size() > 0
            }
        }
    }

    /**
     * �B��w���^����ꂽ�Ƃ��́A������f�q�́i�l���P�́j�����t�m����Ԃ��A�܂��́A
     * ���w���^����ꂽ�Ƃ��́A����B��f�q�́i�l���P�́j�����t�m����Ԃ�
     * RBM�̐�����A�����͊��S�ɑΏ̂Ȃ̂łP���\�b�h�ɂ܂Ƃ߂��B
     */
    def getConditionedProbability(Neuron n) {
        def partners = visibleNeurons.contains(n) ? hiddenNeurons : visibleNeurons
        // TODO �Đ��Ŗ������Ă��܂����B�ق�ƂɃG�l���M�[�ω����ɂȂ邩�͗v����
        double energyDiff = n.bias + partners.sum { Neuron p ->
            w[n, p] * p.value
        }
        // ���W�X�e�B�b�N�֐�
        sigma(energyDiff)
    }

    public double getEnergy(List<Double> pattern) {
        -visibleNeurons.sum { Neuron n ->
            n.bias * n.value
        } - hiddenNeurons.sum { Neuron n ->
            n.bias * n.value
        } - [visibleNeurons, hiddenNeurons].combinations().sum { List<Neuron> pair ->
            if (pair[0] == pair[1]) return 0
            w[pair[0], pair[1]] * pair[0].value * pair[1].value
        }
    }

    /**
     * �����̃f�[�^���A���݂̃{���c�}���}�V���ɂ���č쐬���ꂽ�ΐ��ޓx��Ԃ�
     * ���z�㏸�@�ŏd�݂�o�C�A�X���X�V����΁A�ΐ��ޓx�͑�������͂�
     * ����`�F�b�N��e�X�g�Ɏg��
     */
    public double getLikelihood(List<List<Integer>> patterns) {

        def allVisiblePatterns = getAllPattern(visibleNeurons.size())
        def allHiddenPatterns = getAllPattern(hiddenNeurons.size())

        // ���K���萔
        def Z = 0
        allVisiblePatterns.each { visiblePattern ->
            setVisibleValues(visiblePattern)
            allHiddenPatterns.each { hiddenPattern ->
                setHiddenValues(hiddenPattern)
                Z += Math.exp(-getEnergy())
            }
        }

        // �uv,h�ɂ��Ẵ{���c�}���m�����z��h�Ŏ��Ӊ��i�ώZ�j�������́v�̑ΐ����A�e�T���v�����Ɍv�Z���Ęa�����Ηǂ�
        // �̂����A������ƌv�Z������ς��Ă���
        patterns.sum { List<Integer> pattern ->
            setVisibleValues(pattern)
            def marginAboutHidden = allHiddenPatterns.sum { List<Integer> hiddenPattern ->
                setHiddenValues(hiddenPattern)
                Math.exp(-getEnergy())
            }
            Math.log(marginAboutHidden)
        } - patterns.size() * Z
    }

    public List<Double> recall(List<Double> pattern) {
        setVisibleValues(pattern)
        hiddenNeurons.each { Neuron n ->
            n.value = getConditionedProbability(n) >= 0.5 ? 1 : 0
        }
        def allNeurons = visibleNeurons + hiddenNeurons

        boolean isChanged = true
        while (isChanged) {
            isChanged = false
            (allNeurons.size() * 20).times {
                Neuron n = getRandom(allNeurons)
                def tmp = n.value
                n.value = getConditionedProbability(n) >= 0.5 ? 1 : 0
                if (tmp != n.value) isChanged = true
            }
        }
        visibleValues
    }

    /**
     * KL���ʁiKullback-Leibler Divergence�j
     * Closure�n�����钊�ۓI�ȃ��\�b�h�Ƃ���Util�ɐ؂�o�������C������
     */
    public double getKL(List<List<Double>> patterns) {

        // getlikelihood()�Ƒ������Ԃ��Ă���B���܂��܂Ƃ߂������́B

        def allVisiblePatterns = getAllPattern(visibleNeurons.size())
        def allHiddenPatterns = getAllPattern(hiddenNeurons.size())

        // ���K���萔
        def Z = 0
        allVisiblePatterns.each { visiblePattern ->
            setVisibleValues(visiblePattern)
            allHiddenPatterns.each { hiddenPattern ->
                setHiddenValues(hiddenPattern)
                Z += Math.exp(-getEnergy())
            }
        }

        // patterns���ɏd���͂Ȃ��Ɖ���
        patterns.sum { List<Integer> pattern ->
            setVisibleValues(pattern)
            // �t�^�f�[�^�ɂ����邱��pattern�̐��N�m��
            def q = 1 / patterns.size()
            // ���f�q��pattern�ɂȂ�m�����{���c�}���m�����B��f�q�̑S�p�^�[���Ŏ��Ӊ�
            def marginAboutHidden = allHiddenPatterns.sum { List<Integer> hiddenPattern ->
                setHiddenValues(hiddenPattern)
                Math.exp(-getEnergy())
            }
            // �{���c�}���}�V���Ƃ��ẮA����pattern�̐��N�m��
            def p = Math.log(marginAboutHidden) / Z
            q * (Math.log(q) - Math.log(p))
        }
    }

    public void updateVisibleValues() {
        visibleNeurons.each { Neuron n ->
            n.value = Math.random() < getConditionedProbability(n) ? 1 : 0
        }
    }

    public void updateHiddenValues() {
        hiddenNeurons.each { Neuron n ->
            n.value = Math.random() < getConditionedProbability(n) ? 1 : 0
        }
    }

    public void setVisibleValues(List<Double> values) {
        visibleNeurons.eachWithIndex { n, i ->
            n.value = values[i]
        }
    }

    public void setHiddenValues(List<Double> values) {
        hiddenNeurons.eachWithIndex { n, i ->
            n.value = values[i]
        }
    }

    public double[] getVisibleValues() {
        visibleNeurons*.value as double[]
    }

    public double[] getHiddenValues() {
        hiddenNeurons*.value as double[]
    }
}