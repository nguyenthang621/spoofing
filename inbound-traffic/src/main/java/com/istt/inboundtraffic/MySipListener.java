package com.istt.inboundtraffic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionAlreadyExistsException;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.TransactionUnavailableException;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.URI;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import com.istt.inboundtraffic.modal.CallLog;
import com.istt.inboundtraffic.service.Constants;

import gov.nist.javax.sip.header.CSeq;
import gov.nist.javax.sip.header.ContentLength;
import gov.nist.javax.sip.header.ContentType;
import gov.nist.javax.sip.header.From;
import gov.nist.javax.sip.header.Via;

public class MySipListener implements SipListener {

	private String ipAddr = "192.168.10.44";

	private int port = 5060;

	private int countLoadBalanceAPI = 1;

	private AddressFactory addressFactory = null;

	private MessageFactory msgFactory = null;

	private HeaderFactory headerFactory = null;

	private SipProvider sipProvider = null;

	public static CallLog callApi(String calling, String called, String callid, String apiUrl) throws IOException {
		try {
			URL url = new URL(apiUrl + "?calling=" + calling + "&called=" + called + "&callid=" + callid);

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setConnectTimeout(1000);
			connection.setRequestMethod("GET");
			connection.setReadTimeout(1000);

			int responseCode = connection.getResponseCode();
			System.out.println("Response Code: " + responseCode);

			StringBuilder responseData = new StringBuilder();
			BufferedReader reader;

			if (responseCode >= 200 && responseCode < 300) {
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			} else {
				reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
			}

			String line;
			while ((line = reader.readLine()) != null) {
				responseData.append(line);
			}

			reader.close();

			System.out.println("Response Data: " + responseData.toString());

			CallLog callLog = CallLog.fromJson(responseData.toString());
			callLog.setCallId(callid);
			callLog.setCalling(calling);
			callLog.setCalled(called);

//			if (responseCode != 200) {
//				callLog.setErrorCode(422);
//				callLog.setErrorDesc("Error server spoofing");
//			}

			connection.disconnect();
			return callLog;
		} catch (IOException e) {
			String line = "";
			// Handle exceptions, e.g., timeout or unreachable server
			CallLog errorLog = new CallLog(callid, calling, called, line, line, line, 422, line, line, null, false,
					null, null, 422, line, line, line, calling, apiUrl, line, called, callid);
			errorLog.setErrorCode(700);
			errorLog.setErrorDesc("ERROR_SERVER_SPOOFING");
			// System.out.println("Error server spoofing");
			return errorLog;
		}
	}

	public static boolean callApiPing(String apiUrl) {
		try {

			URL url = new URL(apiUrl.trim());
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(1000);
			connection.setReadTimeout(1000);
			connection.setRequestMethod("GET");

			int responseCode = connection.getResponseCode();

			if (responseCode == 401 || responseCode == 200) {
				return true;
			}
			return false;

		} catch (IOException e) {
			return false;
		}
	}

	public static String extractPhoneNumber(String input) {
		String regex = "<sip:(\\+?\\d+)@";
		Pattern pattern = Pattern.compile(regex);

		Matcher matcher = pattern.matcher(input);

		if (matcher.find()) {
			return matcher.group(1).replace("+", "");
		} else {
			return null;
		}
	}

	public MySipListener(String ipAddr, int port, AddressFactory addressFactory, MessageFactory msgFactory,
			HeaderFactory headerFactory, SipProvider sipProvider) {
		this.addressFactory = addressFactory;
		this.msgFactory = msgFactory;
		this.headerFactory = headerFactory;
		this.sipProvider = sipProvider;
		this.ipAddr = ipAddr;
		this.port = port;
	}

	private static Hashtable<URI, URI> currUser = new Hashtable();

	@Override
	public void processDialogTerminated(DialogTerminatedEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("processDialogTerminated " + arg0.toString());
	}

	@Override
	public void processIOException(IOExceptionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("processIOException " + arg0.toString());
	}

	class TimerTask extends Timer {

		public TimerTask() {

		}

		public void run() {

		}
	}

