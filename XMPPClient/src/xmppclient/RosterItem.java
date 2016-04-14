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
public class RosterItem {
    String name;
    String jid;
    String sub;
    String group; //non used
    public RosterItem(String n,String j, String s, String g) {
        name = n;
        jid = j;
        sub = s;
        group = g; //non used
    }
    public RosterItem() {
        this("","","","");
    }
    @Override
    public String toString() {
        return ("SUBSCRIPTION: "+sub+" NAME: "+name+" JID: "+jid);
  }
}
