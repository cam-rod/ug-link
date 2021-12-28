package me.camrod.ug_link;

import java.io.*;
import java.util.Arrays;
import java.util.Map;

public class UGLink {

    private static String username;
    private static String password;
    public static int selectedServer;

    public static void main(String[] args) {
        Console credLoader = System.console();

        // TODO: check for valid inputs, loop back if server fails it
        getCredentials(credLoader);
        ProcessBuilder unloadedBuilder = getUnloadedServers();

        try {
            Process unloadedProcess =  unloadedBuilder.start();
            String serverLoad = selectServer(unloadedProcess);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void getCredentials(Console credLoader){
        System.out.println("Enter your EECG username: ");
        username = credLoader.readLine();
        System.out.println("Enter your EECG password:");
        password = Arrays.toString(credLoader.readPassword());
    }

    // Check for unloaded servers on ug251
    public static ProcessBuilder getUnloadedServers() {
        ProcessBuilder unloadedBuilder = new ProcessBuilder("pwsh", ".\\Find-UnloadedServer.ps1");
        unloadedBuilder.directory(new File("..\\..\\..\\..\\resources"));
        Map<String, String> envVars = unloadedBuilder.environment();
        envVars.put("UG_LINK_USER", username);
        envVars.put("UG_LINK_PASS", password);
        return unloadedBuilder;
    }

    public static String selectServer(Process unloadedProcess) {
        BufferedReader unloadedOutput = new BufferedReader(new InputStreamReader(unloadedProcess.getInputStream()));
    }
}
