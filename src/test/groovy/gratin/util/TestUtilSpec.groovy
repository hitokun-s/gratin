package gratin.util

import gratin.image.ImageUtil
import spock.lang.Specification

import javax.imageio.ImageIO


/**
 * @author Hitoshi Wada
 */
class TestUtilSpec extends Specification {

    def "MNIST"(){
        when:
            List<Map> mnist = TestUtil.getMNIST(100)
            ImageIO.write(ImageUtil.matrixToImage(mnist[0].image), "jpg", new File("mnist0.jpg"))
            ImageIO.write(ImageUtil.matrixToImage(mnist[1].image), "jpg", new File("mnist1.jpg"))
            ImageIO.write(ImageUtil.matrixToImage(mnist[2].image), "png", new File("mnist2.png"))
            ImageIO.write(ImageUtil.matrixToImage(mnist[3].image), "png", new File("mnist3.png"))
        then:
            mnist[0].label == 5
            mnist[1].label == 0
            mnist[2].label == 4
            mnist[3].label == 1

            mnist.size() == 100
            mnist[0].image instanceof Matrix
            mnist[0].image.rowCount == 28
            mnist[0].image.colCount == 28
    }
}