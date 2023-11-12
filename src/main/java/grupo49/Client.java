package grupo49;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import grupo49.CtSAutMsg;
import grupo49.CtSExecMsg;
import grupo49.StCStatusMsg;

/**
 * Hello world!
 *
 */
public class Client 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World client!" );
        try {
            Socket socket = new Socket("localhost", 12345); // o host é conhecido do lado do cliente, neste caso o host do servidor é "localhost"

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            //testar
            // CtSAutMsg testeMsg = new CtSAutMsg("Nestor","pastor");
            // testeMsg.serialize(out);
            CtSExecMsg testeMsg2 = new CtSExecMsg("É o nosso guiador".getBytes(),150,1);
            testeMsg2.serialize(out);

            socket.shutdownOutput();
            socket.shutdownInput();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
