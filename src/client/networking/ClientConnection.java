package client.networking;

import protocol.Message;
import protocol.MessageType;
import protocol.Protocol;

import java.net.*;
import java.io.*;

public class ClientConnection {
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void connect(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        clientSocket.setSoTimeout(Protocol.SOCKET_TIMEOUT);
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        in = new ObjectInputStream(clientSocket.getInputStream());
    }

    public Message sendMessage(Message msg) throws ClassNotFoundException, IOException {
        out.writeObject(msg);
        return (Message) in.readObject();
    }

    public void disconnect() throws IOException, ClassNotFoundException {
        sendMessage(new Message(MessageType.DISCONNECT));
        in.close();
        out.close();
        clientSocket.close();
    }
}
