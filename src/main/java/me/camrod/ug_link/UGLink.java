package me.camrod.ug_link;

import java.io.*;
import java.util.TreeMap;

public class UGLink {

    private static String username;
    private static String password;
    public static TreeMap<Double, String> sortedServers = new TreeMap<>();

    public static void main(String[] args) {
        /*TODO
        *  - Add support for ECF machines
        *  - Split ECF and EECG machines into separate classes or organize as needed
        */
        Console credLoader = System.console();
        Runtime rt = Runtime.getRuntime();

        // TODO: check for valid inputs, loop back if server fails it
        getCredentials(credLoader);
        try {
            // Authenticate fingerprint, then retrieve servers
            rt.exec("echo yes | " + genSSHCmd("ug251", "exit", false) + " exit");
            Process unloadedProcess = rt.exec(genSSHCmd("ug251", "ruptime -rl", true));
            sortServers(unloadedProcess);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Launch SSH session on least loaded server
        //Process serverConnect = Runtime.getRuntime().exec(
        //System.out.println("pwsh -NoExit -Command ssh " + username + "@" + sortedServers.firstEntry().getValue() + ".eecg.utoronto.ca");
    }

    /**
     * Generates commands used to form SSH connections
     *
     * @param server the EECG server to connect to
     * @param command the command to run
     * @param batch whether the session should run in batch mode
     * @return the completed base command
     */
    public static String genSSHCmd(String server, String command, boolean batch) {
        if (batch) {
            return "plink " + username + "@" + server + ".eecg.utoronto.ca -batch -pw " + password + " " + command;
        } else {
            return "cmd /c plink " + username + "@" + server + ".eecg.utoronto.ca -pw " + password;
        }
    }

    public static void getCredentials(Console credLoader){
        username = credLoader.readLine("Enter your EECG username: ");
        password = new String(credLoader.readPassword("Enter your EECG password: "));
    }

    public static void sortServers(Process unloadedProcess) throws IOException {
        String outLine;
        BufferedReader unloadedOutput = new BufferedReader(new InputStreamReader(unloadedProcess.getInputStream()));
        while ((outLine = unloadedOutput.readLine()) != null) {
            System.out.println(outLine.length());
            String serverName = outLine.substring(0, 5).trim();
            String[] loadAvgStr = outLine.substring(48).split(",\\s+");

            double[] allAvgs = new double[3];
            for(int i = 0; i < 3; i++) {
                allAvgs[i] = Double.parseDouble(loadAvgStr[i]);
            }
            double loadAvg = (0.5 * allAvgs[0]) + (0.3 * allAvgs[1]) + (0.2 * allAvgs[2]);
            sortedServers.put(loadAvg, serverName);
        }
        System.out.println(sortedServers);
    }
}
