package gratin.image

import gratin.util.Matrix
import gratin.util.TestUtil
import spock.lang.Specification

import javax.imageio.ImageIO
import java.awt.Image
import java.awt.image.BufferedImage


/**
 * @author Hitoshi Wada
 */
class ImageUtilSpec extends Specification {

    def "imageToMatrix and matrixToImage"(){
        given:
            File file = new File(TestUtil.getClassLoader().getResource("img/396px-Mona_Lisa.jpg").getFile())
            BufferedImage img = ImageIO.read(file)
        when:
            def res = ImageUtil.imageToMatrix(img)
            ImageIO.write(ImageUtil.matrixToImage(res[0]), "jpg", new File("r.jpg"))
            ImageIO.write(ImageUtil.matrixToImage(res[1]), "jpg", new File("g.jpg"))
            ImageIO.write(ImageUtil.matrixToImage(res[2]), "jpg", new File("b.jpg"))
        then:
            true
    }

    def "filter"(){
        given:
            File file = new File(TestUtil.getClassLoader().getResource("img/396px-Mona_Lisa.jpg").getFile())
            BufferedImage img = ImageIO.read(file)
            def filter = new Matrix([
                [0.1, 0.1, 0.1],
                [0.1, 0.2, 0.1],
                [0.1, 0.1, 0.1]
            ])
        when:
            img = ImageUtil.filter(img, filter)
            ImageIO.write(img, "jpg", new File("out/filtered.jpg"))
        then:
            true
    }
}