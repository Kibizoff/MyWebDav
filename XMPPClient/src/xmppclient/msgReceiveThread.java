/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xmppclient;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author fix
 */
public class msgReceiveThread extends Thread {
    private Socket connection;
    //private String lastStr;
    
    private List<RosterItem> Roster = new ArrayList<>();
    //private volatile boolean IS_ROSTER_UPDATED;
    private volatile boolean IS_MESSAGES_PENDING; //есть ли не прочитанные сообщения
    private volatile List<Message> msgs = new ArrayList<>();
    private boolean isRosterFinished = true; //КОММЕНТ ПРО ЭТУ ПЕРЕМЕННУЮ НИЖЕ
    private volatile DefaultListModel listModel = null;
    private boolean ROSTER_RECEIVED;
    //private final int maxMsgs = 200;
    public boolean isMessagesPending() {
        return IS_MESSAGES_PENDING;
    }
//    public void clearMessages() {
//        msgs.clear();
//    }
    public List<Message> getMessages() {
        IS_MESSAGES_PENDING = false;
        //return msgs;
        
        List<Message> c = new ArrayList<>(); //возвращаем копию, так как исходный очистим
        
        for (Message m:msgs) {
            //System.out.println("getMessage "+m.toString());
            c.add(m);
        }
        msgs.clear();
        
        return c;
    }
    public void setListModel(DefaultListModel lm) {
        listModel = lm;
    }
    public List<RosterItem> getRoster() {
        //IS_ROSTER_UPDATED =false;
        List<RosterItem> rost = new ArrayList<>(); //возвращаем копию, так как исходный очистим
        
        for (RosterItem r:Roster) {
            //System.out.println("getMessage "+m.toString());
            rost.add(r);
        }
        Roster.clear();
        
        return rost;
    }

