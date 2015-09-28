package gratin.util

/**
 * how to use:
 * def n = new Normalizer([[1,2,3],[2,3,4],...])
 *
 * def normalized = n([4,2,3]) // can normalize new sample
 *
 * @author Hitoshi Wada
 */
class Normalizer extends Closure {

    def avg = []
    def sd = []

    Normalizer(List<List<Double>> inputs) {
        super(null)
        // Using closure such as List.collect{} here, causes an error. So I use 'for' loop instead.
        for (int colIdx in (0..inputs[0].size() - 1)) {
            def colData = inputs.collect { it[colIdx] }
            avg[colIdx] = Util.avg(colData)
            sd[colIdx] = Util.sd(colData)
            def normalized = Util.normalize(colData)
            inputs.eachWithIndex { input, idx ->
                input[colIdx] = normalized[idx]
            }
        }
    }

//    public List<List<Double>> getOriginalData(){
//
//    }

    // normalize function for one data
    @Override
    Object call(Object... args) {
        List<Double> input = args[0]

        //  input.size().times { int colIdx ->
        //       input[colIdx] = (input[colIdx] - avg[colIdx]) / sd[colIdx]
        //  }
        // Using closure such as List.collect{} here, causes an error. So I use 'for' loop instead.
        for (int colIdx = 0; colIdx < input.size(); colIdx++) {
            input[colIdx] = (input[colIdx] - avg[colIdx]) / sd[colIdx]
        }
        input
    }
}
