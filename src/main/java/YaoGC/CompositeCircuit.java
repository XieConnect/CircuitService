// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package YaoGC;

public abstract class CompositeCircuit extends Circuit {
    protected Circuit[] subCircuits;
    protected int nSubCircuits;

    /**
     * Constructor
     *
     * @param inDegree     number of inputs
     * @param outDegree    number of outputs
     * @param nSubCircuits number of sub-circuits
     * @param name
     * @author Yan Huang
     * @author Wei Xie
     */
    public CompositeCircuit(int inDegree, int outDegree, int nSubCircuits, String name) {
        super(inDegree, outDegree, name);

        this.nSubCircuits = nSubCircuits;

        subCircuits = new Circuit[nSubCircuits];
    }

    /**
     * Major construction logic
     * Need to override a few functions being called here
     * @author Yan Huang, Wei Xie
     */
    public void build() throws Exception {
        createInputWires();
        createSubCircuits();
        connectWires();
        defineOutputWires();
        fixInternalWires();
    }

    /**
     * This method gets overloaded in child class
     */
    protected void createSubCircuits() throws Exception {
        for (int i = 0; i < nSubCircuits; i++)
            subCircuits[i].build();
    }

    abstract protected void connectWires() throws Exception;

    abstract protected void defineOutputWires();

    protected void fixInternalWires() {
    }

    protected void compute() {
    }

    protected void execute() {
    }
}
