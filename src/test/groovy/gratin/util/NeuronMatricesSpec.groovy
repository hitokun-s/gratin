package gratin.util

import spock.lang.Specification


/**
 * @author Hitoshi Wada
 */
class NeuronMatricesSpec extends Specification {

    def "constructor"() {
        when:
            def m = new NeuronMatrices(3, 4, 5)
        then:
            m.size() == 5
            m[0].rowCount == 3
            m[0].colCount == 4
    }
}