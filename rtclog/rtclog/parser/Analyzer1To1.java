package rtclog.parser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtclog.parser.StringHelper.MatchInfo;

public class Analyzer1To1 {
	static final Logger logger = LoggerFactory.getLogger(Analyzer1To1.class.getSimpleName());
	   public void analyze(String[] fileNames) throws Exception{
		   logger.info("files=[{}]", fileNames.length);
		   State[] states = new State[fileNames.length];
		   for(int i = 0; i < states.length; i++){
			   State state = new State();
			   states[i] = state;
			   state.data = readFile(fileNames[i], "file-" + (i+1));
			   logger.info("  [{}]=[{}]", state.data.getShortName(), state.data.getFileName());
		   }
		   
		   for(int i = 0; i < states.length; i++){
			   analyze(states[i], states);
		   }
	   }
	   
	   void analyze(State state, State[] states){
			final ConferecneMessage.Field[] printFields = {
					ConferecneMessage.Field.OP,
					ConferecneMessage.Field.CAND,
					ConferecneMessage.Field.SENT_AP,
					ConferecneMessage.Field.RECV_AP,
					ConferecneMessage.Field.CONN,
			};
			
		   logger.info("---");
		   logger.info("analyzing [{}] ...", state.data.getShortName());
	    	for(LogFileData.Session session : state.data.getSessions()){
	    		logger.info("session=[{}]", session.getSessionId());
	    		for(ConferecneMessage msg : session.getMsgs()){
	    			boolean warning = false;
	    			StringBuilder strBuilder = new StringBuilder();
					if(msg.getPeer() == null && states.length > 1){
						boolean found = true;
			    		for(String tsxId : msg.getFieldValues(ConferecneMessage.Field.TSXID)){
			    			for(State other : states){
			    				if(other == state) {
			    					continue;
			    				}
			    				ConferecneMessage peerMsg = other.data.getMsgByTsxId(tsxId);
			    				if(peerMsg != null){
			    					String peer = other.data.getShortName() + "," + peerMsg.lineNumber;
			    					msg.setPeer(peer);
			    				}else{
			    					found = false;
			    				}
			    			}
			    		}
			    		if(!found){
			    			msg.setPeer(null);
			    		}
					}
					
					strBuilder.append(msg.toString(printFields));
					if(!msg.isAck() || msg.getPeer() == null){
						warning = true;
						msg.append(strBuilder, ConferecneMessage.Field.TSXID);
					}
					
	    			if(!warning){
	    				logger.info("  {}", strBuilder.toString());
	    			}else{
	    				logger.warn("  {}", strBuilder.toString());
	    			}
	    			
	    		}
	    	}
	   }
	   
	   
	   
	   static class State{
		   LogFileData data;
		   int lastLineNum = -1;
	   }
	   
