package com.javarush.test.level30.lesson15.big01;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Михаил on 22.10.2016.
 */
public class Server
{
    private static Map<String, Connection> connectionMap=new ConcurrentHashMap<>();

    public static void main(String[] args)
    {
        ConsoleHelper.writeMessage("Port name");
        try(ServerSocket serverSocket= new ServerSocket(ConsoleHelper.readInt())){


        ConsoleHelper.writeMessage("Server run");
        while (true)
        {
            Socket socket = serverSocket.accept();
            Handler handler = new Handler(socket);
            handler.start();
        }
    }
        catch(IOException e){
            ConsoleHelper.writeMessage("Error");
        }
    }
    private static class Handler extends Thread
    {
        private Socket socket;
        Handler(Socket socket){
            this.socket=socket;
        }

        private String serverHandshake(Connection connection) throws IOException,
                ClassNotFoundException
        {
            while (true)
            {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message m = connection.receive();
                if (m.getType().equals(MessageType.USER_NAME))
                {
                    if (!m.getData().isEmpty() & !connectionMap.containsKey(m.getData()))
                    {
                        connectionMap.put(m.getData(), connection);
                        connection.send(new Message(MessageType.NAME_ACCEPTED));
                        return m.getData();
                    }
                }
            }
        }

        private void sendListOfUsers(Connection connection, String userName) throws
                IOException{

            for(String key : connectionMap.keySet()){
                if(!key.equals(userName)){
                    connection.send(new Message(MessageType.USER_ADDED,key));
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws
                IOException, ClassNotFoundException{
        while(true)
        {
            Message m = connection.receive();
            if (m.getType().equals(MessageType.TEXT))
            {
                sendBroadcastMessage(new Message(MessageType.TEXT, userName + ": " + m.getData()));
            } else
            {
                ConsoleHelper.writeMessage("Dont sent message");
            }
        }
        }

        public void run()
        {
            ConsoleHelper.writeMessage("New connection " + socket.getRemoteSocketAddress());
            String nameUser=null;
               try( Connection connection = new Connection(socket);){
                nameUser =serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED,nameUser));
                sendListOfUsers(connection,nameUser);
                serverMainLoop(connection,nameUser);
            }catch (IOException e){
                ConsoleHelper.writeMessage("Error"+socket.getRemoteSocketAddress());
            }
               catch (ClassNotFoundException e){
                ConsoleHelper.writeMessage("Error"+socket.getRemoteSocketAddress());
            }
            finally
               {
                    if(nameUser!=null){
                        connectionMap.remove(nameUser);
                        sendBroadcastMessage(new Message(MessageType.USER_REMOVED,nameUser));
                    }
                   ConsoleHelper.writeMessage("End working"+socket.getRemoteSocketAddress());
               }

        }
    }

    public static void sendBroadcastMessage(Message message){
        try{
            for(Map.Entry<String, Connection> map:connectionMap.entrySet()){
                map.getValue().send(message);
            }
        }
        catch (IOException e){
            ConsoleHelper.writeMessage("Don`t sent message");
        }
    }
}
