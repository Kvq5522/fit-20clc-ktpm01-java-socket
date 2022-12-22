package Init;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

class ClientHandler implements Runnable {
    public static HashMap<String, ClientHandler> clientHandlers = new HashMap<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientName;

    public ClientHandler(Socket socket) {
        try  {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientName = bufferedReader.readLine();
            clientHandlers.put(clientName, this);
            broadcastMessage(System.getProperty("user.dir"));
        } catch (Exception e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
            e.printStackTrace();
        }
    }

    public void run() {
        String message;

        while (socket.isConnected()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                HashMap<String, Long> filesAndFolders = (HashMap<String, Long>) ois.readObject();

                for (String key : filesAndFolders.keySet()) {
                    System.out.println(key + " - Size: " + filesAndFolders.get(key));
                }
            } catch (Exception e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                e.printStackTrace();
                break;
            }
        }
    }

    public void broadcastMessage(String message) {
        try {
            clientHandlers.get(clientName).bufferedWriter.write(message);
            clientHandlers.get(clientName).bufferedWriter.newLine();
            clientHandlers.get(clientName).bufferedWriter.flush();
        } catch (Exception e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
            e.printStackTrace();
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("Server: "+clientName + " has left the chat");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            removeClientHandler();

            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

public class Server {
    private ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(!serverSocket.isClosed()) {
                        Socket socket = serverSocket.accept();
                        System.out.println("New client connected");
                        ClientHandler clientHandler = new ClientHandler(socket);

                        Thread thread = new Thread(clientHandler);
                        thread.start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void closeServer() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8080);
            Server server = new Server(serverSocket);
            server.startServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
