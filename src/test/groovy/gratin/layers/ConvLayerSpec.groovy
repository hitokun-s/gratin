package gratin.layers

import gratin.components.Neuron
import gratin.image.ImageUtil
import gratin.util.Bond
import gratin.util.Matrix
import gratin.util.Matrix3D
import gratin.util.Matrix4D
import gratin.util.NMatrix
import gratin.util.NMatrix3D
import gratin.util.TestUtil
import spock.lang.Specification

import javax.imageio.ImageIO
import java.awt.Image
import java.awt.image.BufferedImage


/**
 * @author Hitoshi Wada
 */
class ConvLayerSpec extends Specification {

    def "constructor"() {
        given:
            def (n1, n2, n3, n4, n5, n6, n7, n8, n9, n10,
                 n11, n12, n13, n14, n15, n16, n17, n18, n19, n20,
                 n21, n22, n23, n24, n25, n26, n27, n28, n29, n30,
                 n31, n32, n33, n34, n35, n36, n37, n38, n39, n40,
                 n41, n42, n43, n44, n45, n46, n47, n48, n49, n50,
                 n51, n52, n53, n54, n55, n56, n57, n58, n59, n60) = (1..60).collect { new Neuron(idx: it) }
            // 入力は３チャネル
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
                ]),
                new NMatrix([
                    [n25, n26, n27, n28],
                    [n29, n30, n31, n32],
                    [n33, n34, n35, n36]
                ]),
            ])
            // 出力は２チャネル（＝フィルタが2種類）
            def outputs = new NMatrix3D([
                new NMatrix([
                    [n37, n38, n39, n40],
                    [n41, n42, n43, n44],
                    [n45, n46, n47, n48]
                ]),
                new NMatrix([
                    [n49, n50, n51, n52],
                    [n53, n54, n55, n56],
                    [n57, n58, n59, n60]
                ])
            ])
        when:
            def layer = new ConvLayer(inputs, outputs, [
                windowSize: 3,
                stride    : 1
            ])
            def target = (List) (layer.sharedWeights[1][1][2][2])
        then:
            // 「２種類目のフィルタの２番目の入力チャネル用のものの、右下座標の重み」を共有している結合達は？
            target.size() == 6
            target.containsAll([
                Bond.findBySandE(n18, n49),
                Bond.findBySandE(n19, n50),
                Bond.findBySandE(n20, n51),
                Bond.findBySandE(n22, n53),
                Bond.findBySandE(n23, n54),
                Bond.findBySandE(n24, n55)
            ])
            // 1つの出力ユニットは、H * H * K 個の入力ユニットと結合している（H：フィルタwindowサイズ、K：入力チャネル数）
            Bond.findAllByE(n42).size() == 9 * 3
            // ただし、はみ出し領域（パディング領域）にかかる場合は、結合数はちょっと減る
            Bond.findAllByE(n40).size() == 4 * 3 // 角の出力ユニットは、あるフィルタのあるチャネルの4ユニットと結合する
            Bond.findAllByE(n47).size() == 6 * 3 // 辺縁の出力ユニットは、あるフィルタのあるチャネルの6ユニットと結合する
    }

    def "image filtering test"() {
        given:
            File file = new File(TestUtil.getClassLoader().getResource("img/396px-Mona_Lisa.jpg").getFile())
            BufferedImage img = ImageIO.read(file)
            def window = new Matrix([
                [0.1, 0.1, 0.1],
                [0.1, 0.2, 0.1],
                [0.1, 0.1, 0.1]
            ])
            def filters = new Matrix4D(window)

            // RGBを別々の入力として、別々の層で処理する（そうしないと通常の画像フィルタにならない）。各inputは、深さ１の行列
            def (inputR, inputG, inputB) = ImageUtil.imageToNMatrix3D(img).collect{new NMatrix3D(it)}

        when:
            long totalTime = 0
            def (outputR, outputG, outputB) = [inputR, inputG, inputB].collect{NMatrix3D inputs ->
                NMatrix3D outputs = new NMatrix3D(1, inputs.row, inputs.col) // フィルターは1種類にする
                println "convLayer construct!"
                def layer = new ConvLayer(inputs, outputs, [
                    filters: filters,
                    stride : 1
                ])
                println "convLayer forward!"
                long stime = System.currentTimeMillis()
                layer.forward()
                long etime = System.currentTimeMillis()
                totalTime += (etime - stime)
                println "convLayer output!"
                layer.outputs
            }
            println "totalTime:${totalTime}"
            Image output = ImageUtil.nmatrixToImage(new NMatrix3D([outputR[0], outputG[0], outputB[0]]))
            ImageIO.write(output, "jpg", new File("out/filtered.jpg"))
        then:
            true // totalTime:19725 ms = 20 s // そんなに悪くないような？（forward内のshareWeight()の時間を除くと13sくらい）
            // ImageMagickとかだと、何秒くらいなんだろう？
    }
}