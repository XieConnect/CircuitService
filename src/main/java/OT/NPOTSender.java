// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>
// Copyright (C) 2011 by Nathaniel Husted <nhusted@indiana.edu>

package OT;

import Cipher.Cipher;
import Utils.StopWatch;

import java.io.*;
import java.math.BigInteger;
import java.util.Random;

/**
 * This class implements the Sender portion of the Naor-Pinkas OT protocol.
 * <p/>
 * <p/>
 * <p/>
 * The description of this protocol can be found in their 2001 paper "Efficient
 * oblivious transfer protocols" published in the Proceedings of the twelfth
 * annual ACM-SIAM symposium on Discrete algorithms.
 *
 * @author yhuang
 * @author nhusted
 * @see Sender
 * @see OTExctReceiver
 */
public class NPOTSender extends Sender {

    /**
     * Random number generate
     */
    private static Random rnd = new Random();

    /**
     * Certainty for Prime generation
     */
    private static final int certainty = 80;

    /**
     * Bit length of q
     */
    private final static int qLength = 512; //512;

    /**
     * Bit length of p
     */
    private final static int pLength = 15360; //15360;

    /**
     * Variables as they relate to the notation in the Naor-Pinkas OT paper
     */
    private BigInteger p, q, g, C, r;
    private BigInteger Cr, gr;

    /**
     * Constructor
     *
     * @param numOfPairs
     * @param msgBitLength
     * @param in
     * @param out
     * @throws Exception
     */
    public NPOTSender(int numOfPairs, int msgBitLength, ObjectInputStream in,
                      ObjectOutputStream out) throws Exception {
        super(numOfPairs, msgBitLength, in, out);

        StopWatch.pointTimeStamp("right before NPOT public key generation");
        initialize();
        StopWatch.taskTimeStamp("NPOT public key generation");
    }

    /**
     * @param msgPairs
     * @throws Exception
     */
    public void execProtocol(BigInteger[][] msgPairs) throws Exception {
        super.execProtocol(msgPairs);

        step1();
    }

    /**
     * @throws Exception
     */
    private void initialize() throws Exception {
        File keyfile = new File("NPOTKey");
        if (keyfile.exists()) {

            // If the keyfile exists, read all our key related objects from it.

            FileInputStream fin = new FileInputStream(keyfile);
            ObjectInputStream fois = new ObjectInputStream(fin);


            // random C -- constant
            C = (BigInteger) fois.readObject();

            // prime of length pLength
            p = (BigInteger) fois.readObject();

            // prime of length qlength
            q = (BigInteger) fois.readObject();

            // generator in z^*_p
            g = (BigInteger) fois.readObject();

            // g^r
            gr = (BigInteger) fois.readObject();

            // random r -- constant
            r = (BigInteger) fois.readObject();

            fois.close();

            oos.writeObject(C);
            oos.writeObject(p);
            oos.writeObject(q);
            oos.writeObject(g);
            oos.writeObject(gr);
            oos.writeInt(msgBitLength);
            oos.flush();

            // C^r mod p
            Cr = C.modPow(r, p);
        } else {

            // Our key file doesn't exist, so we need to generate them

            BigInteger pdq;

            // qlength-bit random prime
            q = new BigInteger(qLength, certainty, rnd);

            do {
                // pdq is a random integer of length pLength-qLength bits
                pdq = new BigInteger(pLength - qLength, rnd);

                // Clear the first bit for our addition
                pdq = pdq.clearBit(0);

                // Question: Does PDQ have to be prime?

                // (p = q * pdq + 1) == pdq = q / (p - 1) && pdq = true
                p = q.multiply(pdq).add(BigInteger.ONE);

                // This means q is a factor of Z^*_p's order (\phi).

                // We must make sure that p is prime
            } while (!p.isProbablePrime(certainty));

            do {
                // Create a random number g
                g = new BigInteger(pLength - 1, rnd);

                // g must not be the inverse of pdq or q in mod p,
                // i.e. g^pdq mod p != 1 && g^q mod p != 1.

                // This somehow gives us a test that g is a generator in Z^*_q
                // but I'm not sure how it work.

            } while ((g.modPow(pdq, p)).equals(BigInteger.ONE)
                    || (g.modPow(q, p)).equals(BigInteger.ONE));


            // r is a random number in group Z_q
            r = (new BigInteger(qLength, rnd)).mod(q);

            // g^r is a random element in z^*_q
            gr = g.modPow(r, p);

            // C is a random element in Z_q
            C = (new BigInteger(qLength, rnd)).mod(q);

            // Sne dthe key values to the Receiver
            oos.writeObject(C);
            oos.writeObject(p);
            oos.writeObject(q);
            oos.writeObject(g);
            oos.writeObject(gr);
            oos.writeInt(msgBitLength);
            oos.flush();

            // C is a random element C^r belonging to Z^*_p
            Cr = C.modPow(r, p);


            // Now write the key values to a file for later use

            FileOutputStream fout = new FileOutputStream(keyfile);
            ObjectOutputStream foos = new ObjectOutputStream(fout);

            foos.writeObject(C);
            foos.writeObject(p);
            foos.writeObject(q);
            foos.writeObject(g);
            foos.writeObject(gr);
            foos.writeObject(r);

            foos.flush();
            foos.close();
        }
    }

    /**
     * @throws Exception
     */
    private void step1() throws Exception {

        // Get PK_0
        BigInteger[] pk0 = (BigInteger[]) ois.readObject();

        // Create PK_1 with room for numOfPairs messages
        BigInteger[] pk1 = new BigInteger[numOfPairs];

        // Create array of messages numOfPairs long. Each message has two slots.
        BigInteger[][] msg = new BigInteger[numOfPairs][2];

        for (int i = 0; i < numOfPairs; i++) {
            // Calculat PK0_i = PK0_i ^ r mod p
            pk0[i] = pk0[i].modPow(r, p);

            // Calculate PK1_i = C^r * pk0_i * (pk0_i^-1 mod p) mod p.
            pk1[i] = Cr.multiply(pk0[i].modInverse(p)).mod(p);

            // m_i^0 = H(pk0_i, m_i^0, msgLength)
            msg[i][0] = Cipher.encrypt(pk0[i], msgPairs[i][0], msgBitLength);

            // m_i^1 = H(pk1_i, m_^1, msgLength)
            msg[i][1] = Cipher.encrypt(pk1[i], msgPairs[i][1], msgBitLength);
        }

        // Write the message array out
        oos.writeObject(msg);

        // Flush the buffer to send
        oos.flush();
    }
}