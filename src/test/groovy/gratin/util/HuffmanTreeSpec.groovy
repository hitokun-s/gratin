package gratin.util

import spock.lang.Specification

/**
 * @author Hitoshi Wada
 */
class HuffmanTreeSpec extends Specification {

    def "encode"() {
        given:
            def sample = ["a", "b", "a", "c", "b", "d", "a", "a", "c", "b"]
        when:
            def tree = new HuffmanTree(sample)
            tree.encode()
        then:
            tree.dict == ["a": "0", "b": "01", "c": "111", "d": "011"]
    }
}