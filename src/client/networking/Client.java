package client.networking;

import common.Protocol;

import java.net.*;
import java.io.*;
import java.util.*;

public class Client {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void connect(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        clientSocket.setSoTimeout(Protocol.TIMEOUT*1000);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public List<String> sendMessage(String msg) throws IOException {
        out.println(msg);
        ArrayList<String> response = new ArrayList<>();
        String line = "";
        do {
            line = in.readLine();
            response.add(line);
        } while (!line.equals(Protocol.ACKNOWLEDGED) && !line.equals(Protocol.EOF));
        return response;
    }

    public void stopConnection() throws IOException {
        sendMessage(Protocol.CLOSE_CONNECTION);
        in.close();
        out.close();
        clientSocket.close();
    }

    public static void main(String[] args) {
        System.out.println("Hello, I'm the client!");


    }
}
