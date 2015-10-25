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
            List<Matrix> mnist = TestUtil.getMNIST(100)
            ImageIO.write(ImageUtil.matrixToImage(mnist[0]), "jpg", new File("mnist0.jpg"))
            ImageIO.write(ImageUtil.matrixToImage(mnist[1]), "jpg", new File("mnist1.jpg"))
            ImageIO.write(ImageUtil.matrixToImage(mnist[2]), "png", new File("mnist2.png"))
            ImageIO.write(ImageUtil.matrixToImage(mnist[3]), "png", new File("mnist3.png"))
        then:
            mnist.size() == 100
            mnist[0] instanceof Matrix
            mnist[0].rowCount == 28
            mnist[0].colCount == 28
    }
}