package Program;

import Utils.StopWatch;
import Utils.Utils;
import YaoGC.State;
import YaoGC.Wire;

import java.math.BigInteger;

public class EstimateNClient extends ProgClient {
    private BigInteger cBits;
    private BigInteger[] sBitslbs, cBitslbs;

    private State outputState;

    public EstimateNClient(BigInteger bv, int length) {
        System.out.println("[DEBUG] Client input: " + bv);

        cBits = bv;
        EstimateNCommon.bitVecLen = length;
    }

    protected void init() throws Exception {
        EstimateNCommon.bitVecLen = EstimateNCommon.ois.readInt();

        EstimateNCommon.initCircuits();

        otNumOfPairs = EstimateNCommon.bitVecLen;

        super.init();
    }

    protected void execTransfer() throws Exception {
        sBitslbs = new BigInteger[EstimateNCommon.bitVecLen];

        for (int i = 0; i < EstimateNCommon.bitVecLen; i++) {
            int bytelength = (Wire.labelBitLength - 1) / 8 + 1;
            sBitslbs[i] = Utils.readBigInteger(bytelength, EstimateNCommon.ois);
        }
        StopWatch.taskTimeStamp("receiving labels for peer's inputs");

        cBitslbs = new BigInteger[EstimateNCommon.bitVecLen];
        rcver.execProtocol(cBits);
        cBitslbs = rcver.getData();
        StopWatch.taskTimeStamp("receiving labels for self's inputs");
    }

    protected void execCircuit() throws Exception {
        outputState = EstimateNCommon.execCircuit(sBitslbs, cBitslbs);
    }


    protected void interpretResult() throws Exception {
        EstimateNCommon.oos.writeObject(outputState.toLabels());
        EstimateNCommon.oos.flush();
    }

    protected void verify_result() throws Exception {
        EstimateNCommon.oos.writeObject(cBits);
        EstimateNCommon.oos.flush();
    }
}