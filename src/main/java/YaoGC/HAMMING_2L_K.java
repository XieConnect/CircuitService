// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package YaoGC;

// Circuit for Hamming distance
public class HAMMING_2L_K extends CompositeCircuit {
    private final int COUNTER;

    public HAMMING_2L_K(int l, int k) {
        // The COUNTER has k bits output including the carry-out of the adder.
        super(2 * l, k, l + 1, "Hamming_" + (2 * l) + "_" + k);

        COUNTER = l;
    }

    // Construct the actual circuit
    protected void createSubCircuits() throws Exception {
        // in the case of two l-bit inputs, inDegree == 2*l
        //?? l copies of XOR to parallelize circuit in future??
        for (int i = 0; i < inDegree / 2; i++)
            subCircuits[i] = new XOR_2_1();
        subCircuits[COUNTER] = new COUNTER_L_K(inDegree / 2, outDegree);

        super.createSubCircuits();
    }

    /**
     * Connect circuit components using wires
     */
    protected void connectWires() throws Exception {
        for (int i = 0; i < subCircuits.length - 1; i++) {
            // provide inputs to each XOR circuit
            inputWires[X(i)].connectTo(subCircuits[i].inputWires, 0);
            inputWires[Y(i)].connectTo(subCircuits[i].inputWires, 1);

            // direct all output of XOR to the COUNTER circuit
            subCircuits[i].outputWires[0].connectTo(subCircuits[COUNTER].inputWires, i);
        }
    }

    protected void defineOutputWires() {
        System.arraycopy(subCircuits[COUNTER].outputWires, 0, outputWires, 0, outDegree);
    }

    private int X(int i) {
        return i + inDegree / 2;
    }

    private int Y(int i) {
        return i;
    }
}