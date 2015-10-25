package gratin.util

import org.spockframework.util.Assert

import java.nio.ByteBuffer

/**
 * @author Hitoshi Wada
 */
class TestUtil {

    public static List getIris(){
        File file = new File(TestUtil.getClassLoader().getResource("data/iris.data.txt").getFile())
        Util.process(file, [0, 1, 2, 3], 4)
    }

    public static List<Matrix> getMNIST(int limit = 60000){
        def stream = new FileInputStream(TestUtil.getClassLoader().getResource("data/train-images.idx3-ubyte").getFile())
        byte[] header = new byte[16]
        stream.read(header)
        assert ByteBuffer.wrap(header[0..3] as byte[]).int == 0x00000803 // magic number
        assert ByteBuffer.wrap(header[4..7] as byte[]).int == 60000 // number of images
        assert ByteBuffer.wrap(header[8..11] as byte[]).int == 28 // number of rows
        assert ByteBuffer.wrap(header[12..15] as byte[]).int == 28 // number of cols

        int cnt = 0
        List<Matrix> res = []
        while(cnt++ < limit){
            // 1 byte(unsigned byte) for 1 pixel of greyscale, 1 image is made of 28 * 28 pixels
            byte[] tmp = new byte[28 * 28]
            stream.read(tmp)
            // refs : http://stackoverflow.com/questions/4266756/can-we-make-unsigned-byte-in-java
            int[] tmp2 = tmp.collect{byte unsigned ->
                (int)(unsigned & 0xFF)
            }
            res << new Matrix((tmp2 as List<Double>).collate(28))
        }
        stream.close()
        res
    }

    public static boolean nearlyEquals(double d1, int d2){
        (d1 as Double).round(10) == d2
    }

    public static boolean nearlyEquals(double d1, double d2){
        println "d1:$d1, d2:$d2"
        (d1 as Double).round(10) == (d1 as Double).round(10)
    }

    public static boolean nearlyEquals(List<Double> list1, List<Double> list2){
        list1.eachWithIndex{double v, int idx ->
            if(!nearlyEquals(v, list2[idx])) {
                println "Not Equal! : $v in $list1 vs ${list2[idx]} in $list2"
                return false
            }
        }
        true
    }

//    private static final char[] HEX = [
//        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
//    ]
//    public static String toHex(byte[] bytes){
//        final int nBytes = bytes.length;
//        char[] result = new char[2 * nBytes];
//        int j = 0;
//        for (int i = 0; i < nBytes; i++) {
//            // Char for top 4 bits
//            result[j++] = HEX[(0xF0 & bytes[i]) >>> 4];
//            // Bottom 4
//            result[j++] = HEX[(0x0F & bytes[i])];
//        }
//        new String(result)
//    }

}
