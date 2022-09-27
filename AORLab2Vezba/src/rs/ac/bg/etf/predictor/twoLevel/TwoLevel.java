package rs.ac.bg.etf.predictor.twoLevel;

import rs.ac.bg.etf.automaton.Automaton;
import rs.ac.bg.etf.predictor.BHR;
import rs.ac.bg.etf.predictor.Instruction;
import rs.ac.bg.etf.predictor.Predictor;

public class TwoLevel implements Predictor{

    BHR bhr;
    Automaton[] automats;

    public TwoLevel(int bhrsize, Automaton.AutomatonType type){
        bhr = new BHR(bhrsize);
        automats = Automaton.instanceArray(type,(int)Math.pow(2,bhrsize));
    }

    @Override
    public boolean predict(Instruction branch) {
        return automats[bhr.getValue()].predict();
    }

    @Override
    public void update(Instruction branch) {
        boolean outcome = branch.isTaken();
        automats[bhr.getValue()].updateAutomaton(outcome);
        bhr.insertOutcome(outcome);
    }
}