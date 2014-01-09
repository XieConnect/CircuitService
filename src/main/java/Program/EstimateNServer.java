package Program;

import Utils.StopWatch;
import Utils.Utils;
import YaoGC.State;
import YaoGC.Wire;

import java.math.BigInteger;
import java.util.Random;

public class EstimateNServer extends ProgServer {
    // input value (in decimal; "server bits")
    private BigInteger sBits;

    private State outputState;

    private BigInteger[][] sBitslps, cBitslps;

    private static final Random rnd = new Random();

    //NOTE updated
    /**
     * @param bv input value to circuit (in decimal representation)
     * @param length max bit length of allowed input values
     */
    public EstimateNServer(BigInteger bv, int length, int maxN) {
        System.out.println("[DEBUG] Serv input: " + bv);
        sBits = bv;
        EstimateNCommon.bitVecLen = length;
    }

    //NOTE updated
    // initialize circuit (automatically prior to run())
    protected void init() throws Exception {
        EstimateNCommon.oos.writeInt(EstimateNCommon.bitVecLen);
        EstimateNCommon.oos.flush();

        // create circuit (high-level entry)
        EstimateNCommon.initCircuits();

        generateLabelPairs();

        super.init();
    }

    //NOTE updated
    private void generateLabelPairs() {
        sBitslps = new BigInteger[EstimateNCommon.bitVecLen][2];
        cBitslps = new BigInteger[EstimateNCommon.bitVecLen][2];

        for (int i = 0; i < EstimateNCommon.bitVecLen; i++) {
            BigInteger glb0 = new BigInteger(Wire.labelBitLength, rnd);
            BigInteger glb1 = glb0.xor(Wire.R.shiftLeft(1).setBit(0));
            sBitslps[i][0] = glb0;
            sBitslps[i][1] = glb1;

            glb0 = new BigInteger(Wire.labelBitLength, rnd);
            glb1 = glb0.xor(Wire.R.shiftLeft(1).setBit(0));
            cBitslps[i][0] = glb0;
            cBitslps[i][1] = glb1;
        }
    }

    //NOTE updated
    // Send input to opponent
    protected void execTransfer() throws Exception {
        for (int i = 0; i < EstimateNCommon.bitVecLen; i++) {
            // check whether current bit is set or not
            int idx = sBits.testBit(i) ? 1 : 0;

            int bytelength = (Wire.labelBitLength - 1) / 8 + 1;
            Utils.writeBigInteger(sBitslps[i][idx], bytelength, EstimateNCommon.oos);
        }

        EstimateNCommon.oos.flush();
        StopWatch.taskTimeStamp("sending labels for selfs inputs");

        snder.execProtocol(cBitslps);
        StopWatch.taskTimeStamp("sending labels for peers inputs");
    }

    //NOTE updated
    protected void execCircuit() throws Exception {
        BigInteger[] sBitslbs = new BigInteger[EstimateNCommon.bitVecLen];
        BigInteger[] cBitslbs = new BigInteger[EstimateNCommon.bitVecLen];

        for (int i = 0; i < sBitslps.length; i++)
            sBitslbs[i] = sBitslps[i][0];

        for (int i = 0; i < cBitslps.length; i++)
            cBitslbs[i] = cBitslps[i][0];

        outputState = EstimateNCommon.execCircuit(sBitslbs, cBitslbs);
    }

    //NOTE updated
    protected void interpretResult() throws Exception {
        BigInteger[] outLabels = (BigInteger[]) EstimateNCommon.ois.readObject();

        BigInteger output = BigInteger.ZERO;
        for (int i = 0; i < outLabels.length; i++) {
            if (outputState.wires[i].value != Wire.UNKNOWN_SIG) {
                if (outputState.wires[i].value == 1)
                    output = output.setBit(i);
                continue;
            } else if (outLabels[i].equals(outputState.wires[i].invd ?
                    outputState.wires[i].lbl :
                    outputState.wires[i].lbl.xor(Wire.R.shiftLeft(1).setBit(0)))) {
                output = output.setBit(i);
            } else if (!outLabels[i].equals(outputState.wires[i].invd ?
                    outputState.wires[i].lbl.xor(Wire.R.shiftLeft(1).setBit(0)) :
                    outputState.wires[i].lbl))
                throw new Exception("Bad label encountered: i = " + i + "\t" +
                        outLabels[i] + " != (" +
                        outputState.wires[i].lbl + ", " +
                        outputState.wires[i].lbl.xor(Wire.R.shiftLeft(1).setBit(0)) + ")");

        }

        System.out.println("# OUTPUT (pp):     " + output);
        StopWatch.taskTimeStamp("output labels received and interpreted");
    }

    //NOTE updated
    protected void verify_result() throws Exception {
        BigInteger cBits = (BigInteger) EstimateNCommon.ois.readObject();

        System.out.println("# INPUTS (DEBUG): [Server]: " + sBits +
                         "\n                  [Client]: " + cBits + "\n");
    }
}