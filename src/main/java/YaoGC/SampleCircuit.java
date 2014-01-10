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
        subCircuits[0] = new ADD1_Lplus1_L(bitLength);

        super.createSubCircuits();
    }

    /**
     * Connect circuit components using wires
     * Inputs in order: (x, est)
     */
    protected void connectWires() throws Exception {
        /*
        for (int i = 0; i < bitLength; i++) {
            inputWires[i].connectTo( subCircuits[0].inputWires, i + 1 );
        }
        */
    }

    // Output with two parts: (x, new_estimate)
    protected void defineOutputWires() {
        for (int i = 0; i < bitLength; i++) {
            outputWires[i] = subCircuits[0].outputWires[i];
        }

    }

    private int leftIn(int i) {
        return i;
    }

    private int rightIn(int i) {
        return bitLength + i;
    }

    protected void fixInternalWires() {
        subCircuits[0].inputWires[0].fixWire(1);

        subCircuits[0].inputWires[1].fixWire(1);
        for (int i = 2; i < bitLength + 1; i++) {
            subCircuits[0].inputWires[i].fixWire(0);
        }
    }
}