	private void processRegister(Request request, RequestEvent requestEvent) {
		if (null == request) {
			System.out.println("processInvite request is null.");
			return;
		}
		// System.out.println("Request " + request.toString());
		ServerTransaction serverTransactionId = requestEvent.getServerTransaction();

		try {
			Response response = null;
			ToHeader head = (ToHeader) request.getHeader(ToHeader.NAME);
			Address toAddress = head.getAddress();

			URI toURI = toAddress.getURI();
			ContactHeader contactHeader = (ContactHeader) request.getHeader("Contact");

			Address contactAddr = contactHeader.getAddress();

			URI contactURI = contactAddr.getURI();
			System.out.println("processRegister from: " + toURI + " request str: " + contactURI);
			System.out.println();
			int expires = request.getExpires().getExpires();

			if (expires != 0 || contactHeader.getExpires() != 0) {
				currUser.put(toURI, contactURI);
				System.out.println("register user " + toURI);
			} else {
				currUser.remove(toURI);
				System.out.println("unregister user " + toURI);
			}

			response = msgFactory.createResponse(200, request);
			System.out.println("send register response  : " + response.toString());

			if (serverTransactionId == null) {
				serverTransactionId = sipProvider.getNewServerTransaction(request);
				serverTransactionId.sendResponse(response);
				// serverTransactionId.terminate();
				System.out.println("register serverTransaction: " + serverTransactionId);
			} else {
				System.out.println("processRequest serverTransactionId is null.");
			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void processOptionsPing(Request request, RequestEvent requestEvent) throws IOException {
		if (null == request) {
			System.out.println("processOptionsPing request is null.");
			return;
		}

		ServerTransaction serverTransactionId = requestEvent.getServerTransaction();

		try {
			Response response = null;
			boolean checkHeatBeat = callApiPing(Constants.getApiPing());
//			String url = Constants.getApiSpoofing();

//			CallLog callLog = callApi(String.valueOf(Constants.getNumbercallblock()),
//					String.valueOf(Constants.getNumbercalledblock()), "", url);
//			System.out.println("----------OPTIONS PING------------: " + callLog.getErrorCode());
			if (checkHeatBeat) {
				System.out.println("RESPONSE OPTIONS PING");
				serverTransactionId = sipProvider.getNewServerTransaction(request);
				callerDialog = serverTransactionId.getDialog();
				response = msgFactory.createResponse(Response.OK, request);
				serverTransactionId.sendResponse(response);
			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void processInvite(Request request, RequestEvent requestEvent) {
		if (null == request) {
			System.out.println("processInvite request is null.");
			return;
		}
		try {
			// 100 Trying
			serverTransactionId = requestEvent.getServerTransaction();
			if (serverTransactionId == null) {
				serverTransactionId = sipProvider.getNewServerTransaction(request);
				// callerDialog = serverTransactionId.getDialog();
				Response response = msgFactory.createResponse(Response.TRYING, request);
				serverTransactionId.sendResponse(response);
			}

			// Sip
//			String inviteLine = request.getMethod() + " " + request.getRequestURI() + " SIP/2.0";
//			System.out.println("INVITE line: " + inviteLine);

			// Via
//			ViaHeader viaHeader = (ViaHeader) request.getHeader(ViaHeader.NAME);
//			System.out.println("viaHeader: " + viaHeader);
//			String transport = viaHeader.getTransport();
//			String viaAddress = viaHeader.getHost();
//			int viaPort = viaHeader.getPort();

//			System.out.println("Via: " + transport + " " + viaAddress + ":" + viaPort);

			// From
			FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
			Address fromAddress = fromHeader.getAddress();
//			System.out.println("fromAddress: " + fromAddress);
//			String fromUri = fromAddress.getURI().toString();
//			String fromTag = fromHeader.getTag();

//			System.out.println("From: " + fromUri + ";tag=" + fromTag);

			// To
			ToHeader toHeader = (ToHeader) request.getHeader(ToHeader.NAME);
			Address toAddress = toHeader.getAddress();
//			System.out.println("toAddress: " + toAddress);
//			String toUri = toAddress.getURI().toString();

//			System.out.println("To: " + toUri);

//			ContactHeader contactHeader = (ContactHeader) request.getHeader(ContactHeader.NAME);
//			System.out.println("ContactHeader: " + contactHeader);

			// Call-ID
			CallIdHeader callIdHeader = (CallIdHeader) request.getHeader(CallIdHeader.NAME);
			String callId = callIdHeader.getCallId();

			System.out.println("Call-ID: " + callId);

//			// CSeq
//			CSeqHeader cSeqHeader = (CSeqHeader) request.getHeader(CSeqHeader.NAME);
//			long sequenceNumber = cSeqHeader.getSeqNumber();
//			String method = cSeqHeader.getMethod();
//
//			System.out.println("CSeq: " + sequenceNumber + " " + method);
//
//			// Max-Forwards
//			MaxForwardsHeader maxForwardsHeader = (MaxForwardsHeader) request.getHeader(MaxForwardsHeader.NAME);
//			int maxForwards = maxForwardsHeader.getMaxForwards();
//
//			System.out.println("Max-Forwards: " + maxForwards);
//
//			// Content-Type
//			ContentTypeHeader contentTypeHeader = (ContentTypeHeader) request.getHeader(ContentTypeHeader.NAME);
//			String contentType = contentTypeHeader.getContentType();
//
//			System.out.println("Content-Type: " + contentType);

//			// Content-Length
//			ContentLengthHeader contentLengthHeader = (ContentLengthHeader) request.getHeader(ContentLengthHeader.NAME);
//			int contentLength = contentLengthHeader.getContentLength();
//			

			// System.out.println("check: " + extractPhoneNumber(String.valueOf(fromAddress))
			// 		+ extractPhoneNumber(String.valueOf(fromAddress)).startsWith("84"));

			String url = Constants.getApiSpoofing();

			CallLog callLog = callApi(extractPhoneNumber(String.valueOf(fromAddress)),
					extractPhoneNumber(String.valueOf(toAddress)), callId, url);
			System.out.println("ErrorCode: " + callLog.getErrorCode());

//			if (!String.valueOf(callLog.getErrorCode()).contains((CharSequence) Constants.ignoreStatusCodes)) {
			if (!Constants.getIgnoreStatusCodes().contains(String.valueOf(callLog.getErrorCode()))) {
				System.out.println("Response 404");
				// callerDialog = serverTransactionId.getDialog();
				Response response = msgFactory.createResponse(Response.NOT_FOUND, request);
				serverTransactionId.sendResponse(response);

			} else {
				System.out.println("Response 503");
				// callerDialog = serverTransactionId.getDialog();
				Response response = msgFactory.createResponse(Response.SERVICE_UNAVAILABLE, request);
				serverTransactionId.sendResponse(response);
			}

		} catch (TransactionUnavailableException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processSubscribe(Request request) {
		if (null == request) {
			System.out.println("processSubscribe request is null.");
			return;
		}
		ServerTransaction serverTransactionId = null;
		try {
			serverTransactionId = sipProvider.getNewServerTransaction(request);
		} catch (TransactionAlreadyExistsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransactionUnavailableException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			Response response = null;
			response = msgFactory.createResponse(200, request);
			if (response != null) {
				ExpiresHeader expireHeader = headerFactory.createExpiresHeader(30);
				response.setExpires(expireHeader);
			}
			System.out.println("response : " + response.toString());

			if (serverTransactionId != null) {
				serverTransactionId.sendResponse(response);
				serverTransactionId.terminate();
			} else {
				System.out.println("processRequest serverTransactionId is null.");
			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void processBye(Request request, RequestEvent requestEvent) {
		if (null == request || null == requestEvent) {
			System.out.println("processBye request is null.");
			return;
		}
		Request byeReq = null;
		Dialog dialog = requestEvent.getDialog();
		System.out.println("calleeDialog : " + calleeDialog);
		try {
			if (dialog.equals(calleeDialog)) {
				byeReq = callerDialog.createRequest(request.getMethod());
				ClientTransaction clientTran = sipProvider.getNewClientTransaction(byeReq);
				callerDialog.sendRequest(clientTran);
				calleeDialog.setApplicationData(requestEvent.getServerTransaction());
			} else if (dialog.equals(callerDialog)) {
				byeReq = calleeDialog.createRequest(request.getMethod());
				ClientTransaction clientTran = sipProvider.getNewClientTransaction(byeReq);
				calleeDialog.sendRequest(clientTran);
				callerDialog.setApplicationData(requestEvent.getServerTransaction());
			} else {
				System.out.println("");
			}

			System.out.println("send bye to peer:" + byeReq.toString());
		} catch (SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void processCancel(Request request) {
		if (null == request) {
			System.out.println("processCancel request is null.");
			return;
		}
	}

	private void processInfo(Request request) {
		if (null == request) {
			System.out.println("processInfo request is null.");
			return;
		}
	}

	private void processAck(Request request, RequestEvent requestEvent) {
		if (null == request) {
			System.out.println("processAck request is null.");
			return;
		}

		try {
			Request ackRequest = null;
			CSeq csReq = (CSeq) request.getHeader(CSeq.NAME);
			ackRequest = calleeDialog.createAck(csReq.getSeqNumber());
			calleeDialog.sendAck(ackRequest);
			System.out.println("send ack to callee:" + ackRequest.toString());
		} catch (SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void processCancel(Request request, RequestEvent requestEvent) {

		if (request == null || requestEvent == null) {
			System.out.println("processCancel input parameter invalid.");
			return;
		}

		try {
			// CANCEL 200 OK
			Response response = msgFactory.createResponse(Response.OK, request);
			ServerTransaction cancelServTran = requestEvent.getServerTransaction();
			if (cancelServTran == null) {
				cancelServTran = sipProvider.getNewServerTransaction(request);
			}
			cancelServTran.sendResponse(response);

			// CANCEL
			Request cancelReq = null;
			Request inviteReq = clientTransactionId.getRequest();
			List list = new ArrayList();
			Via viaHeader = (Via) inviteReq.getHeader(Via.NAME);
			list.add(viaHeader);

			CSeq cseq = (CSeq) inviteReq.getHeader(CSeq.NAME);
			CSeq cancelCSeq = (CSeq) headerFactory.createCSeqHeader(cseq.getSeqNumber(), Request.CANCEL);
			cancelReq = msgFactory.createRequest(inviteReq.getRequestURI(), inviteReq.getMethod(),
					(CallIdHeader) inviteReq.getHeader(CallIdHeader.NAME), cancelCSeq,
					(FromHeader) inviteReq.getHeader(From.NAME), (ToHeader) inviteReq.getHeader(ToHeader.NAME), list,
					(MaxForwardsHeader) inviteReq.getHeader(MaxForwardsHeader.NAME));
			ClientTransaction cancelClientTran = sipProvider.getNewClientTransaction(cancelReq);
			cancelClientTran.sendRequest();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransactionAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransactionUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ServerTransaction serverTransactionId = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sip.SipListener#processRequest(javax.sip.RequestEvent)
	 */
	@Override
	public void processRequest(RequestEvent arg0) {
		Request request = arg0.getRequest();
		if (null == request) {
			System.out.println("processRequest request is null.");
			return;
		}
		if (Request.INVITE.equals(request.getMethod())) {
			processInvite(request, arg0);
		} else if (Request.REGISTER.equals(request.getMethod())) {
			processRegister(request, arg0);
		} else if (Request.SUBSCRIBE.equals(request.getMethod())) {
			processSubscribe(request);
		} else if (Request.ACK.equalsIgnoreCase(request.getMethod())) {
			processAck(request, arg0);
		} else if (Request.BYE.equalsIgnoreCase(request.getMethod())) {
			processBye(request, arg0);
		} else if (Request.CANCEL.equalsIgnoreCase(request.getMethod())) {
			processCancel(request, arg0);
		} else if (Request.MESSAGE.equalsIgnoreCase(request.getMethod())) {
			processMessage(request, arg0);
		} else if (Request.OPTIONS.equalsIgnoreCase(request.getMethod())) {
			System.out.println("OPTIONS PING");
			try {
				processOptionsPing(request, arg0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println("no support the method!");
		}
	}

	private void processMessage(Request request, RequestEvent requestEvent) {
		System.out.println("message coming");
	}

	private Dialog calleeDialog = null;

	private Dialog callerDialog = null;

	ClientTransaction clientTransactionId = null;

	/**
	 * BYE
	 * 
	 * @param response
	 * @param responseEvent
	 */
	private void doByeResponse(Response response, ResponseEvent responseEvent) {
		Dialog dialog = responseEvent.getDialog();

		try {
			Response byeResp = null;
			if (callerDialog.equals(dialog)) {
				ServerTransaction servTran = (ServerTransaction) calleeDialog.getApplicationData();
				byeResp = msgFactory.createResponse(response.getStatusCode(), servTran.getRequest());
				servTran.sendResponse(byeResp);
			} else if (calleeDialog.equals(dialog)) {
				ServerTransaction servTran = (ServerTransaction) callerDialog.getApplicationData();
				byeResp = msgFactory.createResponse(response.getStatusCode(), servTran.getRequest());
				servTran.sendResponse(byeResp);
			} else {

			}
			System.out.println("send bye response to peer:" + byeResp.toString());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sip.SipListener#processResponse(javax.sip.ResponseEvent)
	 *
	 */
	@Override
	public void processResponse(ResponseEvent arg0) {

		Response response = arg0.getResponse();

		System.out.println("recv the response :" + response.toString());
		System.out.println("respone to request : " + arg0.getClientTransaction().getRequest());

		if (response.getStatusCode() == Response.TRYING) {
			System.out.println("The response is 100 response.");
			return;
		}

		try {
			ClientTransaction clientTran = arg0.getClientTransaction();

			if (Request.INVITE.equalsIgnoreCase(clientTran.getRequest().getMethod())) {
				int statusCode = response.getStatusCode();
				Response callerResp = null;

				callerResp = msgFactory.createResponse(statusCode, serverTransactionId.getRequest());

				// contact
				ContactHeader contactHeader = headerFactory.createContactHeader();
				Address address = addressFactory.createAddress("sip:sipsoft@" + ipAddr + ":" + port);
				contactHeader.setAddress(address);
				contactHeader.setExpires(3600);
				callerResp.addHeader(contactHeader);

				// to
				ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
				callerResp.setHeader(toHeader);

				ContentLength contentLen = (ContentLength) response.getContentLength();
				if (contentLen != null && contentLen.getContentLength() != 0) {
					ContentType contentType = (ContentType) response.getHeader(ContentType.NAME);
					System.out.println("the sdp content type is " + contentType);

					callerResp.setContentLength(contentLen);
					// callerResp.addHeader(contentType);
					callerResp.setContent(response.getContent(), contentType);
				} else {
					System.out.println("sdp is null.");
				}
				if (serverTransactionId != null) {
					callerDialog = serverTransactionId.getDialog();
					calleeDialog = clientTran.getDialog();
					serverTransactionId.sendResponse(callerResp);
					System.out.println("callerDialog=" + callerDialog);
					System.out.println("serverTransactionId.branch=" + serverTransactionId.getBranchId());
				} else {
					System.out.println("serverTransactionId is null.");
				}

				System.out.println("send response to caller : " + callerResp.toString());
			} else if (Request.BYE.equalsIgnoreCase(clientTran.getRequest().getMethod())) {
				doByeResponse(response, arg0);
			} else if (Request.CANCEL.equalsIgnoreCase(clientTran.getRequest().getMethod())) {
				// doCancelResponse(response, arg0);
				System.out.println("response cancel");
			} else {
				System.out.println("========response " + clientTran.getRequest().getMethod() + "===========");
			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void doCancelResponse(Response response, ResponseEvent responseEvent) {

		ServerTransaction servTran = (ServerTransaction) callerDialog.getApplicationData();
		Response cancelResp;
		try {
			cancelResp = msgFactory.createResponse(response.getStatusCode(), servTran.getRequest());
			servTran.sendResponse(cancelResp);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void processTimeout(TimeoutEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println(" processTimeout " + arg0.toString());
	}

	@Override
	public void processTransactionTerminated(TransactionTerminatedEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println(" processTransactionTerminated " + arg0.getClientTransaction().getBranchId() + " "
				+ arg0.getServerTransaction().getBranchId());
	}

}
