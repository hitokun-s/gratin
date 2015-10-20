package gratin.util

import gratin.components.Neuron

/**
 * @author Hitoshi Wada
 */
class Bond {
    Neuron s // �n�[
    Neuron e // �I�[
    double w // �d��
    double wd // �d�݌��z

    static Map<Neuron, List> sMap = [:]
    static Map<Neuron, List> eMap = [:]

    Bond(Neuron s, Neuron e){
        this.s = s
        this.e = e
        pool << this
        if(sMap[s]){
            sMap[s] << this
        }else{
            sMap[s] = [this]
        }
        if(eMap[e]){
            eMap[e] << this
        }else{
            eMap[e] = [this]
        }
    }

    @Override
    public String toString(){
        "Bond(s:$s, e:$e)"
    }

    static List<Bond> pool = []

    static List<Bond> findAllByS(Neuron n){
        // pool.findAll {it.s == n } // ���x���P
        sMap[n]
    }

    static List<Bond> findAllByE(Neuron n){
        // pool.findAll {it.e == n } // ���x���P
        eMap[n]
    }

    static Bond findBySandE(Neuron s, Neuron e){
        pool.find {it.s == s && it.e == e}
    }
}
