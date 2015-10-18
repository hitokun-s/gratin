package gratin.util

import spock.lang.Specification


/**
 * @author Hitoshi Wada
 */
class GMatrix4DSpec extends Specification {

    def "forEachWithIndex"(){
        given:
            def gm = new GMatrix4D(2,3,4,5,0)
        when:
            int cnt = 0
            gm.forEachWithIndex {v,depth2,depth,row,col ->
                cnt++
            }
        then:
            cnt == 2 * 3 * 4 * 5
    }
}