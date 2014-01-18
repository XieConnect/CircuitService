/**
 * @author Wei Xie
 * @description Sub-circuit inside the loop of estimating n in 2^n (to approximate value x)
 * Output (in order): (est, n)
 */

package YaoGC;

import Program.EstimateNConfig;

public class EstimateNSubstep extends CompositeCircuit {
    private int bitLength;
    private static int subcircuitTypes = 4;
    // max N as in 2^n
    private static int maxN = EstimateNConfig.maxN;
    // index of ADD sub-circuit for obtaining X
    private int X_INDEX = 0;
    // index of last subtraction sub-circuit
    private int SUB_INDEX = subCircuits.length - 1;

    public EstimateNSubstep(int l, int k) {
        // Two input shares, one output, and one sub-circuit in total
        super(2 * l, 2 * k, 1 + subcircuitTypes * maxN + 1, "EstimateNSubstep_" + (2 * l) + "_" + (2 * k) );
        bitLength = l;
    }

    // Note: index 0 is reserved for ADD to get X
    private int GT_INDEX(int i) {
        return subcircuitTypes * i + 1;
    }

    private int MUX_INDEX(int i) {
        return subcircuitTypes * i + 2;
    }

    // for (n+1) sub-circuit
    private int ADD1_N_INDEX(int i) {
        return subcircuitTypes * i + 3;
    }

    // for (n+1) sub-circuit
    private int MUX_N_INDEX(int i) {
        return subcircuitTypes * i + 4;
    }

    // Construct the actual circuit
    protected void createSubCircuits() throws Exception {
        subCircuits[X_INDEX] = new ADD_2L_L(bitLength);

        for (int i = 0; i < maxN; i++) {
            subCircuits[GT_INDEX(i)] = new GT_2L_1(bitLength);

            // for estimating est
            subCircuits[MUX_INDEX(i)] = new MUX_2Lplus1_L(bitLength);

            // for estimating n
            subCircuits[ADD1_N_INDEX(i)] = new ADD1_Lplus1_L(bitLength);
            subCircuits[MUX_N_INDEX(i)] = new MUX_2Lplus1_L(bitLength);
        }

        subCircuits[SUB_INDEX] = new SUB_2L_L(bitLength);

        super.createSubCircuits();
    }

    /**
     * Connect circuit components using wires
     * Inputs are from two shares of X
     */
    protected void connectWires() throws Exception {
        // X result:  subCircuits[X_INDEX].outputWires;  (from bits 0 to bitLength - 1)

        //-- connect first set of subcircuits --
        for (int i = 0; i < bitLength; i++) {
            // ADD to get x
            inputWires[leftIn(i)].connectTo(subCircuits[X_INDEX].inputWires, leftIn(i));
            inputWires[rightIn(i)].connectTo(subCircuits[X_INDEX].inputWires, rightIn(i));

            // Greater-Than: x > est ? 1 : 0
            //NOTE: est will be provided by fixed wires instead
            subCircuits[X_INDEX].outputWires[i].connectTo( subCircuits[GT_INDEX(0)].inputWires, GT_2L_1.X(i) );

            subCircuits[ADD1_N_INDEX(0)].outputWires[i].connectTo(
                    subCircuits[MUX_N_INDEX(0)].inputWires, MUX_2Lplus1_L.Y(i) );
        }

        // use the Greater-Than result as decision signal for MUX
        subCircuits[GT_INDEX(0)].outputWires[0].connectTo(
                subCircuits[MUX_INDEX(0)].inputWires, inDegree);
        subCircuits[GT_INDEX(0)].outputWires[0].connectTo(
                subCircuits[MUX_N_INDEX(0)].inputWires, inDegree);


        //-- handle other sets of circuits --
        for (int circuitIndex = 1; circuitIndex < maxN; circuitIndex++) {
            for (int i = 0; i < bitLength; i++) {
                // Greater-Than: x > est ? 1 : 0
                subCircuits[X_INDEX].outputWires[i].connectTo(
                        subCircuits[GT_INDEX(circuitIndex)].inputWires, GT_2L_1.X(i));
                subCircuits[MUX_INDEX(circuitIndex - 1)].outputWires[i].connectTo(
                        subCircuits[GT_INDEX(circuitIndex)].inputWires, GT_2L_1.Y(i));

                // MUX: if 0, then left value, else right value; (we use bit shifting to compute 2*est)
                // most significant bit will be lost (bitLength-1)
                if (i < bitLength - 1) {
                    subCircuits[MUX_INDEX(circuitIndex - 1)].outputWires[i].connectTo(
                            subCircuits[MUX_INDEX(circuitIndex)].inputWires, MUX_2Lplus1_L.Y( i + 1 ));
                }

                subCircuits[MUX_INDEX(circuitIndex - 1)].outputWires[i].connectTo(
                        subCircuits[MUX_INDEX(circuitIndex)].inputWires, MUX_2Lplus1_L.X(i));


                //-- Estimating n
                // input n starts from the 2nd bit for ADD1
                subCircuits[MUX_N_INDEX(circuitIndex - 1)].outputWires[i].connectTo(
                    subCircuits[ADD1_N_INDEX(circuitIndex)].inputWires, i+1);
                subCircuits[ADD1_N_INDEX(circuitIndex)].outputWires[i].connectTo(
                    subCircuits[MUX_N_INDEX(circuitIndex)].inputWires, MUX_2Lplus1_L.Y(i) );
                subCircuits[MUX_N_INDEX(circuitIndex - 1)].outputWires[i].connectTo(
                        subCircuits[MUX_N_INDEX(circuitIndex)].inputWires, MUX_2Lplus1_L.X(i));
            }

            subCircuits[GT_INDEX(circuitIndex)].outputWires[0].connectTo(
                    subCircuits[MUX_INDEX(circuitIndex)].inputWires, inDegree);

            subCircuits[GT_INDEX(circuitIndex)].outputWires[0].connectTo(
                    subCircuits[MUX_N_INDEX(circuitIndex)].inputWires, inDegree);
        }

        //-- Subtraction to compute est = x - est
        for (int i = 0; i < bitLength; i++) {
            subCircuits[MUX_INDEX(maxN - 1)].outputWires[i].connectTo(
                    subCircuits[SUB_INDEX].inputWires, leftIn(i) );
            subCircuits[X_INDEX].outputWires[i].connectTo(
                    subCircuits[SUB_INDEX].inputWires, rightIn(i) );
        }
    }

