package YaoGC;

/*
 * Subtraction: Fig. 3 of [KSS09] (similar to the ADD_2L_L)
 * For inputs <X, Y>, it will compute Y - X  via two-complement and addition
 */
public class SUB_2L_L extends CompositeCircuit {
    private final int L;

    public SUB_2L_L(int l) {
        super(2 * l, l, 1 + l, "SUB_" + (2 * l) + "_" + l);

        L = l;
    }

    protected void createSubCircuits() throws Exception {
        subCircuits[0] = new XOR_2L_L(L);

        for (int i = 1; i < L+1; i++) {
            subCircuits[i] = new ADD_3_2();
        }

        super.createSubCircuits();
    }

    protected void connectWires() {
        inputWires[Y(0)].connectTo(subCircuits[0].inputWires, Y(0));

        inputWires[X(0)].connectTo(subCircuits[1].inputWires, ADD_3_2.X);
        subCircuits[0].outputWires[0].connectTo(subCircuits[1].inputWires, ADD_3_2.Y);

        for (int i = 2; i < L+1; i++) {
            // XOR
            inputWires[Y(i-1)].connectTo(subCircuits[0].inputWires, Y(i-1));

            // ADD
            inputWires[X(i-1)].connectTo(subCircuits[i].inputWires, ADD_3_2.X);
            subCircuits[0].outputWires[i-1].connectTo(subCircuits[i].inputWires, ADD_3_2.Y);
            subCircuits[i - 1].outputWires[ADD_3_2.COUT].connectTo(
                    subCircuits[i].inputWires, ADD_3_2.CIN);
        }
    }

    protected void defineOutputWires() {
        for (int i = 1; i < L+1; i++) {
            outputWires[i-1] = subCircuits[i].outputWires[ADD_3_2.S];
        }
    }

    protected void fixInternalWires() {
        subCircuits[1].inputWires[ADD_3_2.CIN].fixWire(1);

        // Implement NOT using (XOR 1)
        for (int i = 0; i < L; i++) {
            subCircuits[0].inputWires[X(i)].fixWire(1);
        }
    }

    private int X(int i) {
        return i + L;
    }

    private int Y(int i) {
        return i;
    }
}