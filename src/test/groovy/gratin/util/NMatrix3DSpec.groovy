package gratin.util

import gratin.components.Neuron
import spock.lang.Specification


/**
 * @author Hitoshi Wada
 */
class NMatrix3DSpec extends Specification {

    def "constructor from neuron List"() {
        given:
            def (n1, n2, n3, n4, n5, n6, n7, n8, n9, n10, n11, n12) = (1..12).collect { new Neuron(idx: it) }
            def source = [n1, n2, n3, n4, n5, n6, n7, n8, n9, n10, n11, n12]
        when:
            def res = new NMatrix3D(source, 2, 2, 3)
        then:
            res.depth == 2
            res.row == 2
            res.col == 3
            res[1][1][1] == n11
    }

    def "forEach"() {
        given:
            def m = new NMatrix3D(2, 3, 4)
        when:
            m.forEach { Neuron n ->
                n.value = 5
            }
        then:
            m[1][2][3].value == 5
    }

    def "toNeurons"() {
        given:
            def (n1, n2, n3, n4, n5, n6, n7, n8, n9, n10, n11, n12) = (1..12).collect { new Neuron(idx: it) }

            def m = new NMatrix3D([
                new NMatrix([
                    [n1, n2, n3],
                    [n4, n5, n6]
                ]),
                new NMatrix([
                    [n7, n8, n9],
                    [n10, n11, n12]
                ]),
            ])
        when:
            def res = m.toNeurons()
        then:
            res == [n1, n2, n3, n4, n5, n6, n7, n8, n9, n10, n11, n12]
    }
}