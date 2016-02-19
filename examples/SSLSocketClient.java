package com.example.ssldemo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.ssldemo.naga.NIOService;
import com.example.ssldemo.naga.NIOSocket;
import com.example.ssldemo.naga.NIOSocketSSL;
import com.example.ssldemo.naga.SSLSocketChannelResponder;
import com.example.ssldemo.naga.SSLSocketObserver;

public class MainActivity extends Activity {

	private static final String SERVICR_HOST = "10.192.42.174";
	private static final int SERVICR_PORT = 8881;
	public static final String TAG = "SSL"; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		 new MinaClientThread().start();
	}

	private void creatNIOSSLConnect() {
		new Thread() {
			public void run() {
				try {
					NIOService service = new NIOService();

					SSLEngine engine = getSSLContext().createSSLEngine();
					// SSLEngine engine = _FakeX509TrustManager.allowAllSSL();
					if (null != engine) {
						SSLSocketChannelResponder socket = service.openSSLSocket(engine,
								SERVICR_HOST, SERVICR_PORT);
						socket.listen(new SSLSocketObserver() {
							public void packetSent(NIOSocket socket, Object tag) {
								System.out.println("Packet sent");
							}

							public void connectionOpened(NIOSocket nioSocket) {
								try {
									((NIOSocketSSL) nioSocket).beginHandshake();
								} catch (SSLException e) {
									e.printStackTrace();
								}
								System.out.println("*Connection opened");
							}

							public void connectionBroken(NIOSocket nioSocket, Exception exception) {
								System.out.println("*Connection broken");
								if (exception != null)
									exception.printStackTrace();
								// System.exit(9);
							}

							public void packetReceived(NIOSocket socket, byte[] packet) {
								System.out.println("Client Received: " + new String(packet)); 
								receivMessage(new String(packet));
							}

							@Override
							public void handleFinished(NIOSocket socket) {
								sendMessage(socket);
							}

						});
						while (true) {
							service.selectBlocking();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}.start();
	}

	public SSLContext getSSLContext() throws KeyStoreException, IOException,
			NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException,
			KeyManagementException {
				
		char[] password = "testssl".toCharArray();
		KeyStore keyStore = KeyStore.getInstance("BKS");
		KeyStore trustStore = KeyStore.getInstance("BKS");

		keyStore.load(getResources().openRawResource(R.raw.clientkey), password);
		trustStore.load(getResources().openRawResource(R.raw.clienttrust), password);

		KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
		kmf.init(keyStore, password);
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
		tmf.init(trustStore);

		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

		return sslContext;
	}

	// 发数据
	public void sendMessage(NIOSocket socket) {
		socket.write(cp.getBytes());
		System.out.println("*Client sent: hello word -- 11111");
	}

	// 收数据
	public void receivMessage(String string) {
		// TODO Auto-generated method stub
	}


}
