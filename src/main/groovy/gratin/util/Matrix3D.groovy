package gratin.util

/**
 * ���̍s��
 * �v�f�A�N�Z�X�́AMatrix3D[depth][row][col]�̏����B
 * Matrix3D[depth]���ƁA���̐[���ɂ���Matrix���Ԃ�B
 * @author Hitoshi Wada
 */
class Matrix3D extends ArrayList<Matrix>{

    // �������́A�A�N�Z�X���iMatrix3D[depth][row][col]�j�ƈ�v�����邱�ƁB
    Matrix3D(int depth, int row, int col) {
        depth.times{
            this << new Matrix(row, col)
        }
    }

    Matrix3D(List<Matrix> source){
        if(source[0] instanceof Matrix){
            source.each{
                this << it
            }
        }else{
            source.each{
                this << new Matrix(it)
            }
        }
    }

    public Matrix3D(Matrix m){
        this << m
    }

    int getDepth(){
        this.size()
    }

    int getRow(){
        this[0].size()
    }

    int getCol(){
        this[0][0].size()
    }

    Object sumWithDepth(Closure cls){
        def res
        this.eachWithIndex{v,i ->
            def tmp = cls(v,i)
            res = res ? res + tmp : tmp
        }
        res
    }

    // cls(value, depth, row, col)
    public void forEachWithIndex(Closure cls) {
        depth.times { depth ->
            this[depth].forEachWithIndex {v,row,col ->
                cls(v, depth, row, col)
            }
        }
    }

    // �S�v�f��
    public int getTotalCount(){
        this.depth * this[0].totalCount
    }

    // ���p�i�H�j���琔�����C���f�b�N�X
    public int getTotalIndex(int depth, int row, int col){
        this[0].totalCount * depth + this[0].getTotalIndex(row, col)
    }

    public int ti(int depth, int row, int col){
        getTotalIndex(depth, row, col)
    }

    /**
     * getTotalIndex �̋t
     * @return [depth, row, col]
     */
    public List getEachIndex(int totalIndex){
        [(int)(totalIndex / this[0].totalCount)] + this[0].getEachIndex(totalIndex % this[0].totalCount)
    }
}
