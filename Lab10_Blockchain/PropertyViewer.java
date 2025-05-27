import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * PropertyViewer - A complementary application to view property records stored on the blockchain
 */
public class PropertyViewer {

    private static final String NODE_URL = "http://localhost:8080";
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Property Blockchain Viewer ===");
        System.out.println("This application allows viewing property records on the blockchain");
        
        boolean running = true;
        while (running) {
            System.out.println("\nChoose an action:");
            System.out.println("1. View all property records");
            System.out.println("2. View property by ID");
            System.out.println("3. View property history");
            System.out.println("4. Exit");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            
            switch (choice) {
                case 1:
                    viewAllProperties();
                    break;
                case 2:
                    System.out.print("Enter property ID: ");
                    String propertyId = scanner.nextLine();
                    viewPropertyById(propertyId);
                    break;
                case 3:
                    System.out.print("Enter property ID: ");
                    String historyPropertyId = scanner.nextLine();
                    viewPropertyHistory(historyPropertyId);
                    break;
                case 4:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice, please try again.");
            }
        }
        
        System.out.println("Thank you for using Property Viewer!");
        scanner.close();
    }
    
    private static void viewAllProperties() {
        try {
            // First try the block endpoint
            String blockchainData = getBlockchainData("/block");
            JSONArray data;
            
            try {
                data = new JSONArray(blockchainData);
                // Check if data is blocks or transactions
                if (data.length() > 0 && data.getJSONObject(0).has("transactions")) {
                    // It's blocks
                    processBlocksForAllProperties(data);
                } else {
                    // Try transactions endpoint
                    String transactionData = getBlockchainData("/transaction");
                    data = new JSONArray(transactionData);
                    processTransactionsForAllProperties(data);
                }
            } catch (Exception e) {
                // Try transactions endpoint
                String transactionData = getBlockchainData("/transaction");
                data = new JSONArray(transactionData);
                processTransactionsForAllProperties(data);
            }
            
        } catch (Exception e) {
            System.out.println("Error retrieving blockchain data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void processBlocksForAllProperties(JSONArray blocks) {
        System.out.println("\n=== All Property Records (Block Format) ===");
        int found = 0;
        
        for (int i = 0; i < blocks.length(); i++) {
            JSONObject block = blocks.getJSONObject(i);
            if (!block.has("transactions")) continue;
            
            JSONArray transactions = block.getJSONArray("transactions");
            
            for (int j = 0; j < transactions.length(); j++) {
                JSONObject transaction = transactions.getJSONObject(j);
                String message = transaction.getString("text");
                
                if (message.startsWith("REGISTER_PROPERTY|")) {
                    String[] parts = message.split("\\|");
                    if (parts.length >= 3) {
                        System.out.println("Property ID: " + parts[1]);
                        System.out.println("Description: " + parts[2]);
                        System.out.println("Owner: " + transaction.getString("senderHash"));
                        System.out.println("---");
                        found++;
                    }
                }
            }
        }
        
        if (found == 0) {
            System.out.println("No property records found in blocks.");
        }
    }
    
    private static void processTransactionsForAllProperties(JSONArray transactions) {
        System.out.println("\n=== All Property Records (Transaction Format) ===");
        int found = 0;
        
        for (int j = 0; j < transactions.length(); j++) {
            JSONObject transaction = transactions.getJSONObject(j);
            String message = transaction.getString("text");
            
            if (message.startsWith("REGISTER_PROPERTY|")) {
                String[] parts = message.split("\\|");
                if (parts.length >= 3) {
                    System.out.println("Property ID: " + parts[1]);
                    System.out.println("Description: " + parts[2]);
                    System.out.println("Owner: " + transaction.getString("senderHash"));
                    System.out.println("---");
                    found++;
                }
            }
        }
        
        if (found == 0) {
            System.out.println("No property records found in transactions.");
        }
    }
    
    private static void viewPropertyById(String propertyId) {
        try {
            // First try blocks
            String blockchainData = getBlockchainData("/block");
            JSONArray blocks;
            String currentOwner = null;
            String description = null;
            
            try {
                blocks = new JSONArray(blockchainData);
                
                // Try getting from blocks first
                if (blocks.length() > 0 && blocks.getJSONObject(0).has("transactions")) {
                    for (int i = blocks.length() - 1; i >= 0; i--) {
                        JSONObject block = blocks.getJSONObject(i);
                        JSONArray transactions = block.getJSONArray("transactions");
                        
                        for (int j = 0; j < transactions.length(); j++) {
                            JSONObject transaction = transactions.getJSONObject(j);
                            String message = transaction.getString("text");
                            
                            if (message.startsWith("REGISTER_PROPERTY|")) {
                                String[] parts = message.split("\\|");
                                if (parts.length >= 3 && parts[1].equals(propertyId)) {
                                    description = parts[2];
                                    currentOwner = transaction.getString("senderHash");
                                }
                            } else if (message.startsWith("TRANSFER_PROPERTY|")) {
                                String[] parts = message.split("\\|");
                                if (parts.length >= 3 && parts[1].equals(propertyId)) {
                                    currentOwner = parts[2];
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // If block parsing failed, try transactions
            }
            
            // If not found in blocks, try transactions
            if (currentOwner == null) {
                String transactionData = getBlockchainData("/transaction");
                JSONArray transactions = new JSONArray(transactionData);
                
                for (int j = transactions.length() - 1; j >= 0; j--) {
                    JSONObject transaction = transactions.getJSONObject(j);
                    String message = transaction.getString("text");
                    
                    if (message.startsWith("REGISTER_PROPERTY|")) {
                        String[] parts = message.split("\\|");
                        if (parts.length >= 3 && parts[1].equals(propertyId)) {
                            description = parts[2];
                            currentOwner = transaction.getString("senderHash");
                            break;
                        }
                    } else if (message.startsWith("TRANSFER_PROPERTY|")) {
                        String[] parts = message.split("\\|");
                        if (parts.length >= 3 && parts[1].equals(propertyId)) {
                            currentOwner = parts[2];
                            break;
                        }
                    }
                }
            }
            
            if (currentOwner != null) {
                System.out.println("\n=== Property Details ===");
                System.out.println("Property ID: " + propertyId);
                System.out.println("Description: " + description);
                System.out.println("Current Owner: " + currentOwner);
            } else {
                System.out.println("No property found with ID: " + propertyId);
            }
            
        } catch (Exception e) {
            System.out.println("Error retrieving blockchain data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void viewPropertyHistory(String propertyId) {
        try {
            boolean foundProperty = false;
            
            // Try blocks first
            try {
                String blockchainData = getBlockchainData("/block");
                JSONArray blocks = new JSONArray(blockchainData);
                
                if (blocks.length() > 0 && blocks.getJSONObject(0).has("transactions")) {
                    System.out.println("\n=== Property History (Block Format) ===");
                    for (int i = 0; i < blocks.length(); i++) {
                        JSONObject block = blocks.getJSONObject(i);
                        JSONArray transactions = block.getJSONArray("transactions");
                        
                        for (int j = 0; j < transactions.length(); j++) {
                            JSONObject transaction = transactions.getJSONObject(j);
                            String message = transaction.getString("text");
                            
                            if (message.startsWith("REGISTER_PROPERTY|")) {
                                String[] parts = message.split("\\|");
                                if (parts.length >= 3 && parts[1].equals(propertyId)) {
                                    System.out.println("REGISTERED by " + transaction.getString("senderHash"));
                                    System.out.println("Description: " + parts[2]);
                                    foundProperty = true;
                                }
                            } else if (message.startsWith("TRANSFER_PROPERTY|")) {
                                String[] parts = message.split("\\|");
                                if (parts.length >= 3 && parts[1].equals(propertyId)) {
                                    System.out.println("TRANSFERRED from " + transaction.getString("senderHash") + " to " + parts[2]);
                                    foundProperty = true;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // If block parsing failed, continue to transactions
            }
            
            // Try transactions directly if no property found in blocks
            if (!foundProperty) {
                String transactionData = getBlockchainData("/transaction");
                JSONArray transactions = new JSONArray(transactionData);
                
                System.out.println("\n=== Property History (Transaction Format) ===");
                for (int j = 0; j < transactions.length(); j++) {
                    JSONObject transaction = transactions.getJSONObject(j);
                    String message = transaction.getString("text");
                    
                    if (message.startsWith("REGISTER_PROPERTY|")) {
                        String[] parts = message.split("\\|");
                        if (parts.length >= 3 && parts[1].equals(propertyId)) {
                            System.out.println("REGISTERED by " + transaction.getString("senderHash"));
                            System.out.println("Description: " + parts[2]);
                            foundProperty = true;
                        }
                    } else if (message.startsWith("TRANSFER_PROPERTY|")) {
                        String[] parts = message.split("\\|");
                        if (parts.length >= 3 && parts[1].equals(propertyId)) {
                            System.out.println("TRANSFERRED from " + transaction.getString("senderHash") + " to " + parts[2]);
                            foundProperty = true;
                        }
                    }
                }
            }
            
            if (!foundProperty) {
                System.out.println("No property found with ID: " + propertyId);
            }
            
        } catch (Exception e) {
            System.out.println("Error retrieving blockchain data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String getBlockchainData(String endpoint) throws Exception {
        URL url = new URL(NODE_URL + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        return response.toString();
    }
} 