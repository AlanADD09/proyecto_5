//Proyecto 5 y 6 -- Domínguez Durán Alan Axel -- 4CM11 -- DSD
package com.alan.app;

public class App {
    public static void main( String[] args ){
        // System.out.println( "Hello World!" );
        int serverPort = 8080;
        if (args.length == 1) {
            serverPort = Integer.parseInt(args[0]);
        }

        WebServer webServer = new WebServer(serverPort);
        webServer.startServer();

        System.out.println("Servidor escuchando en el puerto " + serverPort);
    }
}
