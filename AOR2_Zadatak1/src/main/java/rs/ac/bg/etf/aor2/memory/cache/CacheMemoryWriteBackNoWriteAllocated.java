package main.java.rs.ac.bg.etf.aor2.memory.cache;

import main.java.rs.ac.bg.etf.aor2.memory.IMemory;
import main.java.rs.ac.bg.etf.aor2.memory.MemoryOperation;
import main.java.rs.ac.bg.etf.aor2.replacementpolicy.IReplacementPolicy;

public class CacheMemoryWriteBackNoWriteAllocated extends CacheMemory {


    public CacheMemoryWriteBackNoWriteAllocated(int adrLen, int setAsoc, int blockNum, int blockSize, IMemory nextLevelMemory, IReplacementPolicy replacementPolicy) {
        super(adrLen, setAsoc, blockNum, blockSize, nextLevelMemory, replacementPolicy);
    }

    @Override
    protected Tag createTag() {
        return new Tag((int) getTagLen(), true);
    }

    @Override
    protected int getType() {
        return WRITEBACK_NOWRITENOALLOCATED;
    }

    @Override
    protected void writeMiss(long block, long adr, long data) {
        if (nextLevelMemory != null) {
            nextLevelMemory.write(adr, data);
        }

        writeMissNoFreeSpace++;
        accessCharacteristics.setAccessTime(timeCalculator.getWriteMissNoFreeTimming(getType(), adrLen, setAsoc, blockNum, blockSize));

        String start = Long.toHexString(adr);
        accessCharacteristics.setAddresses(accessCharacteristics.getAddresses() + "[" + start + "]");

        memoryAccessNum++;
        memoryWriteNum++;

        cacheMissNum++;
    }

    @Override
    protected void writeHit(long block, long adr, long data) {
        writeValue(block, adr, data);
        replacementPolicy.doOperation(MemoryOperation.write(adr));

        cacheHitNum++;
        writeHits++;
        accessCharacteristics.setAddresses("-");
        accessCharacteristics
                .setAccessTime(timeCalculator.getWriteHitTimming(getType(), adrLen, setAsoc, blockNum, blockSize));
    }

    private void writeValue(long block, long adr, long data) {
        cacheWriteNum++;

        long[] blockData = dataMemory.get((int) block);
        blockData[(int) extractOffset(adr)] = data;

        Tag tag = tagMemory.get((int) block);
        tag.setDirty(true);
    }

    @Override
    protected void getFromMemory(int blockToFill, long adr) {
        long[] blockData = new long[blockSize];
        Tag tag = tagMemory.get(blockToFill);
        long tagVal = extractTag(adr);
        long set = extractSet(adr);
        for (int i = 0; i < blockSize; i++) {
            long adrRead = compact(tagVal, set, i);
            if (nextLevelMemory != null) {
                blockData[i] = nextLevelMemory.read(adrRead);
            }
            memoryAccessNum++;
            memoryReadNum++;
        }
        tag.setDirty(false);
        tag.V = true;
        tag.tag = tagVal;
        dataMemory.set(blockToFill, blockData);

        String start = Long.toHexString(compact(tagVal, set, 0));
        String end = Long.toHexString(compact(tagVal, set, blockSize - 1));
        accessCharacteristics.setAddresses(accessCharacteristics.getAddresses() + "[" + start + "-" + end + "]");
    }

    @Override
    protected boolean returnToMemory(int blockToReplace) {
        long[] blockData = dataMemory.get(blockToReplace);
        Tag tag = tagMemory.get(blockToReplace);
        tag.V = false;

        if (!tag.isDirty()) {
            // blok se nije menjao od dovlacenja u cache,
            // pa nije potrebno azurirati sledeci nivo memorije
            return false;
        }

        long tagVal = tag.tag;
        long set = blockToReplace / setAsoc;

        for (int i = 0; i < blockSize; i++) {
            long adr = compact(tagVal, set, i);
            if (nextLevelMemory != null)
                nextLevelMemory.write(adr, blockData[i]);
            memoryAccessNum++;
            memoryWriteNum++;
        }

        tag.setDirty(false);
        String start = Long.toHexString(compact(tagVal, set, 0));
        String end = Long.toHexString(compact(tagVal, set, blockSize - 1));
        accessCharacteristics.setAddresses(accessCharacteristics.getAddresses() + "[" + start + "-" + end + "]");
        return true;
    }


}
