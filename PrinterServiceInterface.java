
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

public interface PrinterServiceInterface extends Remote {
    
    public String print(String filename, String printer) throws RemoteException; // prints file filename on the specified printer

    public String queue(String printer) throws RemoteException; // lists the print queue for a given printer on the user's display in lines of the form <job number>   <file name>

    public void topQueue(String printer, int job) throws RemoteException; // moves job to the top of the queue

    public void start() throws RemoteException; // starts the print server

    public void stop() throws RemoteException; // stops the print server

    public void restart() throws RemoteException; // stops the print server, clears the print queue and starts the print server again

    public String status(String printer) throws RemoteException; // prints status of printer on the user's display

    public String readConfig(String parameter) throws RemoteException; // prints the value of the parameter on the print server to the user's display

    public void setConfig(String parameter, String value) throws RemoteException; // sets the parameter on the print server to value

    public void initPrinters(String[] printers) throws RemoteException;

    public boolean authenticate (User user ) throws RemoteException, NoSuchAlgorithmException;

    public int getSessionToken(int token) throws RemoteException;

    public int getSessionToken() throws RemoteException;
}
