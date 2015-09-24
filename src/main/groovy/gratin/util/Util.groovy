package gratin.util

import gratin.components.Neuron
import groovy.util.logging.Log4j

/**
 * @author Hitoshi Wada
 */
@Log4j
class Util {

    static{
        File file = new File(this.classLoader.getResource("banner.txt").getFile())
        if(file.exists()){
            println file.text
        }
    }

    // Array utilities
    public static def zeros = { n ->
        new double[n ?: 0]
    }

    public static def randf = { float a, b ->
        Math.random() * (b - a) + a
    }
    public static def randi = { int a, b ->
        Math.floor(Math.random() * (b - a) + a) as int
    }
    public static def randn = { mu, std ->
        mu + Util.gaussRandom() * std
    }

    static Boolean return_v = false
    static def v_val = 0.0

    // syntactic sugar function for getting default parameter values
    public static def getopt = { opt, field_name, default_value ->
        def ret
        if (field_name instanceof String) {
            // case of single string
            ret = opt[field_name] ?: default_value
        } else {
            // assume we are given a list of string instead
            ret = default_value;
            (field_name as String[]).each { f ->
                if (opt[f]) {
                    ret = opt[f] // overwrite return value
                }
            }
        }
        return ret
    }

    private static def gaussRandom = {
        if (return_v) {
            return_v = false;
            return v_val;
        }
        def u = 2 * Math.random() - 1;
        def v = 2 * Math.random() - 1;
        def r = u * u + v * v;
        if (r == 0 || r > 1) return Util.gaussRandom();
        def c = Math.sqrt(-2 * Math.log(r) / r);
        v_val = v * c; // cache this
        return_v = true;
        return u * c;
    }

    public static def dotProduct = { x, y ->
        assert x && y && x.size() == y.size()
        [x, y].transpose().collect { xx, yy -> xx * yy }.sum()
    }

    // looks at a column i of data and guesses what's in it
    // returns results of analysis: is column numeric? How many unique entries and what are they?
    public static def guessColumn = { List data, c ->

//        for(var i=0,n=data.length;i<n;i++) {
//            var v = data[i][c];
//            vs.push(v);
//            if(isNaN(v)) numeric = false;
//        }
        def vs = data.collect { it[c] as String }
        Boolean numeric = (vs.find { !it.isNumber() } == null)

        def u = vs.unique()
        if (!numeric) {
            // if we have a non-numeric we will map it through uniques to an index
            return [numeric: numeric, num: u.size(), uniques: u]
        } else {
            return [numeric: numeric, num: u.size()]
        }
    }

    /**
     * 渡されたListからランダムに１つ選んで返す
     */
    public static def getRandom = { List list ->
        Random rnd = new Random()
        int idx = rnd.nextInt(list.size())
        list[idx]
    }

    // logistic function
    public static double sigma(double d) {
        1 / (1 + Math.exp(-d))
    }

    // derived function of sigma
    public static double sigmad(double d) {
        sigma(d) * (1 - sigma(d))
    }

    /**
     * 0,1からなる全てのパターンを配列で返す。配列の長さは、2の[unitCnt]乗になる
     * 例：unitCnt : 3の場合
     * [[0,0,0],[0,0,1],[0,1,0],[0,1,1],[1,0,0],[1,0,1],[1,1,0],[1,1,1]]
     */
    public static def List getAllPattern(int unitCnt) {
        def list = []
        unitCnt.times {
            list << [0, 1]
        }
        list.combinations()
    }

    public static def List<List<Neuron>> pairs(List<Neuron> neurons, Boolean bidirectional = false) {
        def res = [neurons, neurons].combinations()
        if (!bidirectional) {
            res.unique { List<Neuron> list ->
                list.sort()
            }
        }
        res
    }

    /**
     * average
     */
    public static def avg(List<Double> data){
        data.sum() / data.size()
    }

    /**
     * variance
     */
    public static def var(List<Double> data){
        def avg = avg(data)
        data.sum{(it - avg) * (it - avg)} / data.size()
    }

    /**
     * deviation
     */
    public static def dev(List<Double> data){
        Math.sqrt(var(data))
    }

    public static def normalize(List<Double> data) {
        // とりあえず[0,1]（平均0、分散１）に正規化
        def avg = avg(data)
        def deviation = dev(data)
        data.eachWithIndex { double d, int i ->
            data[i] = (d - avg) / deviation
        }
    }

    public static def List<Neuron> neurons(int cnt) {
        // TODO ugly?
        (0..cnt - 1).collect {
            new Neuron(idx: it)
        }
    }

    /**
     * 計算機イプシロン
     * Machine Epsilon
     * refs : http://www.ibm.com/developerworks/jp/java/library/j-math2.html
     */
//    static double eps = {
//        double s1 = 1.0
//        Math.nextUp(s1) - s1
//    }()
    static double eps = Math.ulp(1.0 as double)
    // this returns same result of above implementation
    // ulp = unit of least precision, unit in the last place （最終桁単位）

}
