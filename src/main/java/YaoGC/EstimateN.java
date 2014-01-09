/**
 * @author Wei Xie
 * @description Circuit for estimating n in 2^n (to approximate value x)
 */

package YaoGC;

public class EstimateN extends CompositeCircuit {
    private int bitLength;

    public EstimateN(int l, int k) {
        // Two input shares, one output, and one sub-circuit in total
        super(2 * l, k, 1, "EstimateN_" + (2 * l) + "_" + k);
        bitLength = l;
    }

    // Construct the actual circuit
    protected void createSubCircuits() throws Exception {
        subCircuits[0] = new EstimateNSubstep(bitLength, bitLength);

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
        }
    }

    protected void defineOutputWires() {
        System.arraycopy(subCircuits[0].outputWires, bitLength, outputWires, 0, bitLength);
    }

    private int leftIn(int i) {
        return i;
    }

    private int rightIn(int i) {
        return bitLength + i;
    }
}