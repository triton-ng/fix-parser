package com.cib.fix;

import java.util.HashMap;
import java.util.Map;

import com.cib.fix.parser.FixMessageBenchMarkParser;
import com.cib.fix.parser.FixMessageParser;


public class FixParser {
	private static final char DELIMITER = '\u0001';
    public static void main(String[] args) {

        String fixMessage = "8=FIX.4.2|9=176|35=D|49=SenderCompId|56=TargetCompId|34=2|52=20250431-12:30:00|11=12345|21=1|40=2|54=1|60=20250430-12:30:00|10=128|";
        System.out.println("Original Message:" + fixMessage);
		byte[] fixMsg = fixMessage.replace("|", "\u0001").getBytes();
		FixMessageParser.FixMap map = FixMessageParser.parseMessage(fixMsg);

    	System.out.println("Parsed Message:" + map.toString());
		System.out.println("Value of Tag 8 :" + map.getValueAsString(8)); //return FIX.4.2
		
		
		Map<Integer, String> benmarkParser = FixMessageBenchMarkParser.parse(fixMessage.getBytes());

    	System.out.println("Parsed Message:" + benmarkParser.toString());
		System.out.println("Value of Tag 8 :" + benmarkParser.get(8)); //return FIX.4.2
        
    }
    
}
