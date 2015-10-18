package gratin.util

import spock.lang.Specification


/**
 * @author Hitoshi Wada
 */
class Matrix3DSpec extends Specification {

    def "get each sizes and values"() {
        when:
            def m = new Matrix3D([
                new Matrix([
                    [1, 2, 3, 5],
                    [4, 1, 6, 1],
                    [7, 3, 5, 2]
                ]),
                new Matrix([
                    [2, 3, 6, 7],
                    [1, 6, 9, 4],
                    [3, 5, 4, 2]
                ])
            ])
        then:
            m.depth == 2
            m.row == 3
            m.col == 4
            m[1] == [
                [2, 3, 6, 7],
                [1, 6, 9, 4],
                [3, 5, 4, 2]
            ]
            m[0][1][2] == 6
    }

    def "sumWithDepth"() {
        given:
            def m = new Matrix3D([
                new Matrix([
                    [1, 2, 3],
                    [4, 1, 6],
                    [7, 3, 5]
                ]),
                new Matrix([
                    [2, 3, 6],
                    [1, 6, 9],
                    [3, 5, 4]
                ])
            ])
        when:
            def res = m.sumWithDepth { v, i ->
                v + i
            }
        then:
            res == [
                [4, 6, 10],
                [6, 8, 16],
                [11, 9, 10],
            ]
    }

    def "getEachIndex"() {
        given:
            def m = new Matrix3D([
                new Matrix([
                    [1, 2, 3, 5],
                    [4, 1, 6, 1],
                    [7, 3, 5, 2]
                ]),
                new Matrix([
                    [2, 3, 6, 7],
                    [1, 6, 9, 4],
                    [3, 5, 4, 2]
                ])
            ])
        when:
            def res = m.getEachIndex(16)
        then:
            res == [1, 1, 0]
    }
}