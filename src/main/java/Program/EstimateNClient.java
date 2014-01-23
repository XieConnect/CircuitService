package Program;

import Utils.StopWatch;
import Utils.Utils;
import YaoGC.State;
import YaoGC.Wire;

import java.math.BigInteger;
import java.util.Random;

public class EstimateNClient extends ProgClient {
    private BigInteger cBits;
    private BigInteger[] sBitslbs, cBitslbs;
    public static BigInteger randa = BigInteger.valueOf(0),  randb = BigInteger.valueOf(0);

    private State outputState;

    // max bit length of data representation
    public EstimateNClient(int maxInputBits, int maxNLoops) {
        EstimateNConfig.MaxInputBits = maxInputBits;
        EstimateNCommon.bitVecLen = maxInputBits;
        EstimateNConfig.MaxNLoops = maxNLoops;
    }

    public void setInputs(BigInteger bitValue) {
        cBits = bitValue;
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
        StopWatch.taskTimeStamp("OT inputs (peer's)");

        cBitslbs = new BigInteger[EstimateNCommon.bitVecLen];
        rcver.execProtocol(cBits);
        cBitslbs = rcver.getData();
        StopWatch.taskTimeStamp("OT inputs (self's)");
    }

    protected void execCircuit() throws Exception {
        outputState = EstimateNCommon.execCircuit(sBitslbs, cBitslbs);
    }

    protected void interpretResult() throws Exception {
        System.out.println("# client to result to server...");

        EstimateNCommon.oos.writeObject(outputState.toLabels());
        EstimateNCommon.oos.flush();

        System.out.println("  client finished sending.");
    }

    protected void verify_result() throws Exception {
        EstimateNCommon.oos.writeObject(cBits);
        EstimateNCommon.oos.flush();
    }
}