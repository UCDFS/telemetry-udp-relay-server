package ie.ucdracing.relay;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class RelayServer {
	private static final int CLIENT_PORT = 4445;
	private static final int REGISTER_HEADER = 0x0f;

	private DatagramSocket mSocket = new DatagramSocket(37823);

	private Map<SocketAddress, Long> mClients = new HashMap<>();

	public RelayServer() throws SocketException {
	}

	public static void main(String[] args) {
		try {
			new RelayServer().run();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	private void run() {
		System.out.println("Starting loop");
		int packetNumber = 0;
		while (true) {
			byte[] buffer = new byte[65536];
			DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
			try {
				mSocket.receive(receivePacket);
			} catch (IOException e) {
				continue;
			}

			if (buffer[0] == REGISTER_HEADER) {
				System.out.println("New client registered" + receivePacket.getSocketAddress());
				mClients.put(receivePacket.getSocketAddress(), System.currentTimeMillis());
				continue;
			}

			if (packetNumber % 100 == 0) {
				System.out.println("Sending packet #" + packetNumber + " from " + receivePacket.getSocketAddress() + " to other clients");
			}
			packetNumber++;

			Iterator<Map.Entry<SocketAddress, Long>> iterator = mClients.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<SocketAddress, Long> client = iterator.next();

				if ((System.currentTimeMillis() - client.getValue()) > 1000 * 60) {
					iterator.remove();
					continue;
				}

				DatagramPacket sendPacket = new DatagramPacket(buffer, receivePacket.getLength(), client.getKey());
				try {
					mSocket.send(sendPacket);
				} catch (IOException e) {
					iterator.remove();
				}
			}
		}
	}
}
