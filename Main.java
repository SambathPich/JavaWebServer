package project1_sambathpich;

import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) throws Exception{
        int port = 9999;
        ServerSocket myServerSocket = new ServerSocket(port);
        System.out.println("\n***** WEB SERVER HAS STARTED *****\n");

        /* ::::: Accept all requests ::::: */
        while (true) {
            Socket myConnSocket = myServerSocket.accept();
            HttpRequestHandler myHttpRequest = new HttpRequestHandler(myConnSocket);

            //Create a new thread to process the request.
            Thread thread = new Thread(myHttpRequest);
            thread.start();
        }
    }
}