	   static LogFileData readFile(String fileName, String shortName) throws Exception{
	    	
	    	BufferedReader bufferedReader = null;
			try {
				LogFileData logData = new LogFileData();
				logData.setFileName(fileName);
				logData.setShortName(shortName);
				
				Map<String, ConferecneMessage> pendingAckMsgs = new HashMap<>();
				Map<String, ConferecneMessage> pendingOp0Msgs = new HashMap<>();
				bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)), 40960);
				String preLine = null;
				String line = null;
				int lineNumber = 0;
				while((line = bufferedReader.readLine()) != null){
					++lineNumber;
					if(line.contains("ns : CONFERENCE")){
						ConferecneMessage msg = ConferecneMessage.newFromeLine(lineNumber, preLine, line);
						logData.addMessage(msg);
						if(!msg.ack){
							for(String id : msg.getFieldValues(ConferecneMessage.Field.ID)){
								pendingAckMsgs.put(id, msg);
							}
						}
						if(msg.getDir() == ConferecneMessage.Direction.RECV){
							for(String str : msg.getFieldValues(ConferecneMessage.Field.TSXID)){
								ConferecneMessage pendingMsg = pendingOp0Msgs.remove(str);
								if(pendingMsg != null){
									pendingMsg.setPeer("self,"+msg.lineNumber);
									msg.setPeer("self,"+pendingMsg.lineNumber);
								}
							}
						}else if(msg.getDir() == ConferecneMessage.Direction.SEND && msg.findValue(ConferecneMessage.Field.OP, "0")){
							for(String str : msg.getFieldValues(ConferecneMessage.Field.TSXID)){
								pendingOp0Msgs.put(str, msg);
							}
						}
					}else{
						String metaIdRegex = "(meta_id) : ([^\\\"\\,]+)";  // [(meta_id) : ([^\"\,]+)]
						List<MatchInfo> infos = StringHelper.getAllMatchGroups(line, metaIdRegex);
						if(infos != null){
							ConferecneMessage.Direction dir =  ConferecneMessage.splitDirection(preLine);
							if(dir == ConferecneMessage.Direction.RECV){
								for (MatchInfo info: infos) {
									for(int i = 0; i < info.groups.length/2; i++){
										String value = info.groups[1+2*i+1];
										ConferecneMessage pendingMsg = pendingAckMsgs.remove(value);
										if(pendingMsg != null){
											pendingMsg.setAck(true);
										}
									}
								}
							}
						}
						
					}
					preLine = line;
				}
				return logData;
			} finally {
				if(bufferedReader != null){
					bufferedReader.close();
				}
			}
	    }
	    

	    static class LogFileData{
	        static class Session{
	        	String sessionId;
	        	List<ConferecneMessage> msgs = new LinkedList<ConferecneMessage>();
	        	
	    		public Session(String sessionId) {
	    			super();
	    			this.sessionId = sessionId;
	    		}
	    		public void addMessage(ConferecneMessage msg){
	    			this.msgs.add(msg);
	    		}
	    		
				public String getSessionId() {
					return sessionId;
				}
				public List<ConferecneMessage> getMsgs() {
					return msgs;
				}
	        }
	        
			String fileName;
			String shortName;
	    	List<ConferecneMessage> msgs = new LinkedList<ConferecneMessage>();
	    	Map<String, ConferecneMessage> msgMap = new HashMap<String, ConferecneMessage>();
	    	List<Session> sessions = new LinkedList<Session>();
	    	Map<String, Session> sessionMap = new HashMap<String, Session>();
	    	public void addMessage(ConferecneMessage msg){
	    		msgs.add(msg);
	    		for(String str : msg.getFieldValues(ConferecneMessage.Field.TSXID)){
	    			this.msgMap.put(str, msg);
	    		}
	    		for(String str : msg.getFieldValues(ConferecneMessage.Field.SESSID)){
	    			Session session = sessionMap.get(str);
	    			if(session == null){
	    				session = new Session(str);
	    				sessionMap.put(str, session);
	    				sessions.add(session);
	    			}
	    			session.addMessage(msg);
	    		}
	    	}

	    	public String getFileName() {
				return fileName;
			}

			public void setFileName(String fileName) {
				this.fileName = fileName;
			}

			public String getShortName() {
				return shortName;
			}

			public void setShortName(String shortName) {
				this.shortName = shortName;
			}

			public ConferecneMessage getMsgByTsxId(String tsxId){
	    		return this.msgMap.get(tsxId);
	    	}
	    	
			public List<ConferecneMessage> getMsgs() {
				return msgs;
			}
	    	
			public List<Session> getSessions() {
				return this.sessions;
			}
			
	    }
	    
	    static class ConferecneMessage{
	    	public static enum Direction{
	    		NONE("?=?"), SEND("==>"), RECV("<==");
	    		String name;
	    		private Direction(String name){
	    			this.name = name;
	    		}
	    		@Override
	    		public String toString() {
	    			return name;
	    		}
	    	}
	    	
	    	public static enum Field{
	    		ID("id", "\\{ id \\: (\\S+),")
	    		, OP("op", "\\\"op\\\"\\:(\\d+)")
	    		, TSXID("tsxId", "tsxId\\\"\\:\\\"([^\\\"\\,]+)\\\"")
	    		, SESSID("session_id","session_id : ([^\\\"\\,]+)") // (session_id) : ([^\"\,]+)
	    		, CAND("cand", "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\s\\d{1,5}\\styp\\s[a-z]+)") // \d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\s\d{1,5}\styp\s[a-z]+
	    		, SENT_AP("sentAP", "\\\"sentAP\\\\\\\":(\\d+)") // \"sentAB\\\":(\d+)
	    		, RECV_AP("recvAP", "\\\"recvAP\\\\\\\":(\\d+)")
	    		, CONN("conn", "\\\"conn\\\\\\\":\\\\\\\"([a-z]+)\\\\\\\"") // \"conn\\\":\\\"relay\\\"
	    		;
	    		String name;
	    		String regex;
	    		private Field(String name, String regex){
	    			this.name = name;
	    			this.regex = regex;
	    		}
				public String getName() {
					return name;
				}

				public String getRegex() {
					return regex;
				}
	    	}
			
	    	final static String ZERO_TIME_STR = "00:00:00:000";
	    	
	    	int lineNumber = -1;
	    	String timeString = ZERO_TIME_STR;
	    	Direction dir = Direction.NONE;
	    	Map<String, List<String>> valuesMap = new HashMap<String, List<String>>();
	    	boolean ack = false;
	    	String peer = null;
	    	
	    	public ConferecneMessage(){
	    		for(Field field : Field.values()){
	    			this.valuesMap.put(field.getName(), new ArrayList<String>(2));
	    		}
	    	}
	    	
	    	public String getTimeString() {
				return timeString;
			}

			public Direction getDir() {
				return dir;
			}
	    	
	    	public boolean isAck() {
				return ack;
			}

			public void setAck(boolean ack) {
				this.ack = ack;
			}

			public String getPeer() {
				return peer;
			}

			public void setPeer(String recv) {
				this.peer = recv;
			}

			public List<String> getFieldValues(Field field){
	    		return this.valuesMap.get(field.getName());
	    	}
			
			public boolean findValue(Field field, String value){
				List<String> list = getFieldValues(field);
				if(list != null){
					for(String str : list){
						if(str.equals(value)){
							return true;
						}
					}
				}
				return false;
			}
			
			public void addToField(Field field, String value){
	    		List<String> list = this.valuesMap.get(field.getName());
	    		if(list == null){
	    			list = new ArrayList<String>(2);
	    			this.valuesMap.put(field.getName(), list);
	    		}
	    		list.add(value);
	    	}
	    	
	    	@Override
			public String toString() {
	    		return toString(ConferecneMessage.Field.values());
			}
	    	
	    	public String toString(ConferecneMessage.Field[] fields) {
				StringBuilder strBuilder = new StringBuilder();
				strBuilder.append(String.format("%4d", this.lineNumber));
				strBuilder.append(", ").append(this.getTimeString());
				strBuilder.append(", ").append(this.getDir());
				for(ConferecneMessage.Field field : fields){
					this.append(strBuilder, field);
				}
				if(!this.ack){
					strBuilder.append(", ack=").append(this.ack);					
				}
				if(this.peer == null){
					strBuilder.append(", peer=[").append(this.peer).append("]");
				}
				return strBuilder.toString();
	    	}
	    	
	    	public ConferecneMessage append(StringBuilder strBuilder, ConferecneMessage.Field field){
				List<String> values =  this.getFieldValues(field);
				for(String str : values){
					strBuilder.append(", ")
						.append(field.name).append("=")
						.append("[").append(str).append("]");
				}
				return this;
	    	}

			public static ConferecneMessage newFromeLine(int lineNumber,String preLine, String line){
				ConferecneMessage msg = new ConferecneMessage();
				msg.lineNumber = lineNumber;
				msg.timeString = splitTimeString(preLine);
				msg.dir = splitDirection(preLine);
				if(msg.dir == Direction.RECV){
					msg.setAck(true);
				}
				
				for(Field field : Field.values()){
					List<MatchInfo> infos = StringHelper.getAllMatchGroups(line, field.getRegex());
					if(infos != null){
						for (MatchInfo info: infos) {
							for(int i = 1; i < info.groups.length; i++){
								String value = info.groups[i];
								msg.addToField(field, value);
							}
						}
					}
				}
				return msg;
	    	}
	    	
	        static String splitTimeString(String line){    	
	        	
	        	if(line == null){
	        		return ZERO_TIME_STR;
	        	}else{
	        		return line.substring(12, 12+ZERO_TIME_STR.length());
	        	}
	        }
	        
	        static Direction splitDirection(String line){
	        	String dirString = splitDirectionString(line);
				if(dirString.equals("SEND:")){
					return Direction.SEND;
				}else if(dirString.equals("RECV:")){
					return Direction.RECV;
				}else{
					return Direction.NONE;
				}
	        }
	        static String splitDirectionString(String line){
	        	if(line == null){
	        		return "NONE:";
	        	}else{
	        		return line.substring(line.length()-5);
	        	}
	        	
	        }
	    }
}
