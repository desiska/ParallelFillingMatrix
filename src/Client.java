import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    private static final int BUFFER_SIZE = 16384;
    private static final String LOCALHOST = "127.0.0.1";
    private static final int PORT = 8080;
    private static final ByteBuffer buff = ByteBuffer.allocate(BUFFER_SIZE);

    public static void main(String[] args) {
        try (SocketChannel channel = SocketChannel.open();
             Scanner scan = new Scanner(System.in)){

            channel.connect(new InetSocketAddress(LOCALHOST, PORT));

            welcomeMessage();

            while (true){
                System.out.print("-- ");
                String input = scan.nextLine();

                if(input.equals("stop"))
                    break;

                buff.clear();
                buff.put(input.getBytes());
                buff.flip();
                channel.write(buff);

                buff.clear();

                channel.read(buff);
                buff.flip();

                byte[] outputByte = new byte[buff.remaining()];
                buff.get(outputByte);
                String output = new String(outputByte, StandardCharsets.UTF_8);
                System.out.println(output);
            }

            System.out.println("Disconnecting from the server...");
        }
        catch (IOException ex){
            throw new RuntimeException(ex);
        }
    }

    //Печатане на съобщение при стартирване
    private static void welcomeMessage() {
        System.out.println("-- Client connected to the server!\n" +
                "Welcome to the Matrix Filling Client-Server App!\n" +
                "Its purpose is to fill a 2D matrix using multiple threads\n" +
                "where each thread fills the matrix with a different number\n" +
                "that is randomly generated.\n");
    }
}
