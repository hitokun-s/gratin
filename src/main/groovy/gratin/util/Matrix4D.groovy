package gratin.util

/**
 * ���������Ȃ�ƁA�S�v�f��List���L�[�ɂ���Map�Ƃ����Ⴂ�͂Ȃ��悤�ȋC�����Ă���B�B�B
 * @author Hitoshi Wada
 */
class Matrix4D extends ArrayList<Matrix3D>{

    // �������́A�A�N�Z�X���iMatrix4D[depth2][depth][row][col]�j�ƈ�v�����邱�ƁB
    Matrix4D(int depth2, int depth, int row, int col) {
        depth2.times{
            this << new Matrix3D(depth, row, col)
        }
    }

    public Matrix4D(Matrix m){
        this << new Matrix3D(m)
    }

    int getDepth2(){
        this.size()
    }

    int getDepth(){
        this[0].depth
    }

    int getRow(){
        this[0].row
    }

    int getCol(){
        this[0].col
    }

    // �S�v�f��
    public int getTotalCount(){
        this.depth2 * this[0].totalCount
    }

    // �w����W�̑��C���f�b�N�X
    public int getTotalIndex(int depth2, int depth, int row, int col){
        this[0].totalCount * depth2 + this[0].getTotalIndex(depth, row, col)
    }

    public int ti(int depth2, int depth, int row, int col){
        getTotalIndex(depth2, depth, row, col)
    }

    /**
     * getTotalIndex �̋t
     * @return [depth2, depth, row, col]
     */
    public List getEachIndex(int totalIndex){
        [(int)(totalIndex / this[0].totalCount)] + this[0].getEachIndex(totalIndex % this[0].totalCount)
    }
}
