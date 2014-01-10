/**
 * @author Wei Xie
 * @description Sub-circuit inside the loop of estimating n in 2^n (to approximate value x)
 */

package YaoGC;

public class EstimateNSubstep extends CompositeCircuit {
    private int bitLength;
    private static int subcircuitTypes = 2;
    //TODO read from config: max N as in 2^n
    private static int maxN = 80;

    public EstimateNSubstep(int l, int k) {
        // Two input shares, one output, and one sub-circuit in total
        super(2 * l, k, subcircuitTypes * maxN, "EstimateNSubstep_" + (2 * l) + "_" + (k) );
        bitLength = l;
    }

    private int GT_INDEX(int i) {
        return subcircuitTypes * i;
    }

    private int MUX_INDEX(int i) {
        return 1 + subcircuitTypes * i;
    }

    // Construct the actual circuit
    protected void createSubCircuits() throws Exception {
        for (int i = 0; i < subCircuits.length / subcircuitTypes; i++) {
            subCircuits[GT_INDEX(i)] = new GT_2L_1(bitLength);
            subCircuits[MUX_INDEX(i)] = new MUX_2Lplus1_L(bitLength);
        }

        super.createSubCircuits();
    }

    /**
     * Connect circuit components using wires
     * Inputs in order: (x, est)
     */
    protected void connectWires() throws Exception {
        //-- connect first set of subcircuits --
        for (int i = 0; i < bitLength; i++) {
            // Greater-Than: x > est ? 1 : 0
            inputWires[leftIn(i)].connectTo( subCircuits[GT_INDEX(0)].inputWires, GT_2L_1.X(i) );
            inputWires[rightIn(i)].connectTo( subCircuits[GT_INDEX(0)].inputWires, GT_2L_1.Y(i) );

            // MUX: if 0, then left value, else right value
            inputWires[rightIn(i)].connectTo(
                    subCircuits[MUX_INDEX(0)].inputWires, MUX_2Lplus1_L.Y( (i + 1 + bitLength) % bitLength ));
            inputWires[rightIn(i)].connectTo( subCircuits[MUX_INDEX(0)].inputWires, MUX_2Lplus1_L.X(i) );
        }

        // use the Greater-Than result as decision making for MUX (last bit)
        subCircuits[GT_INDEX(0)].outputWires[0].connectTo(subCircuits[MUX_INDEX(0)].inputWires, inDegree);

        //-- now handle other sets of circuits --
        for (int circuitIndex = 1; circuitIndex < subCircuits.length / subcircuitTypes; circuitIndex++) {
            for (int i = 0; i < bitLength; i++) {
                // Greater-Than: x > est ? 1 : 0
                inputWires[leftIn(i)].connectTo(subCircuits[GT_INDEX(circuitIndex)].inputWires, GT_2L_1.X(i));
                subCircuits[MUX_INDEX(circuitIndex - 1)].outputWires[i].connectTo(
                        subCircuits[GT_INDEX(circuitIndex)].inputWires, GT_2L_1.Y(i));

                // MUX: if 0, then left value, else right value
                subCircuits[MUX_INDEX(circuitIndex - 1)].outputWires[i].connectTo(
                        subCircuits[MUX_INDEX(circuitIndex)].inputWires, MUX_2Lplus1_L.Y( (i + 1 + bitLength) % bitLength ));
                subCircuits[MUX_INDEX(circuitIndex - 1)].outputWires[i].connectTo(
                        subCircuits[MUX_INDEX(circuitIndex)].inputWires, MUX_2Lplus1_L.X(i));
            }

            subCircuits[GT_INDEX(circuitIndex)].outputWires[0].connectTo(
                    subCircuits[MUX_INDEX(circuitIndex)].inputWires, inDegree);
        }
    }

    // Output with two parts: (x, new_estimate)
    protected void defineOutputWires() {
        //System.arraycopy(inputWires, 0, outputWires, 0, bitLength);
        System.arraycopy(subCircuits[MUX_INDEX(subCircuits.length / subcircuitTypes - 1)].outputWires, 0,
                outputWires, 0, bitLength);
    }

    private int leftIn(int i) {
        return i;
    }

    private int rightIn(int i) {
        return bitLength + i;
    }

}