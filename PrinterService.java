import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
//needed to write the log file
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
// Encryption
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

// Reading JSON
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PrinterService extends UnicastRemoteObject implements PrinterServiceInterface {

    private HashMap<String, Printer> printers; // list with all the printers
    private HashMap<String, String> configs; // List with all parameters and values
    private Map<String, List<byte[]>> database = new HashMap<String, List<byte[]>>(); // Username and Hashed p/w
                                                                                      // database
    private String[] printersNames = { "Printer1", "Printer2", "Printer3" };
    private List<User> loggedClientList = new ArrayList<>();
    private clientCallBackInterface clientCallBack;
    private AuthenticationServiceInterface session;
    private JSONObject jsonAccessList;
    private User authenticatedUser;

    private int sessionToken;

    // Create the log file
    String fileName = "log.txt";

    public PrinterService() throws RemoteException, NoSuchAlgorithmException {
        super();
        printers = new HashMap<String, Printer>();
        configs = new HashMap<String, String>();

        // Uncomment the correct user/pw database depending on what UserPermissions file is being used!

        // Original scenario
        loggedClientList.add(new User("alice", "alice123"));
        loggedClientList.add(new User("bob", "bob123"));
        loggedClientList.add(new User("cecilia", "cecilia123"));
        loggedClientList.add(new User("david", "david123"));
        loggedClientList.add(new User("erica", "erica123"));
        loggedClientList.add(new User("fred", "fred123"));
        loggedClientList.add(new User("george", "george123"));

        // Scenario 2 after company changes are made
        /* loggedClientList.add(new User("alice", "alice123"));
        loggedClientList.add(new User("cecilia", "cecilia123"));
        loggedClientList.add(new User("david", "david123"));
        loggedClientList.add(new User("erica", "erica123"));
        loggedClientList.add(new User("fred", "fred123"));
        loggedClientList.add(new User("george", "george123"));
        loggedClientList.add(new User("henry", "henry123"));
        loggedClientList.add(new User("ida", "ida123")); */

        // initialize printers
        initPrinters(printersNames);

        for (User loggedClient : loggedClientList) {
            PasswordEncryptStore(loggedClient, loggedClient.getPassword(), generateSalt());
        }

    }

    public void setClientCallBack(clientCallBackInterface c) throws RemoteException {
        this.clientCallBack = c;
    }

    public int getSessionId() throws RemoteException {
        session = new AuthenticationService();
        int sessionID = session.getToken();
        clientCallBack.printOnClient("The session ID token is: " + sessionID);
        logEntry("Session ID assigned");
        return sessionID;
    }

    public void PasswordEncryptStore(User user, String password, byte[] salted) throws NoSuchAlgorithmException {
        // Encyrpt the password.
        byte[] hashPassword = hashPassword(password, salted);

        // Create a list containing the two byte[] hashed pw and salt.
        List<byte[]> pwsaltList = new ArrayList<>();
        pwsaltList.add(hashPassword);
        pwsaltList.add(salted);

        // Add the username and list containing encrypted pw and salt to the HashMap
        // (our simulation of a database)
        database.put(user.getUsername(), pwsaltList);
    }

    public static byte[] generateSalt() throws NoSuchAlgorithmException {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    private static byte[] hashPassword(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
        messageDigest.update(salt); // Link salt to the hash
        byte[] hasedBytes = messageDigest.digest(password.getBytes());
        messageDigest.reset(); // Reset digest to ensure no values are stored.

        return hasedBytes;
    }

    public void initPrinters(String[] printerList) throws RemoteException {
        for (String p : printerList) {
            printers.put(p, new Printer(p));
        }
    }

    public String[] getPrintersList() throws RemoteException {
        return printersNames;
    }

    private Printer getPrinter(String printer) {
        Printer foundPrinter = printers.get(printer);
        if (foundPrinter == null) {
            return null;
        }
        return foundPrinter;
    }

    public void print(int sessionIDClient, String filename, String printer) throws RemoteException { /// prints file
                                                                                                     /// filename on the
        /// specified printer
        if (sessionIDClient == session.getToken()) {

            if (hasAccess("print")) {
                Printer foundPrinter = getPrinter(printer);
                if (foundPrinter != null) {
                    foundPrinter.addToQueue(filename);
                    clientCallBack.printOnClient("Printing " + filename + " on printer name: " + printer);
                    logEntry("print function invoked - session ID: " + sessionIDClient);
                }
            } else {
                logEntry(authenticatedUser.getUsername() + " print access denied");
                clientCallBack.printOnClient("You do not have permissions to use: Print");
            }
        }
    }

    public void queue(int sessionIDClient, String printer) throws RemoteException { // lists the print queue for a given
                                                                                    // printer on the
        // user's display in lines of the form <job number> <file
        // name>
        if (sessionIDClient == session.getToken()) {
            if (hasAccess("queue")) {
                Printer foundPrinter = getPrinter(printer);
                if (foundPrinter != null) {
                    List<String> queueList = foundPrinter.queue();
                    // printing the queue list
                    for (String queue : queueList) {
                        clientCallBack.printOnClient(queue);
                    }

                } else {
                    clientCallBack.printOnClient("queue is empty");
                }
                logEntry("queue function invoke - session ID: " + sessionIDClient);
            } else {
                logEntry(authenticatedUser.getUsername() + " queue access denied");
                clientCallBack.printOnClient("You do not have permissions to use: Queue");
            }
        }
    }

    public void topQueue(int sessionIDClient, String printer, int job) throws RemoteException { // moves job to the top
                                                                                                // of the queue

        if (sessionIDClient == session.getToken()) {
            if (hasAccess("topQueue")) {
                Printer foundPrinter = getPrinter(printer);
                if (foundPrinter != null) {
                    foundPrinter.topQueue(job);
                    clientCallBack.printOnClient("moving job number: " + job + " on top");
                }
                logEntry("Top queue function invoke - session ID: " + sessionIDClient);
            } else {
                logEntry(authenticatedUser.getUsername() + " topQueue access denied");
                clientCallBack.printOnClient("You do not have permissions to use: topQueue");
            }
        }
    }

    public void start(int sessionIDClient) throws RemoteException { // starts the print server
        if (sessionIDClient == session.getToken()) {
            if (hasAccess("start")) {
                logEntry("--Print server started.");
                clientCallBack.printOnClient("starting the printer server");
            } else {
                logEntry(authenticatedUser.getUsername() + " start access denied");
                clientCallBack.printOnClient("You do not have permissions to use: start");
            }
        }
    }

    public void stop(int sessionIDClient) throws RemoteException { // stops the print server
        if (sessionIDClient == session.getToken()) {
            if (hasAccess("stop")) {
                logEntry("--Print server stopped.");
                clientCallBack.printOnClient("stopping the printer server");
            } else {
                logEntry(authenticatedUser.getUsername() + " stop access denied");
                clientCallBack.printOnClient("You do not have permissions to use: stop");
            }
        }
    }

    public void restart(int sessionIDClient) throws RemoteException { // stops the print server, clears the print queue
                                                                      // and starts the
        if (sessionIDClient == session.getToken()) {
            if (hasAccess("restart")) { // print server again
                stop(sessionIDClient);
                for (Printer p : printers.values()) {
                    p.clearPrinterQueue();
                }
                start(sessionIDClient);

                logEntry("--Print server restarted.");
            } else {
                logEntry(authenticatedUser.getUsername() + " restart access denied");
                clientCallBack.printOnClient("You do not have permissions to use: restart");
            }
        }
    }

    public void status(int sessionIDClient, String printer) throws RemoteException { // prints status of printer on the
                                                                                     // user's display
        if (sessionIDClient == session.getToken()) {
            if (hasAccess("status")) {
                Printer foundPrinter = getPrinter(printer);
                if (foundPrinter.getStatus()) {
                    clientCallBack.printOnClient("Printer is active");
                } else {
                    clientCallBack.printOnClient("Printer is not active");
                }
                logEntry("status function invoked - session ID: " + sessionIDClient);
            }
            else {
                logEntry(authenticatedUser.getUsername() + " Status access denied");
                clientCallBack.printOnClient("You do not have permissions to use Status");
            }
        }

    }

    public void readConfig(int sessionIDClient, String parameter) throws RemoteException { // prints the value of the
                                                                                           // parameter on the print
        // server to the user's display
        if (sessionIDClient == session.getToken()) {
            if (hasAccess("readConfig")) {
                clientCallBack.printOnClient("reading configuration: " + configs.get(parameter));
                logEntry("readConfig function invoked, printing on display - session ID: " + sessionIDClient);
            } else {
                logEntry(authenticatedUser.getUsername() + " Read Config access denied");
                clientCallBack.printOnClient("You do not have permissions to use Read Config");
            }
        }
    }

    public void setConfig(int sessionIDClient, String parameter, String value) throws RemoteException { // sets the
                                                                                                        // parameter on
                                                                                                        // the print
        // server to value
        if (sessionIDClient == session.getToken()) {
            if (hasAccess("setConfig")) {
                configs.put(parameter, value);

                clientCallBack.printOnClient("setting configuration" + parameter + " value: " + value);
                logEntry("setConfig function invoked - session ID: " + sessionIDClient);
            } else {
                logEntry(authenticatedUser.getUsername() + " Set Config access denied");
                clientCallBack.printOnClient("You do not have permissions to use Set Config");
            }
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////
    /////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void logEntry(String text) { // Writes the log file
        try {
            FileWriter fileWriter = new FileWriter(fileName, true); // Create a FileWriter with the file name
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter); // Create a BufferedWriter to efficiently
                                                                            // write to the file
            bufferedWriter.write(text);
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String toHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    public int authenticate(User user) throws RemoteException, NoSuchAlgorithmException {

        if (database.containsKey(user.getUsername())) {
            List<byte[]> pwsaltList = database.get(user.getUsername());

            byte[] password = pwsaltList.get(0);
            byte[] salt = pwsaltList.get(1);
            byte[] passwordIn = hashPassword(user.getPassword(), salt);

            // Check user entered password against out hashed and salted database
            if (toHex(password).equals(toHex(passwordIn))) {
                authenticatedUser = user;
                return getSessionId();

            }
        }

        return 0;
    }

    public Object loadACL(String filename) throws RemoteException, FileNotFoundException, IOException, ParseException {
        // JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader(filename)) {
            // Read JSON file
            JSONObject obj = (JSONObject) jsonParser.parse(reader);

            // JSONArray employeeList = (JSONArray) obj;
            jsonAccessList = obj;
            return obj;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Boolean hasAccess(String function) {
        if (authenticatedUser != null) {
            JSONArray permissions = (JSONArray) jsonAccessList.get(authenticatedUser.getUsername());
            for (int i = 0; i < permissions.size(); i++) {
                if (permissions.get(i).equals(function)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getSessionToken(int token) {
        sessionToken = token;
        return sessionToken;
    }

    public void printWithCallback(String message) throws RemoteException {
        clientCallBack.printOnClient(message);
    }
}
