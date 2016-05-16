/*
 *  Chat System
 *  Lavet af Gruppe 24 - DTU 2016
 *  --------------------
 *  Magnus Andrias Nielsen, s141899
 *  --------------------
 */
package server;

/**
 *
 * @author Magnus A. Nielsen
 */

import brugerautorisation.data.Bruger;
import brugerautorisation.transport.rmi.Brugeradmin;
import java.net.*;
import java.io.*;
import java.rmi.Naming;
import java.util.*;

public class ChatSystem_Server{

    ArrayList clientOutputStreams;
    ArrayList<String> users;
    static String firstName = "";
    static String lastName = "";
    
    public class ClientHandler implements Runnable{
        BufferedReader reader;
        Socket sock;
        PrintWriter client;
                
        public ClientHandler(Socket clientSocket, PrintWriter user){
            
            client = user;
            try{
                
                sock = clientSocket;
                InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
                reader = new BufferedReader(isReader);
                
            }catch (Exception ex){
                //System.out.println("Unexpected error...\n");
            }
            
        }
        
        @Override
        public void run(){
            
            String message, connect = "Connect", disconnect = "Disconnect", 
                    chat = "Chat" , login = "Login", firstLogin = "firstLogin", 
                    loginError = "LoginError";
            String[] data;
            
            try{
                
                while ((message = reader.readLine()) != null){
                    
                    //System.out.println("Received: " + message + "\n");
                    data = message.split(":");
                    
                    /*for (String token:data){
                        
                    }*/
                    
                    if (data[2].equals(connect)){
                        
                        tellEveryone((firstName + " (" + data[0] + ")" + ":" + data[1] + ":" + chat));
                        userAdd(data[0]);
                        
                    }else if (data[2].equals(disconnect)){
                        tellEveryone((data[0] + ":has disconnected." + ":" + chat));
                        userRemove(data[0]);
                        
                    }else if (data[2].equals(chat)){
                        tellEveryone(message);
                    
                    }else if (data[2].equals(firstLogin)){
                        tellEveryone(message);
                        
                    //}else if (data[2].equals(loginError)){
                        //tellEveryone(message);
                        //System.out.println("\nWrong username or password..\n");
                        
                    }else if (data[2].equals(login)){
                        Brugeradmin ba = (Brugeradmin) Naming.lookup("rmi://javabog.dk/brugeradmin");
                        String usernameInput = data[0];
                        String passwordInput = data[1];
                        
                        try{
                            Bruger b = ba.hentBruger(usernameInput, passwordInput);
                                
                            //System.out.println("\nLogged in as: " + b.fornavn + " " + b.efternavn + ".\n");
                            //loggedIn = true;
                            firstName = b.fornavn;
                            lastName = b.efternavn;
                            
                            tellEveryone((firstName + " (" + data[0] + ")" + ":has connected." + ":" + firstLogin));
                            
                            userAdd(firstName+"("+data[0]+")");
                                  
                        }catch(IllegalArgumentException e){
                            //System.out.println("\nWrong username or password..\n");
                            tellEveryone(("a :"+ "s:" + loginError));
                        }
                        
                        
                    }else{
                        //System.out.println("No connection were met.\n");
                    }
                    
                }
                
            }catch(Exception ex){
                //ex.printStackTrace();
                clientOutputStreams.remove(client);
                //System.out.println("Lost a connection. \n");
            }
            
        }
        
    }
    
    
    public static void main(String[] args) {
        
        java.awt.EventQueue.invokeLater(new Runnable(){
            
            @Override
            public void run(){
                ChatSystem_Server starter = new ChatSystem_Server();
                starter.ServerStart();
                
                //System.out.println("Server started...\n");
            }
        });
                        
    }
    
    public void ServerStart(){
        Thread starter = new Thread(new ServerStart());
        starter.start();
    }
    
    public class ServerStart implements Runnable{
        
        @Override
        public void run(){
            
            clientOutputStreams = new ArrayList();
            users = new ArrayList();
            
            try{
                
                ServerSocket serverSock = new ServerSocket(1108);
                
                while (true){                    
                    Socket clientSock = serverSock.accept();
                    PrintWriter writer = new PrintWriter(clientSock.getOutputStream());
                    clientOutputStreams.add(writer);

                    Thread listener = new Thread(new ClientHandler(clientSock, writer));
                    listener.start();   
                    //System.out.println("Got a connection...\n");
                }
                
            }catch (Exception ex){
                //System.out.println("Error making a connection.\n");
            }
            
        }
        
    }
    
    public void userAdd (String data){
        
        String message, add = ": :Connect", done = "Server: :Done", name = data;
        users.add(name);
        //System.out.println(name + " was added.\n");
        String[] tempList = new String[(users.size())];
        users.toArray(tempList);
        
        for (String token:tempList){            
            message = (token + add);
            tellEveryone(message);            
        }
        tellEveryone(done);        
    }
    
    public void userRemove (String data){
        
        String message, add = ": :Connect", done = "Server: :Done", name = data;
        users.remove(name);
        String[] tempList = new String[(users.size())];
        users.toArray(tempList);
        
        for (String token:tempList){            
            message = (token + add);
            tellEveryone(message);            
        }
        tellEveryone(done); 
        
    }
    
    public void tellEveryone(String message){
        
        Iterator it = clientOutputStreams.iterator();
        
        while (it.hasNext()){
            
            try{
                PrintWriter writer = (PrintWriter) it.next();
		writer.println(message);
                writer.flush();
            }catch (Exception ex){
                //System.out.println("Error telling everyone.\n");
            }
            
        }
        
    }
    
}
