package gratin.util

/**
 * HuffmanTree encoder
 * TODO revise package architecture later, e.g. place under tree package
 * @author Hitoshi Wada
 */
class HuffmanTree {

    // 暗号化マップ（keyは各ノード、valueは各ノードを暗号化されたバイナリ配列（List<Integer>））
    def dict = [:]
    List data

    public HuffmanTree(List list) {
        data = list
    }

    public void encode() {
        // count frequency
        def res = data.countBy { it }.collect {
            [
                item : it.key,
                count: it.value,
            ]
        } // results like this,  [ [key : elm1, count : 2], [key : elm2, count : 5], [ key : elm3, count : 3], ...]
        println res

        def comparator = new Comparator<Map>() {
            @Override
            int compare(Map o1, Map o2) {
                Math.signum(o1.count - o2.count) // -1, 0 , +1
            }
        }
        // TreeSet is not working in this case. If do so, we cannot remove(or pop) element,
        // because TreeSet try to remove element by compare
//        def freeNodes = new TreeSet(comparator)
//        freeNodes.addAll(res)

        PriorityQueue queue = new PriorityQueue(res.size(), comparator)
        res.each{
            queue.add it
        }
//        queue.addAll(res.clone())

        // finish condition :
        // only 1 free node exists (That,s root !). Here 'free node' means, the node has no parent
        while (queue.size() > 1) {
            def childL = queue.poll() // left child => 0
            childL.bit = 0
            def childR = queue.poll() // right child => 1
            childR.bit = 1
            def parent = [
//                cl   : childL,
//                cr   : childR,
                count: childL.count + childR.count
            ]
            childL.parent = parent
            childR.parent = parent
            queue.add(parent)
        }
        res.each{
            def target = it
            def bits = []
            while(target.parent){
                bits << target.bit
                target = target.parent
            }
            dict[it.item] = bits
        }
    }

    public List decode() {

    }
}
