package util

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
}