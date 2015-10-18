package gratin.util

/**
 * ���������Ȃ�ƁA�S�v�f��List���L�[�ɂ���Map�Ƃ����Ⴂ�͂Ȃ��悤�ȋC�����Ă���B�B�B
 * Matrix��generics�œ���ł��Ȃ����̂��Ȃ��B�B�B
 * @author Hitoshi Wada
 */
class GMatrix4D extends ArrayList<GMatrix3D>{

    // �������́A�A�N�Z�X���iMatrix4D[depth2][depth][row][col]�j�ƈ�v�����邱�ƁB
    GMatrix4D(int depth2, int depth, int row, int col, Object defValue = null) {
        depth2.times{
            this << new GMatrix3D(depth, row, col, defValue)
        }
    }

    int getDepth2(){
        this.size()
    }

    // cls(value, depth2, depth, row, col)
    public void forEachWithIndex(Closure cls) {
        depth2.times { depth2 ->
            this[depth2].forEachWithIndex {v,depth,row,col ->
                cls(v, depth2, depth, row, col)
            }
        }
    }

    @Override
    public String toString(){
        "depth2:${this.size()}, depth:${this[0].size()}, row:${this[0][0].rowCount}, col:${this[0][0].colCount}}"
    }
}
