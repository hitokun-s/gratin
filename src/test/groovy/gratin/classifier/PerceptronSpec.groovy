package gratin.classifier

import gratin.components.Net
import spock.lang.Specification


/**
 * @author Hitoshi Wada
 */
class PerceptronSpec extends Specification {

    def "multi layer perceptron"() {
        given:
//            def defs = [
//                [name: 'fc', count: 4],
//                [name: 'si', count: 4],
//                [name: 'fc', count: 4],
//                [name: 'sm', count: 4]
//            ]
//            def net = new Net(defs, 4)
            def perceptron = new Perceptron(4,4,1)
            def sample = [
                [in: [1, 2, 3, 4], out: [0, 1, 0, 0]],
                [in: [1, 2, 1, 2], out: [1, 0, 0, 0]],
                [in: [4, 3, 2, 1], out: [0, 0, 0, 1]]
            ]
        when:
            def error1 = perceptron.getError(sample)
            perceptron.train(sample, 1000)
            def error2 = perceptron.getError(sample)
            println error1
            println error2
        then:
            error1 > error2

    }
}