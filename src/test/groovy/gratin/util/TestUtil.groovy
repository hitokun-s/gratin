package gratin.util

/**
 * @author Hitoshi Wada
 */
class TestUtil {

    public static List getIris(){
        File file = new File(TestUtil.getClassLoader().getResource("data/iris.data.txt").getFile())
        Util.process(file, [0, 1, 2, 3], 4)
    }

    public static boolean nearlyEquals(double d1, int d2){
        (d1 as Double).round(10) == d2
    }
}
