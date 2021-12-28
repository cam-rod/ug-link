package me.camrod.ug_link;

import java.io.*;
import java.util.Arrays;
import java.util.Map;

public class UGLink {

    private static String username;
    private static String password;
    public static int selectedServer;
    public ClassLoader classLoader;

    public static void main(String[] args) {
        Console credLoader = System.console();

        // TODO: check for valid inputs, loop back if server fails it
        getCredentials(credLoader);
        ProcessBuilder unloadedBuilder = getUnloadedServers();

        try {
            Process unloadedProcess =  unloadedBuilder.start();
            String serverLoad = selectServer(unloadedProcess);
            System.out.println(serverLoad); // TODO: remove when implemented
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    public static String selectServer(Process unloadedProcess) throws IOException {
        String outLine;
        BufferedReader unloadedOutput = new BufferedReader(new InputStreamReader(unloadedProcess.getInputStream()));
        while ((outLine = unloadedOutput.readLine()) != null) {
            System.out.println(outLine);
        }
        return "\n";
    }
}
