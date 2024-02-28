//package com.istt.inboundtraffic.modal;
//
//public class UDPPacket {
//	private long id;
//	private String data;
//
//	public UDPPacket(long id, String data) {
//		this.id = id;
//		this.data = data;
//	}
//
//	public UDPPacket(byte[] data) {
//		Parse(data);
//	}
//
//	private void Parse(byte[] data) {
//		StringBuilder pID = new StringBuilder();
//		StringBuilder pData = new StringBuilder();
//
//		boolean isPID = true;
//		for (byte datum : data)
//			if (isPID)
//				if (datum == '\n')
//					isPID = false;
//				else
//					pID.append((char) datum);
//			else if (datum == '\0')
//				break;
//			else
//				pData.append((char) datum);
//
//		this.data = pData.toString();
//		this.id = Integer.parseInt(pID.toString());
//	}
//
//	public String GetData() {
//		return data;
//	}
//
//	public long GetID() {
//		return id;
//	}
//
//	public byte[] toBytes() {
//		return (String.valueOf(id) + '\n' + data).getBytes();
//	}
//}

package com.istt.inboundtraffic.modal;

public class UDPPacket {
	private String data;

	public UDPPacket(String data) {
		this.data = data;
	}

	public UDPPacket(byte[] data) {
		Parse(data);
	}

	private void Parse(byte[] data) {
		this.data = new String(data, 0, data.length).trim();
	}

	public String GetData() {
		return data;
	}

	public byte[] toBytes() {
		return (data + '\0').getBytes();
	}
}
