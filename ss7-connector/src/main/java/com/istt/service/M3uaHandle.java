package com.istt.service;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.client.utils.URIBuilder;
import org.joda.time.DateTime;
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
import org.mobicents.protocols.ss7.mtp.Mtp3TransferPrimitiveFactory;
import org.mobicents.protocols.ss7.mtp.Mtp3UserPartListener;
import org.mobicents.protocols.ss7.mtp.RoutingLabelFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReactiveStreamCommands.ReadCommand;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
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
public class M3uaHandle  {

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
	public Map< String,CicData > cicMap = new ConcurrentHashMap<String, CicData>();
	private ExecutorService executor;
	
	/////////////////////////////////////////////////////
	
	@PostConstruct void Init() {
		readFsm(props.getIsupFsmPath(), true);
		if (props.getWorkerThreadNum()>0) {
			log.info("init {} worker thread executors",props.getWorkerThreadNum());
			executor=Executors.newFixedThreadPool(props.getWorkerThreadNum());
		}
	}

    @PreDestroy
    public void stop() {
    	if (executor!=null)   executor.shutdown();//cuong1
    }
	
	


    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Handle incoming ISUP message
	 * using sctp thread- not to lock 
	 * @param mtpMsg
	 */
	
	public void handleISUPSingleThread(Mtp3TransferPrimitive mtpMsg) {
		// 2(CIC) + 1(CODE)
		byte[] payload = mtpMsg.getData();
		int commandCode = payload[2];
		
		AbstractISUPMessage msg = (AbstractISUPMessage) isupMessageFactory.createCommand(commandCode);
		try {
			msg.decode(payload, isupMessageFactory, isupParameterFactory);
		} catch (ParameterException e) {
			log.error("Error decoding of incoming Mtp3TransferPrimitive" + e.getMessage(), e);
		}
		msg.setSls(mtpMsg.getSls()); // store SLS...
		// should take here OPC or DPC????? since come in different direction looks like
		// opc
		// isup.onEvent(msg, mtpMsg.getOpc());
		int cic = Optional.ofNullable(msg.getCircuitIdentificationCode()).map(CircuitIdentificationCode::getCIC)
				.orElse(-1);
		
		 
		//cic key=less point code + more point code + cic
		String cicKey = mtpMsg.getOpc() + "_" + mtpMsg.getDpc() + "_" + cic; 
		if (mtpMsg.getDpc()<mtpMsg.getOpc()) {
			cicKey = mtpMsg.getDpc() + "_" + mtpMsg.getOpc() + "_" + cic;
		}
		CicData cd=cicMap.get(cicKey);
		if (cd==null) {
			cd=new CicData(cicKey,cic);
			cicMap.put(cicKey, cd);
		}
		//Integer state = (Integer) serializableRedisTemplate.opsForValue().get(cicKey + Constants.CIC_MASK);
		Integer state=cd.state;
		
		Metrics.globalRegistry.counter("isup_rx", 
				"messageType", msg.getMessageType().getMessageName().name(), 
				"cickey", cicKey, 
				"state", Optional.ofNullable(state).map(Object::toString).orElse("UNKNOWN")
		).increment();
		log.debug("async ++ CIC: {} | {} {}-{}", cicKey, msg.getMessageType().getMessageName(),mtpMsg.getOpc(),mtpMsg.getDpc());
		
	   
	   try {
    		int direction=mtpMsg.getDpc()==props.getLocalPointCode()?1000:0;
    		int code=msg.getMessageType().getCode();
    		int reject=0;
    		if ( code==InitialAddressMessage.MESSAGE_CODE) {
    			reject=handleIam(msg, mtpMsg, cicKey)?0:10000;
    		}
    		int event=code+direction+reject;
    		
    		runFsm(state, event,msg,mtpMsg,cicKey,cic,cd);
            
        	return ;
        } catch(Exception e) {
            log.error("handleIsupEventFromQueue {}",e);
            return ;
        }
	}
    
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Handle incoming ISUP message
	 * lock fail -> return
	 * handle all messages from link queue
	 * @param mtpMsg
	 */
	

	
	
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
	 * true: bypass
	 * false: reject
	 */
	private boolean handleIam(AbstractISUPMessage msg, Mtp3TransferPrimitive mtpMsg, String cicKey) {
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
			return true;
		} catch (HttpClientErrorException e) {
			log.info("Blocking call by sending back REL: {}", message);
			return false;
		} catch (HttpServerErrorException e) {
			log.warn("Bypass call due to server error: {}", message);
			return true;
		} catch (Exception e) {
			log.error("Unknown error from URL: {} : {}", isupProps.getCallbackUrl(), dto, e);
			return true;
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


/////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////
	int maxState=0;
	String[] actionList= {"bypass","skip","tx-rel","tx-rlc"};

	ArrayList<ArrayList<FsmNode>> curFsmList;
	List<String> curStateList=new ArrayList<String>();
	List<FsmEventName> curEventNames = new ArrayList<FsmEventName>();

	//temporaty list 
	ArrayList<ArrayList<FsmNode>> fsmList;
	List<String> stateList=new ArrayList<String>();
	List<FsmEventName> eventNames = new ArrayList<FsmEventName>();
	
	public class FsmNode{
		public FsmNode(int state,int event,String action,int nextState) {
			this.state=state;
			this.event=event;
			this.action=action;
			this.nextState=nextState;
		}
		public int state,event,nextState;
		public String action;
		public String toString() {
			return String.format("state %d, event %d , action %s, next state %d", state,event,action,nextState);
		}
	}
	public class FsmEventName{
		public FsmEventName(String na,int ev){
			this.name=na.trim();
			this.event=ev;
		}
		String name;
		int event;
	}
	
	public static List<String> readFileInList(String fileName)
	  {
	 
	    List<String> lines = Collections.emptyList();
	    try
	    {
	      lines =  Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
	    }
	 
	    catch (IOException e)
	    {
	    	log.error(e.getMessage());
	    }
	    return lines;
	  }
	public int getStateByName(String stateName) {
		int state=-1;
		for (int k=0;k<stateList.size();k++) {
			if (stateList.get(k).equals(stateName)) {
				state=k;
				break;
			}
		}
		return state;
	}
	public int getEventByName(String eventName) {
		for (int k=0;k<eventNames.size();k++) {
			FsmEventName evn=eventNames.get(k);
			if (evn.name.equals(eventName)) {
				return evn.event;
			}
		}
		return -1;
	}
	public String getEventName(int event) {
		for (int k=0;k<eventNames.size();k++) {
			FsmEventName evn=eventNames.get(k);
			if (evn.event==event) {
				return evn.name;
			}
		}
		return "";
	}
	public int getActionByName(String action) {
		for(int i=0;i<actionList.length;i++) {
			if (actionList[i].equals(action)) {
				return i;
			}
		}
		return -1;
	}
	
	//return number of error
	public int  readFsm(String path,boolean apply) {
		int error=0;
		
		//String path=props.getIsupFsmPath();
		if(path.isEmpty()) {
			log.error("fsm file name is empty");
			return 1;
		}
		log.info("reading fsm data {}",path);
		List<String> lines=readFileInList(path);
		if (lines.isEmpty()) {
			log.error("error reading fsm data file");
			return 1;
		}
		stateList=new ArrayList<String>();
		eventNames = new ArrayList<FsmEventName>();
		fsmList=new ArrayList<ArrayList<FsmNode>>();
		
		for (int i=0;i<lines.size();i++) {
			String line=lines.get(i);
			line=line.toLowerCase().trim();
			if (line.isEmpty() || line.startsWith("#")) continue;
			String[] items=line.split(":");
			if (items.length==2 && items[0].trim().equals("states")) { //states: NULL;IN-REJECT;OUT-REJECT;IN-OUT-REJECT
				String[] list=items[1].split(";");
				for (int k=0;k<list.length;k++) {
					stateList.add(list[k].trim());
					ArrayList<FsmNode> al=new ArrayList<FsmNode>();
					fsmList.add(al);
				}
				
			}else if (items.length==2 && items[0].trim().equals("events")) {//events: NAME,NUM;NAME,NUM
				String[] list=items[1].split(";"); //iam-in-bypass,xxx;iam-in-reject,xxx
				for (int k=0;k<list.length;k++) {
					String[] li=list[k].split(",");
					if (li.length==2) {
						try {
							FsmEventName evName=new FsmEventName(li[0].trim() ,  Integer.parseInt(li[1]));
							eventNames.add(evName);
							
						}catch(Exception e) {
							log.error("exception error event name {}",list[k]);
							error++;
						}
						
					}else {
						log.error("wrong event {}",list[k]);
						error++;
					}
				}
				
			}else if (items.length==2) {
				//check in state list
				String stateName=items[0].trim();
		
				int state=getStateByName(stateName);
				if (state<0) {
					log.error("not found state in list {}",stateName);
				}else {
					//get fsm
					String[] li=items[1].split(","); //event,action,new-state
					if (li.length==3) {
						//
						int event=getEventByName(li[0].trim());
						String action=li[1].trim();
						int nextState=getStateByName(li[2].trim());
						if (event<0) {
							log.error("wrong event name {}",items[1]);
							error++;
							continue;
						}
						if (action.isEmpty()) {
							log.error("wrong action name {}",items[1]);
							error++;
							continue;
						}
						
						if (nextState<0) {
							log.error("wrong next state {}",items[1]);
							error++;
							continue;								
						}
					    fsmList.get(state).add(new FsmNode(state,event,action,nextState));
						
					}else {
						log.error("error node {}",items[1]);
						error++;
						continue;
					}
					
				}
			}else {
				log.error("wrong line format {}",line);
				error++;
			}
		}
		
		displayNewFsm();
		if(apply && error==0) {
			curFsmList=fsmList;
			curStateList=stateList;
			curEventNames=eventNames;
			
			displayCurFsm();
		}
		return error;
		
	}
	public void displayNewFsm() {
		log.info("=========== new fsm data ========================");
		displayFsm(fsmList, stateList, eventNames);
	}
	public void displayCurFsm() {
		log.info("=========== current fsm data ========================");
		displayFsm(curFsmList, curStateList, curEventNames);		
	}
	public void displayFsm(ArrayList<ArrayList<FsmNode>> fsmList,List<String> stateList,List<FsmEventName> eventNames ) {
		for(int i=0;i<fsmList.size();i++) {
			ArrayList<FsmNode> list=fsmList.get(i);
			if(i>=stateList.size()) {
				log.error("state over size {} {}",i,stateList.size());
				continue;
			}
			String stateName=stateList.get(i);
			log.info("-------------------------------------");
			log.info("state {}",stateName);
			for(int k=0;k<list.size();k++) {
				FsmNode node=list.get(k);
				if(node.state>=stateList.size() || node.nextState>=stateList.size()) {
					log.error("state over size {} ",node);
					continue;
				}
				String sn=stateList.get(node.state);
				String en="";
				String nsn=stateList.get(node.nextState);
				for (int n=0;n<eventNames.size();n++) {
					FsmEventName evn=eventNames.get(n);
					if (evn.event==node.event) {
						en=evn.name;
						break;
					}
				}
				log.info("state [{}], event [{}], action [{}] , next state [{}]",sn,en,nsn);
				
			}
		}
	}
	/////////////
	public void runFsm(int state,int event,AbstractISUPMessage isupMsg, Mtp3TransferPrimitive mtpMsg, String cicKey,int cic,CicData cd) {
		FsmNode node=getFsmNode(state,event);
		if (node==null ) {
			log.error("not found fsm node state {} event {} -> bypass",state,event);
			bypass2(isupMsg, mtpMsg, cicKey);
			return;
		}
		if (node.action==null) {
			log.error("not found action : state {} event {} -> bypass",state,event);
			bypass2(isupMsg, mtpMsg, cicKey);
			return;
		}
		log.info("state [{}] event [{}] action [{}]",state,event,node.action);
		doAction(node.action,isupMsg,mtpMsg,cicKey,cic);
		
		cd.state=node.nextState;
	}
	
	public FsmNode getFsmNode(int state,int event) {
		ArrayList<ArrayList<FsmNode>> curFsm=curFsmList;
		if (curFsm==null) {
			log.error("no fsm data defined");
			return null;
		}
		if(state>=curFsm.size()) {
			log.error("state {} event {}no state fsm data",state,event);
			return null;
		}
		ArrayList<FsmNode> nodeList=curFsm.get(state);
		if (nodeList==null) {
			log.error("state {} event {}no node list",state,event);
			return null;			
		}
		
	    Iterator<FsmNode> iter       = nodeList.iterator();
 

       while (iter.hasNext()) {
    	   FsmNode node=iter.next();
    	   if (node.event==event) {
    		   return node; 
    	   }
       }
       
       return null;
		
	}
	public void doAction(String action,AbstractISUPMessage event, Mtp3TransferPrimitive mtpMsg, String cicKey,int cic) {
		if (action.equals("bypass")) {
			bypass2(event, mtpMsg, cicKey);
		}else if (action.equals("skip")) {
			return;
		}else if (action.equals("tx-rel")) {
			txRel(event,mtpMsg,cicKey,cic);
		}else if (action.equals("tx-rlc")) {
			txRlc(cic,mtpMsg);
		}else {
			//default action: bypass
			log.error("not defined action -> bypass {}",action);
			bypass2(event, mtpMsg, cicKey);
		}
		
	}
	
	private void txRel(AbstractISUPMessage event, Mtp3TransferPrimitive iamMsg, String cicKey,int cic) {
		try {
			ReleaseMessage rel = isupMessageFactory.createREL(cic);
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

			log.info("txRel: {} -> {} : {}", opc, dpc, rel.getMessageType().getMessageName());

			Mtp3TransferPrimitive mtp3TransferPrimitive = M3UAConfiguration.clientM3UAMgmt
					.getMtp3TransferPrimitiveFactory().createMtp3TransferPrimitive(si, ni, mp, opc, dpc, sls, payload);
			M3UAConfiguration.clientM3UAMgmt.sendMessage(mtp3TransferPrimitive);
		} catch (Exception e) {
			log.error("Error  tx rel : {}", event, e);
		}
	}
	private void txRlc(int cic, Mtp3TransferPrimitive mtpMsg) {
		try {
			ReleaseCompleteMessage rlc = isupMessageFactory.createRLC(cic);
			byte[] payload = ((AbstractISUPMessage) rlc).encode();
			int si = Mtp3._SI_SERVICE_ISUP;
			int ni = mtpMsg.getNi();
			int mp = mtpMsg.getMp();
			int opc = mtpMsg.getDpc();
			int dpc = mtpMsg.getOpc();
			int sls = mtpMsg.getSls();

			log.info("tx rlc: {} -> {} : {}", opc, dpc, rlc.getMessageType().getMessageName());

			Mtp3TransferPrimitive mtp3TransferPrimitive = M3UAConfiguration.clientM3UAMgmt
					.getMtp3TransferPrimitiveFactory().createMtp3TransferPrimitive(si, ni, mp, opc, dpc, sls, payload);
			M3UAConfiguration.clientM3UAMgmt.sendMessage(mtp3TransferPrimitive);
		} catch (Exception e) {
			log.error("Error tx rlc : {}", mtpMsg, e);
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////

	
	private void bypass2(AbstractISUPMessage isupMsg, Mtp3TransferPrimitive mtpMsg, String cicKey) {
		try {
			log.debug("{} | BYPASS: {} -> {} : {}", cicKey, mtpMsg.getOpc(), mtpMsg.getDpc(),
					isupMsg.getMessageType().getMessageName());
			M3UAConfiguration.clientM3UAMgmt.sendMessage(mtpMsg);
			// flush the message queue
			//bypassQueue(mtpMsg, cicKey);
		} catch (Exception e) {
			log.error("Error handling also failed: {}", isupMsg, e);
		}
	}

	private boolean saveQueue(Mtp3TransferPrimitive mtpMsg,int command,String msgName, String cicKey) {
		try {

			String hexDump = Hex.encodeHexString(mtpMsg.encodeMtp3());
			Long idx = redisTemplate.opsForList().rightPush(cicKey, hexDump);
			log.debug("save queue cicKey: {} msg: {} idx: {} | {}", cicKey, msgName, idx, hexDump);
			return true;
		} catch (Exception e) {
			log.error("Cannot enqueue msg", e);
			return false;
		}
	}
	

	 
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	

		


		
	

		public String getStateName(int state) {
			if (state>=stateList.size()) {
				return "unkown";
			}
			return stateList.get(state);
		}
		
		public String displayStates() {
			String res="";
			   for (String key : cicMap.keySet()) {
			        res+=String.format("%s %d %s\n",key,cicMap.get(key),getStateName(cicMap.get(key).state));
			    }
			   if (res.isEmpty())res="empty\n";
			   
			   
			   return res;
		}
		
		public String displayConfig() {
			return String.format("handle mode %d, sctp worker thread %d, worker thread mode %d, worker thread num %d",props.getHandleMode(),props.getSctpWorkerThread(),props.getWorkerThreadMode(),props.getWorkerThreadNum());
		}
		////////////////////////////

		
		/////////////////////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		public class CicData{
			String key;
			int cic;
			int state;
			Lock lock;
			ConcurrentLinkedQueue<Mtp3TransferPrimitive> queue;
			public CicData(String cicKey,int cic) {
				this.key=cicKey;
				lock=new ReentrantLock();
				state=0;
				queue=new ConcurrentLinkedQueue<Mtp3TransferPrimitive>();
			}
		}
		
	    public void doHandleISUP(Mtp3TransferPrimitive mtpMsg) {
	    	
	    	
	    
	    		if (executor==null) {
	    			log.error("executor is null");
	    		}else {
	    			//save to concurent link queue
	    			CicData cd=saveLinkQueue(mtpMsg);
	    			if (cd!=null) {
	    				executor.execute(runAsyncMode1(mtpMsg,cd));	
	    			}
	    				
	    		}
	    		
	    	
	    }
	    
		private void handleMtp(Mtp3TransferPrimitive mtpMsg,CicData cd) {
			try {

				byte[] payload = mtpMsg.getData();
				int commandCode = payload[2];

				
				//log.debug("mode1 {} {} ni {} mp {} opc {} dpc {} sls {}",cicKey,hexString,ni,mp,opc,dpc,sls);

				AbstractISUPMessage msg = (AbstractISUPMessage) isupMessageFactory.createCommand(commandCode);
				try {
					msg.decode(payload, isupMessageFactory, isupParameterFactory);
				} catch (ParameterException e) {
					log.error("Error decoding of incoming Mtp3TransferPrimitive" + e.getMessage(), e);
					return ;
				}
				msg.setSls(mtpMsg.getSls()); // store SLS...
		
				Integer state=cd.state;
			
				
		        try {
			    		int direction=mtpMsg.getDpc()==props.getLocalPointCode()?1000:0;
			    		int code=msg.getMessageType().getCode();
			    		int reject=0;
		    		    if ( code==InitialAddressMessage.MESSAGE_CODE) {
			    			reject=handleIam(msg, mtpMsg, cd.key)?0:10000;
			    		}
			    		int event=code+direction+reject;
			    		
			    		runFsm(state, event,msg,mtpMsg,cd.key,cd.cic,cd);
			            
			        	return ;
			        } catch(Exception e) {
			            log.error("handleIsupEventFromQueue {}",e);
			            return ;
			        }
				
			
			
			} catch (Exception e) {
				log.error("handle mtp exception", e);
				return ;
			} 

		}
		
		public void handleISUP(Mtp3TransferPrimitive mtpMsg,CicData cd) {
			log.debug("handle isup {} {}",cd.key,cd.cic);
			
			//Integer state = (Integer) serializableRedisTemplate.opsForValue().get(cicKey + Constants.CIC_MASK);
			if(!cd.lock.tryLock()) {
				log.debug("try lock fail %s",cd.key);
				return;
			}
			log.debug("lock {}",cd.key);
			Integer state=cd.state;
				//pop from queue
				while(true) {
					
				try {
					Mtp3TransferPrimitive msg = cd.queue.poll();
					if (msg==null) {
						unlock(cd);
						//check queue again
						long queueSize=cd.queue.size();
						if(queueSize==0) {
							return;
						}
						if (cd.lock.tryLock()) {
							log.debug("lock {}",cd.key);
							continue;
						}
						return;
					}else {
						handleMtp(msg, cd);
					}
					
				}catch(Exception e) {
					log.error("handleIsupMoreEvent exception1 {}",e);
				}
			}
		}
		
		 private Runnable runAsyncMode1( Mtp3TransferPrimitive mtpMsg,CicData cd ) {
		        return new Runnable() {
		            public void run() {
		            	handleISUP(mtpMsg,cd);
		            }
		        };
		 }
			
		private void unlock(CicData cd) {
				try {
					cd.lock.unlock();	
					log.debug("unlock {}",cd.key);
				}catch(Exception e) {
					log.error("error unlock key: {} {}",cd.key,e);
				}
				
			
		}
		
		public CicData saveLinkQueue(Mtp3TransferPrimitive mtpMsg) {
			// 2(CIC) + 1(CODE)
			byte[] payload = mtpMsg.getData();
			int commandCode = payload[2];
			
			AbstractISUPMessage msg = (AbstractISUPMessage) isupMessageFactory.createCommand(commandCode);
			try {
				msg.decode(payload, isupMessageFactory, isupParameterFactory);
			} catch (ParameterException e) {
				log.error("Error decoding of incoming Mtp3TransferPrimitive" + e.getMessage(), e);
				return null;
			}

			int cic = Optional.ofNullable(msg.getCircuitIdentificationCode()).map(CircuitIdentificationCode::getCIC)
					.orElse(-1);
			
			//cic key=less point code + more point code + cic
			String cicKey = mtpMsg.getOpc() + "_" + mtpMsg.getDpc() + "_" + cic; 
			if (mtpMsg.getDpc()<mtpMsg.getOpc()) {
				cicKey = mtpMsg.getDpc() + "_" + mtpMsg.getOpc() + "_" + cic;
			}
			
			try {
				CicData cd=cicMap.get(cicKey);
				if (cd==null) {
					cd=new CicData(cicKey,cic);
					cicMap.put(cicKey, cd);
				}
				cd.queue.add(mtpMsg);
				log.debug("save link queue cicKey: {} cic {} command {} {}", cicKey, cic,commandCode,msg.getMessageType().getMessageName().toString());
				return cd;
			} catch (Exception e) {
				log.error("Cannot enqueue msg", e);
				return null;
			}
						
		}			
}
