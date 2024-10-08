import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.Iterator;

public class Server {
    private static final int BUFFER_SIZE = 16384;
    private static final String LOCALHOST = "127.0.0.1";
    private static final int PORT = 8080;
    private static boolean isStarting = false;
    private static int countThreads;
    private static final ByteBuffer buff = ByteBuffer.allocate(BUFFER_SIZE);
    private static Selector selector;
    private static Matrix matrix;

    //Чете съобщенията/командите от клиента
    private static String readClientInput(SocketChannel client) throws IOException {
        buff.clear();

        int readBytesCount = client.read(buff);
        if(readBytesCount < 0){
            client.close();
            return null;
        }

        buff.flip();
        byte[] readBytes = new byte[buff.remaining()];
        buff.get(readBytes);

        String result = new String(readBytes, StandardCharsets.UTF_8);
        return result;
    }

    //Праща съобщения до клиента
    private static void writeClientOutput(SocketChannel client, String output) throws IOException {
        buff.clear();
        buff.put(output.getBytes());
        buff.flip();

        client.write(buff);
    }

    //Основна функционалност на сървъра
    public static void start(){
        try (ServerSocketChannel server = ServerSocketChannel.open()){
            selector = Selector.open();
            server.bind(new InetSocketAddress(LOCALHOST, PORT));
            server.configureBlocking(false);
            server.register(selector, SelectionKey.OP_ACCEPT);
            isStarting = true;

            System.out.println("Server started!");

            while (isStarting){
                int channels = selector.select();
                if(channels == 0)
                    System.out.println("Client is disconnected!");

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext()){
                    SelectionKey key = iterator.next();

                    if (key.isReadable()){
                        SocketChannel client = (SocketChannel) key.channel();

                        String input = readClientInput(client);
                        System.out.println(input);

                        switch (input){
                            case "stop":
                                stop();
                                break;

                            case "threads":
                                writeClientOutput(client, "Enter count of threads: ");
                                input = readClientInput(client);
                                assert input != null;
                                while (input.equals(""))
                                    input = readClientInput(client);
                                countThreads = Integer.parseInt(input);
                                matrix = new Matrix(countThreads);
                                writeClientOutput(client, "Matrix was created with " + countThreads + " rows and cols.");
                                writeClientOutput(client, matrix.execute());
                                break;

                            case "help":
                                writeClientOutput(client, helpOutput());
                                break;

                            case "":
                                continue;

                            default:
                                throw new InvalidParameterException("Invalid command!");
                        }
                    }
                    else if(key.isAcceptable()){
                        SocketChannel client = ((ServerSocketChannel) key.channel()).accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                        System.out.println("The server received an accept request!");
                    }

                    iterator.remove();
                }
            }
        }
        catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    //Съобщението до клиента при подаване на командата help с цел по-лесно четене на кода
    private static String helpOutput() {
        return "Available commands:\n" +
                "threads - after pressing enter on this command\n" +
                "you must enter the number of threads to work with.\n" +
                "After you enter the number, press enter one more time\n" +
                "to see the result.\n" +
                "help - get a list of the available commands\n" +
                "stop - disconnect from the server";
    }

    //Спиране на сървъра
    private static void stop() {
        if(selector.isOpen())
            selector.wakeup();

        isStarting = false;
    }
}
