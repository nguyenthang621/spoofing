package com.istt.service;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.mobicents.protocols.ss7.isup.ISUPMessageFactory;
import org.mobicents.protocols.ss7.isup.ISUPParameterFactory;
import org.mobicents.protocols.ss7.isup.ParameterException;
import org.mobicents.protocols.ss7.isup.impl.message.AbstractISUPMessage;
import org.mobicents.protocols.ss7.isup.impl.message.ISUPMessageFactoryImpl;
import org.mobicents.protocols.ss7.isup.impl.message.parameter.CauseIndicatorsImpl;
import org.mobicents.protocols.ss7.isup.impl.message.parameter.ISUPParameterFactoryImpl;
import org.mobicents.protocols.ss7.isup.message.AddressCompleteMessage;
import org.mobicents.protocols.ss7.isup.message.AnswerMessage;
import org.mobicents.protocols.ss7.isup.message.CallProgressMessage;
import org.mobicents.protocols.ss7.isup.message.ContinuityMessage;
import org.mobicents.protocols.ss7.isup.message.InitialAddressMessage;
import org.mobicents.protocols.ss7.isup.message.ReleaseCompleteMessage;
import org.mobicents.protocols.ss7.isup.message.ReleaseMessage;
import org.mobicents.protocols.ss7.isup.message.ResumeMessage;
import org.mobicents.protocols.ss7.isup.message.SuspendMessage;
import org.mobicents.protocols.ss7.isup.message.parameter.CauseIndicators;
import org.mobicents.protocols.ss7.isup.message.parameter.CircuitIdentificationCode;
import org.mobicents.protocols.ss7.mtp.Mtp3;
import org.mobicents.protocols.ss7.mtp.Mtp3PausePrimitive;
import org.mobicents.protocols.ss7.mtp.Mtp3ResumePrimitive;
import org.mobicents.protocols.ss7.mtp.Mtp3StatusPrimitive;
import org.mobicents.protocols.ss7.mtp.Mtp3TransferPrimitive;
import org.mobicents.protocols.ss7.mtp.Mtp3UserPartListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.istt.config.ApplicationProperties;
import com.istt.config.Constants;
import com.istt.config.app.IsupProxyConfiguration;
import com.istt.config.ss7.M3UAConfiguration;
import com.istt.service.dto.AddressTranslatorDTO;
import com.istt.service.dto.CallLog;
import com.istt.service.util.MessageProcessingUtil;
import com.istt.ss7.dto.IAMDTO;

