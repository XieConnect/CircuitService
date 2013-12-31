// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Test;

import Program.HammingDistanceServer;
import Utils.StopWatch;
import jargs.gnu.CmdLineParser;

import java.math.BigInteger;
import java.util.Random;

class TestHammingServer {
    static BigInteger bits;
    static int n;

    static Random rnd = new Random();

    private static void printUsage() {
        System.out.println("Usage: java TestHammingServer [{-n, --bit-length} length]");
    }

    private static void process_cmdline_args(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option optionBitLength = parser.addIntegerOption('n', "bit-length");

        try {
            parser.parse(args);
        } catch (CmdLineParser.OptionException e) {
            System.err.println(e.getMessage());
            printUsage();
            System.exit(2);
        }

        n = ((Integer) parser.getOptionValue(optionBitLength, new Integer(100))).intValue();
    }

    /**
     * Generate random input value (at most of bit length n)
     */
    private static void generateData() throws Exception {
        bits = new BigInteger(n, rnd);
    }

    public static void main(String[] args) throws Exception {

        StopWatch.pointTimeStamp("Starting program");
        process_cmdline_args(args);

        generateData();

        HammingDistanceServer hammingserver = new HammingDistanceServer(bits, n);
        hammingserver.run();
    }
}