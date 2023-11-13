package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


//Client to server authentication and register message
public class CtSAutMsg {
    private String name;
    private String password;

    public CtSAutMsg() {
        // n faz nada, preencher com setters
    }

    public CtSAutMsg(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public void serialize(DataOutputStream dos) throws IOException{
        byte[] data = this.name.getBytes("UTF-8");
        dos.writeInt(this.name.length());
        dos.write(data);
        data = this.password.getBytes("UTF-8");
        dos.writeInt(this.password.length());
        dos.write(data); 
        dos.flush();
    }

    //deserialize assumes opcode was previously read, only uses information after opcode
    public void deserialize(DataInputStream dis) throws IOException {
        byte[] data = new byte[dis.readInt()];
        dis.readFully(data);
        this.setName(new String(data,"UTF-8"));
        data = new byte[dis.readInt()];
        dis.readFully(data);
        this.setPassword(new String(data,"UTF-8"));
    }

    public String getName() {
        return this.name;
    }

    public String getPassword() {
        return this.password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String pass) {
        this.password = pass;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("name: " + this.name);
        sb.append(" pass: " + this.password);
        return sb.toString();
    }

}