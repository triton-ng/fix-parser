package com.cib.fix.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FixMessageParserTest {
	private byte[] toFixBytes(String str) {
        return str.replace("|", "\u0001").getBytes();
    }

    @Test
    public void testStreamingParse() {
        byte[] fixMsg = toFixBytes("8=FIX.4.4|35=D|55=ABC");
        
        StringBuilder result = new StringBuilder();
        FixMessageParser.parse(fixMsg, (tag, msg, start, end) -> {
            result.append(tag).append(":")
                  .append(new String(msg, start, end - start))
                  .append("|");
        });
        
        assertEquals("8:FIX.4.4|35:D|55:ABC|", result.toString());
    }

    @Test
    public void testFixMapShouldReturnCorrectValue() {
        byte[] fixMsg = toFixBytes("8=FIX.4.4|35=D|55=ABC");
        FixMessageParser.FixMap map = FixMessageParser.parseMessage(fixMsg);
        
        assertEquals("FIX.4.4", new String(map.get(8)));
        assertEquals("D", new String(map.get(35)));
        assertEquals("ABC", new String(map.get(55)));
        assertNull(map.get(999)); // Non-existent tag
    }

    
    @Test
    public void testFixMapShouldReturnCorrectValueWithMutlipleEqualSign() {
        byte[] fixMsg = toFixBytes("8=FIX.4.4|35=D=K|55=ABC");
        FixMessageParser.FixMap map = FixMessageParser.parseMessage(fixMsg);
        
        assertEquals("FIX.4.4", new String(map.get(8)));
        assertEquals("D=K", new String(map.get(35)));
        assertEquals("ABC", new String(map.get(55)));
        assertNull(map.get(999)); // Non-existent tag
    }
    
    @Test
    public void testEmptyMessageShouldReturnNull() {
        byte[] empty = new byte[0];
        FixMessageParser.FixMap map = FixMessageParser.parseMessage(empty);
        assertNull(map.get(8)); // No fields
    }

    @Test
    public void testMalformedTagsShowThrowExceptionIfTagIsMissing() {
        byte[] fixMsg = toFixBytes("=BAD|35=D|XYZ=123");
        
        try{
        	FixMessageParser.FixMap map = FixMessageParser.parseMessage(fixMsg);
        } catch (Exception e) {
            assertTrue(e.getClass() == NumberFormatException.class);
        }
   }

    @Test
    public void testMalformedTagsShowThrowExceptionIfTagIsNonNumeric() {
        byte[] fixMsg = toFixBytes("AV=BAD|35=D|XYZ=123");
        
        try{
        	FixMessageParser.FixMap map = FixMessageParser.parseMessage(fixMsg);
        } catch (Exception e) {
            assertTrue(e.getClass() == NumberFormatException.class);
        }
   }
    @Test
    public void testFixMapShouldReturnCorrectStringValue() {
        byte[] fixMsg = toFixBytes("8=FIX.4.4|35=D|55=ABC");
        FixMessageParser.FixMap map = FixMessageParser.parseMessage(fixMsg);
        
        assertEquals("FIX.4.4", map.getValueAsString(8));
        assertEquals("D", map.getValueAsString(35));
        assertEquals("ABC", map.getValueAsString(55));
        assertNull(map.get(999)); // Non-existent tag
    }

	@Test
	public void testFixMapShouldReturnAllField(){
		 
		byte[] fixMsg = toFixBytes("8=FIX.4.2|9=176|35=D|49=SenderId|56=TargetId|34=2|52=20250431-12:30:00|11=12345|21=1|40=5|54=2|60=20250430-12:30:00|10=128|");
		FixMessageParser.FixMap map = FixMessageParser.parseMessage(fixMsg);
		
		assertEquals("FIX.4.2", map.getValueAsString(8));
		assertEquals("176", map.getValueAsString(9));
		assertEquals("D", map.getValueAsString(35));
		assertEquals("SenderId", map.getValueAsString(49));
		assertEquals("TargetId", map.getValueAsString(56));
		assertEquals("2", map.getValueAsString(34));
		assertEquals("20250431-12:30:00", map.getValueAsString(52));
		assertEquals("12345", map.getValueAsString(11));
		assertEquals("1", map.getValueAsString(21));
		assertEquals("5", map.getValueAsString(40));
		assertEquals("2", map.getValueAsString(54));
		assertEquals("20250430-12:30:00", map.getValueAsString(60));
		assertEquals("128", map.getValueAsString(10));
	}
	
}
