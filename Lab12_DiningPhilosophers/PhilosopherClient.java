import java.io.*;
import java.net.*;

public class PhilosopherClient {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java PhilosopherClient <philosopher_id>");
            return;
        }

        int id = Integer.parseInt(args[0]);

        try (
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            out.println("ID:" + id);

            while (true) {
                System.out.println("Philosopher " + id + " is thinking...");
                Thread.sleep((long)(Math.random() * 2000 + 1000));

                System.out.println("Philosopher " + id + " requests to eat.");
                out.println("REQUEST_EAT");

                String response = in.readLine();
                while (!response.equals("GRANTED")) {
                    System.out.println("Philosopher " + id + " is waiting...");
                    response = in.readLine();
                }

                System.out.println("Philosopher " + id + " is eating...");
                Thread.sleep((long)(Math.random() * 2000 + 1000));

                System.out.println("Philosopher " + id + " has finished eating.");
                out.println("DONE_EATING");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
