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
public class XMLGenerator {
    
    public String getInitString(String server) {
        return "<?xml version='1.0' encoding='UTF-8'?><stream:stream to='"+server+"' xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams'>";
    }
//    public String getAuthReqString(String username) {
//        return "<iq type='get' id='auth1'><query xmlns='jabber:iq:auth'><username>"+username+"</username></query></iq>";
//    }
    public String getAuthString(String username, String password, String resource) {
        return "<iq type='set' id='auth2'><query xmlns='jabber:iq:auth'><username>"+username +"</username> <password>"+password+"</password><resource>"+resource+"</resource></query></iq>";
    }
    public String getPresenceString() {
        return "<presence><show></show></presence>";
    }
    public String getSendMessageString(String from,String to,String id,String msg) {
        return "<message type='chat' to='"+to+"' id='"+id+"' from='"+from+"'><body>"+msg+"</body></message>";
    }
    public String getGetRosterString(String from,String res) {
        return "<iq from='"+from+"/"+res+"' type='get' id='roster_1'><query xmlns='jabber:iq:roster'/></iq> ";
    }
    public String getAddContactString(String from, String res, String name, String jid, String group ) {
        //return "<iq from='"+from+"/"+res+" 'type='set' id='add1'><query xmlns='jabber:iq:roster'><item jid='"+jid+"' name='"+name+"'><group>"+group+"</group></item></query></iq>";
        return "<iq type='set' id='add2'><query xmlns='jabber:iq:roster'><item jid='"+jid+"' name='"+name+"' subscription='none'><group>"+group+"</group></item></query></iq><presence to='"+jid+"' type='subscribe'/>" ;
    }

    public String getRemoveContactString(String from, String res, String rmC) {
        return "<iq from='"+from+"/"+res+"' type='set' id='roster_4'><query xmlns='jabber:iq:roster'><item jid='"+rmC+"' subscription='remove'/></query></iq>";
        
    }
    public String getSubscribeString(String to) {
        return "<presence to='"+to+"' type='subscribe'/>";
    }
}
