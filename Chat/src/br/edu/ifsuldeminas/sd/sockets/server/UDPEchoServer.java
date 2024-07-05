package br.edu.ifsuldeminas.sd.sockets.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

public class UDPEchoServer {
    private static final int MIN_BUFFER_SIZE = 100;
    private static DatagramSocket datagramSocket = null;
    private static byte[] incomingBuffer = null;
    private static int portNumber;
    private static int bufferSize;
    private static boolean isRunning = false;

    public static void start(int portNumber, int bufferSize) throws UDPEchoServerException {
        validateAttributes(portNumber, bufferSize);
        assignAttributes(portNumber, bufferSize);
        try {
            prepare();
            run();
        } catch (IOException ioException) {
            isRunning = false;
            throw new UDPEchoServerException("Houve algum erro ao executar o servidor de eco UDP.", ioException);
        } finally {
            closeResources();
            System.out.println("Servidor parou devido a erros.");
        }
    }

    public static void stop() {
        if (isRunning) {
            closeResources();
            isRunning = false;
            System.out.println("Servidor parado.");
        } else {
            System.out.println("Servidor já está parado.");
        }
    }

    private static void validateAttributes(int portNumber, int bufferSize) {
        if (portNumber <= 1024)
            throw new IllegalArgumentException("O servidor UDP não pode usar portas reservadas.");

        if (bufferSize < MIN_BUFFER_SIZE)
            throw new IllegalArgumentException(
                    String.format("O buffer de mensagem precisa ser maior que %d", MIN_BUFFER_SIZE));
    }

    private static void assignAttributes(int portNumber, int bufferSize) {
        UDPEchoServer.portNumber = portNumber;
        UDPEchoServer.bufferSize = bufferSize;
    }

    private static void prepare() throws SocketException {
        if (isRunning)
            stop();
        datagramSocket = new DatagramSocket(portNumber);
        incomingBuffer = new byte[bufferSize];
    }

    private static void run() throws IOException {
        System.out.printf("Servidor de eco rodando em '%s:%d' ...\n", InetAddress.getLocalHost().getHostAddress(),
                portNumber);
        isRunning = true;
        Scanner scanner = new Scanner(System.in);

        while (isRunning) { // Loop contínuo enquanto o servidor estiver em execução
            DatagramPacket received = receive();
            if (dataIsUnderSize(received) && !isMessageEmpty(received)) {
                String receivedMessage = new String(received.getData(), 0, received.getLength()).trim();
                System.out.printf("Cliente disse: %s\n", receivedMessage);

                // Esperar a resposta do operador do servidor
                System.out.print("Responder: ");
                String responseMessage = scanner.nextLine().trim();

                reply(received, responseMessage.getBytes(), responseMessage.length());
            } else {
                reply(received, "Dados acima do tamanho.".getBytes(), "Dados acima do tamanho.".length());
            }
        }
        scanner.close();
    }

    private static DatagramPacket receive() throws IOException {
        DatagramPacket received = new DatagramPacket(incomingBuffer, incomingBuffer.length);
        datagramSocket.receive(received);
        return received;
    }

    private static boolean dataIsUnderSize(DatagramPacket received) {
        return incomingBuffer.length >= received.getData().length;
    }

    private static boolean isMessageEmpty(DatagramPacket received) {
        return new String(received.getData(), 0, received.getLength()).trim().isEmpty();
    }

    private static void reply(DatagramPacket received, byte[] message, int length) throws IOException {
        DatagramPacket reply = new DatagramPacket(message, length, received.getAddress(), received.getPort());
        datagramSocket.send(reply);
        System.out.printf("Servidor enviou mensagem para %s:%d\n", received.getAddress().getHostAddress(),
                received.getPort());
    }

    private static void closeResources() {
        if (datagramSocket != null)
            datagramSocket.close();
        datagramSocket = null;
    }
}
