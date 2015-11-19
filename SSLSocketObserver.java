package com.example.ssldemo.naga;

public interface SSLSocketObserver extends SocketObserver {

	void handleFinished(NIOSocket nioSocket);
}
