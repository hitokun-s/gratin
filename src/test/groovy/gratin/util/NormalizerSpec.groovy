package gratin.util

import spock.lang.Specification

import static gratin.util.TestUtil.nearlyEquals
import static gratin.util.Util.*

/**
 * @author Hitoshi Wada
 */
class NormalizerSpec extends Specification {

    def "general usage"(){
        given:
            def samples = [
                [4,6,8],
                [1,-3,7],
                [-4,9,3]
            ]
        when:
            def n = new Normalizer(samples)
            println samples
            println n([7,8,9])
        then:
            nearlyEquals(avg(samples.collect{it[0]}), 0) // average of col 1 data will be 0
            nearlyEquals(var(samples.collect{it[0]}), 1) // variance of col 1 data will be 1
    }
}