/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xmppclient;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 *
 * @author fix
 */
class addContactDialog extends JDialog {

    private final JLabel jlblUsername = new JLabel("Username");
   
    
    //private boolean succed
    
    
    private final JTextField jtfUsername = new JTextField(15);
    
    
    private final JButton jbtAdd = new JButton("Add");
    private final JButton jbtCancel = new JButton("Cancel");

    public addContactDialog() {
        this(null, true);
    }

    public addContactDialog(final XMPPClientGUI parent, boolean modal) {
        super(parent, modal);

        JPanel p3 = new JPanel(new GridLayout(2, 1));
        p3.add(jlblUsername);
       

        JPanel p4 = new JPanel(new GridLayout(2, 1));
        p4.add(jtfUsername);
        jtfUsername.setText("");  
        JPanel p1 = new JPanel();
        p1.add(p3);
        p1.add(p4);

        JPanel p2 = new JPanel();
        p2.add(jbtAdd);
        p2.add(jbtCancel);

        JPanel p5 = new JPanel(new BorderLayout());
        p5.add(p2, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(p1, BorderLayout.CENTER);
        add(p5, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 
//        addWindowListener(new WindowAdapter() {  
//            @Override
//            public void windowClosing(WindowEvent e) {  
//                dispose();  
//            }  
//        });
        

        jbtAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (parent.getXMPPClient().addContact(jtfUsername.getText())) {
                    //if (parent.updRoster()) parent.updRosterList();
                    parent.updRoster();
                    dispose();
                }
            }
        });
        jbtCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
}
