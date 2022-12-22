import FileTracker.FileTracker;

import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

class ClientHandler implements Runnable {
    public static HashMap<String, ClientHandler> clientHandlers = new HashMap<>();
    public static HashMap<String, FileTracker> clientsInfo = new HashMap<>();
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
            ServerApp.users.add(clientName);
            ServerApp.addItemToClientPanel(clientName);
            ServerApp.addItemToLogPanel(clientName + " has joined.");
            broadcastMessage(System.getProperty("user.dir"));
        } catch (Exception e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
            e.printStackTrace();
        }
    }

    public void run() {
        while (socket.isConnected()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                HashMap<String, Long> filesAndFolders = (HashMap<String, Long>) ois.readObject();

                if (!clientsInfo.containsKey(clientName)) {
                    clientsInfo.put(clientName, new FileTracker(filesAndFolders));
                }

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

public class ServerApp implements ActionListener {
    private ServerSocket serverSocket;
    static ArrayList<String> users = new ArrayList<>();
    static JFrame mainFrame;
    static JPanel introPanel;
    static JPanel logPanel;
    static JScrollPane logScrollPane;
    static JPanel clientPanel;
    static JScrollPane clientScrollPane;
    static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    public ServerApp(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        mainFrame = new JFrame("Server");
        mainFrame.setSize((int) (screenSize.width * 0.5),(int) (screenSize.height * 0.5));
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());

        introPanel = new JPanel();
        introPanel.setLayout(new BorderLayout());
        introPanel.setBackground(Color.WHITE);
        JLabel introLabel = new JLabel("Server is running at PORT: " + serverSocket.getLocalPort() + " - IP: " + serverSocket.getInetAddress().getHostAddress());
        introLabel.setFont(new Font("Arial", Font.BOLD, 20));
        introLabel.setForeground(Color.BLACK);
        introLabel.setHorizontalAlignment(JLabel.CENTER);
        introPanel.add(introLabel, BorderLayout.CENTER);
        mainFrame.add(introPanel, BorderLayout.NORTH);

        clientScrollPane = new JScrollPane();
        clientScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        clientScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        clientScrollPane.setPreferredSize(new Dimension((int) (screenSize.width * 0.35), (int) (screenSize.height * 0.35)));
        clientPanel = new JPanel();
        clientPanel.setLayout(new BoxLayout(clientPanel, BoxLayout.Y_AXIS));
        clientScrollPane.setViewportView(clientPanel);
        mainFrame.add(clientScrollPane, BorderLayout.WEST);

        logScrollPane = new JScrollPane();
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        logScrollPane.setPreferredSize(new Dimension((int) (screenSize.width * 0.15), (int) (screenSize.height * 0.15)));
        logPanel = new JPanel();
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.Y_AXIS));
        logScrollPane.setViewportView(logPanel);
        mainFrame.add(logScrollPane, BorderLayout.EAST);

        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    static void addItemToClientPanel(String clientName) {
        JPanel newClient = new JPanel();
        newClient.setLayout(new BorderLayout());
        newClient.setBackground(Color.WHITE);
        newClient.setPreferredSize(new Dimension((int) (screenSize.width * 0.35), (int) (screenSize.height * 0.05)));

        JLabel clientLabel = new JLabel(clientName);
        clientLabel.setFont(new Font("Arial", Font.BOLD, 20));
        clientLabel.setForeground(Color.BLACK);
        clientLabel.setHorizontalAlignment(JLabel.CENTER);
        newClient.add(clientLabel, BorderLayout.WEST);

        JButton seeDetail = new JButton("See details");
        newClient.add(seeDetail, BorderLayout.EAST);

        clientPanel.add(newClient);
        clientPanel.revalidate();
        clientPanel.repaint();
    }

    static void addItemToLogPanel(String log) {
        JPanel newLog = new JPanel();
        newLog.setLayout(new BorderLayout());
        newLog.setBackground(Color.WHITE);
        newLog.setPreferredSize(new Dimension((int) (screenSize.width * 0.15), (int) (screenSize.height * 0.05)));

        JLabel logLabel = new JLabel(log);
        logLabel.setFont(new Font("Arial", Font.BOLD, 20));
        logLabel.setForeground(Color.BLACK);
        logLabel.setHorizontalAlignment(JLabel.CENTER);
        newLog.add(logLabel, BorderLayout.CENTER);

        logPanel.add(newLog);
        logPanel.revalidate();
        logPanel.repaint();
    }

    public void startServer() {
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
            ServerApp serverApp = new ServerApp(serverSocket);
            serverApp.startServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
