import java.util.Scanner;

/**
 * PropertyRegistry - A simple application to demonstrate using JBlockchain
 * for recording property ownership rights
 */
public class PropertyRegistry {

    private static final String NODE_URL = "http://localhost:8080";
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Property Rights Registry on Blockchain ===");
        System.out.println("This application allows recording property ownership on the blockchain");
        
        // Get user information
        System.out.print("Enter your address hash: ");
        String addressHash = scanner.nextLine();
        
        System.out.print("Enter path to your private key file: ");
        String privateKeyPath = scanner.nextLine();
        
        boolean running = true;
        while (running) {
            System.out.println("\nChoose an action:");
            System.out.println("1. Register new property");
            System.out.println("2. Transfer property");
            System.out.println("3. Exit");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            
            switch (choice) {
                case 1:
                    registerProperty(scanner, addressHash, privateKeyPath);
                    break;
                case 2:
                    transferProperty(scanner, addressHash, privateKeyPath);
                    break;
                case 3:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice, please try again.");
            }
        }
        
        System.out.println("Thank you for using Property Registry!");
    }
    
    private static void registerProperty(Scanner scanner, String addressHash, String privateKeyPath) {
        System.out.print("Enter property ID (e.g., land parcel number): ");
        String propertyId = scanner.nextLine();
        
        System.out.print("Enter property description: ");
        String description = scanner.nextLine();
        
        // Create a formatted message for the blockchain
        String message = String.format("REGISTER_PROPERTY|%s|%s", propertyId, description);
        
        // Execute the client command to send the transaction
        String command = String.format(
            "java -jar client/target/client-0.0.1-SNAPSHOT.jar --transaction " +
            "--node \"%s\" --sender \"%s\" --message \"%s\" --privatekey \"%s\"",
            NODE_URL, addressHash, message, privateKeyPath
        );
        
        executeCommand(command);
        System.out.println("Property registration transaction submitted!");
    }
    
    private static void transferProperty(Scanner scanner, String addressHash, String privateKeyPath) {
        System.out.print("Enter property ID to transfer: ");
        String propertyId = scanner.nextLine();
        
        System.out.print("Enter new owner address hash: ");
        String newOwnerHash = scanner.nextLine();
        
        // Create a formatted message for the blockchain
        String message = String.format("TRANSFER_PROPERTY|%s|%s", propertyId, newOwnerHash);
        
        // Execute the client command to send the transaction
        String command = String.format(
            "java -jar client/target/client-0.0.1-SNAPSHOT.jar --transaction " +
            "--node \"%s\" --sender \"%s\" --message \"%s\" --privatekey \"%s\"",
            NODE_URL, addressHash, message, privateKeyPath
        );
        
        executeCommand(command);
        System.out.println("Property transfer transaction submitted!");
    }
    
    private static void executeCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                System.out.println("Error executing command: " + exitCode);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 