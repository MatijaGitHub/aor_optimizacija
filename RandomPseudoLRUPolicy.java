package main.java.rs.ac.bg.etf.aor2.replacementpolicy;

import main.java.rs.ac.bg.etf.aor2.memory.MemoryOperation;
import main.java.rs.ac.bg.etf.aor2.memory.cache.ICacheMemory;
import main.java.rs.ac.bg.etf.aor2.memory.cache.Tag;

import java.util.ArrayList;
import java.util.Arrays;

public class RandomPseudoLRUPolicy implements IReplacementPolicy{

    protected ICacheMemory ICacheMemory;
    protected int[][] LRUCnts;
    protected int setAsoc;
    protected int numOfSubGroups;

    public RandomPseudoLRUPolicy(){

    }

    @Override
    public void init(ICacheMemory c) {
        this.ICacheMemory = c;
        setAsoc = (int) c.getSetAsociativity();
        int size = (int) ICacheMemory.getSetNum();
        LRUCnts = new int[size][];
        if(setAsoc == 4 || setAsoc == 8) numOfSubGroups = 2;
        else if(setAsoc == 16) numOfSubGroups = 4;
        else numOfSubGroups = 8;
        for(int i = 0; i < size; i++) {
            if (numOfSubGroups == 2) {
                LRUCnts[i] = new int[2];
            } else if(numOfSubGroups == 4) {
                LRUCnts[i] = new int[4];
            }
            else {
                LRUCnts[i] = new int[8];
            }
        }
    }

    @Override
    public int getBlockIndexToReplace(long adr) {
        int set = (int) ICacheMemory.extractSet(adr);
        return set * setAsoc + getEntry(adr);
    }

    private int getEntry(long adr){
        int set = (int) ICacheMemory.extractSet(adr);
        ArrayList<Tag> tagMemory = ICacheMemory.getTags();
        int result = 0;
        for (int i = 0; i < setAsoc; i++) {
            int block = set * setAsoc + i;
            Tag tag = tagMemory.get(block);
            if (!tag.V) {
                return i;
            }
        }
        int LRUCntGroup[] = LRUCnts[set];
        int choice = (int) (Math.random()*numOfSubGroups);
        int LRUCnt = LRUCntGroup[choice];
        int convert[];
        if(setAsoc == 4 && choice == 0) {
            convert = new int[]{3, 2};
            result = convert[LRUCnt & 1];
        }
        else if(setAsoc == 4 && choice == 1){
            convert = new int[]{1, 0};
            result = convert[LRUCnt & 1];
        }
        else if(setAsoc == 8 && choice == 0){
            convert = new int[]{7, 7, 6, 6, 5, 4, 5, 4};
            result = convert[LRUCnt & 7];
        }
        else if(setAsoc == 8 && choice == 1){
            convert = new int[]{3, 3, 2, 2, 1, 0, 1, 0};
            result = convert[LRUCnt & 7];
        }
        else if(setAsoc == 16 && choice == 0){
            convert = new int[]{15, 15, 14, 14, 13, 12, 13, 12};
            result = convert[LRUCnt & 7];
        }
        else if(setAsoc == 16 && choice == 1){
            convert = new int[]{11, 11, 10, 10, 9, 8, 9, 8};
            result = convert[LRUCnt & 7];
        }
        else if(setAsoc == 16 && choice == 2){
            convert = new int[]{7, 7, 6, 6, 5, 4, 5, 4};
            result = convert[LRUCnt & 7];
        }
        else if(setAsoc == 16 && choice == 3){
            convert = new int[]{3, 3, 2, 2, 1, 0, 1, 0};
            result = convert[LRUCnt & 7];
        }
        else if(choice == 0){
            convert = new int[]{31,31,30,30,29,28,29,28};
            result = convert[LRUCnt & 7];
        }
        else if(choice == 1){
            convert = new int[]{27, 27, 26, 26, 25, 24, 25, 24};
            result = convert[LRUCnt & 7];
        }
        else if(choice == 2){
            convert = new int[]{23, 23, 22, 22, 21, 20, 21, 20};
            result = convert[LRUCnt & 7];
        }
        else if(choice == 3){
            convert = new int[]{19, 19, 18, 18, 17, 16, 17, 16};
            result = convert[LRUCnt & 7];
        }
        else if(choice == 4){
            convert = new int[]{15,15,14,14,13,12,13,12};
            result = convert[LRUCnt & 7];
        }
        else if(choice == 5){
            convert = new int[]{11, 11, 10, 10, 9, 8, 9, 8};
            result = convert[LRUCnt & 7];
        }
        else if(choice == 6){
            convert = new int[]{7, 7, 6, 6, 5, 4, 5, 4};
            result = convert[LRUCnt & 7];
        }
        else if(choice == 7){
            convert = new int[]{3, 3, 2, 2, 1, 0, 1, 0};
            result = convert[LRUCnt & 7];
        }
        return result;
    }



