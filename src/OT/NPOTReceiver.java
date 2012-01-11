// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>
// Copyright (C) 2011 by Nathaniel Husted <nhusted@indiana.edu>

package OT;

import java.math.*;
import java.util.*;
import java.io.*;
import Cipher.Cipher;

/**
 * 
 * This class implements the Sender portion of the Naor-Pinkas OT protocol.
 * 
 * <p>
 * 
 * The description of this protocol can be found in their 2001 paper "Efficient
 * oblivious transfer protocols" published in the Proceedings of the twelfth
 * annual ACM-SIAM symposium on Discrete algorithms.
 *
 * @author yhuang
 * @author nhusted 
 * @see Receiver
 * @see OTExtSender
 */

public class NPOTReceiver extends Receiver {
    private static Random rnd = new Random();

    private int msgBitLength;
    private BigInteger p, q, g, C;
    private BigInteger gr;

    private BigInteger[] gk, C_over_gk;
    private BigInteger[][] pk;

    private BigInteger[] keys;

    public NPOTReceiver(int numOfChoices, 
			    ObjectInputStream in, ObjectOutputStream out) throws Exception {
	super(numOfChoices, in, out);

	initialize();
    }

    public void execProtocol(BigInteger choices) throws Exception {
	super.execProtocol(choices);

	step1();
	step2();
    }

    private void initialize() throws Exception {
    	
    	// Constant 
		C  = (BigInteger) ois.readObject();
		
		// p is pLength-bit prime #
		p  = (BigInteger) ois.readObject();
		
		// q is qLength-bit prime #
		q  = (BigInteger) ois.readObject();
		
		// g is a generator in group Z^*_p
		g  = (BigInteger) ois.readObject();
		
		// g^r element of z^*_p;
		gr = (BigInteger) ois.readObject();
		
		// Size of msg
		msgBitLength = ois.readInt();
	
		gk = new BigInteger[numOfChoices];
		
		C_over_gk = new BigInteger[numOfChoices];
		
		keys = new BigInteger[numOfChoices];
		
		// For each N = numOfChoices we have in the 1-of-N OT protocol...
		for (int i = 0; i < numOfChoices; i++) {
			
			// choose random integer k in Z_q
			// also known as r in the paper
		    BigInteger k = (new BigInteger(q.bitLength(), rnd)).mod(q);
		    
		    // Set gk = PK_i = g^k mod p
		    // Also known as PK_\sigma
		    gk[i] = g.modPow(k, p);
		    
		    // C * PK_i^{-1} mod p
		    // Also known as PK_{\sigma-1}
		    C_over_gk[i] = C.multiply(gk[i].modInverse(p)).mod(p);
		    
		    // PK_i^r
		    // Also known as g^r^k for the ith index
		    keys[i] = gr.modPow(k, p);
		}
		
    }

    private void step1() throws Exception {
	pk = new BigInteger[numOfChoices][2];
	BigInteger[] pk0 = new BigInteger[numOfChoices];
	for (int i = 0; i < numOfChoices; i++) {
	    int sigma = choices.testBit(i) ? 1 : 0;
	    pk[i][sigma] = gk[i];
	    pk[i][1-sigma] = C_over_gk[i];

	    pk0[i] = pk[i][0];
	}

	oos.writeObject(pk0);
	oos.flush();
    }

    private void step2() throws Exception {
	BigInteger[][] msg = (BigInteger[][]) ois.readObject();

	data = new BigInteger[numOfChoices];
	for (int i = 0; i < numOfChoices; i++) {
	    int sigma = choices.testBit(i) ? 1 : 0;
	    data[i] = Cipher.decrypt(keys[i], msg[i][sigma], msgBitLength);
	}
    }
}
