/**
 * @author Wei Xie
 * @description Sub-circuit inside the loop of estimating n in 2^n (to approximate value x)
 */

package YaoGC;

public class EstimateNSubstep extends CompositeCircuit {
    private int bitLength;
    private int ADD_INDEX = 0, GT_INDEX = 1, MUX_INDEX = 2;

    public EstimateNSubstep(int l) {
        // Two input shares, one output, and one sub-circuit in total
        super(2 * l, l, 3, "EstimateNSubstep_" + (2 * l) + "_" + l);
        bitLength = l;
    }

    // Construct the actual circuit
    protected void createSubCircuits() throws Exception {
        subCircuits[ADD_INDEX] = new ADD_2L_L(bitLength);
        subCircuits[GT_INDEX] = new GT_2L_1(bitLength);
        subCircuits[MUX_INDEX] = new MUX_2Lplus1_L(bitLength);

        super.createSubCircuits();
    }

    /**
     * Connect circuit components using wires
     * Inputs in order: (x, est)
     */
    protected void connectWires() throws Exception {
        for (int i = 0; i < bitLength; i++) {
            // ADD: has two (same) inputs (est)
            inputWires[rightIn(i)].connectTo(subCircuits[ADD_INDEX].inputWires, leftIn(i));
            inputWires[rightIn(i)].connectTo(subCircuits[ADD_INDEX].inputWires, rightIn(i));

            // Greater-Than: est > x ? 1 : 0
            inputWires[rightIn(i)].connectTo(subCircuits[GT_INDEX].inputWires, leftIn(i));
            inputWires[leftIn(i)].connectTo(subCircuits[GT_INDEX].inputWires, rightIn(i));

            // MUX: if 0, then left value, else right value
            subCircuits[ADD_INDEX].outputWires[i].connectTo(subCircuits[MUX_INDEX].inputWires, MUX_2Lplus1_L.Y(i));
            inputWires[rightIn(i)].connectTo(subCircuits[MUX_INDEX].inputWires, MUX_2Lplus1_L.X(i));
        }

        // use the Greater-Than result as decision making for MUX (last bit)
        subCircuits[GT_INDEX].outputWires[0].connectTo(subCircuits[MUX_INDEX].inputWires, inDegree);
    }

    // Output with two parts: (x, new_estimate)
    protected void defineOutputWires() {
        //System.arraycopy(inputWires, 0, outputWires, 0, bitLength);
        //System.arraycopy(subCircuits[MUX_INDEX].outputWires, 0,  outputWires, bitLength, bitLength);
        System.arraycopy(subCircuits[MUX_INDEX].outputWires, 0,  outputWires, 0, bitLength);
    }

    private int leftIn(int i) {
        return i;
    }

    private int rightIn(int i) {
        return bitLength + i;
    }

}