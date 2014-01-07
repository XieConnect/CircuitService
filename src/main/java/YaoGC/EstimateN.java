package YaoGC;

// Circuit for estimating n as in 2^n (to approach value x)
public class EstimateN extends CompositeCircuit {
    private final int COUNTER;

    public EstimateN(int l, int k) {
        // Two input shares, one output, and one sub-circuit in total
        super(2 * l, k, 1, "EstimateN_" + (2 * l) + "_" + k);

        COUNTER = l;
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
        /*
        for (int i = 0; i < subCircuits.length - 1; i++) {
            // provide inputs to each XOR circuit
            inputWires[X(i)].connectTo(subCircuits[i].inputWires, 0);
            inputWires[Y(i)].connectTo(subCircuits[i].inputWires, 1);

            // direct all output of XOR to the COUNTER circuit
            subCircuits[i].outputWires[0].connectTo(subCircuits[COUNTER].inputWires, i);
        }
        */
    }

    protected void defineOutputWires() {
        System.arraycopy(subCircuits[COUNTER].outputWires, 0, outputWires, 0, outDegree);
    }

    private int X() {
        return inDegree;
    }

    private int Y() {
        return 0;
    }
}