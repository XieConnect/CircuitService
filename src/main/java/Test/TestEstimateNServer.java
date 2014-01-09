/**
 * Test EstimateN circuit
 */
package Test;

import Program.EstimateNServer;
import Utils.StopWatch;
import jargs.gnu.CmdLineParser;

import java.math.BigInteger;
import java.util.Random;

class TestEstimateNServer {
    // input value to the circuit (random)
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
     * Generate random value from range [0,  2^n - 1]
     */
    private static void generateData() throws Exception {
        bits = new BigInteger(4, rnd).add(BigInteger.ONE);
        //bits = new BigInteger("4");
    }

    public static void main(String[] args) throws Exception {

        StopWatch.pointTimeStamp("Starting program");
        process_cmdline_args(args);

        generateData();

        EstimateNServer hammingserver = new EstimateNServer(bits, n);
        hammingserver.run();
    }
}