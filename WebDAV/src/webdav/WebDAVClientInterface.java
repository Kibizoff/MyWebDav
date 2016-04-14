/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webdav;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

//класс реализовывающий интерфейс клиента

public class WebDAVClientInterface extends javax.swing.JFrame {
    private DefaultTreeModel resourceTreeModel; //модель дерева файлов и папок
    private final WebDAVClient wdclient = new WebDAVClient(); //клиент
    DefaultMutableTreeNode rootNode; //корень дерева
   
    //инициализация дерева ресурсов
    //функция из списка ресурсов заполняет дочерние элементы корня
    public void initResourceTree(List<Resource> newList) {
        if (newList.isEmpty()) return;
        rootNode = new DefaultMutableTreeNode(newList.get(0)); //корень - 0 элемент списка
        resourceTreeModel = new DefaultTreeModel(rootNode,true); //инициализруем модель
        //второй параметр - askAllowChildren - при true, вершинам можно задать тип - файл или папка
        //файл не может иметь потомков
        
        //цикл по всем элементам списка
        for (int i=1; i<newList.size(); i++) {
            Resource r = newList.get(i);
            DefaultMutableTreeNode chld = new DefaultMutableTreeNode(r,r.getType()); //создаем дочернюю вершину
            rootNode.add(chld); //добавляем ее к корню
            }
        resourceTreeModel.reload(rootNode); //обновляем дерево
        
        
    }
    //фукнция обновляет вершину node добавляя потомков
    public void updateNode(DefaultMutableTreeNode node) {
        Resource r = (Resource) node.getUserObject(); //получили ресурс, ассоциированный с вершиной
        wdclient.sendRequest("PROPFIND", r.getUrl(), "localhost",""); //отправили запрос PROPFIND с указанием url'а вершины
        //System.out.println(r.getUrl());
        int code = wdclient.getResponseCode(wdclient.getResponseHeader2()); //получаем код запроса
        if (code == 207) { //успех
            System.out.println("PROPFIND = OK");
            addChildNodes(node,wdclient.getResourceList(wdclient.getXMLContent(wdclient.getResponse()))); //добавляем дочерние вершины
        }
        //System.out.println(responce);
        //List<Resource> rList = wdclient.getResourceList(wdclient.getXMLContent(responce));
        
    }
    
