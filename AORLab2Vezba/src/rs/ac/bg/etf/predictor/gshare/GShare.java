package rs.ac.bg.etf.predictor.gshare;

import rs.ac.bg.etf.automaton.Automaton;
import rs.ac.bg.etf.predictor.BHR;
import rs.ac.bg.etf.predictor.Instruction;
import rs.ac.bg.etf.predictor.Predictor;

public class GShare implements Predictor {
    BHR bhr;
    Automaton[] automats;
    int bitsMask;


    public GShare(int BHRSize , Automaton.AutomatonType type){
        bhr = new BHR(BHRSize);
        automats = Automaton.instanceArray(type,(int)Math.pow(2,BHRSize));
        bitsMask = (1<<BHRSize)-1;
    }
    @Override
    public boolean predict(Instruction branch) {
        int adr = (int) (branch.getAddress()&bitsMask);
        int val = adr ^ bhr.getValue();
        return automats[val].predict();
    }

    @Override
    public void update(Instruction branch) {
        boolean outcome = branch.isTaken();
        int adr = (int) (branch.getAddress()&bitsMask);
        int val = adr ^ bhr.getValue();
        automats[val].updateAutomaton(outcome);
        bhr.insertOutcome(outcome);
    }
}
