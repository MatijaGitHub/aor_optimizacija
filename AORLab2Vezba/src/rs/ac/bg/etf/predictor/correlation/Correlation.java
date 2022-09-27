package rs.ac.bg.etf.predictor.correlation;

import rs.ac.bg.etf.automaton.Automaton;
import rs.ac.bg.etf.predictor.BHR;
import rs.ac.bg.etf.predictor.Instruction;
import rs.ac.bg.etf.predictor.Predictor;

public class Correlation implements Predictor{

    BHR bhr;
    Automaton[][] automats;
    int mask;

    public Correlation(int bhrsize, int adrsize, Automaton.AutomatonType type){
        int num = (int)Math.pow(2,bhrsize);
        bhr = new BHR(bhrsize);
        automats = new Automaton[num][];
        for(int i = 0;i<num;i++){
            automats[i] = Automaton.instanceArray(type,(int)Math.pow(2,adrsize));
        }
        mask = (1<<adrsize)-1;
    }

    @Override
    public boolean predict(Instruction branch) {
        return automats[bhr.getValue()][(int) (branch.getAddress()&mask)].predict();
    }

    @Override
    public void update(Instruction branch) {
        boolean outcome = branch.isTaken();
        automats[bhr.getValue()][(int) (branch.getAddress()&mask)].updateAutomaton(outcome);
        bhr.insertOutcome(outcome);
    }
}