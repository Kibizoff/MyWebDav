/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xmppclient;

/**
 *
 * @author fix
 */
public class Message {
    private String from;
    private String to;
    private String body;
    public Message() {
        from="";
        to="";
        body=""; 
    }
    public String getSender() {
        return from;
    }
    public String getBody() {
        return body;
    }
    public Message(String f, String t, String b) {
        from = f;
        to = t;
        body = b;
        //isRead=false;
    }

//    @Override
//    public String toString() {
//        return ("_______________\n from: "+from+" to: "+to+"\nmsg: " +body + "\n________\n");
//    }
}
