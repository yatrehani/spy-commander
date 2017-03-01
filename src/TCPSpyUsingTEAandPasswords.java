
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
/**
 * TCP client used by each spy in the field.
 * @author Yatin Rehani
 * 10-7-2016
 */
public class TCPSpyUsingTEAandPasswords {
    
    String symmetricKey = null;  // to store the symmetric key
    private static TEA tea = null;
    /**
     * This is the main method. Opens Socket to Server.
     * Collects location from spy and sends to server
     * Displays message from server
     */
    public static void main(String args[]) {
        Socket serverSocket = null;
        
        try {
            int serverPort = 7896; // this is server port
            serverSocket = new Socket("localhost",serverPort); // create a new socket
            DataOutputStream out = new DataOutputStream(serverSocket.getOutputStream());
            DataInputStream in = new DataInputStream(serverSocket.getInputStream());

            TCPSpyUsingTEAandPasswords spyFinal = new TCPSpyUsingTEAandPasswords();
            String spyData = spyFinal.startupSpy();
            byte[] encryptedSpyData = tea.encrypt(spyData.getBytes()); // store the encrypted data
            out.write(encryptedSpyData);

            // Message received from server
            byte[]  serverMessage = new byte[1000];
	    // serverMessage stores the message
            in.read(serverMessage);
            System.out.println(new String(serverMessage));
            in.close();
            out.close();
            serverSocket.close();
        } catch (UnknownHostException e) {    // handle the exception
            System.out.println(e.getMessage());
        } catch (EOFException e) {    // handle the exception
            System.out.println(e.getMessage());
        } catch (IOException e) {    // handle the exception
            System.out.println(e.getMessage());
        } finally {
            if (serverSocket != null) try {
                serverSocket.close();  // close the socket
            } catch (IOException e) {  
                System.out.println(e.getMessage());
            }
        }
    }
    
    /**
     * The startup method which takes all input from spies
     */
    public String startupSpy(){
            System.out.println("Enter symmetric key for TEA (taking first sixteen bytes)");
            Scanner scan = new Scanner(System.in); // Scanner to take inputs from spies
            symmetricKey = scan.nextLine().trim();
            tea = new TEA(symmetricKey.getBytes());
            System.out.println("Enter your ID:");
            scan = new Scanner(System.in);
            String id = scan.nextLine().trim();
            System.out.println("Enter your Password:");
            scan = new Scanner(System.in);
            String password = scan.nextLine().trim();
            System.out.println("Enter your location:");
            scan = new Scanner(System.in);
            String location = scan.nextLine();
            return id+":"+password+":"+location;
                    }
}
