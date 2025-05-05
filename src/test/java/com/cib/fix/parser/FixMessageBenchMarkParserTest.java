package com.cib.fix.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class FixMessageBenchMarkParserTest {
	private byte[] toFixBytes(String str) {
        return str.getBytes();
    }



    @Test
    public void testFixMapShouldReturnCorrectValue() {
        byte[] fixMsg = toFixBytes("8=FIX.4.4|35=D|55=ABC");
        Map<Integer,String> map = FixMessageBenchMarkParser.parse(fixMsg);
        
        assertEquals("FIX.4.4", map.get(8));
        assertEquals("D", map.get(35));
        assertEquals("ABC", map.get(55));
        assertNull(map.get(999)); // Non-existent tag
    }

    
    @Test
    public void testFixMapShouldReturnCorrectValueWithMutlipleEqualSign() {
        byte[] fixMsg = toFixBytes("8=FIX.4.4|35=D=K|55=ABC");
        Map<Integer,String> map = FixMessageBenchMarkParser.parse(fixMsg);
        
        assertEquals("FIX.4.4", map.get(8));
        assertEquals("D=K", map.get(35));
        assertEquals("ABC", map.get(55));
        assertNull(map.get(999)); // Non-existent tag
    }
    
    @Test
    public void testEmptyMessageShouldReturnNull() {
        byte[] empty = new byte[0];
        Map<Integer,String> map = FixMessageBenchMarkParser.parse(empty);
        assertNull(map.get(8)); // No fields
    }

    @Test(expected = NumberFormatException.class)
    public void testMalformedTagsShouldThrowExceptionIfTagIsMissing() {
        byte[] fixMsg = toFixBytes("=BAD|35=D|XYZ=123");
        Map<Integer,String> map = FixMessageBenchMarkParser.parse(fixMsg);
        
   }

    @Test(expected = NumberFormatException.class)
    public void testMalformedTagsShouldThrowExceptionIfTagIsNonNumeric() {
        byte[] fixMsg = toFixBytes("AV=BAD|35=D|XYZ=123");

        Map<Integer,String> map = FixMessageBenchMarkParser.parse(fixMsg);

   }


	@Test
	public void testFixMapShouldReturnAllField(){
		 
		byte[] fixMsg = toFixBytes("8=FIX.4.2|9=176|35=D|49=SenderId|56=TargetId|34=2|52=20250431-12:30:00|11=12345|21=1|40=5|54=2|60=20250430-12:30:00|10=128|");
		Map<Integer,String> map = FixMessageBenchMarkParser.parse(fixMsg);
		
		assertEquals("FIX.4.2", map.get(8));
		assertEquals("176", map.get(9));
		assertEquals("D", map.get(35));
		assertEquals("SenderId", map.get(49));
		assertEquals("TargetId", map.get(56));
		assertEquals("2", map.get(34));
		assertEquals("20250431-12:30:00", map.get(52));
		assertEquals("12345", map.get(11));
		assertEquals("1", map.get(21));
		assertEquals("5", map.get(40));
		assertEquals("2", map.get(54));
		assertEquals("20250430-12:30:00", map.get(60));
		assertEquals("128", map.get(10));
	}
	
}
