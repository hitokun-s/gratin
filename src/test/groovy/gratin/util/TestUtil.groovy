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
}
