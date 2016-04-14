
package webdav;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;

import java.io.IOException;

import java.io.InputStreamReader;

import java.io.OutputStreamWriter;

import java.io.StringReader;

import java.net.Socket;
import java.net.UnknownHostException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;



import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;




//Основной класс клиента webdav
public class WebDAVClient {

    /**
     * @param args the command line arguments
     */
    private Socket connectionSocket; //сокет
    private BufferedReader fromserver; //ридер данных от сервера
    private BufferedWriter toserver; //писатель данных на сервер
    private String login=""; //логин
    private String password=""; //пароль
    private List<Resource> resourceList; //список ресурсов
    private String server=""; //сервер
    private int port =0; //порт
    //установить логин и пароль
    public void setAuthInfo(String l, String pw) {
        login = l;
        password = pw;
    }
    //геттеры
    public String getLogin() {
        return login;
    }
    public String getPassword() {
        return password;
    }
    public String getServer() {
        return server;
    }
    public int getPort() {
        return port;
    }
    //заверешние клиента - закрытие всего открытого
    public void close() {
        try {
            System.out.println("close called");
            fromserver.close();
            toserver.close();
            connectionSocket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    //инициализация подключения
    public boolean initConnection(String serverAddr, int portN) {
        try {
            server = serverAddr; 
            port = portN;
            connectionSocket = new Socket(serverAddr,portN); //инициализируем сокет
            DataInputStream input = new DataInputStream(connectionSocket.getInputStream()); //получаем входной поток
            
            fromserver = new BufferedReader(new InputStreamReader(input));// инициализируем читателя
            toserver = new BufferedWriter(new OutputStreamWriter(connectionSocket.getOutputStream(),"UTF-8")); //иниц писателя
            resourceList = new ArrayList<Resource>(); //иниц список ресурсов
        } catch (UnknownHostException ex) {
            System.out.println("HOST UNKNOWN");
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    //получить строку для аутентификации
    //используется Basic аутентификация
    //Base64
    public String getAuthString(String login, String password) {
        String authString = login + ":" + password;   
        byte[] authBytes = authString.getBytes(StandardCharsets.UTF_8);
        return DatatypeConverter.printBase64Binary(authBytes);
    }
    public Socket getSocket() {
        return connectionSocket;
    }
    //прочитать из входного потока данных только заголовок
    //тело ответа при этом остается в потоке
    //функция читает побайтово поток и записывает его в строку
    //конец заголовка - наличие в строке подстроки "\r\n\r\n"
    public String getResponseHeader2(){
        DataInputStream in = null;
        String header="";
        try {
            in = new DataInputStream(getSocket().getInputStream());
            byte[] b = new byte[1];
            while(!header.contains("\r\n\r\n")) {
                int count = in.read(b);
                header+= new String(b,0,count);
            }
            return header;
        } catch (IOException ex) {
            ex.printStackTrace();
        } 
        
        return header;
        //return 
    }
//    public String getResponseHeader() {
//        String header="";
//        char c;
//        //fromserver.
//        while(!header.contains("\r\n\r\n")) {
//            try {
//                c=(char)fromserver.read();
//                //fromserver.
//                header+=c;
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//        } //); //этими символами кончается заголовок, и начинаются данные
//        
//        return header;
//
//    }
    
    //получить полный ответ от сервера (заголовок + тело)
    //фукнция читает в строку все что имеется во входном потоке
    public String getResponse() {
        String response = new String("");
        try {
            char c;
            do {
               c=(char)fromserver.read();
               response+=c;
               //System.out.println(t);
            } while(fromserver.ready());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return response;
    }
    
    //отправить запрос на сервер
    //type - типа запроса
    //возможные типы :
    //PROPFIND - получить список файлов
    //GET - скачать
    //DELETE - удалить
    //PUT - залить
    //addr - адрес (список файлов какой директории получать
    //что качать, куда заливать, что удалять
    //host - имя хоста, опционально, ни на что не влияет
    //length - длина файла, используется только для PUT
    //функция генерирует строку запроса и отправляет ее на сервер
    public void sendRequest(String type, String addr, String host,String length){
        try {
            switch (type) {
                case "PROPFIND": 
                    String propfind =    
                        "PROPFIND "+addr+" HTTP/1.1\r\n" +
                        "Host: "+host+"\n" +
                        "User-Agent: simpleclient\n" +
                        "Accept: */*\n" +
                        "Depth: 1\n" +
                        "Authorization: Basic " + getAuthString(login,password) + "\n\n";
                    toserver.write(propfind);
                    toserver.flush();
                    break;
                case "GET":
                    String get = 
                        "GET "+addr+" HTTP/1.1\r\n" +
                        "Host: "+host+"\n" +
                        "Accept: */*\n" +
                        "Authorization: Basic " + getAuthString(login,password) + "\n\n";
                    toserver.write(get);
                    toserver.flush();
                    break;
                case "PUT":
                    String put = 
                        "PUT " +addr+" HTTP/1.1\r\n"+
                        "Host: " + host+"\n" +
                        "Accept: */*\n" +
                        "Expect: 100-continue\n"+
                        "Content-Type: application/octet-stream\n" +
                        "Content-Length: " + length + "\n"+
                        "Authorization: Basic " + getAuthString(login,password)+ "\r\n\r\n";
                    toserver.write(put);
                    toserver.flush();
                    break;
                case "DELETE":
                    String delete = 
                        "DELETE "+addr+" HTTP/1.1\r\n" +
                        "Host: "+host+"\n" +
                        "Accept: */*\n" +
                        "Authorization: Basic " + getAuthString(login,password) + "\n\n";
                       
                    toserver.write(delete);
                    toserver.flush();
                    break;
                default:
                    System.out.println("Unknown request type");

        }
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
    //получить список ресурсов
    public List<Resource> getResourceList() {
        return resourceList;
    }
    //получить XML содержимое ответа от сервера
    //используется для того чтбы распарсить ответ на PROPFIND - список файлов в виде xml
    //Функция просто возвращает подстрку начинающуюся с "<?xml version="
    public String getXMLContent(String response) {
        String xml = "";
        int ind = response.indexOf("<?xml version=");
        if (ind!=-1) {
            xml = response.substring(ind);
        }
        return xml;
    }
    
    //получить из заголовка тип содержимого
    //ищем подстроку которая начинается с "Content-Type: " и берем следующее значение
    public String getContentType(String response) {
        String type="";
        String c = "Content-Type: ";
        int ind=response.indexOf(c);
        if (ind!=-1) {
            ind += c.length();
            String tmp = response.substring(ind);
            int i = tmp.indexOf("\n");
            if (i!=-1) {
                type = tmp.substring(0, i-1);
            }
        }
        return type;  
    }
    //аналогично функции выше, только "Content-Length: " - размер содержимого
    public int getContentLength(String response) {
        String length="";
        String c = "Content-Length: ";
        int ind=response.indexOf(c);
        if (ind!=-1) {
            ind += c.length();
            String tmp = response.substring(ind);
            int i = tmp.indexOf("\n");
            if (i!=-1) {
                length = tmp.substring(0, i-1);
            }
        }
        return Integer.parseInt(length);
    }
    //аналогично функции выше, только "HTTP/1.1 " - код ответа (идем после указанной подстроки)
    public int getResponseCode(String response) {
        String code="1000";
        String c = "HTTP/1.1 ";
        int ind=response.indexOf(c);
        if (ind!=-1) {
            ind += c.length();
            String tmp = response.substring(ind);
            int i = tmp.indexOf(" ");
            if (i!=-1) {
                code = tmp.substring(0, i);
            }
        }
        
        return Integer.parseInt(code);
    }
    
    //фукнция парсинга xml
    public List<Resource> getResourceList(String xmlResponse) {
        
        
        List<Resource> resList = new ArrayList<Resource>();
        
        try {
            
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            
            Document dom = db.parse(new InputSource(new StringReader(xmlResponse))); //создали документ из строки

            Element docEle = dom.getDocumentElement(); //получаем элементы
           //каждый ресурс находится внутри тега <D:response></D:response>
            NodeList nl = docEle.getElementsByTagName("D:response");  //получаем элементы тега
            //цикл по всем элементам, соответствующим ресурсам(файлам и папкам)
            for (int i=0; i< nl.getLength(); i++) {
                Resource res = new Resource(); //создаем новый ресурс
                Element el = (Element)nl.item(i);
                //ссылка на ресурс находится в теге <D:href></D:href>
                NodeList hrefList = el.getElementsByTagName("D:href"); //получаем вершину тега
                if(hrefList != null && hrefList.getLength() > 0) {
                    Element elem = (Element)hrefList.item(0);
                    res.setUrl(elem.getTextContent());  //заполняем поле ссылки
                }
                
                //ниже аналогично, в тегах <D:iscollection></D:iscollection> хранится тип ресурса -
                //папка/файл
                //в соотвествтвии с этим заполняем поле ресурса isCollection методом setType(boolean t)
                NodeList typeList = el.getElementsByTagName("D:iscollection");   
                if(typeList != null && typeList.getLength() > 0) {
                    Element elem = (Element)typeList.item(0);
                    String tVal = elem.getTextContent();
                    if (Integer.parseInt(elem.getTextContent())==1) {
                        res.setType(true);
                    }
                    else res.setType(false);
                }
                //аналогично получаем имя ресурса - тег <D:displayname></D:displayname>
                NodeList nameList = el.getElementsByTagName("D:displayname");
                
                if(nameList != null && nameList.getLength() > 0) {
                    Element elem = (Element)nameList.item(0);
                    res.setName(elem.getTextContent());
                }
                resList.add(res);
            }
    
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return resList;
    }
}

   