package util

import components.Neuron

class Util {
    // Array utilities
    public static def zeros = { n ->
        new double[n?:0]
    }

    public static def randf = { float a, b ->
        Math.random()*(b-a)+a
    }
    public static def randi = { int a, b ->
        Math.floor(Math.random()*(b-a)+a) as int
    }
    public static def randn = { mu, std ->
        mu + Util.gaussRandom()*std
    }

    static Boolean return_v = false
    static def v_val = 0.0

    // syntactic sugar function for getting default parameter values
    public static def getopt = { opt, field_name, default_value ->
        def ret
        if( field_name instanceof String) {
            // case of single string
            ret = opt[field_name] ?: default_value
        } else {
            // assume we are given a list of string instead
            ret = default_value;
            (field_name as String[]).each{ f ->
                if (opt[f]) {
                    ret = opt[f] // overwrite return value
                }
            }
        }
        return ret
    }

    private static def gaussRandom = {
        if(return_v) {
            return_v = false;
            return v_val;
        }
        def u = 2*Math.random()-1;
        def v = 2*Math.random()-1;
        def r = u*u + v*v;
        if(r == 0 || r > 1) return Util.gaussRandom();
        def c = Math.sqrt(-2*Math.log(r)/r);
        v_val = v*c; // cache this
        return_v = true;
        return u*c;
    }

    public static def dotProduct = { x, y ->
        assert x && y && x.size() == y.size()
        [x, y].transpose().collect{ xx, yy -> xx * yy }.sum()
    }

    // looks at a column i of data and guesses what's in it
    // returns results of analysis: is column numeric? How many unique entries and what are they?
    public static def guessColumn = { List data, c ->

//        for(var i=0,n=data.length;i<n;i++) {
//            var v = data[i][c];
//            vs.push(v);
//            if(isNaN(v)) numeric = false;
//        }
        def vs = data.collect{it[c] as String}
        Boolean numeric = (vs.find{!it.isNumber()} == null)

        def u = vs.unique()
        if(!numeric) {
            // if we have a non-numeric we will map it through uniques to an index
            return [numeric:numeric, num:u.size(), uniques:u]
        } else {
            return [numeric:numeric, num:u.size()]
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
    public static def sigma = { double d ->
        1 / (1 + Math.exp(- d))
    }

    /**
     * 0,1からなる全てのパターンを配列で返す。配列の長さは、2の[unitCnt]乗になる
     * 例：unitCnt : 3の場合
     * [[0,0,0],[0,0,1],[0,1,0],[0,1,1],[1,0,0],[1,0,1],[1,1,0],[1,1,1]]
     */
    public static def List getAllPattern(int unitCnt){
        def list = []
        unitCnt.times{
            list << [0,1]
        }
        list.combinations()
    }

    public static def List<List<Neuron>> pairs(List<Neuron> neurons, Boolean bidirectional = false){
        def res = [neurons, neurons].combinations()
        if(!bidirectional){
            res.unique{List<Neuron> list ->
                list.sort()
            }
        }
        res
    }
}
