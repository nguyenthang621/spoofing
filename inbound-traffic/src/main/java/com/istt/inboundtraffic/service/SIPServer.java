package com.istt.inboundtraffic.service;

import java.io.StringReader;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentLengthHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.istt.inboundtraffic.modal.Sip;

@Lazy
@Service
public class SIPServer implements SipListener {

	SipFactory sipFactory; // Used to access the SIP API.
	SipStack sipStack; // The SIP stack.
	SipProvider sipProvider; // Used to send SIP messages.
	MessageFactory messageFactory; // Used to create SIP message factory.
	HeaderFactory headerFactory; // Used to create SIP headers.
	AddressFactory addressFactory; // Used to create SIP URIs.
	ListeningPoint listeningPoint; // SIP listening IP address/port.
	Properties properties; // Other properties.

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

//	@Autowired
//	private KafkaTemplate<String, Object> kafkaTemplate;

	private static void parseSipMessage(String cdataContent) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(cdataContent));
			Document document = builder.parse(is);

			// Lấy tất cả các nút trong CDATA
			NodeList nodes = document.getDocumentElement().getChildNodes();

			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					System.out.println(node.getNodeName() + ": " + node.getTextContent().trim());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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

	public void startServer() {
		try {
			sipFactory = SipFactory.getInstance();
			sipFactory.setPathName("gov.nist");

			Properties properties = new Properties();
			properties.setProperty("javax.sip.STACK_NAME", "SIPServer");
			properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");

			sipStack = sipFactory.createSipStack(properties);
			addressFactory = sipFactory.createAddressFactory();
			messageFactory = sipFactory.createMessageFactory();
			headerFactory = sipFactory.createHeaderFactory();

			ListeningPoint listeningPoint = sipStack.createListeningPoint("127.0.0.1", 5061, "udp");
			sipProvider = sipStack.createSipProvider(listeningPoint);
			sipProvider.addSipListener(this);

			System.out.println("SIP Server started at 127.0.0.1:5061");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ExecutorService executorService = Executors.newSingleThreadExecutor();

	@Override
	public void processRequest(RequestEvent requestEvent) {
		Request request = requestEvent.getRequest();
		ServerTransaction serverTransactionId = requestEvent.getServerTransaction();
		ServerTransaction serverTransaction = requestEvent.getServerTransaction();

		// Chạy processInvite trong một thread riêng
		executorService.submit(() -> {
			try {
				processInvite(request, serverTransaction);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

	}

	public void stop() {
		executorService.shutdown();
	}

	private void processInvite(Request request, ServerTransaction serverTransaction) throws Exception {
		// Process the incoming INVITE request
		// You can extract information from the request, such as headers and content
		// Handle the logic for processing the SIP INVITE here
		System.out.println("-------------------------------------");

		// Lấy thông tin từ trường Sip
		String inviteLine = request.getMethod() + " " + request.getRequestURI() + " SIP/2.0";
		System.out.println("INVITE line: " + inviteLine);

		// Lấy thông tin từ trường Via
		ViaHeader viaHeader = (ViaHeader) request.getHeader(ViaHeader.NAME);
		System.out.println("viaHeader: " + viaHeader);
		String transport = viaHeader.getTransport();
		String viaAddress = viaHeader.getHost();
		int viaPort = viaHeader.getPort();

//		System.out.println("Via: " + transport + " " + viaAddress + ":" + viaPort);

		// Lấy thông tin từ trường From
		FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
		Address fromAddress = fromHeader.getAddress();
		System.out.println("fromAddress: " + fromAddress);
		String fromUri = fromAddress.getURI().toString();
		String fromTag = fromHeader.getTag();

//		System.out.println("From: " + fromUri + ";tag=" + fromTag);

		// Lấy thông tin từ trường To
		ToHeader toHeader = (ToHeader) request.getHeader(ToHeader.NAME);
		Address toAddress = toHeader.getAddress();
		System.out.println("toAddress: " + toAddress);
		String toUri = toAddress.getURI().toString();

//		System.out.println("To: " + toUri);

		ContactHeader contactHeader = (ContactHeader) request.getHeader(ContactHeader.NAME);
		System.out.println("ContactHeader: " + contactHeader);

		// Lấy thông tin từ trường Call-ID
		CallIdHeader callIdHeader = (CallIdHeader) request.getHeader(CallIdHeader.NAME);
		String callId = callIdHeader.getCallId();

		System.out.println("Call-ID: " + callId);

		// Lấy thông tin từ trường CSeq
		CSeqHeader cSeqHeader = (CSeqHeader) request.getHeader(CSeqHeader.NAME);
		long sequenceNumber = cSeqHeader.getSeqNumber();
		String method = cSeqHeader.getMethod();

		System.out.println("CSeq: " + sequenceNumber + " " + method);

		// Lấy thông tin từ trường Max-Forwards
		MaxForwardsHeader maxForwardsHeader = (MaxForwardsHeader) request.getHeader(MaxForwardsHeader.NAME);
		int maxForwards = maxForwardsHeader.getMaxForwards();

		System.out.println("Max-Forwards: " + maxForwards);

		// Lấy thông tin từ trường Content-Type
		ContentTypeHeader contentTypeHeader = (ContentTypeHeader) request.getHeader(ContentTypeHeader.NAME);
		String contentType = contentTypeHeader.getContentType();

		System.out.println("Content-Type: " + contentType);

		// Lấy thông tin từ trường Content-Length
		ContentLengthHeader contentLengthHeader = (ContentLengthHeader) request.getHeader(ContentLengthHeader.NAME);
		int contentLength = contentLengthHeader.getContentLength();

		System.out.println("Content-Length: " + contentLength);

		String sipData = inviteLine + "\r\n" + viaHeader + "\r\n" + fromHeader + "\r\n" + toHeader + "\r\n"
				+ callIdHeader + "\r\n" + cSeqHeader + "\r\n" + maxForwardsHeader + "\r\n" + contentLengthHeader;

		try {
			if (request.getMethod().equals(Request.INVITE)) {
				redisTemplate.opsForValue().set(callId, sipData);
				Sip sipParams = new Sip();
				sipParams.setCallid(callId);
				sipParams.setCalling(extractPhoneNumber(String.valueOf(fromAddress)));
				sipParams.setCalled(extractPhoneNumber(String.valueOf(toAddress)));
//				kafkaTemplate.send("inbountToProviderSIP", sipParams);
				// Send a response
				Response response = messageFactory.createResponse(Response.TRYING, request);
				sipProvider.sendResponse(response);
				System.out.println("response: " + response);
				System.out.println("receiver done");
				// System.out.println("Sending SIP TRYING to " + callId);
//				processInvite(request, serverTransaction);
				sendContinuousTryingResponses(request, serverTransaction, callId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

//		// Send a response
//		Response response = messageFactory.createResponse(Response.TRYING, request);
//		sipProvider.sendResponse(response);
//		System.out.println("response: " + response);
//		System.out.println("receiver done");
	}

	private void sendContinuousTryingResponses(Request request, ServerTransaction serverTransaction, String callId)
			throws Exception {
		String redisData = redisTemplate.opsForValue().get(callId);
		String redisDataResponse = redisTemplate.opsForValue().get("DONE-" + callId);

		int count = 0;
		while (redisData != null || redisDataResponse != null) {
			count += 1;
			redisData = redisTemplate.opsForValue().get(callId);
			redisDataResponse = redisTemplate.opsForValue().get("DONE-" + callId);

			System.out.println("redisData :" + redisData);
			System.out.println("redisDataResponse: " + redisDataResponse);
			if (redisData != null && count == 20) {
//				sendTryingResponse(request);
				System.out.println("Send trying !!!");
				count = 0;
			}

			if (redisDataResponse.startsWith("1") || redisDataResponse.startsWith("2")) {
				System.out.println("4040404040404004");
				redisTemplate.opsForValue().getOperations().delete("DONE-" + callId);
				sendNotFoundResponse(request);
			} else {
				System.out.println("5035030503503503");
				redisTemplate.opsForValue().getOperations().delete("DONE-" + callId);
				sendServiceUnavailableResponse(request);
			}

			Thread.sleep(50);
		}

	}

	private void sendTryingResponse(Request originalRequest) {
		try {
			if (sipProvider != null && messageFactory != null) {
				// Create the 100 Trying response
				Response tryingResponse = messageFactory.createResponse(Response.TRYING, originalRequest);

				// Send the 100 Trying response
				sipProvider.sendResponse(tryingResponse);
			} else {
				System.out.println("sipProvider or messageFactory is null");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendServiceUnavailableResponse(Request originalRequest) {
		try {
			if (sipProvider != null && messageFactory != null) {
				// Create the 503 Service Unavailable response
				Response serviceUnavailableResponse = messageFactory.createResponse(Response.SERVICE_UNAVAILABLE,
						originalRequest);

				// Send the 503 Service Unavailable response
				sipProvider.sendResponse(serviceUnavailableResponse);
			} else {
				System.out.println("sipProvider or messageFactory is null");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendNotFoundResponse(Request originalRequest) {
		try {
			if (sipProvider != null && messageFactory != null) {
				Response serviceUnavailableResponse = messageFactory.createResponse(Response.NOT_FOUND,
						originalRequest);

				// Send the response
				sipProvider.sendResponse(serviceUnavailableResponse);
			} else {
				System.out.println("sipProvider or messageFactory is null");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void processResponse(ResponseEvent responseEvent) {
		// Handle SIP responses if needed

	}

	@Override
	public void processTimeout(TimeoutEvent timeoutEvent) {
		// Handle SIP timeout events if needed
	}

	@Override
	public void processIOException(IOExceptionEvent exceptionEvent) {
		// Handle SIP I/O exceptions if needed
	}

	@Override
	public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
		// Handle SIP transaction terminated events if needed
	}

	@Override
	public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
		// Handle SIP dialog terminated events if needed
	}
}
