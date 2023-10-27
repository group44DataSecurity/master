import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;



public class AuthenticationService extends UnicastRemoteObject implements AuthenticationServiceInterface{
    
    public int token = 0;

    public AuthenticationService() throws RemoteException {
        super();
        
        Random random = new Random();
        token= random.nextInt();
    }

    public int getToken() throws RemoteException{
        return token;
    }



    public boolean isActive(int token) throws RemoteException{
        if (token!=0) return true;
        else return false;
    }

    public void disableToken() throws RemoteException{
        token=0;
    }

}