import io.micrometer.core.instrument.Metrics;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class M3uaListenerImpl implements Mtp3UserPartListener {

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	IsupProxyConfiguration isupProps;

	@Autowired
	ApplicationProperties props;
	
	@Autowired
	HealthService healthService;
	
	public static final transient ISUPParameterFactory isupParameterFactory = new ISUPParameterFactoryImpl();

	public static final transient ISUPMessageFactory isupMessageFactory = new ISUPMessageFactoryImpl(
			isupParameterFactory);

	@Autowired
	RedisTemplate<String, String> redisTemplate;

	@Autowired
	RedisTemplate<String, Serializable> serializableRedisTemplate;
	
	
	@Autowired
	M3uaHandle m3uaHandle;
	
	@Autowired IsupService isupService;

	@Override
	public void onMtp3TransferMessage(Mtp3TransferPrimitive mtpMsg) {
		Metrics.globalRegistry.counter("mtp3Msg", 
				"opc", mtpMsg.getOpc()+"", 
				"dpc", mtpMsg.getDpc()+"" 
		).increment();
		log.debug("onMtp3TransferMessage M3ua: {} -> {},  Event: {}", mtpMsg.getOpc(), mtpMsg.getDpc(),
				Hex.encodeHexString(mtpMsg.encodeMtp3()));
		if (mtpMsg.getSi() == Mtp3._SI_SERVICE_ISUP) {
			if (props.getHandleMode()==2) {
				if (props.getWorkerThreadMode()==0) { //using thread of sctp - 
					m3uaHandle.handleISUPSingleThread(mtpMsg);	
				}else { //use separate executors
					m3uaHandle.doHandleISUP(mtpMsg);
				}	
				
			} else { //old version - in call only
				handleISUP(mtpMsg);
			}
			
		}
	}

	/**
	 * Handle incoming ISUP message
	 *
	 * @param mtpMsg
	 */
	public void handleISUP(Mtp3TransferPrimitive mtpMsg) {
		// 2(CIC) + 1(CODE)
		byte[] payload = mtpMsg.getData();
		int commandCode = payload[2];

		AbstractISUPMessage msg = (AbstractISUPMessage) isupMessageFactory.createCommand(commandCode);
		try {
			msg.decode(payload, isupMessageFactory, isupParameterFactory);
		} catch (ParameterException e) {
			log.error("Error decoding of incoming Mtp3TransferPrimitive {} {} {}" , e.getMessage(),msg.getMessageType().getMessageName(), e);
		}
		msg.setSls(mtpMsg.getSls()); // store SLS...
		// should take here OPC or DPC????? since come in different direction looks like
		// opc
		// isup.onEvent(msg, mtpMsg.getOpc());
		int cic = Optional.ofNullable(msg.getCircuitIdentificationCode()).map(CircuitIdentificationCode::getCIC)
				.orElse(-1);
		String cicKey = mtpMsg.getOpc() + "_" + mtpMsg.getDpc() + "_" + cic;

		Integer state = (Integer) serializableRedisTemplate.opsForValue().get(cicKey + Constants.CIC_MASK);

		Metrics.globalRegistry.counter("isup_rx", 
				"messageType", msg.getMessageType().getMessageName().name(), 
				"cickey", cicKey, 
				"state", Optional.ofNullable(state).map(Object::toString).orElse("UNKNOWN")
		).increment();
		log.debug("++ CIC: {} | {} ", cicKey, msg.getMessageType().getMessageName());
		switch (msg.getMessageType().getCode()) {
		
		case InitialAddressMessage.MESSAGE_CODE: // IAM
			log.debug("++ {} | {} IAM - Call begin", cicKey, state);
			serializableRedisTemplate.delete(cicKey + Constants.CIC_MASK);
			redisTemplate.delete(cicKey);
			handleIam(msg, mtpMsg, cicKey);
			break;

		// RLC received. No more packet. Just erase the queue
		case ReleaseCompleteMessage.MESSAGE_CODE: // REL
			log.debug("-- {} | state {}: RLC - Call complete", cicKey, state);
			if ((state == null) || (state != Constants.STATE_REJECT)) {
				bypass(msg, mtpMsg, cicKey);
			}
			serializableRedisTemplate.delete(cicKey + Constants.CIC_MASK);
			redisTemplate.delete(cicKey);
			log.debug("{} | Terminate transaction on RLC received", cicKey);
			break;

		// REL received. Only bypass if the state is ACCEPT. Otherwise clean queue and
		// return RLC
		case ReleaseMessage.MESSAGE_CODE:
			log.debug("-- {} | state {}: REL - Call release", cicKey, state);
			if (state == null) {
				log.debug("receive REL for outbound calls");
				serializableRedisTemplate.delete(cicKey + Constants.CIC_MASK);
				redisTemplate.delete(cicKey);
				bypass(msg, mtpMsg, cicKey);
			} else {
				switch (state) {
				case Constants.STATE_ACCEPT:
					log.debug("caller terminate call successfully.");
					serializableRedisTemplate.delete(cicKey + Constants.CIC_MASK);
					redisTemplate.delete(cicKey);
					bypass(msg, mtpMsg, cicKey);
					break;
				default:
					createRejectResponse(msg, mtpMsg, cicKey);
					break;
				}
			}
			break;

		// Other standards call flow messages
		// 
		// case AddressCompleteMessage.MESSAGE_CODE: // ACM
		// case AnswerMessage.MESSAGE_CODE: // ANM
		// case CallProgressMessage.MESSAGE_CODE: // CPG
		// case SuspendMessage.MESSAGE_CODE: // SUS
		// case ResumeMessage.MESSAGE_CODE: // RES
		default:
			log.debug("{} | state: {} << {}", cicKey, state, msg.getMessageType().getMessageName());
			if (state == null) {
				bypass(msg, mtpMsg, cicKey);
			} else {
				switch (state) {
				case Constants.STATE_REJECT:
					log.debug("{} | logic rejected. Create Reject Response", cicKey);
					createRejectResponse(msg, mtpMsg, cicKey);
					break;
				case Constants.STATE_PENDING:
					log.debug("{} | Await response from logic. Enqueue", cicKey);
					enqueue(msg, cicKey);
					break;
				default:
					bypass(msg, mtpMsg, cicKey);
					break;
				}
			}
		}
	}

	/**
	 * Response to messages send to us when we want to reject the call
	 * Send RLC if receive a REL
	 * 
	 * Otherwise do nothing
	 *
	 * @param msg
	 * @param mtpMsg
	 * @param cicKey
	 */
	private void createRejectResponse(AbstractISUPMessage msg, Mtp3TransferPrimitive mtpMsg, String cicKey) {
		switch (msg.getMessageType().getCode()) {
		case ReleaseMessage.MESSAGE_CODE:
			sendRLC(msg.getCircuitIdentificationCode().getCIC(), mtpMsg);
			redisTemplate.delete(cicKey);
			serializableRedisTemplate.delete(cicKey + Constants.CIC_MASK);
			log.debug("{} | Terminate transaction on REJECT Response", cicKey);
			break;

		case ContinuityMessage.MESSAGE_CODE:
		case AnswerMessage.MESSAGE_CODE:
		case AddressCompleteMessage.MESSAGE_CODE:
		case CallProgressMessage.MESSAGE_CODE:
		case ResumeMessage.MESSAGE_CODE:
		case SuspendMessage.MESSAGE_CODE:
			log.debug("{} | REJECTED - NoOp: {}", cicKey, msg.getMessageType().getMessageName());
			break;

		default:
			bypass(msg, mtpMsg, cicKey);
			break;
		}
	}

	/**
	 * Push the message Hex dump into redis for later use. we add 2 value: message
	 * code and its hex dump
	 *
	 * @param msg
	 * @param cicKey
	 */
	private void enqueue(AbstractISUPMessage msg, String cicKey) {
		try {
			redisTemplate.opsForList().rightPush(cicKey, msg.getMessageType().getCode() + "");

			String hexDump = Hex.encodeHexString(msg.encode());
			Long idx = redisTemplate.opsForList().rightPush(cicKey, hexDump);
			log.debug("cicKey: {} msg: {} idx: {} | {}", cicKey, msg.getMessageType().getMessageName(), idx, hexDump);
		} catch (ParameterException e) {
			log.error("Cannot enqueue msg", e);
		}
	}

	/**
	 * Send back a RLC
	 *
	 * @param msg
	 * @param mtpMsg
	 */
	private void sendRLC(int cic, Mtp3TransferPrimitive mtpMsg) {
		try {
			ReleaseCompleteMessage rlc = isupMessageFactory.createRLC(cic);
			byte[] payload = ((AbstractISUPMessage) rlc).encode();
			int si = Mtp3._SI_SERVICE_ISUP;
			int ni = mtpMsg.getNi();
			int mp = mtpMsg.getMp();
			int opc = mtpMsg.getDpc();
			int dpc = mtpMsg.getOpc();
			int sls = mtpMsg.getSls();

			log.info("REJECT CALL: {} -> {} : {}", opc, dpc, rlc.getMessageType().getMessageName());

			Mtp3TransferPrimitive mtp3TransferPrimitive = M3UAConfiguration.clientM3UAMgmt
					.getMtp3TransferPrimitiveFactory().createMtp3TransferPrimitive(si, ni, mp, opc, dpc, sls, payload);
			M3UAConfiguration.clientM3UAMgmt.sendMessage(mtp3TransferPrimitive);
		} catch (Exception e) {
			log.error("Error handling also failed: {}", mtpMsg, e);
		}
	}

	/**
	 * Send the IAM back
	 *
	 * @param msg
	 * @param mtpMsg
	 */
	private void handleIam(AbstractISUPMessage msg, Mtp3TransferPrimitive mtpMsg, String cicKey) {
		log.debug("{} | Begin transaction | state {}", cicKey);
		InitialAddressMessage message = (InitialAddressMessage) msg;
		String calledParty = Optional.ofNullable(message.getCalledPartyNumber()).map(
				num -> addressTranslate(num.getAddress().replaceAll("[^\\d.]", ""), num.getNatureOfAddressIndicator()))
				.orElse(null);

		String callingParty = Optional.ofNullable(message.getCallingPartyNumber()).map(
				num -> addressTranslate(num.getAddress().replaceAll("[^\\d.]", ""), num.getNatureOfAddressIndicator()))
				.orElse(null);

		int cic = message.getCircuitIdentificationCode().getCIC();
		String txnId = UUID.randomUUID().toString();
		log.debug("IAM: {} -> {}", callingParty, calledParty);
		serializableRedisTemplate.opsForValue().set(cicKey + Constants.CIC_MASK, Constants.STATE_PENDING);

		IAMDTO dto = new IAMDTO();
		dto.setCalledParty(calledParty);
		dto.setCallingParty(callingParty);
		dto.setCic(cic);
		dto.setTxnId(txnId);
		
		try {
			Map<String, String> uriVariables = new HashMap<>();
			uriVariables.put("callingParty", callingParty);
			uriVariables.put("calledParty", calledParty);
			uriVariables.put("cic", cic + "");
			uriVariables.put("txnId", txnId);
			uriVariables.put("opc", mtpMsg.getOpc() + "");
			uriVariables.put("dpc", mtpMsg.getDpc() + "");
			uriVariables.put("callref", UUID.randomUUID().toString());

			ResponseEntity<CallLog> result = restTemplate.getForEntity(isupProps.getCallbackUrl(), CallLog.class, uriVariables);
			log.info("Response from logic component: {}", result.getBody());
			serializableRedisTemplate.opsForValue().set(cicKey + Constants.CIC_MASK, Constants.STATE_ACCEPT);
			bypass(msg, mtpMsg, cicKey);
		} catch (HttpClientErrorException e) {
			log.info("Blocking call by sending back REL: {}", message);
			serializableRedisTemplate.opsForValue().set(cicKey + Constants.CIC_MASK, Constants.STATE_REJECT);
			rejectCall(message, mtpMsg, cicKey);
			// + increment the redis key
			String blacklistKey = Constants.BLACKLIST_PREFIX + callingParty;
			long cnt = Optional.ofNullable(redisTemplate.opsForValue().increment(blacklistKey)).orElse(0L);
			redisTemplate.expire(blacklistKey, cnt > props.getBlacklistThreshold() ? props.getBlacklistBlockTtl() : props.getBlacklistTtl(), TimeUnit.SECONDS);
		} catch (HttpServerErrorException e) {
			log.warn("Bypass call due to server error: {}", message);
			serializableRedisTemplate.opsForValue().set(cicKey + Constants.CIC_MASK, Constants.STATE_ACCEPT);
			bypass(msg, mtpMsg, cicKey);
		} catch (Exception e) {
			log.error("Unknown error from URL: {} : {}", isupProps.getCallbackUrl(), dto, e);
			serializableRedisTemplate.opsForValue().set(cicKey + Constants.CIC_MASK, Constants.STATE_ACCEPT);
			bypass(msg, mtpMsg, cicKey);
		}
	}

	/**
	 * By pass the message back to network via Destination point code This action
	 * let the application rewrite the destination point code of ISUP message and
	 * send it back to network { OPC, DPC } --> { OPC, DPC }
	 *
	 * @param event
	 */
	private void bypass(AbstractISUPMessage event, Mtp3TransferPrimitive mtpMsg, String cicKey) {
		try {
			log.debug("{} | BYPASS: {} -> {} : {}", cicKey, mtpMsg.getOpc(), mtpMsg.getDpc(),
					event.getMessageType().getMessageName());
			M3UAConfiguration.clientM3UAMgmt.sendMessage(mtpMsg);
			// flush the message queue
			bypassQueue(mtpMsg, cicKey);
		} catch (Exception e) {
			log.error("Error handling also failed: {}", event, e);
		}
	}

	/**
	 * Loop through the queue and send out all pending messages to upstream
	 *
	 * @param mtpMsg
	 * @param cicKey
	 */
	private void bypassQueue(Mtp3TransferPrimitive mtpMsg, String cicKey) {
		try {
			String msgCode = redisTemplate.opsForList().leftPop(cicKey);
			if (msgCode == null)
				return;
			int code = Integer.parseInt(msgCode);
			log.debug("Processing ISUP message with code: {}", code);

			String hexString = redisTemplate.opsForList().leftPop(cicKey);
			if (hexString == null)
				return;
			byte[] payload = Hex.decodeHex(hexString);

			int si = Mtp3._SI_SERVICE_ISUP;
			int ni = mtpMsg.getNi();
			int mp = mtpMsg.getMp();
			int opc = mtpMsg.getOpc();
			int dpc = mtpMsg.getDpc();

			int sls = mtpMsg.getSls();
			M3UAConfiguration.clientM3UAMgmt.sendMessage(mtpMsg);
			Mtp3TransferPrimitive mtp3TransferPrimitive = M3UAConfiguration.clientM3UAMgmt
					.getMtp3TransferPrimitiveFactory().createMtp3TransferPrimitive(si, ni, mp, opc, dpc, sls, payload);
			log.debug("{} | BYPASS RESEND: {} -> {} : {}", cicKey, opc, dpc, hexString);
			M3UAConfiguration.clientM3UAMgmt.sendMessage(mtp3TransferPrimitive);
		} catch (DecoderException e) {
			log.error("Cannot decode message", e);
		} catch (IOException e) {
			log.error("Cannot send message", e);
		}
		bypassQueue(mtpMsg, cicKey);
	}

	/**
	 * Send a REL call { OPC, IPC } IAM --> { DPC, OPC } REL
	 *
	 * @param event
	 * @param message
	 * @param dto
	 */
	private void rejectCall(InitialAddressMessage event, Mtp3TransferPrimitive iamMsg, String cicKey) {
		try {
			ReleaseMessage rel = isupMessageFactory.createREL(event.getCircuitIdentificationCode().getCIC());
			CauseIndicatorsImpl original = new CauseIndicatorsImpl(CauseIndicators._CODING_STANDARD_ITUT,
					CauseIndicators._LOCATION_NETWORK_BEYOND_IP, 1, isupProps.getRlcCauseCode(), null);
			rel.setCauseIndicators(original);
			byte[] payload = ((AbstractISUPMessage) rel).encode();
			int si = Mtp3._SI_SERVICE_ISUP;
			int ni = iamMsg.getNi();
			int mp = iamMsg.getMp();
			int opc = iamMsg.getDpc();
			int dpc = iamMsg.getOpc();

			int sls = event.getSls();

			log.info("REJECT CALL: {} -> {} : {}", opc, dpc, rel.getMessageType().getMessageName());

			Mtp3TransferPrimitive mtp3TransferPrimitive = M3UAConfiguration.clientM3UAMgmt
					.getMtp3TransferPrimitiveFactory().createMtp3TransferPrimitive(si, ni, mp, opc, dpc, sls, payload);
			M3UAConfiguration.clientM3UAMgmt.sendMessage(mtp3TransferPrimitive);
			// send reject messages
			rejectQueue(iamMsg, cicKey);
		} catch (Exception e) {
			log.error("Error handling also failed: {}", event, e);
		}
	}

	/**
	 * Handle those messages on our queues
	 *
	 * @param originalMsg
	 * @param cicKey
	 */
	private void rejectQueue(Mtp3TransferPrimitive originalMsg, String cicKey) {
		try {
			String msgCode = redisTemplate.opsForList().leftPop(cicKey);
			if (msgCode == null)
				return;
			int code = Integer.parseInt(msgCode);
			log.debug("Processing ISUP message with code: {}", code);

			String hexString = redisTemplate.opsForList().leftPop(cicKey);
			if (hexString == null)
				return;
			// send response in case of reject

			log.debug("{} | REJECT | Processing enqueued : {} | {}", cicKey, code, hexString);
			// byte[] payload = Hex.decodeHex(hexString);
			switch (code) {
			case ReleaseMessage.MESSAGE_CODE:
				int cic = Integer.parseInt(cicKey.split("_")[2]);
				sendRLC(cic, originalMsg);
				redisTemplate.delete(cicKey);
				serializableRedisTemplate.delete(cicKey + Constants.CIC_MASK);
				log.debug("{} | Terminate transaction on REJECT QUEUE", cicKey);
				break;
			default:
				log.info("Do nothing");
			}

		} catch (Exception e) {
			log.error("Cannot send message", e);
		}
		rejectQueue(originalMsg, cicKey);
	}

	/**
	 * Perform address translation
	 * 
	 * @param address
	 * @param noa
	 * @return
	 */
	private String addressTranslate(String address, int noa) {
		Set<Long> prefixes = MessageProcessingUtil.toPrefixSet(address);
		List<Integer> lengths = Arrays.asList(0, address.length());
		List<Integer> noas = Arrays.asList(0, noa);
		return Long.parseLong(isupProps.getNats().stream().filter(
				i -> (prefixes.contains(i.getPrefix()) && noas.contains(i.getNoa()) && lengths.contains(i.getLength())))
				.sorted(Comparator.comparing(AddressTranslatorDTO::getLength).reversed())
				.map(AddressTranslatorDTO::isEnabled).findFirst().orElse(false) ? props.msisdn(address) : address) + "";
	}

	/**
	 * Listen to Pause MTP3 and shut down the input links if need
	 */
	@Override
	public void onMtp3PauseMessage(Mtp3PausePrimitive msg) {
		Metrics.globalRegistry.counter("mtp3_pause", "affected_pointcode", "" + msg.getAffectedDpc()).increment();
		log.info("onMtp3PauseMessage: AffectedDPC: {} type {} class {}", msg.getAffectedDpc(), msg.getType(),msg.getClass());
		if (props.getOutputPc().stream().anyMatch(i -> i.equalsIgnoreCase(""+msg.getAffectedDpc()))) {
			log.error("upstream pointcode {} is down. lock the input.", msg.getAffectedDpc());
			healthService.shutdownInputLinks();
		}
	}

	@Override
	public void onMtp3ResumeMessage(Mtp3ResumePrimitive msg) {
		// TODO Auto-generated method stub
		Metrics.globalRegistry.counter("mtp3_resume", "affected_pointcode", "" + msg.getAffectedDpc()).increment();
		log.info("onMtp3ResumeMessage: AffectedDPC: {} type {} class {}", msg.getAffectedDpc(), msg.getType(),
				msg.getClass());
	}

	@Override
	public void onMtp3StatusMessage(Mtp3StatusPrimitive msg) {
		Metrics.globalRegistry.counter("mtp3_status", "affected_pointcode", "" + msg.getAffectedDpc(), "cause", msg.getCause().name()).increment();
		log.info("onMtp3StatusMessage: AffectedDPC: {} type {} class {}: {}", msg.getAffectedDpc(), msg.getType(),
				msg.getClass(), msg.toString());
	}

	////////////////////////
	
	
}
