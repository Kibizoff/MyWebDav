/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xmppclient;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import org.xml.sax.SAXException;

/**
 *
 * @author fix
 */
public class XMPPClientGUI extends JFrame {
    private PasswordDialog passDialog;
    private boolean loginSucceed;
    private XMPPClient client;
    private JList rosterList;
    private JLabel rosterLable;
    private JButton addButton;
    private JButton rmButton;
    private JList list;
    
    DefaultListModel listModel;
    /**
     * Creates new form XMPPClientGUI
     */
    //XMPPClient client;
    public XMPPClientGUI() {
        initComponents();
    }
//    public boolean sendMessage(String msg, String receiver) {
//        return true;
//    }
    public XMPPClient getXMPPClient() {
        return client;
    
    }    
    public boolean authenticate(String server, String login, String password, int port) {
        client = new XMPPClient();
        return client.connectAndAuth(server, port, login, password);
    }
    public boolean updRoster() {
        return client.updateRoster();
    }
    public void setLoginSuccess(boolean s) {
        loginSucceed = s;
    }
    public DefaultListModel getListModel() {
        return listModel;
    }
//    public void updRosterList() {
//       listModel.clear();
//       List<RosterItem> rList = client.getRosterArrayList();
//       //System.out.println("CURRENT ROSTER: \n");
//       
//       for (RosterItem r:rList) {
//                //System.out.println(r.toString());
//                listModel.addElement(r.jid);
//            }
//
//        //rosterList.setModel(listModel);
//    }
    
    private void initComponents() {
        
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            loginSucceed = false;
            JFrame mainFrame = new JFrame("XMPP Client");
            rosterLable = new JLabel("Список контактов: ");
            listModel = new DefaultListModel();
            mainFrame.setVisible(false);
            passDialog = new PasswordDialog(this, true); //диалог сделает главное окно видимым после логина
            passDialog.setVisible(true);
            //client.
           
            while(!loginSucceed); //ждем логина
            client.getRecvThread().setListModel(listModel);
            this.setTitle(Integer.toString(client.getSocket().getLocalPort()) +" "+ client.getJID());
            updRoster();
               
            JPanel listPanel = new JPanel();
            JPanel rLablePanel = new JPanel();
            rLablePanel.add(rosterLable);
            list = new JList(listModel);
            //list.addMouseListener(null);
            listPanel.add(new JScrollPane(list));
            addButton = new JButton("Add");
            rmButton = new JButton("Remove");
            //JPanel p2 = new JPanel();
            //p2.add(jbtOk);
            add(rLablePanel,BorderLayout.NORTH);
            add(listPanel,BorderLayout.CENTER);
            JPanel p2 = new JPanel();
            p2.add(addButton);
            p2.add(rmButton);
            add(p2,BorderLayout.SOUTH);
            MouseListener mouseListener = new MouseAdapter() {
                public void mouseClicked(MouseEvent mouseEvent) {
                    JList theList = (JList) mouseEvent.getSource();
                    if (mouseEvent.getClickCount() == 2) {
                        int index = theList.locationToIndex(mouseEvent.getPoint());
                        if (index >= 0) {
                            Object o = theList.getModel().getElementAt(index);
                            SendMessageWindow sndmsgWindow = new SendMessageWindow(o.toString(),client);
                            sndmsgWindow.setVisible(true);
                            //System.out.println("Double-clicked on: " + o.toString());
                        }
                    }
                }
            };
            list.addMouseListener(mouseListener);
            XMPPClientGUI copy = this;
            addButton.addActionListener(new ActionListener()
            {
              public void actionPerformed(ActionEvent e)
              {
                  addContactDialog addDialog = new addContactDialog(copy,true);
                  addDialog.setVisible(true);
                  //updRoster();


              }
            });
            
            rmButton.addActionListener(new ActionListener()
            {
              public void actionPerformed(ActionEvent e)
              {
                  Object o = list.getSelectedValue();
                  if (o!=null) {
                      System.out.println(o.toString());
                      client.removeContact(o.toString());
                      updRoster();
                  }
                }
            });
            this.addWindowListener(new WindowAdapter(){
                    public void windowClosing(WindowEvent e){
                        client.close();
                        System.exit(0);
                    }
                });
            pack();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new XMPPClientGUI().setVisible(true);
   
            }
        });
    }

}
