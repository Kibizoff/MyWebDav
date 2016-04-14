/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webdav;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JButton;
import javax.swing.JProgressBar;

//поток загрузок
//тип - загрузки или заливки - определяется по исопльзуемому конструктору и хранится в поле type
//в себе хранит прогрессБар для того чтобы возможно было его динамически изменять
//клиент, текущий ресурс, пути и файл
public class LoadThread extends Thread {
    private JProgressBar downloadProgressBar;
//    private volatile int bytesRead = 0;
//    private volatile int len = 0;
    private WebDAVClient wdclient;
    private Resource resource;
    private String path;
    private File file;
    private String threadType;
    JButton openButton;
    public LoadThread() {
       wdclient = new WebDAVClient();
       resource = new Resource();
       path = new String();
       threadType = new String();
    }
    //КОНСТРУКТОР ДЛЯ СКАЧИВАНИЯ
    //path - куда скачивать
    //кнопка - openButton - после загрузки становится активной
    public LoadThread(WebDAVClient w, Resource r, String f, JProgressBar bar, JButton b) {
        wdclient = w;
        resource = r;
        path = f;
        downloadProgressBar = bar;
        openButton = b;
        threadType = "download";
        openButton.setVisible(true);
    }
    //КОНСТРУКТОР ДЛЯ ЗАЛИВКИ
    //в этом случае path - url куда заливать
    public LoadThread(WebDAVClient w, File fN, String f, JProgressBar bar) {
        wdclient = w;
        path = f;
        downloadProgressBar = bar;
        file = fN;
        threadType = "upload";
        
    }
    
    //функция загрузки
    public void upload() {
        FileInputStream fis = null; //создаем поток загрузки файла
        try {
            fis = new FileInputStream(file); //инициализурем поток
            String fN=  file.getName(); //получаем имя файла
            
            String fName = fN.replaceAll(" ", "%20"); //убираем из имени пробелы
            int len = (int) file.length(); // получаем размер файла
            System.out.println(len);
            wdclient.sendRequest("PUT", path+fName, "localhost", Integer.toString(len)); //отправляем запрос PUT
            System.out.println("sending put req: " +path +fName );
            String str = wdclient.getResponseHeader2();
            System.out.println(str);
            DataOutputStream out = new DataOutputStream(wdclient.getSocket().getOutputStream()); //создаем поток записи
            byte[] b = new byte[2048]; //массив байт
            int count=0; //прочитано байт 
            int bytesWritten = 0; //всего отправлено байт
            if (wdclient.getResponseCode(str) ==100) { //если после отправки заголовка пришел код 100 то можн отправлять содержимое файла
                //цикл пока входной файл не прочитан
                while(fis.available()!=0) {
                    count = fis.read(b); //читаем в b из файла
                    out.write(b); //пишем на сервер
                    bytesWritten+= count; //прочитано байт
                    int perc = (int) (bytesWritten*100.0/len); //процент готовоности
                    //обновляем прогресс бар
                    downloadProgressBar.setValue(perc);
                    downloadProgressBar.setString( (int)(bytesWritten/1024) + "/" + (int)(len/1024)+"KB" + " ("+  perc + "%)");
                    
                }
                if(wdclient.getResponseCode(wdclient.getResponseHeader2()) == 201) {
                    //если получили после отправки заголовок с кодом 201 - успех
                    downloadProgressBar.setString("Ready");
                    System.out.println("Upload " + fName + " =SUCCESS");
                }
            }
            else {
                System.out.println("Uploading Error: "+Integer.toString(wdclient.getResponseCode(str)));
                wdclient.close();
            }
            wdclient.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } 
    }
    
    //аналогично функция скачивания
    public void download() {
        try {
            wdclient.sendRequest("GET",resource.getUrl(),"localhost",""); //отправляем запрос на скачивание
            String str = wdclient.getResponseHeader2(); // читаем заголовок
            int len = wdclient.getContentLength(str); //получаем из заголовка размер
            int code = wdclient.getResponseCode(str);
            if (code!=200) {
                System.out.println("GET ERROR");
                wdclient.close();
            }
            System.out.println(str);
            
            //создаем вход/выход потоки
            DataInputStream in = new DataInputStream(wdclient.getSocket().getInputStream());
            OutputStream dos = new FileOutputStream(path+resource.getName());
            int count;
            byte[] buffer = new byte[2048];
            int bytesRead = 0;
            //цикл пока не прочитали все, либо ошибка чтения
            while (bytesRead!=len && (count = in.read(buffer)) != -1)
            {
                dos.write(buffer, 0, count); //пишем в файл
                dos.flush();
                bytesRead+=count; //скачано байт
                //обновляем прогрессбар
                downloadProgressBar.setValue((int) (bytesRead*100.0/len));
                //System.out.println(Integer.toString(bytesRead)+"/"+Integer.toString(len));
            }   
            System.out.println("download finished");
            openButton.setEnabled(true);
            in.close();
            dos.close();
            wdclient.close();
            System.out.println("download client finished");
        } catch (IOException ex) {
            System.out.println("Socket error");
        }
    }
    //запуск потока
    //в зависимости от типа вызывается нужная функция
    @Override
    public void run() {
        if (threadType.equals("download")) {
            System.out.println("download thread started");
            download();
            System.out.println("download thread finished");
        }
        else if (threadType.equals("upload")) {
            System.out.println("upload thread started");
            upload();
            System.out.println("upload thread finished");
        }
    }
    
}
