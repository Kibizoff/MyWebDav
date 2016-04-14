/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xmppclient;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author fix
 */
public class XMPPClient {
    
    /**
     * @param args the command line arguments
     */
    private static Socket connection;
    private List<RosterItem> Roster = new ArrayList<>();
    private List<Message> Messages = new ArrayList<>();
    //private DataInputStream input;
    //public BufferedReader d;
     BufferedWriter to_server;
    private XMLGenerator XMLStr;
    private String jid;
    private msgReceiveThread recvTh; //идентефикатор потока на принятие сообщений
    
  
    public msgReceiveThread getRecvThread() {
        return recvTh;
    }
    public Socket getSocket() {
        return connection;
    }
    
    public void close() {
        try {
            connection.close();
            recvTh.join();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public final boolean sendMessage(String msg, String receiver) {
        try {
            to_server.write(XMLStr.getPresenceString());
            to_server.flush();
            int ind = receiver.indexOf('/');
            String recv ="";
            if (ind != -1)
                recv = receiver.substring(0, ind);
            else recv = receiver;
            //String recv = receiver.substring( 0 , r );
            
            to_server.write(XMLStr.getSendMessageString(jid, recv, "snd1", msg));
            to_server.flush();
          
            //readBuf(d);
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return true;
    }
    public boolean addContact(String c) {
        try {
            to_server.write(XMLStr.getAddContactString(jid, "PC", c, c, ""));
            to_server.flush();
            //readBuf(d);
            to_server.write(XMLStr.getSubscribeString(c));
            to_server.flush();
            
//            readBuf(d);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return true;
    }
    
    public final boolean connectAndAuth(String server, int port, String login, String password) {
                try {
                    
            jid = login+"@"+server;
            connection = new Socket(server, port);
            //System.out.println("PORT:" + Integer.toString(connection.getLocalPort()));
            recvTh= new msgReceiveThread(connection); //создаем поток для принятия сообщений
            recvTh.start();
            //System.out.println("some text");
            //input = new DataInputStream(connection.getInputStream());
            //d = new BufferedReader(new InputStreamReader(input,"UTF-8"));
            to_server = new BufferedWriter(
                    new OutputStreamWriter(connection.getOutputStream(),"UTF-8")
                    );
            XMLStr = new XMLGenerator();
           
            to_server.write(XMLStr.getInitString(server));
            to_server.flush();

            to_server.write(XMLStr.getAuthString(login,password,"PC"));
            to_server.flush();
       
            to_server.write(XMLStr.getPresenceString());
            to_server.flush();
         
            return true;

            

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } 
                return false;
    }
    public List<RosterItem> getRosterArrayList() {
        return Roster;
    }

    public List<Message> getMessagesFrom(String from) {
        if(recvTh.isMessagesPending()) {
            getMessages();
        }
        List<Message> msgFrom = new ArrayList<>();
        Iterator<Message> it = Messages.iterator();
        //msgFrom.clear();
        Message m = new Message();
        while(it.hasNext()) {
            m = it.next();
            if (m.getSender().contains(from)) {  
                msgFrom.add(m);
                it.remove();
            }
        }
        return msgFrom;

    }

    public void removeContact(String s) {
        try {
            to_server.write(XMLStr.getRemoveContactString(jid, "PC", s));
            to_server.flush();
        } catch (IOException ex) {
           ex.printStackTrace();
        }
    }
    public String getJID() {
        return jid;
    }
    private void getMessages() {
        Messages = recvTh.getMessages();
    }
    public boolean updateRoster(){
        try {
            to_server.write(XMLStr.getGetRosterString(jid,"PC")); //отравляем XML запрос списка контактов
            to_server.flush();
            return true;
        } catch (IOException e) {
           e.printStackTrace();
        }
        return false;

    }
    
            

    
}
