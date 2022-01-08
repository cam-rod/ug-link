package me.camrod.ug_link;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

// Class for connecting to EECG servers
public class EECG {
    public enum plinkError {GOOD, SERVER_NOT_AVAILABLE, INVALID_CREDENTIALS};

    private String username;
    private String password;
    private Runtime rt;
    private Console credLoader;
    public plinkError status;
    public TreeMap<Double, String> sortedServers;

    EECG(Console credLoader, Runtime rt) {
        this.rt = rt;
        this.credLoader = credLoader;
        sortedServers = new TreeMap<>();
        loadCredentials(credLoader);
    }

    class plinkEECGConsumer extends Thread {
        private final BufferedReader reader;
        private final ArrayList<String> output;

        plinkEECGConsumer(Process plinkProcess, boolean errorReader, ArrayList<String> output) {
            this.output = output;
            if (errorReader) {
                reader = new BufferedReader(new InputStreamReader(plinkProcess.getErrorStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(plinkProcess.getInputStream()));
            }
        }

        @Override
        public void run() {
            String outLine;
            try {
                while ((outLine = reader.readLine()) != null) {
                    output.add(outLine);
                }
            } catch (IOException e) {
                 e.printStackTrace();
            }
        }
    }

    private void loadCredentials(Console credLoader) {
        username = credLoader.readLine("Enter your EECG username: ");
        password = new String(credLoader.readPassword("Enter your EECG password: "));
    }

    public void connect() {
        int attempts = 0;

        System.out.println("Loading EECG servers...");
        for (; attempts < 3; attempts++) {
            ArrayList<String> output = runPlinkCmd(genPlinkCmd("ug251","exit", true, false));
            switch (status) {
                case GOOD -> {
                    if (output != null) {
                        UGLink.sortServers(output, this.sortedServers);
                    } else {
                        System.out.println("ERROR: no output received from server.\n");
                        return;
                    }
                }
                case INVALID_CREDENTIALS -> {
                    System.out.print("ERROR: authentication failed. ");
                    if (attempts < 3) {
                        System.out.println("Please reenter your login details.\n");
                        loadCredentials(credLoader);
                    }
                }
                case SERVER_NOT_AVAILABLE -> {
                    System.out.println("ERROR: main EECG server (ug251) not available. Please try again later.\n");
                    return;
                }
            }
        }
        if (attempts == 3) {
            System.out.println("\nToo many failed login attempts. Ensure you have the correct credentials.\n");
            return;
        }

        // Launch user SSH session
        attempts = 0;
        for (Map.Entry<Double, String> server : sortedServers.entrySet()) {
            System.out.println("Connecting to " + server.getValue() + "...");
            runPlinkCmd(genPlinkCmd(server.getValue(), "uname -r", true, false));
            switch (status) {
                case GOOD -> {
                    System.out.println("Authenticated! Launching session.");
                    String launchStr = "putty " + username + "@" + server + ".eecg.utoronto.ca -pw " + password;
                    try {
                        rt.exec(launchStr).waitFor();
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    } finally {
                        System.out.println("Session closed. Goodbye!\n");
                        return;
                    }
                }
                case SERVER_NOT_AVAILABLE -> {
                    System.out.println("WARNING: Server " + server.getValue() + " not available. Switching to next server.");
                    attempts++;
                    if (attempts >= 5) {
                        System.out.println("ERROR: failed to connect to any of top 5 servers.");
                        return;
                    }
                }
                case INVALID_CREDENTIALS -> {
                    System.out.println("ERROR: authentication failed. " +
                            "Please verify your credentials and try again later.");
                    return;
                }
            }
        }
    }

    /**
     * Generates commands used to form SSH connections over plink
     *
     * @param server the EECG server to connect to
     * @param command the command to run, if non-null
     * @param echoYes echo Yes to plink to approve server fingerprint
     * @param batch whether the session should run in batch mode
     * @return the completed base command
     */
    private String genPlinkCmd(String server, String command, boolean echoYes, boolean batch) {
        String plinkCmd = "plink " + username + "@" + server + ".eecg.utoronto.ca ";
        if (echoYes) { plinkCmd = "echo yes | " + plinkCmd; }
        if (batch) {
            plinkCmd = plinkCmd.concat("-batch ");
        } else {
            plinkCmd = "cmd /c " + plinkCmd;
        }
        plinkCmd = plinkCmd.concat("-pw " + password);
        if(command != null) { plinkCmd = plinkCmd.concat(" " + command); }
        return plinkCmd;
    }

    private ArrayList<String> runPlinkCmd(String command) {
        ArrayList<String> output = new ArrayList<>();
        ArrayList<String> error = new ArrayList<>();
        Process p = null;
        try {
            p = rt.exec(command);
            plinkEECGConsumer eecgOutput = new plinkEECGConsumer(p, false, output);
            plinkEECGConsumer eecgError = new plinkEECGConsumer(p, false, error);
            eecgOutput.start();
            eecgError.start();
            Thread.sleep(3000);
            eecgOutput.join();
            eecgOutput.join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        if (error.get(1).startsWith("Host does not exist")) {
            status = plinkError.SERVER_NOT_AVAILABLE;
            return null;
        } else if (error.get(1).startsWith("Access denied") || error.get(11).startsWith("Access denied")) {
            status = plinkError.INVALID_CREDENTIALS;
            return null;
        } else {
            status = plinkError.GOOD;
            return output;
        }
    }
}