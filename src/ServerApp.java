import FileTracker.FileTracker;

import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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
            ServerApp.logs.add("Client " + clientName + " connected to server");
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

                synchronized (clientName) {
                    if (!clientsInfo.containsKey(clientName)) {
                        clientsInfo.put(clientName, new FileTracker(filesAndFolders));
                    } else if (filesAndFolders.size() != 0) {
                        clientsInfo.get(clientName).setMap(filesAndFolders);
                    }
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

    static void broadcastMessageToUser(String message, String user) {
        try {
            clientHandlers.get(user).bufferedWriter.write(message);
            clientHandlers.get(user).bufferedWriter.newLine();
            clientHandlers.get(user).bufferedWriter.flush();
        } catch (Exception e) {
            closeUser(user);
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

    static void closeUser(String user) {
        if (!clientHandlers.containsKey(user)) {
            return;
        }

        clientHandlers.get(user).closeEverything(clientHandlers.get(user).socket, clientHandlers.get(user).bufferedReader, clientHandlers.get(user).bufferedWriter);
    }
}

public class ServerApp implements ActionListener {
    private ServerSocket serverSocket;
    static ArrayList<String> users = new ArrayList<>();
    static ArrayList<String> logs = new ArrayList<>();
    static JFrame mainFrame;
    static JPanel introPanel;
    static JPanel logPanel;
    static JScrollPane logScrollPane;
    static JPanel clientPanel;
    static JScrollPane clientScrollPane;
    static JPanel tablePanel;
    static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    static JTextField pathTextArea;

    public ServerApp() {

    }

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

        showInitUI();

        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    static void addItemToClientPanel(String clientName) {
        JPanel newClient = new JPanel();
        newClient.setName(clientName);
        newClient.setLayout(new BorderLayout());
        newClient.setBackground(Color.WHITE);
        newClient.setPreferredSize(new Dimension((int) (screenSize.width * 0.35), (int) (screenSize.height * 0.05)));

        JLabel clientLabel = new JLabel(clientName);
        clientLabel.setFont(new Font("Arial", Font.BOLD, 20));
        clientLabel.setForeground(Color.BLACK);
        clientLabel.setHorizontalAlignment(JLabel.CENTER);
        newClient.add(clientLabel, BorderLayout.WEST);

        JButton seeDetail = new JButton("See details");
        seeDetail.setFont(new Font("Arial", Font.BOLD, 20));
        seeDetail.setActionCommand("detail-"+clientName);
        seeDetail.addActionListener(new ServerApp());
        newClient.add(seeDetail, BorderLayout.EAST);

        clientPanel.add(newClient);
        clientPanel.revalidate();
        clientPanel.repaint();
    }

    static void addItemToLogPanel(String log) {
        JPanel newLog = new JPanel();
        newLog.setName(log);
        newLog.setLayout(new BorderLayout());
        newLog.setBackground(Color.WHITE);
        newLog.setPreferredSize(new Dimension((int) (screenSize.width * 0.15), (int) (screenSize.height * 0.05)));

        JLabel logLabel = new JLabel(log);
        logLabel.setFont(new Font("Arial", Font.BOLD, 20));
        logLabel.setForeground(Color.BLACK);
        logLabel.setHorizontalAlignment(JLabel.CENTER);
        newLog.add(logLabel, BorderLayout.WEST);

        logPanel.add(newLog);
        logPanel.revalidate();
        logPanel.repaint();
    }

    void showInitUI() {
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
    }

    void removeInitPanels() {
        mainFrame.remove(clientScrollPane);
        mainFrame.remove(logScrollPane);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    void showDetailUI(String clientName) {
        tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());

        JPanel inputField = new JPanel();
        inputField.setLayout(new BorderLayout());
        inputField.setPreferredSize(new Dimension((int) (screenSize.width * 0.25), (int) (screenSize.height * 0.35)));
        pathTextArea = new JTextField(20);
        JButton sendPath = new JButton("Send path");
        sendPath.setActionCommand("send-"+clientName);
        sendPath.addActionListener(this);
        JButton refresh = new JButton("Refresh");
        refresh.setActionCommand("refresh-"+clientName);
        refresh.addActionListener(this);
        inputField.add(pathTextArea, BorderLayout.CENTER);
        inputField.add(sendPath, BorderLayout.EAST);
        inputField.add(refresh, BorderLayout.SOUTH);

        JTable table = new JTable();
        table.setPreferredScrollableViewportSize(new Dimension((int) (screenSize.width * 0.25), (int) (screenSize.height * 0.35)));
        table.setFillsViewportHeight(true);
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setGridColor(Color.BLACK);

        String columnNames[] = {"Name", "Type", "Size"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        table.setModel(tableModel);

        HashMap<String, Long> files = ClientHandler.clientsInfo.get(clientName).getMap();

        for (String iterator : files.keySet()) {
            String name = iterator.split("-")[0];
            String type = iterator.split("-")[1];
            Long size = files.get(iterator);
            Object[] data = {name, type, size};
            tableModel.addRow(data);
        }

        JButton back = new JButton("Back");
        back.setActionCommand("back-to-init");
        back.addActionListener(this);
        tablePanel.add(back, BorderLayout.PAGE_END);

        tablePanel.add(table.getTableHeader(), BorderLayout.PAGE_START);
        tablePanel.add(table, BorderLayout.WEST);
        tablePanel.add(inputField, BorderLayout.EAST);

        mainFrame.add(tablePanel, BorderLayout.CENTER);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    void reloadDetailUI() {
        mainFrame.remove(tablePanel);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    void returnToInitUI() {
        mainFrame.remove(tablePanel);
        mainFrame.add(clientScrollPane, BorderLayout.WEST);
        mainFrame.add(logScrollPane, BorderLayout.EAST);
        mainFrame.revalidate();
        mainFrame.repaint();
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
        String command = e.getActionCommand();

        if (command.contains("detail")) {
            String clientName = command.split("-")[1];
            removeInitPanels();
            showDetailUI(clientName);
        } else if (command.equals("back-to-init")) {
            returnToInitUI();
        } else if (command.contains("send")) {
            ClientHandler.broadcastMessageToUser(pathTextArea.getText(), command.split("-")[1]);
        } else if (command.contains("refresh")) {
            reloadDetailUI();
            showDetailUI(command.split("-")[1]);
            mainFrame.revalidate();
            mainFrame.repaint();
        }
    }
}
