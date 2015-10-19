package gratin.util

import gratin.components.Neuron

/**
 * ���̍s��
 * �v�f�A�N�Z�X�́AMatrix3D[depth][row][col]�̏����B
 * Matrix3D[depth]���ƁA���̐[���ɂ���Matrix���Ԃ�B
 * @author Hitoshi Wada
 */
class NMatrix3D extends ArrayList<NMatrix>{

    // �������́A�A�N�Z�X���iMatrix3D[depth][row][col]�j�ƈ�v�����邱�ƁB
    NMatrix3D(int depth, int row, int col) {
        depth.times{
            this << new NMatrix(row, col)
        }
    }

    // �[���P�ō쐬
    public NMatrix3D(NMatrix m){
        this << m
    }

    public NMatrix3D(List<Object> source) {
        if(source[0] instanceof NMatrix){
            source.each {
                this << it
            }
        }else{
            source.each { List<List<Number>> list ->
                this << new NMatrix(list)
            }
        }
    }

    /**
     * Neuron���X�g���A3D�s��ɕϊ�����BConvLayer�APoolingLayer�ȂǂŎg���B
     */
    public NMatrix3D(List<Neuron> source, int depth, int row, int col){
        assert source.size() == depth * row * col
        source.collate(col).collate(row).each{
            this << new NMatrix(it)
        }
    }

    // ��̃R���X�g���N�^�̋t�̏���
    public List<Neuron> toNeurons(){
        this.sum{ NMatrix m ->
            m.sum()
        }
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

    // each���I�[�o�[���C�h��������Ăяo�����ł��Y�킩������Ȃ����A�����\�b�h����each���g����������Ȃ����A
    // �O������each���Ăԉ\��������i���͂��`���l���ʂɏ�������Ƃ��j�̂ŁA��U�I�[�o�[���C�h���Ȃ��ł����B
    void forEach(Closure cls){
        this.each{NMatrix m ->
            m.forEach(cls)
        }
    }

//    // �S�v�f��
//    public int getTotalCount(){
//        this.depth * this[0].totalCount
//    }
//
//    // ���p�i�H�j���琔�����C���f�b�N�X
//    public int getTotalIndex(int depth, int row, int col){
//        this[0].totalCount * depth + this[0].getTotalIndex(row, col)
//    }
//
//    public int ti(int depth, int row, int col){
//        getTotalIndex(depth, row, col)
//    }
//
//    /**
//     * getTotalIndex �̋t
//     * @return [depth, row, col]
//     */
//    public List getEachIndex(int totalIndex){
//        [(int)(totalIndex / this[0].totalCount)] + this[0].getEachIndex(totalIndex % this[0].totalCount)
//    }
}
