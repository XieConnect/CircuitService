// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package OT;

import java.util.*;
import java.math.*;
import java.io.*;
import Cipher.Cipher;
import Utils.*;

/**
 * 
 * OTExtSenter implements the sender portion of the OT Extension protocol
 * created by Ishai, Kilian, Nissim, and Petrank.
 * <p>
 * Details of the protocol can be found in "Extending Oblivious Transfers
 * Efficiently" by Ishai, Kilian, Nissim, and Petrank. 
 * 
 * @author yhuang
 * @author nhusted
 *
 */
public class OTExtSender extends Sender {
	
	/**
	 * SecurityParameter is a static class acting an enumerator
	 * for the two security parameters required by the Extended
	 * OT protocol. 
	 *  
	 * @author yhuang
	 * @author nhusted
	 *
	 * @see OTExtSender
	 */
    static class SecurityParameter {
    	
    	/** Number of Columns in related BitMatrix */
		public static final int k1 = 80;    
		
		/** ??? */
		public static final int k2 = 80;
    }

    private static Random rnd = new Random();
    
    /** ??? */
    private Receiver rcver;
    
    /** "vector" of random bits */
    private BigInteger s;
    
    /** List of keys */
    private BigInteger[] keys;

    /**
     * 
     * Constructor calls the super class and then runs the initialize method.
     * 
     * @param numOfPairs
     * @param msgBitLength
     * @param in
     * @param out
     * @throws Exception
     * 
     * @see Sender
     */
    public OTExtSender(int numOfPairs, int msgBitLength,
		       ObjectInputStream in, ObjectOutputStream out) throws Exception {
		super(numOfPairs, msgBitLength, in, out);
		
		initialize();
    }

    /**
     * 
     * @param msgPairs
     * @throws Exception
     */
    public void execProtocol(BigInteger[][] msgPairs) throws Exception {
		BigInteger[][] cphPairs = (BigInteger[][]) ois.readObject();
		int bytelength;
	
		BitMatrix Q = new BitMatrix(numOfPairs, SecurityParameter.k1);
	
		for (int i = 0; i < SecurityParameter.k1; i++) {
		    if (s.testBit(i))
		    	Q.data[i] = Cipher.decrypt(keys[i], cphPairs[i][1], numOfPairs);
		    else
		    	Q.data[i] = Cipher.decrypt(keys[i], cphPairs[i][0], numOfPairs);
		}
	
		BitMatrix tQ = Q.transpose();
		
		BigInteger[][] y = new BigInteger[numOfPairs][2];
		for (int i = 0; i < numOfPairs; i++) {
		    y[i][0] = Cipher.encrypt(i, tQ.data[i],        msgPairs[i][0], msgBitLength);
		    y[i][1] = Cipher.encrypt(i, tQ.data[i].xor(s), msgPairs[i][1], msgBitLength);
		}
	
		bytelength = (msgBitLength-1)/8 + 1;
		for (int i = 0; i < numOfPairs; i++) {
		    Utils.writeBigInteger(y[i][0], bytelength, oos);
		    Utils.writeBigInteger(y[i][1], bytelength, oos);
		}
		oos.flush();
    }

    /**
     * 
     * @throws Exception
     */
    private void initialize() throws Exception {
    	// Send security parameters 1 and 2 and the message length to 
    	// the OTExtReceiver
		oos.writeInt(SecurityParameter.k1);
		oos.writeInt(SecurityParameter.k2);
		oos.writeInt(msgBitLength);
		oos.flush();
	
		// Create a NaorPinkas Receiver with 2-of-k1 OT. 
		rcver = new NPOTReceiver(SecurityParameter.k1, ois, oos);
	
		// s is a random BitString of size k1.
		s = new BigInteger(SecurityParameter.k1, rnd);
	
		// exec protocol with bit string s.
		rcver.execProtocol(s);
		
		
		keys = rcver.getData();
    }
}