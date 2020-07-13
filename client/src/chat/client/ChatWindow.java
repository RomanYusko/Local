package chat.client;

import chat.network.TCPConnection;
import chat.network.TCPConnectionListener;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ChatWindow extends JFrame implements ActionListener, TCPConnectionListener {

    public static final String TP_ADDR = "213.5.192.246";
    public static final int PORT = 55657;
    public static final int HEIGHT = 400;
    public static final int WIDTH = 600;


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatWindow();
            }
        });
    }

    private final JTextArea log = new JTextArea();
    private final JTextField nickname = new JTextField("roma");
    private final JTextField input = new JTextField();

    private TCPConnection connection;

    private ChatWindow() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH,HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        log.setEditable(false);
        log.setLineWrap(true);
        add(log, BorderLayout.CENTER);
        input.addActionListener(this);
        add(input, BorderLayout.SOUTH);
        add(nickname, BorderLayout.NORTH);
        setVisible(true);
        try {
            connection = new TCPConnection(this, TP_ADDR,PORT);
        } catch (IOException e) {
            printMessage("Connection exception " + e);
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String message = input.getText();
        if (message.equals("")) return;
        input.setText(null);
        connection.sentMessage(nickname.getText() + ": " + message);

    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMessage("Connection ready...");
    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String value) {
        printMessage(value);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMessage("Connection close...");

    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        printMessage("Connection exception " + e);
    }

    private synchronized void printMessage(String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(message + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }
}
