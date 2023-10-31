import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
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


public class PrinterService extends UnicastRemoteObject implements PrinterServiceInterface {

    private HashMap<String, Printer> printers; // list with all the printers
    private HashMap<String, String> configs; // List with all parameters and values
    private Map<String, List<byte[]>> database = new HashMap<String, List<byte[]>>(); // Username and Hashed p/w database
    
    private List<User> loggedClientList = new ArrayList<>();
    
    private int sessionToken=0;

    // Create the log file
    String fileName = "log.txt"; // TODO: Add logs for remaining commands

    public PrinterService() throws RemoteException, NoSuchAlgorithmException {
        super();
        printers = new HashMap<String, Printer>();
        configs = new HashMap<String, String>();

        
        loggedClientList.add(new User("client1", "password1"));
        loggedClientList.add(new User("client2", "passowrd2"));
        
        for (User loggedClient : loggedClientList) {
            PasswordEncryptStore(loggedClient, loggedClient.getPassword(), generateSalt());
        }
        
    }

    public void PasswordEncryptStore(User user, String password, byte[] salted) throws NoSuchAlgorithmException {
        // Encyrpt the password.
        byte[] hashPassword = hashPassword(password, salted);

        //Create a list containing the two byte[] hashed pw and salt.
        List<byte[]> pwsaltList = new ArrayList<>();
        pwsaltList.add(hashPassword);
        pwsaltList.add(salted);

        // Add the username and list containing encrypted pw and salt to the HashMap (our simulation of a database)
        database.put(user.getUsername(), pwsaltList);
    } 

    public static byte[] generateSalt() throws NoSuchAlgorithmException{
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

    private Printer getPrinter(String printer) {
        Printer foundPrinter = printers.get(printer);
        if (foundPrinter == null) {
            return null;
        }
        return foundPrinter;
    }

    public String print(String filename, String printer) throws RemoteException { /// prints file filename on the
                                                                                /// specified printer
        Printer foundPrinter = getPrinter(printer);
        if (foundPrinter != null) {
            foundPrinter.addToQueue(filename);
        }
        else {return "Printer not found.";}
        logEntry("Printing file: "+filename+ " to printer "+printer);
        return "Printing file: "+filename+ " to printer "+printer;
    }

    public String queue(String printer) throws RemoteException { // lists the print queue for a given printer on the
                                                               // user's display in lines of the form <job number> <file
                                                               // name>
        Printer foundPrinter = getPrinter(printer);
        String print="";
        if (foundPrinter != null) {
            foundPrinter.queue();
            print=foundPrinter.printJobs();
        }
        

        logEntry(print);
        return print; 
    }

    public void topQueue(String printer, int job) throws RemoteException { // moves job to the top of the queue
        Printer foundPrinter = getPrinter(printer);
        if (foundPrinter != null) {
            foundPrinter.topQueue(job);
        }
         logEntry("Job " + job+ " moved to the top of queue of printer "+printer);
    }

    public void start() throws RemoteException { // starts the print server
        logEntry("--Print server started.");

    }

    public void stop() throws RemoteException { // stops the print server
        logEntry("--Print server stopped.");
    }

    public void restart() throws RemoteException { // stops the print server, clears the print queue and starts the
                                                   // print server again
        stop();
        for (Printer p : printers.values()) {
            p.clearPrinterQueue();
        }
        start();

         logEntry("--Print server restarted.");
    }

    public String status(String printer) throws RemoteException { // prints status of printer on the user's display
        return "Printer "+printer+" with status "; //TODO

    }

    public String readConfig(String parameter) throws RemoteException { // prints the value of the parameter on the print
                                                                      // server to the user's display
        logEntry(configs.get(parameter));
        return "The Configuration is "+configs.get(parameter);

    }

    public void setConfig(String parameter, String value) throws RemoteException { // sets the parameter on the print
                                                                                   // server to value
        
        logEntry("Configuration changed to "+configs.put(parameter,value));
    }

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

    public static String toHex(byte[] bytes){
        StringBuilder hexString = new StringBuilder(); //(2 * bytes.length);
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    public boolean authenticate(User user) throws RemoteException, NoSuchAlgorithmException{

        if (database.containsKey(user.getUsername())) {
            List<byte[]> pwsaltList = database.get(user.getUsername());

            byte[] password = pwsaltList.getFirst();
            byte[] salt = pwsaltList.getLast();
            byte[] passwordIn = hashPassword(user.getPassword(), salt);

            // Check user entered password against out hashed and salted database
            if (toHex(password).equals(toHex(passwordIn))) {
                return true;
            }  
        } 

        return false;
    }

    public int getSessionToken(int token) throws RemoteException{
        sessionToken=token; 
        return sessionToken;
    }

     public int getSessionToken() throws RemoteException{
       
        return sessionToken;
    }

   
}
