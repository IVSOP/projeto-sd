package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


//Client to server authentication and register message
public class CtSRegMsg implements CtSMsg {
    private static final byte opcode = 0; // value to distinguish message server side
    private String name;
    private String password;

    public CtSRegMsg() {
        // n faz nada, preencher com setters
    }

    public CtSRegMsg(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public void serialize(DataOutputStream dos) throws IOException{
        dos.writeByte(opcode);
        
        dos.writeUTF(this.name);
        dos.writeUTF(this.password);
        dos.flush();
    }

    //deserialize assumes opcode was previously read, only uses information after opcode
    public void deserialize(DataInputStream dis) throws IOException {
        this.setName(dis.readUTF());
        this.setPassword(dis.readUTF());
    }

    public String getName() {
        return this.name;
    }

    public String getPassword() {
        return this.password;
    }

    public int getRequestN() {
        return -1;
    }

    private void setName(String name) {
        this.name = name;
    }

    private void setPassword(String pass) {
        this.password = pass;
    }

    public void setRequestN(int val) {} // só está aqui pela interface

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("name: " + this.name);
        sb.append(" pass: " + this.password);
        return sb.toString();
    }

}