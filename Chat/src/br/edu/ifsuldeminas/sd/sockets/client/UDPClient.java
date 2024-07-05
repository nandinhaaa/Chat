package br.edu.ifsuldeminas.sd.sockets.client;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class UDPClient {
	private static final int TIME_OUT = 10000;
	private static final int SERVER_PORT = 3000;
	private static final int BUFFER_SIZE = 200;
	private static final String KEY_TO_EXIT = "q";

	public static void main(String[] args) {
		DatagramSocket datagramSocket = null;
		Scanner reader = new Scanner(System.in);
		String stringMessage = "";
		try {
			datagramSocket = new DatagramSocket();

			while (!stringMessage.equals(KEY_TO_EXIT)) {
				System.out.printf("Escreva uma mensagem (%s para sair): ", KEY_TO_EXIT);
				stringMessage = reader.nextLine().trim();
				if (!stringMessage.equals(KEY_TO_EXIT) && !stringMessage.isEmpty()) {
					byte[] message = stringMessage.getBytes();
					InetAddress serverAddress = InetAddress.getLocalHost();
					DatagramPacket datagramPacketToSend = new DatagramPacket(message, message.length, serverAddress,
							SERVER_PORT);
					datagramSocket.send(datagramPacketToSend);

					byte[] responseBuffer = new byte[BUFFER_SIZE];  
					DatagramPacket datagramPacketForResponse = new DatagramPacket(responseBuffer,
							responseBuffer.length);
					datagramSocket.setSoTimeout(TIME_OUT);
					datagramSocket.receive(datagramPacketForResponse);
					String serverResponse = new String(datagramPacketForResponse.getData(), 0,
							datagramPacketForResponse.getLength()).trim();
					System.out.printf("Resposta do servidor: %s\n", serverResponse);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (datagramSocket != null)
				datagramSocket.close();
			if (reader != null)
				reader.close();
		}
	}
}
