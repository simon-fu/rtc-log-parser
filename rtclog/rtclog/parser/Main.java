package rtclog.parser;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtclog.parser.StringHelper.MatchInfo;

public class Main {
	static final Logger logger = LoggerFactory.getLogger(Main.class);
	
    public static void main(String[] args) throws Exception {
    	Analyzer1To1 analyzer = new Analyzer1To1();
    	analyzer.analyze(args);
//    	testRegex();
    }
    
    static void testRegex(){
//    	String line = "candidate:2742861829 1 udp 1686052607 124.205.209.110 36791 typ srflx raddr 172.17.3.89 ";
//    	// \d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\s\d{1,5}\styp\s[a-z]+
//    	String regex = "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\s\\d{1,5}\\styp\\s[a-z]+)"; 
    	
    	String line = "{\\\"conn\\\":\\\"relay\\\",\\\"sentAB\\\":0,\\\"recvAB\\\":0,\\\"os\\\":\\\"a\\\",\\\"rvfrm\\\":0}";
//    	String regex = "\\\"sentAB\\\\\\\":(\\d+)";  // \"sentAB\\\":(\d+)
    	String regex = "\\\"conn\\\\\\\":\\\\\\\"([a-z]+)\\\\\\\""; // \"conn\\\":\\\"relay\\\"
    
		List<MatchInfo> infos = StringHelper.getAllMatchGroups(line, regex);
		logger.info("infos=[{}]", infos);
		if(infos != null){
			for (MatchInfo info: infos) {
				logger.info("info=[{}]", info);
				logger.info("info.groups=[{}]", (Object)info.groups);
				for(int i = 0; i < info.groups.length; i++){
					logger.info("group[{}]=[{}]", i, info.groups[i]);;
				}
			}
		}
    }

}
