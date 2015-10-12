package gratin.util

import groovy.util.logging.Log4j
import spock.lang.Specification

/**
 * @author Hitoshi Wada
 */
@Log4j
class MatrixSpec extends Specification {

    def "constructor by row and col"() {
        when:
            def m = new Matrix(2, 3)
        then:
            m == [
                [0, 0, 0],
                [0, 0, 0]
            ]
    }

    def "equals"() {
        when:
            def m1 = new Matrix([
                [1, 2],
                [3, 4]
            ])
        then:
            m1 == [
                [1, 2],
                [3, 4]
            ]
            [
                [1, 2],
                [3, 4]
            ] == m1
    }

    def "coordinate access"() {
        given:
            def m = new Matrix([
                [1, 2, 3],
                [4, 5, 6],
                [7, 8, 9]
            ])
        when:
            def a = m[1][1]
        then:
            a == 5
    }

    def "plus and minus operation"() {
        given:
            def m1 = new Matrix([
                [1, 2],
                [3, 4]
            ])
            def m2 = new Matrix([
                [9, 8],
                [7, 6]
            ])
        when:
            def plusResult = m1 + m2
            def minusResult = m1 - m2
        then:
            plusResult == [
                [10, 10],
                [10, 10]
            ]
            minusResult == [
                [-8, -6],
                [-4, -2]
            ]
            m1 == [
                [1, 2],
                [3, 4]
            ]
            m2 == [
                [9, 8],
                [7, 6]
            ]
    }

    def "plus number"() {
        given:
            def m = new Matrix([
                [1, 2],
                [3, 4]
            ])
        when:
            def res = m + 2
        then:
            res == new Matrix([
                [3, 4],
                [5, 6]
            ])
    }

    def "multiply"() {
        given:
            def m1 = new Matrix([
                [1, 2],
                [3, 4]
            ])
            def m2 = new Matrix([
                [9, 8],
                [7, 6]
            ])
            def m3 = new Matrix([
                [1, 2]
            ])
            def m4 = new Matrix([
                [9],
                [7]
            ])
            def m5 = new Matrix([
                [1],
                [3]
            ])
            def m6 = new Matrix([
                [9, 8]
            ])
        when:
            def multiplied12 = m1 * m2
            def multiplied34 = m3 * m4
            def multiplied56 = m5 * m6
        then:
            multiplied12 == [
                [23, 20],
                [55, 48]
            ]
            multiplied34 == [[23]]
            multiplied56 == [
                [9, 8],
                [27, 24]
            ]
    }

    def "multiply number"() {
        given:
            def m = new Matrix([
                [1, 2],
                [3, 4]
            ])
        when:
            def res = m * 2
        then:
            res == new Matrix([
                [2, 4],
                [6, 8]
            ])
    }

    def "div number"() {
        given:
            def m = new Matrix([
                [1, 2],
                [3, 4]
            ])
        when:
            def res = m / 2
        then:
            res == new Matrix([
                [0.5, 1],
                [1.5, 2]
            ])
    }

    def "sum"() {
        given:
            def m = new Matrix([
                [1, 2, 3],
                [4, 5, 6],
                [7, 8, 9]
            ])
        when:
            def res = m.sum()
        then:
            res == 45
    }

    def "max"() {
        given:
            def m = new Matrix([
                [1, 2, 3],
                [4, 9, 6],
                [7, 8, 5]
            ])
        when:
            def res = m.max()
        then:
            res == 9
    }

    def "min"() {
        given:
            def m = new Matrix([
                [5, 2, 3],
                [4, 1, 6],
                [7, 8, 9]
            ])
        when:
            def res = m.min()
        then:
            res == 1
    }

    def "translate"() {
        given:
            def m = new Matrix([
                [5, 2, 3],
                [4, 1, 6],
                [7, 8, 9]
            ])
        when:
            def res = m.translate(5, 1)
        then:
            res == [
                [3, 1.5, 2],
                [2.5, 1, 3.5],
                [4, 4.5, 5]
            ]
    }

    def "forEach"() {
        given:
            def m = new Matrix([
                [5, 2, 3],
                [4, 1, 6],
                [7, 8, 9]
            ])
        when:
            def res = m.forEach { v, i, j ->
                v + 1
            }
        then:
            res == m + 1
    }

    def "partial"() {
        given:
            def m = new Matrix([
                [5, 2, 3, 7, 1, 0, 2],
                [5, 2, 3, 6, 3, 7, 1],
                [4, 0, 2, 1, 6, 2, 9],
                [5, 6, 4, 1, 8, 9, 1],
                [4, 1, 3, 3, 6, 2, 9],
                [1, 0, 7, 8, 9, 2, 4],
                [8, 3, 7, 6, 9, 2, 4],
            ])
        when:
            def res = m.partial(3, 4, 5)
        then:
            res == [
                [3, 6, 3, 7, 1],
                [2, 1, 6, 2, 9],
                [4, 1, 8, 9, 1],
                [3, 3, 6, 2, 9],
                [7, 8, 9, 2, 4]
            ]
    }

}