    public String printAll() {
        StringBuilder s = new StringBuilder();
        int size = LRUCnts.length;
        for (int i = 0; i < size; i++) {
            s.append("Set ").append(i).append(", Pseudo LRU counters ");
            for(int j = 0;j<numOfSubGroups;j++){
                s.append(LRUCnts[i][j]);
            }
            s.append("\n");
        }
        return s.toString();
    }

    @Override
    public void reset() {
        for (int i = 0; i < LRUCnts.length; i++) {
            for(int j = 0;j<numOfSubGroups;j++){
                LRUCnts[i][j] = 0;
            }
        }
    }

    public String printValid() {
        StringBuilder s = new StringBuilder();
        int setAsoc = (int) ICacheMemory.getSetAsociativity();
        int setNumber = (int) ICacheMemory.getSetNum();
        ArrayList<Tag> tagMemory = ICacheMemory.getTags();
        for (int set = 0; set < setNumber; set++) {
            boolean valid = false;
            for (int j = 0; j < setAsoc; j++) {
                int block = set * setAsoc + j;
                Tag tag = tagMemory.get(block);
                if (tag.V) {
                    valid = true;
                    break;
                }
            }
            if (valid) {
                s.append("Set ").append(set).append(", Pseudo LRU counter ").append(Arrays.toString(LRUCnts[set])).append("\n");
            }
        }
        return s.toString();
    }

