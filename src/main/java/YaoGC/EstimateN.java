/**
 * @author Wei Xie
 * @description Circuit for estimating n in 2^n (to approximate value x)
 */

package YaoGC;

public class EstimateN extends CompositeCircuit {
    public EstimateN(int l, int k) {
        // Two input shares, one output, and one sub-circuit in total
        super(2 * l, k, 1, "EstimateN_" + (2 * l) + "_" + k);
    }

    // Construct the actual circuit
    protected void createSubCircuits() throws Exception {
        subCircuits[0] = new ADD_2L_L(inDegree / 2);

        super.createSubCircuits();
    }

    /**
     * Connect circuit components using wires
     */
    protected void connectWires() throws Exception {
        for (int i = 0; i < inDegree / 2; i++) {
            inputWires[X(i)].connectTo(subCircuits[0].inputWires, X(i));
            inputWires[Y(i)].connectTo(subCircuits[0].inputWires, Y(i));
        }
    }

    protected void defineOutputWires() {
        System.arraycopy(subCircuits[0].outputWires, 0, outputWires, 0, outDegree);
    }

    private int X(int i) {
        return inDegree / 2 + i;
    }

    private int Y(int i) {
        return i;
    }
}