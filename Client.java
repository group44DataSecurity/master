import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.Scanner;

public class Client {
    static Scanner input = new Scanner(System.in);

    public static void main(String[] args) throws MalformedURLException, NotBoundException, RemoteException {
        int port = 5099;
        // String name = "hello";
        // HelloService service = (HelloService)
        // Naming.lookup("rmi://localhost:"+port+"/"+name);
        // System.out.println("--- " + service.echo("hey server"));

        String name = "print";
        PrinterServiceInterface service = (PrinterServiceInterface) Naming
                .lookup("rmi://localhost:" + port + "/" + name);

        System.out.println("Client started.");

        String[] printers = {"Printer1","Printer2","Printer3"};
        service.initPrinters(printers);

        // User user1 = new User("client1", "password1");

        User user = getUser();
        while (!service.authenticate(user)) {
            System.out.println("Try again. Wrong credentials.");
            user = getUser();
        }

        System.out.println("Authenticated user.");
        if (service.getSessionToken()!=0){
            service.restart();
            System.out.println(service.print("file1.pdf", printers[0]));
            System.out.println(service.print("file2.pdf", printers[0]));
            System.out.println(service.print("file3.pdf", printers[0]));
            System.out.println(service.queue(printers[0]));
            service.topQueue(printers[0], 2);
            System.out.println(service.queue(printers[0]));
            service.setConfig("myParameter", "100");
            service.setConfig("myParameter", "200");
            System.out.println("Configuration: "+service.readConfig("myParameter"));
            System.out.println("-----------------------------------");
        }

    }

    public static User getUser() {
        System.out.println("Enter your username:");
        String username = input.nextLine();

        System.out.println("Enter your password:");
        String password = input.nextLine();

        return new User(username, password);
    }
}
