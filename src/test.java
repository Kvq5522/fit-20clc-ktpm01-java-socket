import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class test implements ActionListener {
    JFrame frame;
    JPanel mainPanel;
    public test() {
        mainPanel = new JPanel();

        // Add the main panel to a frame and display it
        frame = new JFrame("Panel Example");
        frame.setPreferredSize(new Dimension(500, 500));
        frame.setLayout(new BorderLayout());
        JButton button = new JButton("Click me");
        button.setActionCommand("button");
        button.addActionListener(this);
        frame.add(button);
        frame.add(mainPanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    public static void main(String[] args) {
        // Create the main panel
        new test();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("button")) {
            JPanel newPanel = new JPanel();
            newPanel.setBackground(Color.RED);
            newPanel.setPreferredSize(new Dimension(100, 100));
            mainPanel.add(newPanel);
            frame.pack();
        }
    }
}