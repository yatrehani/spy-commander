
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * This is the Spy Commander class that is used to validate the spies
 * and also store their location in kml file
 *
 * @author Yatin Rehani
 * 10-7-2016
 */
public class TCPSpyCommanderUsingTEAandPasswords {
    // Specify the Path to create kml file
    public static final String locationFilePath = System.getProperty("user.home") + "/Desktop/";
    public static final String locationFileName = "SecretAgents.kml";
    public static Map<String, String> spiesLocation;
    public static Map<String, String> spiesUserIdSalt;
    public static Map<String, String> spiesUserIdHashSaltPassword;
    public static TEA tea;
    public static String symmetricKey;
    private static CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();

    /**
     * This main method initializes default values. Starts a Server socket. and
     * starts a new Connection thread to connect with a client.
     *
     */
    public static void main(String args[]) {
         // Initialize credentials and default location
        startupCommander();
        // Create file with default locations
        writeKmlFile();

        try {
            // Get key from Commander
            System.out.println("Enter symmetric key for TEA (taking first sixteen bytes)");
            Scanner scan = new Scanner(System.in);
            String symmetricKey = scan.nextLine();
            System.out.println(" Waiting for spies to visit...");
            tea = new TEA(symmetricKey.getBytes());
            int serverPort = 7896; // the server port
            ServerSocket listenSocket = new ServerSocket(serverPort);
            while (true) {
                // Listening
                Socket clientSocket = listenSocket.accept();
                Connection c = new Connection(clientSocket);
                c.tea = tea;
                c.start();
            }
        } catch (IOException e) {
            System.out.println("Listen socket:" + e.getMessage());
        }
    }

// the method initializes credentials and default location
    private static void startupCommander() {
        spiesUserIdSalt = new HashMap<String, String>();
        spiesUserIdSalt.put("joem", "lodlksglkvledrt");
        spiesUserIdSalt.put("jamesb", "lertokoge5kty");
        spiesUserIdSalt.put("mikem", "somnbihboey");

        spiesUserIdHashSaltPassword = new HashMap<String, String>();
        spiesUserIdHashSaltPassword.put("joem", "B1B0CD86BBAD1B1A0472A882AB236550");
        spiesUserIdHashSaltPassword.put("jamesb", "FD5145B7B338D5009C2042E9827B9478");
        spiesUserIdHashSaltPassword.put("mikem", "735D19749A3FC88030BB631CE41A1228");

        spiesLocation = new HashMap<String, String>();
        spiesLocation.put("joem", "-79.945389,40.444216,0.00000");
        spiesLocation.put("jamesb", "-79.945389,40.444216,0.00000");
        spiesLocation.put("mikem", "-79.945389,40.444216,0.00000");
    }


    // The method to write to existing kml file or create a new one
    public static void writeKmlFile() {
        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
                + "<kml xmlns=\"http://earth.google.com/kml/2.2\">\n"
                + "<Document>\n"
                + "<Style id=\"style1\">\n"
                + "<IconStyle>\n"
                + "<Icon>\n"
                + "<href>http://maps.gstatic.com/intl/en_ALL/mapfiles/ms/micons/blue-dot.png</href>\n"
                + "</Icon> </IconStyle> </Style> <Placemark>\n"
                + "<name>seanb</name>\n"
                + "<description>Spy Commander</description> <styleUrl>#style1</styleUrl>\n"
                + "<Point>\n"
                + "<coordinates>-79.945289,40.44431,0.00000</coordinates> </Point>\n"
                + "</Placemark> <Placemark>\n"
                + "<name>jamesb</name> <description>Spy</description> <styleUrl>#style1</styleUrl> <Point>\n"
                + "<coordinates>" + spiesLocation.get("jamesb") + "</coordinates> </Point>\n"
                + "</Placemark>\n"
                + "<Placemark> <name>joem</name> <description>Spy</description> <styleUrl>#style1</styleUrl> <Point>\n"
                + "<coordinates>" + spiesLocation.get("joem") + "</coordinates> </Point>\n"
                + "</Placemark>\n"
                + "<Placemark> <name>mikem</name> <description>Spy</description> <styleUrl>#style1</styleUrl> <Point>\n"
                + "<coordinates>" + spiesLocation.get("mikem") + "</coordinates> </Point>\n"
                + "</Placemark>\n"
                + "</Document>\n"
                + "</kml>\n";
        try {
            // If file does not exist - create file
            File file = new File(locationFilePath + locationFileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            // Write to file
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
            bufferWriter.write(content);

            bufferWriter.flush();
            fileWriter.flush();
            bufferWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Validates user based on username and Hash of Password+Salt MD5 is used to
     * compute Hash
     */
    public static boolean validateUser(String username, String password) {
        if (spiesUserIdSalt.get(username) != null) {
            return spiesUserIdHashSaltPassword.get(username).equals(PasswordHash.computeHash(password + spiesUserIdSalt.get(username)));
        } else {
            return false;
        }
    }
    
        // This method checks if a String ASCII.
    public static boolean isStringAscii(String text) {
        return asciiEncoder.canEncode(text);
    }
}

class Connection extends Thread {
    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;
    TEA tea;
    static int visitorCount;

    /**
     * Constructor.
     */
    public Connection(Socket socket) {
        try {
            clientSocket = socket;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

        /**
     * Receive Data from Client, decrypt it and authenticate credentials.
     * Update location if successfully authenticated
     * Appropriate message sent to client in each case.
     */
    public void run() {
        try {

            byte[] encryptedSpyData;
            encryptedSpyData = new byte[1000];
            int length = in.read(encryptedSpyData);
            encryptedSpyData = Arrays.copyOf(encryptedSpyData, length);
            
            //decrypting data from client
            byte[] spyDataBytes = tea.decrypt(encryptedSpyData);
            String spyData = new String(spyDataBytes);
            //System.out.println("checking:"+spyData);
            if (!TCPSpyCommanderUsingTEAandPasswords.isStringAscii(spyData)) { //check and change this
                clientSocket.close();
                System.out.println("Got visit " + ++visitorCount + " illegal symmetric key used. This may be an attack.");
                return;
            }
            String[] tokens = spyData.split(":");

            String messageToClient = null;
            String messageOnServer = null;

            if (TCPSpyCommanderUsingTEAandPasswords.validateUser(tokens[0], tokens[1])) {
                messageOnServer = "Got visit " + ++visitorCount + " from " + tokens[0];
                messageToClient = "Thank you. Your location was securely transmitted to Intelligence Headquarters.";
                TCPSpyCommanderUsingTEAandPasswords.spiesLocation.put(tokens[0], tokens[2]);
                TCPSpyCommanderUsingTEAandPasswords.writeKmlFile();
            } else {
                messageOnServer = "Got visit " + ++visitorCount + " from " + tokens[0] + ". Illegal Password attempt. This may be an attack.";
                messageToClient = "Not a valid user-id or password";
            }

            System.out.println(messageOnServer);
            out.write(messageToClient.getBytes());
            clientSocket.close();
            TCPSpyCommanderUsingTEAandPasswords.writeKmlFile();
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("readline:" + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {/*close failed*/
                e.printStackTrace();
            }
        }
    }
}