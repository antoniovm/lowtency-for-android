package com.antoniovm.lowtency.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;


/**
 * @author Antonio Vicente Martin
 * 
 */
public class NetworkClient {

	private Socket socket;
	private DatagramSocket datagramSocket;
	private DatagramPacket datagramPacket;

	/**
	 * 
	 */
	public NetworkClient() {
		try {
			this.socket = new Socket();
			this.datagramSocket = new DatagramSocket(socket.getLocalPort());
			this.datagramSocket.setReuseAddress(true);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Receives a datagram socket from
	 */
	public byte[] receiveUDP() {
		try {
			datagramSocket.receive(datagramPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return datagramPacket.getData();
	}

	/**
	 * 
	 * Makes a TCP conecction
	 * 
	 * @param ip
	 * @param port
	 */
	public boolean connect(String host, int port) {
		if (socket.isConnected()) {
			return false;
		}

		try {
			socket.connect(new InetSocketAddress(host, port));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

}
