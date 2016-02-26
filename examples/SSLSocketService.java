package com.example.ssldemo.naga.examples;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

import com.example.ssldemo.naga.NIOServerSocketSSL;
import com.example.ssldemo.naga.NIOService;
import com.example.ssldemo.naga.NIOSocket;
import com.example.ssldemo.naga.NIOSocketSSL;
import com.example.ssldemo.naga.SSLSocketObserver;
import com.example.ssldemo.naga.ServerSocketObserverAdapter;

public class SSLSocketService {

	private static final int SERVICR_PORT = 8881;

	public static void main(String... args) {
		try {
			final NIOService service = new NIOService();
			NIOServerSocketSSL socket = service.openSSLServerSocket(getSSLContext(), SERVICR_PORT);
			System.out.println("Server listens on port " + SERVICR_PORT + "... ...");

			socket.listen(new ServerSocketObserverAdapter() {
				@Override
				public void newConnection(final NIOSocket nioSocket) {

					nioSocket.listen(new SSLSocketObserver() {
						@Override
						public void packetReceived(NIOSocket socket, byte[] packet) {
							System.out.println("Received " + socket.getIp() + ": " +new String(packet));
							
							socket.write("This is an SSL Server".getBytes());
							System.out.println("Server sent: " + "This is an SSL Server");
						}

						@Override
						public void connectionBroken(NIOSocket nioSocket, Exception exception) {
							System.out.println("Client " + nioSocket.getIp()
									+ " disconnected. Exception: " + exception.getMessage());
						}

						@Override
						public void connectionOpened(NIOSocket nioSocket) {
							try {
								((NIOSocketSSL) nioSocket).beginHandshake();
							} catch (SSLException e) {
								e.printStackTrace();
							}

							System.out.println("Client " + nioSocket.getIp() + " connected.");
						}

						@Override
						public void packetSent(NIOSocket socket, Object tag) {
							System.out.println("packetSent");

						}

						@Override
						public void handleFinished(NIOSocket socket) {
							socket.write("This is an SSL Server".getBytes());
							System.out.println("Server sent: " + "This is an SSL Server");

						}
					});
				}
			});

			while (true) {
				service.selectBlocking();
			}
		} catch (Exception e) {
			e.printStackTrace(); 
		}
	}

	private static SSLContext getSSLContext() throws GeneralSecurityException,
			FileNotFoundException, IOException {
		KeyStore ks = KeyStore.getInstance("JKS");
		KeyStore ts = KeyStore.getInstance("JKS");

		char[] passphrase = "testssl".toCharArray();
		char[] password = "testssl".toCharArray();

		// 服务端jks 与 客户端bks相同，只是少了一步转换而已
		ks.load(new FileInputStream("ssl/serverkey.jks"), passphrase);
		ts.load(new FileInputStream("ssl/servertrust.jks"), passphrase);

		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(ks, password);

		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		tmf.init(ts);

		SSLContext sslCtx = SSLContext.getInstance("SSL");

		sslCtx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

		return sslCtx;
	}
}
