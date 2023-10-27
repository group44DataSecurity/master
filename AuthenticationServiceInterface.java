import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AuthenticationServiceInterface extends Remote{
    public int getToken() throws RemoteException; // gets session token
    public boolean isActive(int token) throws RemoteException; // returns if the token is active or not
    public void disableToken() throws RemoteException; //deletes session token
}
