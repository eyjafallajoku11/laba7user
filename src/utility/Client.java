package utility;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Objects;

import static java.lang.System.in;
import static java.lang.System.out;
import static utility.Serialisation.deserialize;
import static utility.Serialisation.serialize;

public class Client {
    private static SocketChannel channel;
    private final static String hostName = "localhost";
    private static SocketAddress inetSocketAddress;
    private static ByteBuffer[] bufferOut;

    public static void start(int portNumber) {
        try {
            System.out.println("попытка подключения");
            inetSocketAddress = new InetSocketAddress(hostName, portNumber);
            channel = SocketChannel.open(inetSocketAddress);
            channel.configureBlocking(false);
            System.out.printf("Подключено к удаленному адресу %s по порту %d%n", hostName, portNumber);
            run();
        } catch (IOException e) {
            try {
                Thread.sleep(333);
            } catch (InterruptedException ex) {
                throw new RuntimeException(e);
            }
            restart();
        }
    }

    public static void restart(){
        System.out.println("попытка подключения");
        try {
            channel = SocketChannel.open(inetSocketAddress);
            System.out.println("соединение восстановлено");
            run();
            } catch (IOException e) {
            try {
                Thread.sleep(333);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            restart();
        }

    }

    private static void run() {
        while (true) {
            out.print(": ");
            String[] t = readLine();
            Request request = CommandManager.execute(t[0], t[1]);
            if (!Objects.isNull(request)) {
                Client.sendRequest(request);
                int[] data = Client.getAnswerData();
                if (!Objects.isNull(data)) {
                    Client.getAnswer(data);
                }
            }
        }

    }

    public static void sendRequest(Request request) {
//        System.out.println("пытаемся");
        try {
            byte[] buf;
            buf = serialize(request);
            int[] requestData = split(buf);
            int size = requestData[1];
            channel.write(ByteBuffer.wrap(serialize(requestData)));
            for (int i = 0; i < size; i++) {
                Thread.sleep(1);
                channel.write(bufferOut[i]);
                bufferOut[i].clear();
            }
        } catch (IOException e) {
            try {
                Thread.sleep(333);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            restart();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public static int[] split(byte[] buffer) {
        int byteBufferSize = 1024;
        int size = (int) Math.ceil((double) buffer.length / byteBufferSize);
        int stop = byteBufferSize;
        bufferOut = new ByteBuffer[size];
        for (int i = 0; i < size; i++){
            if (i == size-1 && buffer.length % byteBufferSize != 0) stop = (buffer.length % byteBufferSize);
            byte[] temp = new byte[stop];
            System.arraycopy(buffer, i * byteBufferSize, temp, 0, stop);
            bufferOut[i] = ByteBuffer.wrap(temp);
        }
        return (new int[] {byteBufferSize,size});
    }
    public static void getAnswer(int[] bufferData){
        ByteBuffer bufferIn = ByteBuffer.allocate(bufferData[0]);
        int size = bufferData[1];
        byte[] input = new byte[0];
        try {
            for (int i=0; i < size; i++) {
                bufferIn.clear();
                int length = 0;
                while (length == 0) {
                    length = channel.read(bufferIn);
                }
                input = combineArray(input, bufferIn.array(), length);
            }
            System.out.println(new String(input));
        } catch (Exception e) {
            try {
                Thread.sleep(333);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            restart();

        }
    }
    public static int[] getAnswerData(){
//        System.out.println("хочу данные прочитать");
        int[] data = null;
        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
        try {
            byteBuffer.clear();
            int length = 0;
            while (length==0) {
                length = channel.read(byteBuffer);
            }
            data = deserialize(byteBuffer.array());
        }  catch (Exception e) {
            try {
                Thread.sleep(333);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            restart();
        }
        return data;
    }
    private static byte[] combineArray(byte[] arr1, byte[] arr2, int length){
        byte[] arr = new byte[arr1.length+arr2.length];
        System.arraycopy(arr1, 0, arr, 0, arr1.length);
        System.arraycopy(arr2, 0, arr, arr1.length, length);
        return arr;
    }

    public static void closeConnection(){
        try {
            channel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String[] readLine() {
        StringBuilder response = new StringBuilder();
        String[] str_out = new String[2];
        try {
            BufferedInputStream Buf_in = new BufferedInputStream(in);
            int in;
            char inChar;
            short t = 0;
            do {
                in = Buf_in.read();
                if (in != -1) {
                    if (in==32 || in==10){
                        str_out[t]=response.toString();
                        t++;
                        response = new StringBuilder();
                    }
                    else {
                        inChar = (char) in;
                        response.append(inChar);
                    }
                }
            } while ((in != -1) & (in != 10) & (t!=2));
            return str_out;
        } catch (IOException e) {
            out.println("Exception: " + e.getMessage());
            return null;
        }
    }
}