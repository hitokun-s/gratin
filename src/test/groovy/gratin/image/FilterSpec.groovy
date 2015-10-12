package gratin.image

import gratin.util.Matrix
import spock.lang.Specification


/**
 * @author Hitoshi Wada
 */
class FilterSpec extends Specification {

    def "exec"() {
        given:
            def window = new Matrix([
                [0.3, 0.2, 0.1],
                [0.4, 0.3, 0.2],
                [0.5, 0.4, 0.3]
            ])
            def filter = new Filter(window)
            def target = new Matrix([
                [1, 2, 3, 4],
                [2, 3, 4, 5],
                [3, 7, 5, 6],
                [1, 8, 6, 9]
            ])
        when:
            def res = filter.exec(target)
        then:
            res == [
                [2.4, 5.0, 7.1, 6.4],
                [4.9, 9.3, 12.3, 9.7],
                [5.8, 11.4, 16.8, 12.6],
                [3.2, 6.8, 10.5, 7.8],
            ]
    }

}