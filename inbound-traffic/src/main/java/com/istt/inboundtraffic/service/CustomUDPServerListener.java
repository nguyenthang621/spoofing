package com.istt.inboundtraffic.service;

import com.istt.inboundtraffic.UDPServer.UDPConnection;
import com.istt.inboundtraffic.UDPServer.UDPServerListener;
import com.istt.inboundtraffic.modal.UDPPacket;

public class CustomUDPServerListener implements UDPServerListener {

	@Override
	public void OnConnectEvent(UDPConnection connection) {
		// Handle connect event
	}

	@Override
	public void OnDisconnectTimeOutEvent(UDPConnection connection) {
		// Handle disconnect timeout event
	}

	@Override
	public void OnFailedToStartEvent() {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnPacketSendEvent(UDPConnection connection, UDPPacket packet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnPacketNotSendEvent(UDPConnection connection, UDPPacket packet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnServerDroppedConnectionEvent(UDPConnection connection, UDPPacket packet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnClientDroppedConnectionEvent(UDPConnection connection, UDPPacket packet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnPacketReceivedEvent(UDPConnection connection, UDPPacket packet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnLatePacketReceivedEvent(UDPConnection connection, UDPPacket packet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnPacketReceiveTimedOutEvent() {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnServerStopEvent() {
		// TODO Auto-generated method stub

	}

	// Implement other listener methods based on your requirements
}
