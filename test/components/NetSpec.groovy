package components

import spock.lang.Specification

/**
 * @author Hitoshi Wada
 */
class NetSpec extends Specification {

    def "train can decrease Error"() {
        given:
            def defs = [
                [name: 'fc', count: 4],
                [name: 'sm', count: 4]
            ]
            def net = new Net(defs, 4)
            def sample = [
                [in: [1, 2, 3, 4], out: [0, 1, 0, 0]],
                [in: [1, 2, 1, 2], out: [1, 0, 0, 0]],
                [in: [4, 3, 2, 1], out: [0, 0, 0, 1]]
            ]
        when:
            def error1 = net.getError(sample)
            net.train(sample)
            def error2 = net.getError(sample)
            println net.predict([1, 2, 3, 4])
            println net.predict([1, 2, 1, 2])
            println net.predict([4, 3, 2, 1])
        then:
            error1 > error2
    }

    def "predict"() {
        given:
            def defs = [
                [name: 'fc', count: 4],
                [name: 'sm', count: 4]
            ]
            def net = new Net(defs, 4)
            def sample = [
                [in: [1, 2, 3, 4], out: [0, 1, 0, 0]],
                [in: [1, 2, 1, 2], out: [1, 0, 0, 0]],
                [in: [4, 3, 2, 1], out: [0, 0, 0, 1]]
            ]
            net.train(sample)
        when:
            def res1 = net.predict([1, 2, 3, 4])
            def res2 = net.predict([1, 2, 1, 2])
            def res3 = net.predict([4, 3, 2, 1])
        then:
            res1 == 1
            res2 == 0
            res3 == 3
    }
}