/**
 * @author Wei Xie
 * @description Sub-circuit inside the loop of estimating n in 2^n (to approximate value x)
 */

package YaoGC;

public class SampleCircuit extends CompositeCircuit {
    private int bitLength;
    //TODO read from config: max N as in 2^n

    public SampleCircuit(int l, int k) {
        // Two input shares, one output, and one sub-circuit in total
        super(2 * l, k, 1, "EstimateNSubstep_" + (2 * l) + "_" + (k) );
        bitLength = l;
    }

    // Construct the actual circuit
    protected void createSubCircuits() throws Exception {
        subCircuits[0] = new GT_2L_1(bitLength);

        super.createSubCircuits();
    }

    /**
     * Connect circuit components using wires
     * Inputs in order: (x, est)
     */
    protected void connectWires() throws Exception {
    }

    // Output with two parts: (x, new_estimate)
    protected void defineOutputWires() {
        // Note: +1 implies shift LEFT! (in bit representation, 0 starts from right-most)
        for (int i = 0; i < bitLength; i++) {
            outputWires[(i + 1 + bitLength) % bitLength] = inputWires[i];
        }
    }

    private int leftIn(int i) {
        return i;
    }

    private int rightIn(int i) {
        return bitLength + i;
    }

}