package gratin.classifier

import spock.lang.Specification


/**
 * @author Hitoshi Wada
 */
class KNNSpec extends Specification {

    def "classify"() {
        given:
            def knn = new KNN([
                [input: [2, -5, 7], output: [1, 0, 0]], // dist square = 1 + 49 + 16 = 66
                [input: [-3, 1, 6], output: [0, 1, 0]],// dist square = 16 + 1 + 9 = 26
                [input: [8, 4, -5], output: [0, 0, 1]]// dist square = 49 + 4 + 64 = 117
            ])
        when:
            def res = knn.classify([1,2,3])
        then:
            res == [0,1,0]
    }
}