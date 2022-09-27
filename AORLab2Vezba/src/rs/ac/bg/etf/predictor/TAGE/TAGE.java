package rs.ac.bg.etf.predictor.TAGE;

import rs.ac.bg.etf.automaton.Automaton;
import rs.ac.bg.etf.predictor.BHR;
import rs.ac.bg.etf.predictor.Instruction;
import rs.ac.bg.etf.predictor.Predictor;

public class TAGE implements Predictor {
    Automaton[][] T;
    int[][] tags;
    BHR bhr;
    int mask;
    int size;
    int adrSize;
    int[][] u;
    static double alpha = 2;
    int L1;
    static boolean altpredVar, predVar;
    static int maxIVar;
    static int secondMaxIVar;
    static long gracefullReset = 0;
    public int getL(int i){
        return (int)(Math.pow(alpha, i-1) * L1 + 0.5);
    }
    public TAGE(int bhrSize,int adrSize,Automaton.AutomatonType type){
        T = new Automaton[5][];
        tags = new int[5][];
        u = new int[5][];
        bhr = new BHR(bhrSize);
        this.adrSize = adrSize;
        size = Math.max(bhrSize,adrSize);
        mask = (1 << size) - 1;
        L1 = 2;
        for (int i = 0; i < 5; i++){
            T[i] = Automaton.instanceArray(type, (int) Math.pow(2,adrSize));
            if(i!=0) {
                tags[i] = new int[(int) Math.pow(2, adrSize)];
                u[i] = new int[(int) Math.pow(2, adrSize)];
                for(int j = 0; j < (int) Math.pow(2, adrSize); j++){
                    tags[i][j] = j & ((int) Math.pow(2,getL(i)) - 1);
                    u[i][j] = 0;

                }
            }
        }
    }


    @Override
    public boolean predict(Instruction branch) {
        long adr = branch.getAddress();
        boolean maxPrediction = false;
        boolean altPred = false;
        int maxI = -1;
        int secondMaxI = -1;
        for (int i = 4; i > 0; i--){
            int Li = getL(i);
            long hashed = (adr ^ bhr.getValue()) & (int)(Math.pow(2, Li) - 1);

            if (tags[i][(int)adr & ((1 << adrSize) - 1)] == hashed){

                if(i > maxI) {
                    maxI = i;
                    maxPrediction = T[i][(int) adr & ((1 << adrSize) - 1)].predict();
                }
                else if(i > secondMaxI){
                    secondMaxI = i;
                    altPred = T[i][(int) adr & ((1 << adrSize) - 1)].predict();
                }
            }
        }
        if (maxI == -1){
            maxPrediction = T[0][(int)adr & ((1 << adrSize) - 1)].predict();
            maxI = 0;
        }
        if(secondMaxI == -1){
            altPred = T[0][(int)adr & ((1 << adrSize) - 1)].predict();
            secondMaxI = 0;
        }
        maxIVar = maxI;
        altpredVar = altPred;
        secondMaxIVar = secondMaxI;
        predVar = maxPrediction;
        gracefullReset++;
        return maxPrediction;
    }

    @Override
    public void update(Instruction branch) {
        boolean isTaken = branch.isTaken();
        long adr = branch.getAddress();

        T[maxIVar][(int) adr & ((1 << adrSize) - 1)].updateAutomaton(isTaken);

        if(secondMaxIVar != -1){
            T[secondMaxIVar][(int) adr & ((1 << adrSize) - 1)].updateAutomaton(isTaken);
        }
        if(altpredVar != predVar){
            if(predVar == isTaken){
                u[maxIVar][(int)adr & ((1 << adrSize) - 1)]++;
                if(u[maxIVar][(int)adr & ((1 << adrSize) - 1)] > 3){
                    u[maxIVar][(int)adr & ((1 << adrSize) - 1)] = 3;
                }
            }
            else{
                u[maxIVar][(int)adr & ((1 << adrSize) - 1)]--;
                if(u[maxIVar][(int)adr & ((1 << adrSize) - 1)] < 0){
                    u[maxIVar][(int)adr & ((1 << adrSize) - 1)] = 0;
                }

            }


        }
        if(gracefullReset == 256000){
            gracefullReset = 0;
            for(int i = 1; i < 5; i++){
                for(int j = 0; j < (1 << adrSize); j++){
                    u[i][j] = 0;
                }
            }
        }

        if(predVar != isTaken && maxIVar < 4){
            boolean found = false;
            for(int i = maxIVar + 1; i < 5; i++){
                for(int j = 0; j < (1 << adrSize); j++){
                    if(u[i][j] == 0 && !found){
                        T[i][j].setWeakTaken();
                        found = true;
                    }
                    else{
                        u[i][j]--;
                        if(u[i][j] < 0){
                            u[i][j] = 0;
                        }
                    }
                }
            }
        }

        bhr.insertOutcome(isTaken);
    }
}
