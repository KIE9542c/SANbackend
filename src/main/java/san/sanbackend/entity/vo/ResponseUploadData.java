package san.sanbackend.entity.vo;

public class ResponseUploadData {
    private String url;
    private String name;

    public ResponseUploadData(){
        this(null,null);
    }
    public ResponseUploadData(String name){
        this(null,name);
    }

    public ResponseUploadData(String url, String name){
        this.url = url;
        this.name = name;
    }

    public ResponseUploadData setUrl(String url){
        this.url = url;
        return this;
    }
    public String getUrl(){
        return this.url;
    }

    public ResponseUploadData setName(String name){
        this.name = name;
        return this;
    }
    public String getName(){
        return this.name;
    }

    @Override
    public String toString() {
        return "ResponseUploadData{" +
                "url=" + url +
                ", name='" + name + '\'' +
                '}';
    }
}
