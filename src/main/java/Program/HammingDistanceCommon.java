// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import Utils.StopWatch;
import YaoGC.Circuit;
import YaoGC.HAMMING_2L_K;
import YaoGC.State;

import java.math.BigInteger;

class HammingDistanceCommon extends ProgCommon {
    // max bit length of allowed input values
    static int bitVecLen;

    static int bitLength(int x) {
        return BigInteger.valueOf(x).bitLength();
    }

    // Initialize the Hamming circuit
    protected static void initCircuits() {
        ccs = new Circuit[1];
        ccs[0] = new HAMMING_2L_K(bitVecLen, bitLength(bitVecLen) + 1);
    }

    public static State execCircuit(BigInteger[] slbs, BigInteger[] clbs) throws Exception {
        BigInteger[] lbs = new BigInteger[2 * bitVecLen];

        // FYI: arraycopy(src,  srcPos, dest, destPos, length)
        System.arraycopy(slbs, 0, lbs, 0, bitVecLen);
        System.arraycopy(clbs, 0, lbs, bitVecLen, bitVecLen);
        State in = State.fromLabels(lbs);

        State out = ccs[0].startExecuting(in);

        StopWatch.taskTimeStamp("circuit garbling");

        return out;
    }
}