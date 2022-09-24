package rs.ac.bg.etf.predictor.YAGS;

import rs.ac.bg.etf.automaton.Automaton;
import rs.ac.bg.etf.predictor.BHR;
import rs.ac.bg.etf.predictor.Instruction;
import rs.ac.bg.etf.predictor.Predictor;

public class YAGS implements Predictor {
    BHR bhr;
    Automaton[] notTakenCash;
    int[] notTakenTags;
    Automaton[] takenCash;
    int[] takenTags;
    Automaton[] a1Bimodal;
    Automaton[] a2Bimodal;
    int[] selectorBimodal;
    int selectorMask;
    int autoMask;

    public YAGS(int bhrSize, int adrSize, Automaton.AutomatonType type){
        bhr = new BHR(bhrSize);
        int autoSizes = Math.max(bhrSize,adrSize);
        notTakenCash = Automaton.instanceArray(type,(int)Math.pow(2,autoSizes));
        notTakenTags = new int[(int)Math.pow(2,autoSizes)];
        takenCash = Automaton.instanceArray(type,(int)Math.pow(2,autoSizes));
        takenTags = new int[(int)Math.pow(2,autoSizes)];
        a1Bimodal = Automaton.instanceArray(type,(int)Math.pow(2,autoSizes));
        a2Bimodal = Automaton.instanceArray(type,(int)Math.pow(2,autoSizes));
        selectorBimodal = new int[(int)Math.pow(2,adrSize)];
        selectorMask = (1 << adrSize) - 1;
        autoMask = (1 << autoSizes) - 1;

    }
    @Override
    public boolean predict(Instruction branch) {
        boolean bimodalPred;
        if(selectorBimodal[(int) (branch.getAddress()&selectorMask)] > 0){
            bimodalPred = a1Bimodal[(int) ((branch.getAddress() ^ bhr.getValue())&autoMask)].predict();
        }
        else  {
            bimodalPred = a2Bimodal[(int) ((branch.getAddress() ^ bhr.getValue())&autoMask)].predict();
        }
        if(bimodalPred){
            if(notTakenTags[(int) ((branch.getAddress()^bhr.getValue())&autoMask)] != (branch.getAddress()&selectorMask)){
                return bimodalPred;
            }
            else {
                return notTakenCash[(int) ((branch.getAddress() ^ bhr.getValue())&autoMask)].predict();
            }
        }
        else {
            if(takenTags[(int) ((branch.getAddress()^bhr.getValue())&autoMask)] != (branch.getAddress()&selectorMask)){
                return bimodalPred;
            }
            else {
                return takenCash[(int) ((branch.getAddress() ^ bhr.getValue())&autoMask)].predict();
            }
        }
    }

    @Override
    public void update(Instruction branch) {
        boolean outcome = branch.isTaken();
        boolean bimodalPred;
        if(selectorBimodal[(int) (branch.getAddress()&selectorMask)] > 0){
            bimodalPred = a1Bimodal[(int) ((branch.getAddress() ^ bhr.getValue())&autoMask)].predict();
        }
        else  {
            bimodalPred = a2Bimodal[(int) ((branch.getAddress() ^ bhr.getValue())&autoMask)].predict();
        }
        if(bimodalPred){
            if(notTakenTags[(int) ((branch.getAddress()^bhr.getValue())&autoMask)] != (branch.getAddress()&selectorMask)){

                if(bimodalPred!=outcome){
                    notTakenTags[(int) ((branch.getAddress()^bhr.getValue())&autoMask)] = (int) (branch.getAddress()&selectorMask);
                    notTakenCash[(int) ((branch.getAddress() ^ bhr.getValue())&autoMask)].updateAutomaton(outcome);

                }
            }
            else {
                notTakenTags[(int) ((branch.getAddress()^bhr.getValue())&autoMask)] = (int) (branch.getAddress()&selectorMask);
                notTakenCash[(int) ((branch.getAddress() ^ bhr.getValue())&autoMask)].updateAutomaton(outcome);
            }
        }
        else {
            if(takenTags[(int) ((branch.getAddress()^bhr.getValue())&autoMask)] != (branch.getAddress()&selectorMask)){

                if(bimodalPred!=outcome){
                    takenTags[(int) ((branch.getAddress()^bhr.getValue())&autoMask)] = (int) (branch.getAddress()&selectorMask);
                    takenCash[(int) ((branch.getAddress() ^ bhr.getValue())&autoMask)].updateAutomaton(outcome);
                }
            }
            else {
                takenTags[(int) ((branch.getAddress()^bhr.getValue())&autoMask)] = (int) (branch.getAddress()&selectorMask);
                takenCash[(int) ((branch.getAddress() ^ bhr.getValue())&autoMask)].updateAutomaton(outcome);
            }
        }
        if(selectorBimodal[(int) (branch.getAddress()&selectorMask)] > 0){
            a1Bimodal[(int) ((branch.getAddress()&autoMask) ^ bhr.getValue())].updateAutomaton(outcome);
        }
        else {
            a2Bimodal[(int) ((branch.getAddress()&autoMask) ^ bhr.getValue())].updateAutomaton(outcome);
        }
        if(outcome){
            if(selectorBimodal[(int) (branch.getAddress()&selectorMask)]<2){
                selectorBimodal[(int) (branch.getAddress()&selectorMask)]++;
            }
        }
        else {
            if(selectorBimodal[(int) (branch.getAddress()&selectorMask)]>-1){
                selectorBimodal[(int) (branch.getAddress()&selectorMask)]--;
            }
        }
        bhr.insertOutcome(outcome);
    }
}
