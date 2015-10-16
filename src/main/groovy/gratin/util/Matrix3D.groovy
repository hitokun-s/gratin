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
        super(source)
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

    // �S�v�f��
    public int getTotalCount(){
        this.depth * this[0].totalCount
    }

    // ���p�i�H�j���琔�����C���f�b�N�X
    public int getTotalIndex(int depth, int row, int col){
        this[0].totalCount * depth + this[0].getTotalIndex(row, col)
    }

    public int ti(int depth, int row, int col){
        this[0].totalCount * depth + this[0].getTotalIndex(row, col)
    }
}
