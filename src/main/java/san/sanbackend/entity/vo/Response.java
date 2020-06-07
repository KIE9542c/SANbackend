package san.sanbackend.entity.vo;

public class Response {
    private Integer code;
    private String msg;
    private Object data;

    public Response() {

    }

    public Response(Integer code){
        this(code, null);
    }

    public Response(Integer code, String msg){
        this(code,msg,null);
    }
    public Response(Integer code, String msg, Object data){
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Integer getCode(){
        return code;
    }

    public Response setCode(Integer code){
        this.code = code;
        return this;
    }

    public String getMsg(){
        return msg;
    }

    public Response setMsg(String msg){
        this.msg = msg;
        return this;
    }

    public Object getData(){
        return data;
    }

    public Response setData(Object data){
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return "Response{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}


