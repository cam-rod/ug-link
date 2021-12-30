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
            System.out.println("Loading EECG servers...");
            rt.exec(genSSHCmd("ug251","exit", "echo yes", false));
            Process unloadedProcess = rt.exec(genSSHCmd("ug251", "ruptime -rl", null, true));
            sortServers(unloadedProcess);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Launch SSH session on least loaded server
        try {
            System.out.println("Connecting to " + sortedServers.firstEntry().getValue() + "...");
            Process a = rt.exec(genSSHCmd("ug168","uname -r", "echo yes", false));
            System.out.println("Authenticated!");
            String launchStr = "putty " + username + "@" + sortedServers.firstEntry().getValue() + ".eecg.utoronto.ca -pw " + password;
            rt.exec(launchStr).waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates commands used to form SSH connections
     *
     * @param server the EECG server to connect to
     * @param command the command to run, if non-null
     * @param pipe output to pipe into the plink program, if non-null
     * @param batch whether the session should run in batch mode
     * @return the completed base command
     */
    public static String genSSHCmd(String server, String command, String pipe, boolean batch) {
        String sshCmd = "plink " + username + "@" + server + ".eecg.utoronto.ca ";
        if (pipe != null) { sshCmd = pipe + " | " + sshCmd; }
        if (batch) {
            sshCmd = sshCmd.concat("-batch ");
        } else {
            sshCmd = "cmd /c " + sshCmd;
        }
        sshCmd = sshCmd.concat("-pw " + password);
        if(command != null) { sshCmd = sshCmd.concat(" " + command); }
        return sshCmd;
    }

    public static void getCredentials(Console credLoader){
        username = credLoader.readLine("Enter your EECG username: ");
        password = new String(credLoader.readPassword("Enter your EECG password: "));
    }

    public static void sortServers(Process unloadedProcess) throws IOException {
        String outLine;
        BufferedReader unloadedOutput = new BufferedReader(new InputStreamReader(unloadedProcess.getInputStream()));
        while ((outLine = unloadedOutput.readLine()) != null) {
            String serverName = outLine.substring(0, 5).trim();
            String[] loadAvgStr = outLine.substring(48).split(",\\s+");

            double[] allAvgs = new double[3];
            for(int i = 0; i < 3; i++) {
                allAvgs[i] = Double.parseDouble(loadAvgStr[i]);
            }
            double loadAvg = (0.5 * allAvgs[0]) + (0.3 * allAvgs[1]) + (0.2 * allAvgs[2]);
            sortedServers.put(loadAvg, serverName);
        }
    }
}
