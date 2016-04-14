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
class PasswordDialog extends JDialog {

    private final JLabel jlblUsername = new JLabel("Username");
    private final JLabel jlblPassword = new JLabel("Password");
    private final JLabel jlblServer = new JLabel("Server");
    private final JLabel jlblPort = new JLabel("Port");
    
    //private boolean succed
    
  
    private final JTextField jtfUsername = new JTextField(15);
    private final JTextField jtfPassword = new JTextField();
    private final JTextField jtfServer = new JTextField(15);
    private final JTextField jtfPort = new JTextField(15);
    
    private final JButton jbtOk = new JButton("Login");
    private final JButton jbtCancel = new JButton("Cancel");

    private final JLabel jlblStatus = new JLabel(" ");

    public PasswordDialog() {
        this(null, true);
    }

    public PasswordDialog(final XMPPClientGUI parent, boolean modal) {
        super(parent, modal);

        JPanel p3 = new JPanel(new GridLayout(4, 1));
        p3.add(jlblUsername);
        p3.add(jlblPassword);
        p3.add(jlblServer);
        p3.add(jlblPort);

        JPanel p4 = new JPanel(new GridLayout(4, 1));
        p4.add(jtfUsername);
        p4.add(jtfPassword);
        p4.add(jtfServer);
        p4.add(jtfPort);
        
        JPanel p1 = new JPanel();
        p1.add(p3);
        p1.add(p4);

        JPanel p2 = new JPanel();
        p2.add(jbtOk);
        p2.add(jbtCancel);

        JPanel p5 = new JPanel(new BorderLayout());
        p5.add(p2, BorderLayout.CENTER);
        p5.add(jlblStatus, BorderLayout.NORTH);
        jlblStatus.setForeground(Color.RED);
        jlblStatus.setHorizontalAlignment(SwingConstants.CENTER);

        setLayout(new BorderLayout());
        add(p1, BorderLayout.CENTER);
        add(p5, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        jtfUsername.setText("javatestab2");
        jtfPassword.setText("qwertyui");
        jtfServer.setText("jabber.ru");
        jtfPort.setText("5222");
        addWindowListener(new WindowAdapter() {  
            @Override
            public void windowClosing(WindowEvent e) {  
                System.exit(0);  
            }  
        });
        

        jbtOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (parent.authenticate(jtfServer.getText(),  jtfUsername.getText(), jtfPassword.getText(), Integer.parseInt(jtfPort.getText()))) {
                    parent.setVisible(true);
                    parent.setLoginSuccess(true);   
                    //setVisible(false);
                    dispose();
                    
                    
                } else {
                    jlblStatus.setText("Invalid username or password");
                }
            }
        });
        jbtCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                parent.dispose();
                System.exit(0);
            }
        });
    }
}