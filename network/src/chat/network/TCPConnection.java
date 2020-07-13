package chat.network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class TCPConnection {
    private final TCPConnectionListener evenConnection;
    private final Socket socket;
    private final Thread rxThreet;
    private final BufferedReader in;
    private final BufferedWriter out;

    public TCPConnection(TCPConnectionListener evenConnection, String ipAddres, int port ) throws IOException {
        this(evenConnection, new Socket(ipAddres, port));
    }

    public TCPConnection(TCPConnectionListener evenConnection, Socket socket) throws IOException {
        this.evenConnection = evenConnection;
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));

        rxThreet = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    evenConnection.onConnectionReady(TCPConnection.this);
                    while (!rxThreet.isInterrupted()) {
                     evenConnection.onReceiveString(TCPConnection.this, in.readLine());
                    }
                } catch (IOException e) {
                    evenConnection.onException(TCPConnection.this, e);
                } finally {
                    evenConnection.onDisconnect(TCPConnection.this);
                }
            }
        });
        rxThreet.start();
    }

    public synchronized void sentMessage(String value) {
        try {
            out.write(value + "\r\n");
            out.flush();
        } catch (IOException e) {
            evenConnection.onException(TCPConnection.this, e);
            disconnect();
        }
    }

    public synchronized void disconnect() {
        rxThreet.isInterrupted();
        try {
            socket.close();
        } catch (IOException e) {
            evenConnection.onException(TCPConnection.this, e);
        }
    }

    @Override
    public String toString() {
        return "TCPConnection: " + socket.getInetAddress() + ": " + socket.getPort();
    }
}
