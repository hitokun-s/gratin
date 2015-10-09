package gratin.util

import spock.lang.Specification

/**
 * @author Hitoshi Wada
 */
class NeuronMatrixSpec extends Specification {

    def "constructor from Matrix"() {
        when:
            def m = new NeuronMatrix(new Matrix([
                [1, 2, 3],
                [4, 5, 6],
                [7, 8, 9]
            ]))
        then:
            m.colCount == 3
            m.rowCount == 3
            m[1][1].value == 5
    }

    def "partial"() {
        given:
            def m = new NeuronMatrix(new Matrix([
                [0, 1, 2, 3, 4],
                [5, 6, 7, 8, 9],
                [1, 2, 3, 4, 5],
                [6, 7, 8, 9, 0],
                [2, 3, 4, 5, 6]
            ]))
        when:
            def res = m.partial(1, 2, 3)
        then:
            res instanceof Matrix
            res == [
                [7, 8, 9],
                [3, 4, 5],
                [8, 9, 0]
            ]
    }
}