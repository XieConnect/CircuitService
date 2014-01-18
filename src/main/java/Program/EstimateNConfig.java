package Program;

import java.math.BigInteger;

public class EstimateNConfig {
    // how many loops to run (max of n as in 2^n)
    public static int maxN = 80;
    // max value bit size
    public static int nBits = 128;
    // socket port for OT
    public static int socketPort = 23456;
    // ln 2 * 2^N
    public static BigInteger Nln2 = new BigInteger("837963523372001241319907");
}