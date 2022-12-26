import FileTracker.FileTracker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;
class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private int crashCount = 0;
    String path = System.getProperty("user.dir");
    String clientName;

    FileTracker fileTracker;

    public Client() {
    }

    public void sendMessage() {
        try {
            bufferedWriter.write(clientName);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (Exception e ) {
            closeEverything(socket, bufferedReader, bufferedWriter);
            e.printStackTrace();
        }
    }

    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String message;

                while (socket.isConnected()) {
                    try {
                        message = bufferedReader.readLine();
                        System.out.println(message);

                        path = message;

                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                        objectOutputStream.writeObject(fileTracker.getFilesAndFolders(message));
                    } catch (Exception e) {
                        crashCount++;

                        if (crashCount == 3) {
                            closeEverything(socket, bufferedReader, bufferedWriter);
                            System.exit(0);
                            break;
                        }
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void sendChanges() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (socket.isConnected()) {
                    try {
                        if (fileTracker.getFilesAndFolders(path).equals(fileTracker.getMap())) {
                            continue;
                        }

                        fileTracker.setMap(fileTracker.getFilesAndFolders(path));
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                        objectOutputStream.writeObject(fileTracker.getFilesAndFolders(path));
                    } catch (Exception e) {
                        crashCount++;

                        if (crashCount == 3) {
                            closeEverything(socket, bufferedReader, bufferedWriter);
                            System.exit(0);
                            break;
                        }
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connectToServer(String clientName, String IP, int port) {
        try {
            this.clientName = clientName;
            this.socket = new Socket(IP, port);
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.fileTracker = new FileTracker();
            this.fileTracker.setMap(fileTracker.getFilesAndFolders(System.getProperty("user.dir")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isConnectionAlive() {
        return socket == null ? false : socket.isConnected();
    }

    public void start() {
        listenForMessage();
        sendChanges();
    }
}

public class ClientApp implements ActionListener {
    Client client = new Client();
    static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    JFrame mainFrame;
    JPanel connectPanel;
    JTextField nameTextField;
    JTextField IPTextField;
    JTextField portTextField;
    JTextArea statusTextArea;
    JPanel mainPanel;

    ClientApp() {
        mainFrame = new JFrame("Client");
        mainFrame.setSize((int) (screenSize.width * 0.5),(int) (screenSize.height * 0.5));
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());

        connectPanel = new JPanel();
        connectPanel.setLayout(new BorderLayout());

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BorderLayout());
        nameTextField = new JTextField();
        nameTextField.setPreferredSize(new Dimension((int) (screenSize.width * 0.35), (int) (screenSize.height * 0.03)));
        JLabel nameLabel = new JLabel("Name:");
        namePanel.add(nameLabel, BorderLayout.WEST);
        namePanel.add(nameTextField, BorderLayout.CENTER);

        JPanel IPPanel = new JPanel();
        IPPanel.setLayout(new BorderLayout());
        IPTextField = new JTextField();
        IPTextField.setPreferredSize(new Dimension((int) (screenSize.width * 0.35), (int) (screenSize.height * 0.03)));
        JLabel IPLabel = new JLabel("IP:");
        IPPanel.add(IPLabel, BorderLayout.WEST);
        IPPanel.add(IPTextField, BorderLayout.CENTER);

        JPanel portPanel = new JPanel();
        portPanel.setLayout(new BorderLayout());
        portTextField = new JTextField();
        portTextField.setPreferredSize(new Dimension((int) (screenSize.width * 0.35), (int) (screenSize.height * 0.03)));
        JLabel portLabel = new JLabel("Port:");
        portPanel.add(portLabel, BorderLayout.WEST);
        portPanel.add(portTextField, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        inputPanel.add(namePanel, BorderLayout.NORTH);
        inputPanel.add(IPPanel, BorderLayout.CENTER);
        inputPanel.add(portPanel, BorderLayout.SOUTH);
        inputPanel.setPreferredSize(new Dimension((int) (screenSize.width * 0.35), (int) (screenSize.height * 0.09)));

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BorderLayout());
        statusTextArea = new JTextArea("Connection: false");
        statusTextArea.setPreferredSize(new Dimension((int) (screenSize.width * 0.15), (int) (screenSize.height * 0.06)));
        statusTextArea.setEditable(false);
        statusTextArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton connectButton = new JButton("Connect");
        connectButton.setPreferredSize(new Dimension((int) (screenSize.width * 0.15), (int) (screenSize.height * 0.03)));
        connectButton.setActionCommand("connect");
        connectButton.addActionListener(this);
        statusPanel.add(statusTextArea, BorderLayout.NORTH);
        statusPanel.add(connectButton, BorderLayout.SOUTH);

        connectPanel.add(inputPanel, BorderLayout.WEST);
        connectPanel.add(statusPanel, BorderLayout.EAST);

        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1, 1));

        mainFrame.add(connectPanel, BorderLayout.NORTH);
        mainFrame.add(mainPanel, BorderLayout.CENTER);
        mainFrame.setVisible(true);
    }

    public static void main(String[] args) {
        ClientApp clientApp = new ClientApp();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("connect")) {
            System.out.println(nameTextField.getText());
            System.out.println(IPTextField.getText());
            System.out.println(portTextField.getText());
            client.connectToServer(nameTextField.getText(), IPTextField.getText(), Integer.parseInt(portTextField.getText()));

            if (client.isConnectionAlive()) {
                client.sendMessage();
                client.start();
                statusTextArea.setText("Connection: true");
                mainPanel.removeAll();
                mainPanel.add(new JLabel("Connected"));
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        }
    }
}

