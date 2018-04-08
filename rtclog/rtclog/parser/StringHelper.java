package rtclog.parser;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringHelper {
	public static void main1(String[] args) {
		String str = "{ verison : MSYNC_V1, compress_algorimth : 0, command : SYNC, encrypt_type : [ 0 ], payload : { meta : { id : 4593, to : @easemob.com, ns : CONFERENCE, payload : { session_id : lz41522723330351, operation : MEDIA_REQUEST, route_flag : 1, content : {\"op\":0,\"callVersion\":\"2.0.0\",\"tsxId\":\"1522723330351-15\",\"audio\":1,\"peer\":\"easemob-demo#chatdemoui_lz1@easemob.com/\",\"push\":0} } } } }";
		System.out.println(str);

		String regex = "meta \\: \\{ (id) \\: (\\S+).*\\\"(op)\\\"\\:(\\d+),.*(tsxId)\\\"\\:\\\"([^\\\"\\,]+)\\\"";

		List<MatchInfo> infos = getAllMatchGroups(str, regex);
		for (MatchInfo info: infos) {
			System.out.println("args = [" + args + "]");
		}
	}
	
	public static class MatchInfo{
		public final int index;
		public final String[] groups;
		
		public MatchInfo(int index, String[] groups) {
			this.index = index;
			this.groups = groups;
		}
	}
	public static List<MatchInfo> getAllMatchGroups(String text, String regex){
		List<MatchInfo> result = null;;
		
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(text);
		
		while (matcher.find()) {
			int index = matcher.start();
			String[] groups = null;
			
			int groupCount = matcher.groupCount();
			
			if(groupCount > 0) {
				groups = new String[groupCount + 1];
				
				for(int i = 0; i <= groupCount; i++){
					groups[i] = matcher.group(i);
				}
			}
			
			MatchInfo matchInfo = new MatchInfo(index, groups);
			
			if(result == null){
				result = new LinkedList<>();
			}
			result.add(matchInfo);
		}
		
		return result;
	}
	
	/**
	 * System.setProperty("aaa.xxx", "4566"); System.setProperty("bbb.ccc",
	 * "7777"); ${aaa.xxx}/1111/${bbb.ccc} == 4566/1111/7777
	 * 
	 * @param properties
	 * @return
	 */
	public static String replaceEL(String elExpression, @SuppressWarnings("rawtypes") Map properties) {
		String regex = "(\\$\\{([^\\$\\{\\}]+)\\})";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(elExpression);

		int last = 0;
		StringBuilder descBuilder = new StringBuilder();

		while (matcher.find()) {
			String replaceProperty = matcher.group(1);
			String replacePropertyKey = matcher.group(2);

			int index = matcher.start();
			descBuilder.append(elExpression.substring(last, index));

			Object replaceString = properties.get(replacePropertyKey);
			descBuilder.append(replaceString == null ? replaceProperty : String.valueOf(replaceString));

			last = index + replaceProperty.length();
		}
		descBuilder.append(elExpression.substring(last, elExpression.length()));

		return descBuilder.toString();
	}
}
