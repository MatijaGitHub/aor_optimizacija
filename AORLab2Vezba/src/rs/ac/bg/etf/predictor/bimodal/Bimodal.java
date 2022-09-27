package rs.ac.bg.etf.predictor.bimodal;

import rs.ac.bg.etf.automaton.Automaton;
import rs.ac.bg.etf.predictor.BHR;
import rs.ac.bg.etf.predictor.Instruction;
import rs.ac.bg.etf.predictor.Predictor;

public class Bimodal implements Predictor{

    int[] selector;
    BHR bhr;
    Automaton[] aut1;
    Automaton[] aut2;
    int mask;
    int adrmask;

    public Bimodal(int bhrsize, int adrsize, Automaton.AutomatonType type){
        bhr = new BHR(bhrsize);
        aut1 = Automaton.instanceArray(type, (int) Math.pow(2,bhrsize));
        aut2 = Automaton.instanceArray(type, (int) Math.pow(2,bhrsize));
        mask = (1<<bhrsize)-1;
        selector = new int[(int) Math.pow(2,adrsize)];
        adrmask = (1<<adrsize)-1;
    }

    @Override
    public boolean predict(Instruction branch) {
        boolean b1 = aut1[(int) ((branch.getAddress()&mask) ^ bhr.getValue())].predict();
        boolean b2 = aut2[(int) ((branch.getAddress()&mask) ^ bhr.getValue())].predict();
        if(selector[(int) (branch.getAddress()&adrmask)] > 0){
            return b1;
        }
        else  return b2;
    }

    @Override
    public void update(Instruction branch) {
        boolean outcome = branch.isTaken();
        if(selector[(int) (branch.getAddress()&adrmask)] > 0){
            aut1[(int) ((branch.getAddress()&mask) ^ bhr.getValue())].updateAutomaton(outcome);
        }
        else {
            aut2[(int) ((branch.getAddress()&mask) ^ bhr.getValue())].updateAutomaton(outcome);
        }
        if(outcome){
            selector[(int) (branch.getAddress()&adrmask)] ++;
            if(selector[(int) (branch.getAddress()&adrmask)]>2){
                selector[(int) (branch.getAddress()&adrmask)] = 2;
            }
        }
        else {
            selector[(int) (branch.getAddress()&adrmask)]--;
            if(selector[(int) (branch.getAddress()&adrmask)]<-1){
                selector[(int) (branch.getAddress()&adrmask)] = -1;
            }
        }
        bhr.insertOutcome(outcome);
    }
}