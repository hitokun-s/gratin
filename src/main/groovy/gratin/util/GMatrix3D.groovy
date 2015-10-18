package gratin.util

/**
 * ���̍s��
 * �v�f�A�N�Z�X�́AMatrix3D[depth][row][col]�̏����B
 * Matrix3D[depth]���ƁA���̐[���ɂ���Matrix���Ԃ�B
 * @author Hitoshi Wada
 */
class GMatrix3D extends ArrayList<GMatrix>{

    // �������́A�A�N�Z�X���iMatrix3D[depth][row][col]�j�ƈ�v�����邱�ƁB
    GMatrix3D(int depth, int row, int col, Object defValue = null) {
        depth.times{
            this << new GMatrix(row, col, defValue)
        }
    }

    GMatrix3D(List<GMatrix> source){
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

    // cls(value, depth, row, col)
    public void forEachWithIndex(Closure cls) {
        depth.times { depth ->
            this[depth].forEachWithIndex {v,row,col ->
                cls(v, depth, row, col)
            }
        }
    }
}
