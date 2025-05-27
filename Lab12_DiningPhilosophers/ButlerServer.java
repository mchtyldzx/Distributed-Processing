import java.io.*;
import java.net.*;
import java.util.*;

public class ButlerServer {
    private static final int MAX_EATING = 4;
    private static final int PORT = 12345;
    private static Set<Integer> currentlyEating = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Butler server is starting...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Butler server started successfully on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new PhilosopherHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class PhilosopherHandler implements Runnable {
        private Socket socket;

        public PhilosopherHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                String line;
                int id = -1;

                while ((line = in.readLine()) != null) {
                    if (line.startsWith("ID:")) {
                        id = Integer.parseInt(line.substring(3));
                        System.out.println("Philosopher " + id + " connected.");
                    } else if (line.equals("REQUEST_EAT")) {
                        synchronized (currentlyEating) {
                            while (currentlyEating.size() >= MAX_EATING) {
                                out.println("WAIT");
                                currentlyEating.wait();
                            }
                            currentlyEating.add(id);
                            out.println("GRANTED");
                            System.out.println("Philosopher " + id + " is allowed to eat.");
                        }
                    } else if (line.equals("DONE_EATING")) {
                        synchronized (currentlyEating) {
                            currentlyEating.remove(id);
                            currentlyEating.notifyAll();
                            System.out.println("Philosopher " + id + " has finished eating.");
                        }
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
