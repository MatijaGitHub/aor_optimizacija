package rs.ac.bg.etf.predictor.tournament;

import rs.ac.bg.etf.automaton.Automaton;
import rs.ac.bg.etf.predictor.BHR;
import rs.ac.bg.etf.predictor.Instruction;
import rs.ac.bg.etf.predictor.Predictor;

public class Tournament implements Predictor {
    BHR bhr;
    Automaton[] bhrAut;
    Automaton[] adrAut;
    int mask;
    int[] selector;
    public Tournament(int Bhrsize, int adrSize, Automaton.AutomatonType type){
        bhr = new BHR(Bhrsize);
        bhrAut = Automaton.instanceArray(type,(int)Math.pow(2,Bhrsize));
        adrAut = Automaton.instanceArray(type,(int)Math.pow(2, adrSize));
        mask = (1<<adrSize) - 1;
        selector = new int[(int)Math.pow(2,adrSize)];
    }
    @Override
    public boolean predict(Instruction branch) {
        boolean p1 = bhrAut[bhr.getValue()].predict();
        boolean p2 = adrAut[(int) (branch.getAddress()&mask)].predict();
        int selected = selector[(int) (branch.getAddress()&mask)];
        if(selected > 0){
            return  p1;
        }
        else return p2;
    }

    @Override
    public void update(Instruction branch) {
        boolean outcome = branch.isTaken();
        if(selector[(int) (branch.getAddress()&mask)] > 0){
            bhrAut[bhr.getValue()].updateAutomaton(outcome);
        }
        else {
            adrAut[(int) (branch.getAddress()&mask)].updateAutomaton(outcome);
        }
        if(outcome){
            selector[(int) (branch.getAddress()&mask)]++;
            if(selector[(int) (branch.getAddress()&mask)] > 2){
                selector[(int) (branch.getAddress()&mask)] = 2;
            }
        }
        else{
            selector[(int) (branch.getAddress()&mask)]--;
            if(selector[(int) (branch.getAddress()&mask)] < -1){
                selector[(int) (branch.getAddress()&mask)] = -1;
            }
        }
        bhr.insertOutcome(outcome);
    }
}
