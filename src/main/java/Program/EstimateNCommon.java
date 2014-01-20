package Program;

import Utils.StopWatch;
import YaoGC.*;

import java.math.BigInteger;

class EstimateNCommon extends ProgCommon {
    // max bit length of allowed input values (value representation)
    static int bitVecLen;

    // Initialize the Hamming circuit
    protected static void initCircuits() {
        ccs = new Circuit[1];
        ccs[0] = new EstimateN(bitVecLen, bitVecLen);
    }

    public static State execCircuit(BigInteger[] slbs, BigInteger[] clbs) throws Exception {
        BigInteger[] lbs = new BigInteger[2 * bitVecLen];

        // FYI: arraycopy(src,  srcPos, dest, destPos, length)
        System.arraycopy(slbs, 0, lbs, 0, bitVecLen);
        System.arraycopy(clbs, 0, lbs, bitVecLen, clbs.length);
        State in = State.fromLabels(lbs);

        State out = ccs[0].startExecuting(in);

        StopWatch.taskTimeStamp("circuit garbling");

        return out;
    }
}