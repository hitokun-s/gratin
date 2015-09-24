package gratin.components

import groovy.util.logging.Log4j
import spock.lang.Specification

/**
 * @author Hitoshi Wada
 */
@Log4j
class BoltzmannMachineSpec extends Specification {

    def "updateはボルツマン分布に近づける"(){
        given:
            def net = new BoltzmannMachine(10)
            println "weights preparation"
            net.memorize([
                    [1,0,0,1,1,0,0,1,1,1],
                    [0,0,1,0,1,1,1,0,0,0],
                    [0,1,1,1,1,0,1,1,0,1]
            ])
            def pattern = [1,0,0,1,1,0,0,1,1,1] // 1番目でテスト
            def allPattern = getAllPattern(10)
            def pBoltzmann = Math.exp(-net.getEnergy(pattern)) / allPattern.sum{
                Math.exp(-net.getEnergy(it))
            }
            // テスト用パターンの理論的生起確率。対象パターンの発生頻度がこの値に近づいていけば、まあ合格
            println "pBoltzmann:$pBoltzmann"

        when:
            net.setValues(pattern)
            def cnt = 0
            def sampleCnt = 0
            net.T = 3
            double af = 0.95 // annealing factor

            100.times{
                cnt = 0
                sampleCnt = 0
                5000.times{
                    net.neurons.each{n ->
                        net.update(n)
                    }
                    if(it % 5 == 0){
                        sampleCnt++
                        if(net.values == pattern) cnt++
                    }
                }
                if(net.T > 1) {
                    net.T *= af
                }else if(net.T < 1){
                    net.T = 1
                }

                // このサイクル内でのpatternの発生頻度（確率）
                def p = cnt / sampleCnt
                println p
            }
        then:
            false // TODO 発生頻度とボルツマン分布による生起確率が近くならない。。。
    }

    def "recall"(){
        given:
            def net = new BoltzmannMachine(10)
            // 記憶させる
            net.memorize([
                    [1,0,0,1,1,0,0,1,1,1]
            ])
        when:
            def res = net.recall([1,1,0,1,1,0,0,0,1,1]) // 2,8番目をノイズとして反転
        then:
            res == [1,0,0,1,1,0,0,1,1,1]
    }

    def "memorizeは尤度を増加させる"(){
        given:
            def net = new BoltzmannMachine(5)
            def patterns = [
                    [1,0,0,1,1],
                    [0,0,1,0,1]
            ]
        when:
            def likelihood1 = net.getLikelihood(patterns)
            net.memorize(patterns)
            def likelihood2 = net.getLikelihood(patterns)
            println "likelihood1:$likelihood1"
            println "likelihood2:$likelihood2"
        then:
            likelihood2 >= likelihood1
    }

    private List getAllPattern(int unitCnt){
        def list = []
        unitCnt.times{
            list << [0,1]
        }
        list.combinations()
    }
}