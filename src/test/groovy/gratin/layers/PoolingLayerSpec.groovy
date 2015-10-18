package gratin.layers

import gratin.components.Neuron
import gratin.image.ImageUtil
import gratin.util.*
import spock.lang.Specification

import javax.imageio.ImageIO
import java.awt.*
import java.awt.image.BufferedImage
import java.util.List


/**
 * @author Hitoshi Wada
 */
class PoolingLayerSpec extends Specification {

    def "constructor"() {
        given:
            def (n1, n2, n3, n4, n5, n6, n7, n8, n9, n10,
                 n11, n12, n13, n14, n15, n16, n17, n18, n19, n20,
                 n21, n22, n23, n24, n25, n26, n27, n28, n29, n30,
                 n31, n32) = (1..32).collect { new Neuron(idx: it) }
            // 入力は２チャネル
            def inputs = new NMatrix3D([
                new NMatrix([
                    [n1, n2, n3, n4],
                    [n5, n6, n7, n8],
                    [n9, n10, n11, n12]
                ]),
                new NMatrix([
                    [n13, n14, n15, n16],
                    [n17, n18, n19, n20],
                    [n21, n22, n23, n24]
                ])
            ])
            // 出力も２チャネル（でなければならない！）
            def outputs = new NMatrix3D([
                new NMatrix([
                    [n25, n26],
                    [n27, n28]
                ]),
                new NMatrix([
                    [n29, n30],
                    [n31, n32]
                ])
            ])
        when:
            def layer = new PoolingLayer(inputs, outputs, [
                windowSize: 3,
                stride    : 2
            ])
        then:
            Bond.findAllByE(n28).size() == 6
            Bond.findAllByE(n28).containsAll([
                Bond.findBySandE(n6, n28),
                Bond.findBySandE(n7, n28),
                Bond.findBySandE(n8, n28),
                Bond.findBySandE(n10, n28),
                Bond.findBySandE(n11, n28),
                Bond.findBySandE(n12, n28)
            ])
    }

}