    //функция добавления дочерних вершин к вершине
    //для всех ресурсов в цикле создается вершина и добавляется к родительской
    public void addChildNodes(DefaultMutableTreeNode node, List<Resource> resList) {
        if (resList.isEmpty()) return;
        node.removeAllChildren(); //сначала нужно удалить всех потомков
        for (int i=1; i<resList.size(); i++) {
            Resource r = resList.get(i);
            DefaultMutableTreeNode chld = new DefaultMutableTreeNode(r,r.getType());
            node.add(chld);
            System.out.println(r.getUrl());
        }
        resourceTreeModel.reload(node); //обновляем модель
        this.repaint();
        
    }
    
    
    //геттер
    public WebDAVClient getWebDAVClient() {
        return wdclient;
    }
    //конструктор интерфейса
    public WebDAVClientInterface() {
        super("webdav client");
        //инициализируем клиент
        if (wdclient.initConnection("localhost", 80)) {
                System.out.println("main client started, port: " + Integer.toString(wdclient.getSocket().getLocalPort()));
                wdclient.setAuthInfo("webdavuser", "qwerty"); //задаем логин пароль
                wdclient.sendRequest("PROPFIND", "/", "localhost",""); //отправляем первый запрос для получения списка файлов
                String responce = wdclient.getResponse(); //получаем ответ
                
                int code = wdclient.getResponseCode(responce); //получаем код ответа
                if (code != 207) { //207 - PROPFIND успешен
                    System.out.println("connection failed");
                    wdclient.close();
                    System.exit(0);
                }
                
                List<Resource> res = wdclient.getResourceList(wdclient.getXMLContent(responce)); //получаем список ресурсов
                for (Resource r:res) {
                    System.out.println(r.toString()); 
                }
                initResourceTree(res); //инициализируем дерево ресурсов
                
                initComponents(); //инициализуруем компоненты фрейма
                //эта функция была сгенерирована редактором форм swing awt
                
                //добавляем действие при раскрытии ветви дерева - получение списка файлов (потомков)
                //обработчик вызывает фукнцию обновления вершины
                resourceTree.addTreeExpansionListener(new TreeExpansionListener() {
                    public void treeExpanded(TreeExpansionEvent event) {
                        TreePath path = event.getPath();
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();  
                        updateNode(node);
                    }
                    public void treeCollapsed(TreeExpansionEvent event) {
                        //ничего не делаем при сворачивании ветви
                    }
                });
                //обработчик кнопки загрузки
                //обработчик запоминает выбранную вершину и создает окно загрузки
                //
                downloadButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt)  {                                               
                            //получаем выбранную вершину
                            DefaultMutableTreeNode node = (DefaultMutableTreeNode)resourceTree.getLastSelectedPathComponent();
                            if (node == null) {
                                node = rootNode; //если ничего не выбрано то корень
                            }
                            Resource r = (Resource) node.getUserObject(); //получаем ассоциированный с вершиной ресурс (файл или каталог)
                            System.out.println(r.getUrl());
                            if (r.getType()) return; //если выбрали коллекцию то скачать ничего нельзя
                            LoadWindow downloadWindow = new LoadWindow(
                                    wdclient.getServer(),wdclient.getPort(),
                                    wdclient.getLogin(),wdclient.getPassword()); //создаем окно загрузок
                            downloadWindow.setVisible(true); //показываем его
                            downloadWindow.downloadResource(r, ""); //вызываем фукнцию загрузки ресурса

                    }
                       
                });
                
                //обработчик кнопки заливки
                //работает примерно аналогично предыдущему
                //при нажатии кнопки получаем текущий каталог
                //после чего создается окно выбора файлов, создается окно загрузок и 
                //файл отправляется в текущий каталог
                uploadButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt)  {                                               
                        
                            DefaultMutableTreeNode node = (DefaultMutableTreeNode)resourceTree.getLastSelectedPathComponent();
                             if (node == null) {
                                node = rootNode;
                            }
                            if (!node.getAllowsChildren() && node.getParent()!=null) {
                                System.out.println("isLeaf");
                                node = (DefaultMutableTreeNode)node.getParent();
                            }
                            //выше выбираем вершину, при этом если бы выбран файл, то получаем родительскую (каталог)
                            //это нужно для того чтобы выбрать в какой каталог заливать
                            Resource r = (Resource) node.getUserObject();
                            System.out.println(r.getUrl());
                            JFileChooser chooser = new JFileChooser(); //создаем окно выбора файла
                            chooser.setDialogTitle("Upload");
                            int returnVal = chooser.showOpenDialog((Component) evt.getSource());

                            if(returnVal == JFileChooser.APPROVE_OPTION) {
                                
                                    File f = chooser.getSelectedFile(); //выбрали файл
                                    //создаем окно загрузко
                                    LoadWindow downloadWindow = new LoadWindow(
                                            wdclient.getServer(),wdclient.getPort(),
                                            wdclient.getLogin(),wdclient.getPassword());
                                    downloadWindow.setVisible(true);
                                    downloadWindow.uploadFile(f, r.getUrl()); //вызываем функцию заливки
  
                            }

                    }
                       
                });
                
                //обработчик кнопки delete
                //посылает запрос DELETE с указанием выбранного ресурса
                deleteButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt)  {                                               
                       
                            DefaultMutableTreeNode node = (DefaultMutableTreeNode)resourceTree.getLastSelectedPathComponent();
                            Resource r = (Resource) node.getUserObject();
                            wdclient.sendRequest("DELETE", r.getUrl(), "localhost", ""); //запрос
                            if (wdclient.getResponseCode(wdclient.getResponseHeader2()) == 200) { //проверяем результат (успех - код 200)
                                System.out.println("DELETE SUCCESS");
                                DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
                                updateNode(parent); //обновляем родительскую вершину
                            }

                    }
                       
                });
                
                //обработчик закрытия окна.
                //закрывает клиент
                this.addWindowListener(new WindowAdapter(){
                    public void windowClosing(WindowEvent e){
                        System.out.println("closing main window");
                        wdclient.close();
                        System.exit(0);
                    }
                });
         
        } 

        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        resourceTree = new javax.swing.JTree();
        downloadButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        uploadButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        resourceTree.setModel(resourceTreeModel);
        jScrollPane1.setViewportView(resourceTree);

        downloadButton.setText("download");

        deleteButton.setText("delete");

        uploadButton.setText("upload");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(deleteButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(uploadButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(downloadButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(downloadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(deleteButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(uploadButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(WebDAVClientInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(WebDAVClientInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(WebDAVClientInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(WebDAVClientInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new WebDAVClientInterface().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton downloadButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree resourceTree;
    private javax.swing.JButton uploadButton;
    // End of variables declaration//GEN-END:variables
}