    public msgReceiveThread(Socket s) {
        connection = s;
       
        //lastStr = new String("");
        //IS_ROSTER_UPDATED = false;
        ROSTER_RECEIVED = false;
    }
    @Override
    public void run()	//Этот метод будет выполнен в новом потоке
    {
            System.out.println("msgrecvthread started");
            parseInput();
 
    }
    public void parseInput() {
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader r = factory.createXMLStreamReader(connection.getInputStream());
            int event = r.getEventType();
            while(true) {
                if (event==XMLStreamConstants.START_ELEMENT) {
                    System.out.println("START: "+r.getName());
                    for (int i=0; i<r.getAttributeCount(); i++) {
                        System.out.println("attr "+Integer.toString(i)+r.getAttributeName(i)+" : "+r.getAttributeValue(i));
                        if (r.getAttributeValue(i).equals("add2")) {
                            ROSTER_RECEIVED = false;
                        }
                        if (r.getAttributeValue(i).equals("roster_1")) {
                            System.out.println("recv roster started");
                           ROSTER_RECEIVED = true;
                           listModel.clear();
                        }
                    }
                }
                if(event==XMLStreamConstants.END_ELEMENT) {
                    System.out.println("END: "+ r.getName());
                }
                
                if (event==XMLStreamConstants.START_ELEMENT && r.getName().toString().equals("{jabber:client}message")) {
                    System.out.println("message reading started");
                    int n = r.getAttributeCount(); //получаем число параметров (кому, от кого, тип)
                    String from = "";
                    String to = "";
                    String type = "";
                    for (int i=0; i<n; i++) {
                        switch(r.getAttributeName(i).toString()) {
                            case "from":
                                from = r.getAttributeValue(i);
                                break;
                            case "to":
                                to = r.getAttributeValue(i);
                                break;
                            case "type":
                                type = r.getAttributeValue(i);
                                break;
                        }
                        
                    }
                    //System.out.println(from + to + type + "DSADSADSA");
                    if (type.equals("chat")) {
                        System.out.println("char equals");
                        do {
                            event=r.next();
                            if (event==XMLStreamConstants.END_ELEMENT) break;
                        } while (event==XMLStreamConstants.START_ELEMENT && r.getName().toString().equals("{jabber:client}body"));
                        String body = r.getText();
                        System.out.println("!!!!BODY: " + body);
                        Message m = new Message(from,to,body);
                        msgs.add(m);
                       
                        IS_MESSAGES_PENDING = true;
                    }
                }
                
                if (event==XMLStreamConstants.START_ELEMENT && r.getName().toString().equals("{jabber:iq:roster}item")) {
                    int n = r.getAttributeCount(); 
                    if (!ROSTER_RECEIVED) {
                        event = r.next();
                        continue;
                    }
                    if (n > 1) {
                        
                        RosterItem rost = new RosterItem();
                        boolean needSub = false;
                        boolean needSubTo = false;
                        for (int i =0; i<n; i++) {
                            if (r.getAttributeName(i).toString().equals("jid")) {
                                System.out.println("__________________________");
                                rost.jid = r.getAttributeValue(i);
                                rost.name = r.getAttributeValue(i); }
                            else if (r.getAttributeName(i).toString().equals("subscription")) {
                                rost.sub = r.getAttributeValue(i);
                                if (rost.sub.equals("from")) {
                                    needSubTo = true;
                                    
                                
                                }
                            }
                            else if (r.getAttributeName(i).toString().equals("ask") && r.getAttributeValue(i).equals("subscribe") ) {
                                needSub = true;
                               
                                
                            }
                            
                        }
                        isRosterFinished = false; //начато заполнение ростера
                        if (listModel!=null) {
                            listModel.addElement(rost.jid);
                        }
                        if (needSub) {
                            BufferedWriter to_server = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(),"UTF-8"));
                            to_server.write("<presence to='"+rost.jid+"' type='subscribed'/>");
                            System.out.println("SUB APPROVE TO " + rost.jid +" " +"SENDED");
                            to_server.flush();
                            
                        }
                        else if (needSubTo) {
                            BufferedWriter to_server = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(),"UTF-8"));
                            to_server.write("<presence to='"+rost.jid+"' type='subscribe'/>");
                            System.out.println("SUB REQUEST TO " + rost.jid +" " +"SENDED");
                            to_server.flush();
                        }
                    }
                    else {
                        event=r.next();
                        continue;
                    }
//                    
                }
                if (event==XMLStreamConstants.START_ELEMENT && r.getName().toString().equals("{jabber:client}presence")) {
                    
                    int n = r.getAttributeCount();
                    
                    if (n==3 && r.getAttributeValue(2).equals("subscribe")) {
                        System.out.println("subscribe event found:" + Integer.toString(n));
                        BufferedWriter to_server = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(),"UTF-8"));
                        to_server.write("<presence to='"+r.getAttributeValue(0)+"' type='subscribed'/>");
                        to_server.flush();
                    } 
                }
                if (event==XMLStreamConstants.END_ELEMENT && r.getName().toString().equals("{jabber:iq:roster}query")) {
                    //с таким завершающим элементов может прийти не только сообщение о конце ростера
                    //поэтому нужно ввести переменную isRosterFinished, чтобы не изменять статус обновления ростера
                    //
                    //for (RosterItem ri:Roster) {
                    //     System.out.println(ri.toString());
                    //}
                    if (!isRosterFinished) { //если заполнение ростера начато
                        isRosterFinished = true;
                        ROSTER_RECEIVED = false;
                        //Roster.clear();
                    }
                                 
                }
               //while(!r.hasNext());
                //System.out.println("before");
                event = r.next(); //будет висеть здесь и ждать новых данных
                //System.out.println("after");
                //r.hasNe
            }
        } catch (XMLStreamException e) {
           //e.printStackTrace();
           return;
       
        } catch (IOException e) {
           System.out.println("socket closed");
           return;
        }
        //;

    }
}
