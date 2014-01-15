/**
 * @author Wei Xie
 * @description Circuit for estimating n in 2^n (to approximate value x)
 *      Outputs: ()
 */

package YaoGC;

public class EstimateN extends CompositeCircuit {
    private int bitLength;

    public EstimateN(int l, int k) {
        // Two input shares, one output, and one sub-circuit in total
        //super(2 * l, l, 2, "EstimateN_" + (2 * l) + "_" + k);
        super(4 * l, l, 2, "EstimateN_" + (2 * l) + "_" + k);

        bitLength = l;
    }

    // Construct the actual circuit
    protected void createSubCircuits() throws Exception {
        subCircuits[0] = new EstimateNSubstep(bitLength, bitLength);
        subCircuits[1] = new ScaleEpsilon(bitLength);

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
        }
    }

    protected void defineOutputWires() {
        //System.arraycopy(subCircuits[1].outputWires, 0, outputWires, 0, bitLength);
        //System.arraycopy(subCircuits[0].outputWires, bitLength, outputWires, bitLength, bitLength);

        System.out.println("Inputs length: " + inputWires.length);
        System.out.println("bitLength: " + bitLength);

        System.arraycopy(inputWires, bitLength * 3, outputWires, 0, bitLength);
    }

    private int leftIn(int i) {
        return i;
    }

    private int rightIn(int i) {
        return bitLength + i;
    }
}