    // circuit output: epsilon, and the n; e.g.: (est, n)
    protected void defineOutputWires() {
        System.arraycopy(subCircuits[SUB_INDEX].outputWires, 0,
                outputWires, 0, bitLength);
        System.arraycopy(subCircuits[MUX_N_INDEX(maxN - 1)].outputWires, 0,
                outputWires, bitLength, bitLength);
    }

    private int leftIn(int i) {
        return i;
    }

    private int rightIn(int i) {
        return bitLength + i;
    }

    protected void fixInternalWires() {
        int initialEst = 1;
        int initialN = 0;

        //-- For estimating est
        subCircuits[GT_INDEX(0)].inputWires[GT_2L_1.Y(0)].fixWire(initialEst);
        // assign value 1
        subCircuits[MUX_INDEX(0)].inputWires[MUX_2Lplus1_L.X(0)].fixWire(initialEst);
        // assign value 2
        subCircuits[MUX_INDEX(0)].inputWires[MUX_2Lplus1_L.Y(1)].fixWire(initialEst);
        subCircuits[MUX_INDEX(0)].inputWires[MUX_2Lplus1_L.Y(0)].fixWire(0);


        for (int i = 1; i < bitLength; i++) {
           subCircuits[GT_INDEX(0)].inputWires[GT_2L_1.Y(i)].fixWire(0);
           subCircuits[MUX_INDEX(0)].inputWires[MUX_2Lplus1_L.X(i)].fixWire(0);
           // assign 2*est through LEFT bit-shifting
            if (i < bitLength - 1) {
                subCircuits[MUX_INDEX(0)].inputWires[MUX_2Lplus1_L.Y(i + 1)].fixWire(0);
            }
        }


        for (int circuitIndex = 0; circuitIndex < maxN; circuitIndex++) {
            //-- for estimating n
            // set first inputs of all ADD1 to 1
            subCircuits[ADD1_N_INDEX(circuitIndex)].inputWires[0].fixWire(1);

            // For est: when shifting left to get 2*est, right-most bit will be complemented by 0
            subCircuits[MUX_INDEX(circuitIndex)].inputWires[MUX_2Lplus1_L.Y(0)].fixWire(0);
        }

        // Initialize n = 1
        subCircuits[ADD1_N_INDEX(0)].inputWires[1].fixWire(initialN);
        subCircuits[MUX_N_INDEX(0)].inputWires[MUX_2Lplus1_L.X(0)].fixWire(initialN);

        for (int i = 2; i < bitLength + 1; i++) {
            subCircuits[ADD1_N_INDEX(0)].inputWires[i].fixWire(0);
            subCircuits[MUX_N_INDEX(0)].inputWires[MUX_2Lplus1_L.X(i - 1)].fixWire(0);
        }
    }
}