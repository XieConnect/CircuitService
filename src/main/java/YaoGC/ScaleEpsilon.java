/**
 * @author Wei Xie
 * @description Circuit for estimating n in 2^n (to approximate value x)
 *  Inputs: index 0 ~ bitLength-1: from server;  index after bitLength: from client
 */

package YaoGC;

import Program.EstimateNConfig;

import java.math.BigInteger;

public class ScaleEpsilon extends CompositeCircuit {
    private int bitLength;
    private static int subcircuitTypes = 2;

    public ScaleEpsilon(int l) {
        super(2 * l, l, 1 + subcircuitTypes * EstimateNConfig.maxN, "ScaleEpsilon_" + (2 * l) + "_" + l);

        bitLength = l;
    }

    private int GT_INDEX(int i) {
        return 1 + i;
    }

    private int MUX_INDEX(int i) {
        return 1 + EstimateNConfig.maxN + i;
    }

    // Construct the actual circuit: one SUB, followed by multiple GTs, and then multiple MUXes
    protected void createSubCircuits() throws Exception {
        subCircuits[0] = new SUB_2L_L(bitLength);

        for (int i = 0; i < EstimateNConfig.maxN; i++) {
            subCircuits[GT_INDEX(i)] = new GT_2L_1(bitLength);
            subCircuits[MUX_INDEX(i)] = new MUX_2Lplus1_L(bitLength);
        }

        super.createSubCircuits();
    }

    /**
     * Connect circuit components using wires
     */
    protected void connectWires() throws Exception {
        for (int i = 0; i < bitLength; i++) {
            // first SUB
            inputWires[rightIn(i)].connectTo(subCircuits[0].inputWires, leftIn(i));

            //-- specially for first iteration of loop
            subCircuits[0].outputWires[i].connectTo(
                    subCircuits[GT_INDEX(0)].inputWires, GT_2L_1.Y(i));

            inputWires[leftIn(i)].connectTo(
                    subCircuits[MUX_INDEX(0)].inputWires, MUX_2Lplus1_L.Y(i));
            inputWires[leftIn(i)].connectTo(
                    subCircuits[MUX_INDEX(0)].inputWires, MUX_2Lplus1_L.X((i + 1) % bitLength));
        }
        subCircuits[GT_INDEX(0)].outputWires[0].connectTo(
                subCircuits[MUX_INDEX(0)].inputWires, 2 * bitLength);

        //-- the rest iterations of for-loop
        for (int circuitIndex = 1; circuitIndex < EstimateNConfig.maxN; circuitIndex++) {
            for (int i = 0; i < bitLength; i++) {
                // jEnd
                subCircuits[0].outputWires[i].connectTo(
                        subCircuits[GT_INDEX(circuitIndex)].inputWires, GT_2L_1.Y(i));

                // est or 2*est (actually should be epsilon)
                subCircuits[MUX_INDEX(circuitIndex - 1)].outputWires[i].connectTo(
                        subCircuits[MUX_INDEX(circuitIndex)].inputWires, MUX_2Lplus1_L.Y(i));
                subCircuits[MUX_INDEX(circuitIndex - 1)].outputWires[i].connectTo(
                        subCircuits[MUX_INDEX(circuitIndex)].inputWires, MUX_2Lplus1_L.X((i + 1) % bitLength));
            }

            subCircuits[GT_INDEX(circuitIndex)].outputWires[0].connectTo(
                    subCircuits[MUX_INDEX(circuitIndex)].inputWires, 2 * bitLength);
        }
    }

    protected void defineOutputWires() {
        System.arraycopy(subCircuits[MUX_INDEX(EstimateNConfig.maxN - 1)].outputWires, 0,
                outputWires, 0, bitLength);
    }

    private int leftIn(int i) {
        return i;
    }

    private int rightIn(int i) {
        return bitLength + i;
    }

    protected void fixInternalWires() {
        BigInteger maxNBig = BigInteger.valueOf(EstimateNConfig.maxN);

        for (int i = 0; i < bitLength; i++) {
            subCircuits[0].inputWires[rightIn(i)].fixWire( (maxNBig.testBit(i) ? 1 : 0) );
        }

        for (int circuitIndex = 0; circuitIndex < EstimateNConfig.maxN; circuitIndex++) {
            for (int i = 0; i < bitLength; i++) {
                subCircuits[GT_INDEX(circuitIndex)].inputWires[GT_2L_1.X(i)].fixWire(
                        (BigInteger.valueOf(circuitIndex).testBit(i) ? 1 : 0) );
            }
        }
    }
}