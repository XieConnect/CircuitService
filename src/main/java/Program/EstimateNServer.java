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

    // output results
    int numberOutputs = 2;
    public BigInteger[] results;

    /**
     * @param maxInputBits max bit length of allowed input values
     */
    public EstimateNServer(int maxInputBits, int maxNLoops) {
        EstimateNConfig.MaxInputBits = maxInputBits;
        EstimateNConfig.MaxNLoops = maxNLoops;
        EstimateNCommon.bitVecLen = maxInputBits;
    }

    // initialize circuit (it needs to be explicitly called)
    protected void init() throws Exception {
        EstimateNCommon.oos.writeInt(EstimateNCommon.bitVecLen);
        EstimateNCommon.oos.flush();

        // create circuit (high-level entry)
        EstimateNCommon.initCircuits();

        generateLabelPairs();

        super.init();
    }

    public void setInputs(BigInteger bitValue) {
        sBits = bitValue;
    }

    private void generateLabelPairs() {
        sBitslps = new BigInteger[EstimateNCommon.bitVecLen][2];
        cBitslps = new BigInteger[EstimateNCommon.bitVecLen][2];
        BigInteger glb0, glb1;

        for (int i = 0; i < EstimateNCommon.bitVecLen; i++) {
            glb0 = new BigInteger(Wire.labelBitLength, rnd);
            glb1 = glb0.xor(Wire.R.shiftLeft(1).setBit(0));
            sBitslps[i][0] = glb0;
            sBitslps[i][1] = glb1;
        }

        // for client
        for (int i = 0; i < EstimateNCommon.bitVecLen; i++) {
            glb0 = new BigInteger(Wire.labelBitLength, rnd);
            glb1 = glb0.xor(Wire.R.shiftLeft(1).setBit(0));
            cBitslps[i][0] = glb0;
            cBitslps[i][1] = glb1;
        }
    }

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

    protected void execCircuit() throws Exception {
        BigInteger[] sBitslbs = new BigInteger[EstimateNCommon.bitVecLen];
        BigInteger[] cBitslbs = new BigInteger[EstimateNCommon.bitVecLen];

        for (int i = 0; i < sBitslps.length; i++)
            sBitslbs[i] = sBitslps[i][0];

        for (int i = 0; i < cBitslps.length; i++)
            cBitslbs[i] = cBitslps[i][0];

        outputState = EstimateNCommon.execCircuit(sBitslbs, cBitslbs);
    }

    protected void interpretResult() throws Exception {
        results = new BigInteger[numberOutputs];

        BigInteger[] outLabels = (BigInteger[]) EstimateNCommon.ois.readObject();
        int lengthPerOutput = outLabels.length / numberOutputs;
        BigInteger output = BigInteger.ZERO;
        int wireIndex = -1;

        for (int outputIndex = 0; outputIndex < numberOutputs; outputIndex++) {

            for (int i = 0; i < lengthPerOutput; i++) {
                wireIndex = outputIndex * lengthPerOutput + i;

                if (outputState.wires[wireIndex].value != Wire.UNKNOWN_SIG) {
                    if (outputState.wires[wireIndex].value == 1) {
                        output = output.setBit(i);
                    }

                    // never skip result processing if reached end of current iteration
                    if (lengthPerOutput -1 != i) {
                        continue;
                    }
                } else if (outLabels[wireIndex].equals(outputState.wires[wireIndex].invd ?
                        outputState.wires[wireIndex].lbl :
                        outputState.wires[wireIndex].lbl.xor(Wire.R.shiftLeft(1).setBit(0)))) {

                    output = output.setBit(i);
                } else if (!outLabels[wireIndex].equals(outputState.wires[wireIndex].invd ?
                        outputState.wires[wireIndex].lbl.xor(Wire.R.shiftLeft(1).setBit(0)) :
                        outputState.wires[wireIndex].lbl)) {

                    throw new Exception("Bad label encountered: i = " + i + "\t" +
                            outLabels[wireIndex] + " != (" +
                            outputState.wires[wireIndex].lbl + ", " +
                            outputState.wires[wireIndex].lbl.xor(Wire.R.shiftLeft(1).setBit(0)) + ")");
                }

                // Result processing: store current value; reset for next output
                if (lengthPerOutput -1 == i) {
                    results[outputIndex] = output;
                    output = BigInteger.ZERO;
                }
            }

        }

        StopWatch.taskTimeStamp("output labels received and interpreted");
    }

    protected void verify_result() throws Exception {
        BigInteger cBits = (BigInteger) EstimateNCommon.ois.readObject();

        System.out.println("# INPUTS (DEBUG): [Server]: " + sBits +
                         "\n                  [Client]: " + cBits + "\n");
    }
}