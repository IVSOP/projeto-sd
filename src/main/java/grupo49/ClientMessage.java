package grupo49;

//Wrapper for different message types client sends
//"T extends IMessage" improves type-safety and doesn´t need explicit casting

public class ClientMessage<T extends IMessage> {
    private String client; //something to identify client;
    private T message; // client message: CtSAut, CtSExec or CtSStatus

    ClientMessage(String clientInfo, T msg) {
        this.client = clientInfo;
        this.message = msg;
    }

    public String getClient() {
        return this.client;
    }

    public T getMessage() {
        return this.message;
    }

    public void setClient(String info) {
        this.client = info;
    }

    public void setMessage(T msg) {
        this.message = msg;
    }
}