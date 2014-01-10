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
    // index of ADD sub-circuit for obtaining X
    private int X_INDEX = 0;

    public EstimateNSubstep(int l, int k) {
        // Two input shares, one output, and one sub-circuit in total
        super(2 * l, k, 1 + subcircuitTypes * maxN, "EstimateNSubstep_" + (2 * l) + "_" + (k) );
        bitLength = l;
    }

    // Note: index 0 is reserved for ADD to get X
    private int GT_INDEX(int i) {
        return subcircuitTypes * i + 1;
    }

    private int MUX_INDEX(int i) {
        return subcircuitTypes * i + 2;
    }

    // Construct the actual circuit
    protected void createSubCircuits() throws Exception {
        subCircuits[X_INDEX] = new ADD_2L_L(bitLength);

        for (int i = 0; i < maxN; i++) {
            subCircuits[GT_INDEX(i)] = new GT_2L_1(bitLength);
            subCircuits[MUX_INDEX(i)] = new MUX_2Lplus1_L(bitLength);
        }

        super.createSubCircuits();
    }

    /**
     * Connect circuit components using wires
     * Inputs are from two shares of X
     */
    protected void connectWires() throws Exception {
        // X will be available at:  subCircuits[X_INDEX].outputWires;  (from bits 0 to bitLength - 1)

        //-- connect first set of subcircuits --
        for (int i = 0; i < bitLength; i++) {
            // ADD to get x
            inputWires[leftIn(i)].connectTo(subCircuits[X_INDEX].inputWires, leftIn(i));
            inputWires[rightIn(i)].connectTo(subCircuits[X_INDEX].inputWires, rightIn(i));

            // Greater-Than: x > est ? 1 : 0
            //NOTE: est will be provided by fixed wires instead
            subCircuits[X_INDEX].outputWires[i].connectTo( subCircuits[GT_INDEX(0)].inputWires, GT_2L_1.X(i) );
        }

        // use the Greater-Than result as decision making for MUX (last bit)
        subCircuits[GT_INDEX(0)].outputWires[0].connectTo(subCircuits[MUX_INDEX(0)].inputWires, inDegree);

        //-- handle other sets of circuits --
        for (int circuitIndex = 1; circuitIndex < subCircuits.length / subcircuitTypes; circuitIndex++) {
            for (int i = 0; i < bitLength; i++) {
                // Greater-Than: x > est ? 1 : 0
                subCircuits[X_INDEX].outputWires[i].connectTo(subCircuits[GT_INDEX(circuitIndex)].inputWires, GT_2L_1.X(i));
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
        System.arraycopy(subCircuits[MUX_INDEX(maxN - 1)].outputWires, 0,
                outputWires, 0, bitLength);
    }

    private int leftIn(int i) {
        return i;
    }

    private int rightIn(int i) {
        return bitLength + i;
    }

    protected void fixInternalWires() {
        // Initialize est = 1
        subCircuits[GT_INDEX(0)].inputWires[GT_2L_1.Y(0)].fixWire(1);
        subCircuits[MUX_INDEX(0)].inputWires[MUX_2Lplus1_L.X(0)].fixWire(1);
        subCircuits[MUX_INDEX(0)].inputWires[MUX_2Lplus1_L.Y(1)].fixWire(1);

        for (int i = 1; i < bitLength; i++) {
           subCircuits[GT_INDEX(0)].inputWires[GT_2L_1.Y(i)].fixWire(0);
           subCircuits[MUX_INDEX(0)].inputWires[MUX_2Lplus1_L.X(i)].fixWire(0);

           // 2*est
           subCircuits[MUX_INDEX(0)].inputWires[MUX_2Lplus1_L.Y((i + 1 + bitLength) % bitLength)].fixWire(0);
        }
    }
}