package components

import spock.lang.Specification

class RBMSpec extends Specification {

    // updateWeightsAndBiases()は尤度を増加させる
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

    // memorize()は尤度を増加させる
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

    // TODO ときどき想起に失敗してしまう
    def "recall"(){
        // 記録可能なパターン数 ≒ 全素子数（可視素子＋隠れ素子）* 0.14　に注意！
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