package org.example;

import java.net.Socket;

public interface ConnectionFactory {
    ConnectionHandler create(Socket socket);
}
