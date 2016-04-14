
package webdav;

//класс описывающий удаленный ресурс (файл/папка)
//url - ссылка
//isCollection - true если папка, False - файл
//name - имя ресурса(файла/папки)
public class Resource {
    private String url;
    private boolean isCollection;
    private String name;
    //2 конструктора
    public Resource() {
        url ="";
        isCollection=false;
    }
    
    public Resource(String n,String u, boolean i) {
        url = u;
        isCollection = i;
        name = n;
    }
    //различные геттеры
    public String getUrl() {
        return url;
    }
    public boolean getType() {
        return isCollection;
    }
    public String getName() {
        return name;
    }
    //сеттеры
    public void setUrl(String u) {
        url = u;
    }
    public void setType(boolean i) {
        isCollection = i;
    }
    public void setName(String n) {
        name = n;
    }
    //переопределение toString()
    //будет использоваться при построении дерева
    @Override
    public String toString() {
        return name;
       
    }
    
}
