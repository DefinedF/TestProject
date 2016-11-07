package com.javarush.test.level30.lesson15.big01.client;

import com.javarush.test.level30.lesson15.big01.Connection;
import com.javarush.test.level30.lesson15.big01.ConsoleHelper;
import com.javarush.test.level30.lesson15.big01.Message;
import com.javarush.test.level30.lesson15.big01.MessageType;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Михаил on 22.10.2016.
 */
public class Client
{
    protected Connection connection;
    private volatile boolean clientConnected;

    protected String getServerAddress(){
        ConsoleHelper.writeMessage("Give me server IP");
        return ConsoleHelper.readString();
    }
    protected int getServerPort(){
        ConsoleHelper.writeMessage("Port");
        return ConsoleHelper.readInt();
    }
    protected String getUserName(){
        ConsoleHelper.writeMessage("Enter your name");
        return ConsoleHelper.readString();
    }
    protected boolean shouldSentTextFromConsole(){
        return true;
    }

    protected SocketThread getSocketThread(){
        return new SocketThread();
    }

    protected void sendTextMessage(String text){
        try
        {
            connection.send(new Message(MessageType.TEXT, text));
        }
        catch (IOException e){
            ConsoleHelper.writeMessage("Error");
            clientConnected=false;
        }
    }

    public void run(){
        SocketThread socketThread=getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        try{
            synchronized (this){
        wait();}

        }catch (InterruptedException ignored){
            ConsoleHelper.writeMessage("Error");
        }
        if(clientConnected){
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");

        }
        else ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        while (clientConnected)
        {
            String s;
            if (!(s = ConsoleHelper.readString()).equals("exit"))
            {
                if (shouldSentTextFromConsole())
                {
                    sendTextMessage(s);
                }
            } else
            {
                return;
            }
        }

    }
    public static void main(String[] args){
        Client client = new Client();
        client.run();
    }
    public class SocketThread extends Thread
    {
        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage(userName+" - connected");
        }
        protected void informAboutDeletingNewUser(String userName){
            ConsoleHelper.writeMessage(userName+" - disconected");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected){
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();

            }
        }

        protected void clientHandshake() throws IOException,
                ClassNotFoundException{
            while(true){
                Message m=connection.receive();
                if(m.getType().equals(MessageType.NAME_REQUEST)){
                    String userName = getUserName();
                    connection.send(new Message(MessageType.USER_NAME,userName));
                }
                else if(m.getType().equals(MessageType.NAME_ACCEPTED)){
                    notifyConnectionStatusChanged(true);
                    break;
                }
                else throw new IOException("Unexpected MessageType");

            }
        }

        protected void clientMainLoop() throws IOException,
                ClassNotFoundException{
            while(true)
            {
                Message txt = connection.receive();
                if (txt.getType().equals(MessageType.TEXT))
                {
                    processIncomingMessage(txt.getData());
                } else if (txt.getType().equals(MessageType.USER_ADDED))
                {
                    informAboutAddingNewUser(txt.getData());
                } else if (txt.getType().equals(MessageType.USER_REMOVED))
                {
                    informAboutDeletingNewUser(txt.getData());
                } else throw new IOException("Unexpected MessageType");
            }
        }

        public void run(){

                String adr = getServerAddress();
                int port = getServerPort();
            try{
                Socket socket = new Socket(adr, port);
                connection=new Connection(socket);
                clientHandshake();
                clientMainLoop();
            }catch (UnknownHostException e){}
            catch (IOException e){
                notifyConnectionStatusChanged(false);
            }
            catch (ClassNotFoundException e){
                notifyConnectionStatusChanged(false);
            }
        }
    }
}
