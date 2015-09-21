package components

import spock.lang.Specification

/**
 * @author Hitoshi Wada
 */
class HopfieldNetSpec extends Specification{

    def "update"(){
        given:
            def net = new HopfieldNet(8)
            net.memorize([
                [1,-1,-1,1,-1,1,1,1,-1,-1],
                [1,1,-1,1,-1,1,-1,-1,-1,1]
            ])
        when:
            net.setValues([1,1,1,1,1,1,1,1,1,1]) // 適当な入力値
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
            // 理論によれば、正しく記憶できるパターン数は、「ニューロンユニット数×0.15」
            // よって２パターンを想起させるためには、2 / 0.15 = 13.3個のユニットが必要なはず
            def net = new HopfieldNet(13)
            net.memorize([
                [1,-1,-1,1,-1,1,1,1,-1,-1,1,-1,1],
                [1,1,-1,1,-1,1,-1,-1,-1,1,-1,1,-1]
            ])
        when:
            // ノイズとして、10番目を-1に変えてみる
            // TODO 正しく想起できるノイズの割合の理論値は？
            def res = net.recall([1,-1,-1,1,-1,1,1,1,-1,1,1,-1,1])
        then:
            // 元のパターンが想起される
            res == [1,-1,-1,1,-1,1,1,1,-1,-1,1,-1,1]
    }
}