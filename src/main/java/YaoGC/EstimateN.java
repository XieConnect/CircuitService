/**
 * @author Wei Xie
 * @description Circuit for estimating n in 2^n (to approximate value x)
 *      Outputs: ()
 */

package YaoGC;

import Program.EstimateNClient;

public class EstimateN extends CompositeCircuit {
    private int bitLength;

    public EstimateN(int l, int k) {
        // Two input shares, one output, and one sub-circuit in total
        super(2 * l, 2 * l, 4, "EstimateN_" + (2 * l) + "_" + k);

        bitLength = l;
    }

    // Construct the actual circuit
    protected void createSubCircuits() throws Exception {
        subCircuits[0] = new EstimateNSubstep(bitLength, bitLength);
        subCircuits[1] = new ScaleEpsilon(bitLength);
        // for randomizing outputs
        subCircuits[2] = new SUB_2L_L(bitLength);
        subCircuits[3] = new SUB_2L_L(bitLength);

        super.createSubCircuits();
    }

    /**
     * Connect circuit components using wires
     * Circuit inputs: (x, est)
     */
    protected void connectWires() throws Exception {
        for (int i = 0; i < bitLength; i++) {
            inputWires[leftIn(i)].connectTo(subCircuits[0].inputWires, leftIn(i));
            inputWires[rightIn(i)].connectTo(subCircuits[0].inputWires, rightIn(i));

            subCircuits[0].outputWires[leftIn(i)].connectTo(subCircuits[1].inputWires, leftIn(i));
            subCircuits[0].outputWires[rightIn(i)].connectTo(subCircuits[1].inputWires, rightIn(i));

            subCircuits[1].outputWires[i].connectTo(subCircuits[2].inputWires, rightIn(i));
            subCircuits[0].outputWires[rightIn(i)].connectTo(subCircuits[3].inputWires, rightIn(i));
        }
    }

    protected void defineOutputWires() {
        System.arraycopy(subCircuits[2].outputWires, 0, outputWires, 0, bitLength);
        System.arraycopy(subCircuits[3].outputWires, 0, outputWires, bitLength, bitLength);
    }

    private int leftIn(int i) {
        return i;
    }

    private int rightIn(int i) {
        return bitLength + i;
    }

    protected void fixInternalWires() {
        for (int i = 0; i < bitLength; i++) {
            subCircuits[2].inputWires[leftIn(i)].fixWire(
                    EstimateNClient.randa.testBit(i) ? 1 : 0);
            subCircuits[3].inputWires[leftIn(i)].fixWire(
                    EstimateNClient.randb.testBit(i) ? 1 : 0);
        }
    }
}