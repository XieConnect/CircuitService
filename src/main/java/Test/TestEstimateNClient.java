package Test;

import Program.EstimateNClient;
import Program.ProgClient;
import Program.Program;
import Utils.StopWatch;
import jargs.gnu.CmdLineParser;

import java.math.BigInteger;
import java.util.Random;

class TestEstimateNClient {
    static BigInteger bits;
    static int n;

    static Random rnd = new Random();

    private static void printUsage() {
        System.out.println("Usage: java TestHammingClient [{-n, --bit-length} length] [{-s, --server} servername] [{-r, --iteration} r]");
    }

    private static void process_cmdline_args(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option optionServerIPname = parser.addStringOption('s', "server");
        CmdLineParser.Option optionBitLength = parser.addIntegerOption('n', "bit-Length");
        CmdLineParser.Option optionIterCount = parser.addIntegerOption('r', "iteration");

        try {
            parser.parse(args);
        } catch (CmdLineParser.OptionException e) {
            System.err.println(e.getMessage());
            printUsage();
            System.exit(2);
        }

        n = ((Integer) parser.getOptionValue(optionBitLength, new Integer(100))).intValue();
        ProgClient.serverIPname = (String) parser.getOptionValue(optionServerIPname, new String("localhost"));
        Program.iterCount = ((Integer) parser.getOptionValue(optionIterCount, new Integer(10))).intValue();
    }

    private static void generateData() throws Exception {
        bits = new BigInteger(n - 1, rnd);
    }

    public static void main(String[] args) throws Exception {
        StopWatch.pointTimeStamp("Starting program");
        process_cmdline_args(args);

        generateData();

        EstimateNClient hammingclient = new EstimateNClient(bits, n);
        hammingclient.run();
    }
}