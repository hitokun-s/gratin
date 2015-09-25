package gratin.classifier

import static gratin.util.Util.*

/**
 * k-nearest neighbor algorithm
 * k-近傍法
 * @author Hitoshi Wada
 */
class KNN {

    List<Map> data // List<[input:[...], output:[...]]>
    int k = 1

    public KNN(List<Map> data, int k = 1){
        this.data = data
        this.k = k
    }

    def classify(List<Double> target){
        def neighbors = data.collect{d ->
            [dist:dist(target, d.input), output:d.output]
        }.sort()[0..k-1]
        // 多数決
        neighbors.countBy {it.output}.max{it.value}.key // returns most frequent output
    }

}
