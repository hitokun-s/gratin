package gratin.image

import gratin.util.Matrix

import javax.imageio.ImageIO
import java.awt.Image
import java.awt.image.BufferedImage

/**
 * @author Hitoshi Wada
 */
class ImageUtil {

    public static int a(int c) {
        c >>> 24
    }

    public static int r(int c) {
        c >> 16 & 0xff
    }

    public static int g(int c) {
        c >> 8 & 0xff
    }

    public static int b(int c) {
        c & 0xff
    }

    public static List rgb(int c) {
        [r(c), g(c), b(c)]
    }

    public static int rgb(int r, int g, int b) {
        0xff000000 | r << 16 | g << 8 | b
    }

    public static int argb(int a, int r, int g, int b) {
        a << 24 | r << 16 | g << 8 | b
    }

    /**
     * 画像ファイルの画素情報を、行列群（チャネル毎に行列がある）に変換する
     * @param imgFile
     * @return
     */
    public static List<Matrix> imageToMatrix(BufferedImage img) {

        int w = img.width
        int h = img.height

        Matrix rMatrix = new Matrix(h, w)
        Matrix gMatrix = new Matrix(h, w)
        Matrix bMatrix = new Matrix(h, w)

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                def (r, g, b) = rgb(img.getRGB(x, y))
                rMatrix[y][x] = r
                gMatrix[y][x] = g
                bMatrix[y][x] = b
            }
        }
        [rMatrix, gMatrix, bMatrix]
    }

    /**
     * 輝度値範囲を0-255に変換してグレースケール画像にする
     */
    public static Image matrixToImage(Matrix m) {
        m = m.translate(255,0)
        int w = m.colCount
        int h = m.rowCount
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = rgb(m[y][x] as int, m[y][x] as int, m[y][x] as int)
                img.setRGB(x, y, rgb);
            }
        }
        img
    }

    /**
     * RGB3チャネルのMatrixを、画像に変換する
     * TODO チャネル順がRGBというのが暗黙の了解になっているのは、やっぱりマズイ？
     */
    public static Image matrixToImage(List<Matrix> list) {
        def (r,g,b) = list as Matrix[]
        int h = ((Matrix)r).rowCount
        int w = ((Matrix)r).colCount
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = rgb(r[y][x] as int, g[y][x] as int, b[y][x] as int)
                img.setRGB(x, y, rgb);
            }
        }
        img
    }

    public static Image filter(BufferedImage img, Matrix window){
        List<Matrix> list = imageToMatrix(img)
        def filter = new Filter(window)
        list = list.collect {Matrix m ->
            filter.exec(m)
        }
        matrixToImage(list)
    }
}
