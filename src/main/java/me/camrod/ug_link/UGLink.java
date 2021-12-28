package me.camrod.ug_link;

import java.io.*;
import java.util.Map;
import java.util.TreeMap;

public class UGLink {

    private static String username;
    private static String password;
    public static Map<Double, String> sortedServers = new TreeMap<>();

    public static void main(String[] args) {
        /*TODO
        *  - Add support for ECF machines
        *  - Split ECF and EECG machines into separate classes or organize as needed
        */
        Console credLoader = System.console();

        // TODO: check for valid inputs, loop back if server fails it
        getCredentials(credLoader);
        ProcessBuilder unloadedBuilder = getUnloadedServers();

        try {
            Process unloadedProcess =  unloadedBuilder.start();
            sortServers(unloadedProcess);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Launch SSH session on least loaded server
        //Process serverConnect = Runtime.getRuntime().exec(
        System.out.println("pwsh -NoExit -Command ssh " + username + "@" + sortedServers.entrySet().iterator().next().getValue() + ".eecg.utoronto.ca");
    }

    public static void getCredentials(Console credLoader){
        username = credLoader.readLine("Enter your EECG username: ");
        password = new String(credLoader.readPassword("Enter your EECG password: "));
    }

    // Check for unloaded servers on ug251
    public static ProcessBuilder getUnloadedServers() {
        ProcessBuilder unloadedBuilder = new ProcessBuilder("pwsh", "Find-UnloadedServer.ps1");
        Map<String, String> envVars = unloadedBuilder.environment();
        envVars.put("UG_LINK_USER", username);
        envVars.put("UG_LINK_PASS", password);
        return unloadedBuilder;
    }

    public static void sortServers(Process unloadedProcess) throws IOException {
        String outLine;
        BufferedReader unloadedOutput = new BufferedReader(new InputStreamReader(unloadedProcess.getInputStream()));
        while ((outLine = unloadedOutput.readLine()) != null && outLine.length() == 64) {
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