    @Override
    public void doOperation(MemoryOperation operation) {
        MemoryOperation.MemoryOperationType opr = operation.getType();

        if ((opr == MemoryOperation.MemoryOperationType.READ)
                || (opr == MemoryOperation.MemoryOperationType.WRITE)) {

            long adr = operation.getAddress();
            int set = (int) ICacheMemory.extractSet(adr);
            long tagTag = ICacheMemory.extractTag(adr);
            ArrayList<Tag> tagMemory = ICacheMemory.getTags();
            int entry = 0;
            for (int i = 0; i < setAsoc; i++) {
                int block = set * setAsoc + i;
                Tag tag = tagMemory.get(block);
                if (tag.V && (tag.tag == tagTag)) {
                    entry = i;
                    break;
                }
            }
            int[] LRUCnt = LRUCnts[set];
            if(setAsoc == 4){
                switch (entry) {
                    case 0 -> LRUCnt[1] = 0;
                    case 1 -> LRUCnt[1] = 1;
                    case 2 -> LRUCnt[0] = 0;
                    case 3 -> LRUCnt[0] = 1;
                }
                LRUCnts[set] = LRUCnt;
            }
            else if(setAsoc == 8){
                switch (entry) {
                    case 0 -> LRUCnt[1] &= 2;
                    case 1 -> LRUCnt[1] = (LRUCnt[1] & 2) | 1;
                    case 2 -> LRUCnt[1] = (LRUCnt[1] & 1) | 4;
                    case 3 -> LRUCnt[1] = (LRUCnt[1] & 1) | 6;
                    case 4 -> LRUCnt[0] &= 2;
                    case 5 -> LRUCnt[0] = (LRUCnt[0] & 2) | 1;
                    case 6 -> LRUCnt[0] = (LRUCnt[0] & 1) | 4;
                    case 7 -> LRUCnt[0] = (LRUCnt[0] & 1) | 6;
                }
            }
            else if(setAsoc == 16){
                switch (entry) {
                    case 0 -> LRUCnt[3] &= 2;
                    case 1 -> LRUCnt[3] = (LRUCnt[1] & 2) | 1;
                    case 2 -> LRUCnt[3] = (LRUCnt[1] & 1) | 4;
                    case 3 -> LRUCnt[3] = (LRUCnt[1] & 1) | 6;
                    case 4 -> LRUCnt[2] &= 2;
                    case 5 -> LRUCnt[2] = (LRUCnt[0] & 2) | 1;
                    case 6 -> LRUCnt[2] = (LRUCnt[0] & 1) | 4;
                    case 7 -> LRUCnt[2] = (LRUCnt[0] & 1) | 6;
                    case 8 -> LRUCnt[1] &= 2;
                    case 9 -> LRUCnt[1] = (LRUCnt[1] & 2) | 1;
                    case 10 -> LRUCnt[1] = (LRUCnt[1] & 1) | 4;
                    case 11 -> LRUCnt[1] = (LRUCnt[1] & 1) | 6;
                    case 12 -> LRUCnt[0] &= 2;
                    case 13 -> LRUCnt[0] = (LRUCnt[0] & 2) | 1;
                    case 14 -> LRUCnt[0] = (LRUCnt[0] & 1) | 4;
                    case 15 -> LRUCnt[0] = (LRUCnt[0] & 1) | 6;
                }
            }
            else if(setAsoc == 32){
                switch (entry) {
                    case 0 -> LRUCnt[7] &= 2;
                    case 1 -> LRUCnt[7] = (LRUCnt[1] & 2) | 1;
                    case 2 -> LRUCnt[7] = (LRUCnt[1] & 1) | 4;
                    case 3 -> LRUCnt[7] = (LRUCnt[1] & 1) | 6;
                    case 4 -> LRUCnt[6] &= 2;
                    case 5 -> LRUCnt[6] = (LRUCnt[0] & 2) | 1;
                    case 6 -> LRUCnt[6] = (LRUCnt[0] & 1) | 4;
                    case 7 -> LRUCnt[6] = (LRUCnt[0] & 1) | 6;
                    case 8 -> LRUCnt[5] &= 2;
                    case 9 -> LRUCnt[5] = (LRUCnt[1] & 2) | 1;
                    case 10 -> LRUCnt[5] = (LRUCnt[1] & 1) | 4;
                    case 11 -> LRUCnt[5] = (LRUCnt[1] & 1) | 6;
                    case 12 -> LRUCnt[4] &= 2;
                    case 13 -> LRUCnt[4] = (LRUCnt[0] & 2) | 1;
                    case 14 -> LRUCnt[4] = (LRUCnt[0] & 1) | 4;
                    case 15 -> LRUCnt[4] = (LRUCnt[0] & 1) | 6;
                    case 16 -> LRUCnt[3] &= 2;
                    case 17 -> LRUCnt[3] = (LRUCnt[1] & 2) | 1;
                    case 18 -> LRUCnt[3] = (LRUCnt[1] & 1) | 4;
                    case 19 -> LRUCnt[3] = (LRUCnt[1] & 1) | 6;
                    case 20 -> LRUCnt[2] &= 2;
                    case 21 -> LRUCnt[2] = (LRUCnt[0] & 2) | 1;
                    case 22 -> LRUCnt[2] = (LRUCnt[0] & 1) | 4;
                    case 23 -> LRUCnt[2] = (LRUCnt[0] & 1) | 6;
                    case 24 -> LRUCnt[1] &= 2;
                    case 25 -> LRUCnt[1] = (LRUCnt[1] & 2) | 1;
                    case 26 -> LRUCnt[1] = (LRUCnt[1] & 1) | 4;
                    case 27 -> LRUCnt[1] = (LRUCnt[1] & 1) | 6;
                    case 28 -> LRUCnt[0] &= 2;
                    case 29 -> LRUCnt[0] = (LRUCnt[0] & 2) | 1;
                    case 30 -> LRUCnt[0] = (LRUCnt[0] & 1) | 4;
                    case 31 -> LRUCnt[0] = (LRUCnt[0] & 1) | 6;
                }
            }
        }
        else if (operation.getType() == MemoryOperation.MemoryOperationType.FLUSHALL) {
            reset();
        }
    }
}
