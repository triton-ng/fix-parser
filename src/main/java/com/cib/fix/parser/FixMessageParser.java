package com.cib.fix.parser;

public final class FixMessageParser {
	private static final byte DELIMITER = 0x01;
	private static final byte EQUALS = 0x3D;

	public interface FieldHandler {
		void handleField(int tag, byte[] message, int valueS, int valueE);
	}

	// static method guarantee thread safety by
	/*
	 * 1. No shared mutable state: All variables are stack-allocated 2. No
	 * instance fields: Nothing persists between method calls 3. Input message
	 * array isn't modified during parsing
	 */
	public static void parse(byte[] message, FieldHandler handler) {
		int fieldStart = 0;
		int currentTag = 0;
		boolean parsingTag = true;
		// Iterate through the message instead of using String.split to avoid
		// string object creation
		for (int i = 0; i < message.length; i++) {
			byte b = message[i];

			if (b == EQUALS && parsingTag) {
				currentTag = parseIntTag(message, fieldStart, i);
				fieldStart = i + 1;
				parsingTag = false;
			} else if (b == DELIMITER) {
				if (!parsingTag) {
					handler.handleField(currentTag, message, fieldStart, i);
				}
				fieldStart = i + 1;
				parsingTag = true;
			}
		}

		if (!parsingTag && fieldStart < message.length) {
			handler.handleField(currentTag, message, fieldStart, message.length);
		}
	}

	// Customer Integer Parser to avoid creation of Integer Object
	private static int parseIntTag(byte[] bytes, int start, int end) {
		int result = 0;
		for (int i = start; i < end; i++) {
			byte b = bytes[i];

			// FIX Tag must be numeric
			if (b < '0' || b > '9')
				throw new NumberFormatException();
			result = result * 10 + (b - '0');
		}
		return result;
	}

	// Implementing Custom Map to optimize Map Operation. Inner Class is use
	// instead of Standard Class for better locality and do now require
	// additional metadata for class creation
	// The map only store index of start / end position of a tag which do not
	// additional string object creation
	public static final class FixMap {
		private int[] keys;
		private int[] starts;
		private int[] ends;
		private byte[] source;
		private int size;

		public FixMap(byte[] source, int initialCapacity) {
			this.keys = new int[initialCapacity];
			this.starts = new int[initialCapacity];
			this.ends = new int[initialCapacity];
			this.source = source;
		}

		/**
		* Returns value of a tag in byte[]
		*
		* @param  key  tag of the fix message
		* @param  start startIndex of tag in byte[]
		* @param  end  endIndex of tag in byte[]
		*/
		private void put(int key, int start, int end) {
			if (size >= keys.length)
				resize();
			keys[size] = key;
			starts[size] = start;
			ends[size] = end;
			size++;
		}

		/**
		* Returns value of a tag in byte[]
		*
		* @param  key  Fix Message Tag as int
		* @return value of tag in byte[]
		*/
		public byte[] get(int key) {
			for (int i = 0; i < size; i++) {
				if (keys[i] == key) {
					int len = ends[i] - starts[i];
					byte[] value = new byte[len];
					System.arraycopy(source, starts[i], value, 0, len);
					return value;
				}
			}
			return null;
		}

		/**
		* Returns value of a tag in String
		*
		* @param  key  Fix Message Tag as int
		* @return value of tag in String
		*/
		public String getValueAsString(int key) {
			for (int i = 0; i < size; i++) {
				if (keys[i] == key) {
					int len = ends[i] - starts[i];
					byte[] value = new byte[len];
					System.arraycopy(source, starts[i], value, 0, len);
					return new String(value);
				}
			}
			return null;
		}

		/**
		* This method return entire fixMap as String after parsing FIX message
		*
		* @return      the entire fixMap 
		*/
		public String toString() {
			StringBuffer strBuffer = new StringBuffer();
			for (int i = 0; i < size; i++) {
				int len = ends[i] - starts[i];
				byte[] value = new byte[len];
				System.arraycopy(source, starts[i], value, 0, len);
				strBuffer.append(keys[i]).append("=").append(new String(value))
						.append("|");
			}

			return strBuffer.toString();
		}

		private void resize() {
			int newCapacity = keys.length * 2;
			int[] newKeys = new int[newCapacity];
			int[] newStarts = new int[newCapacity];
			int[] newEnds = new int[newCapacity];

			System.arraycopy(keys, 0, newKeys, 0, size);
			System.arraycopy(starts, 0, newStarts, 0, size);
			System.arraycopy(ends, 0, newEnds, 0, size);

			keys = newKeys;
			starts = newStarts;
			ends = newEnds;
		}
	}

	public static FixMap parseMessage(byte[] message) {
		FixMap map = new FixMap(message, 16);
		parse(message, (tag, msg, start, end) -> map.put(tag, start, end));
		return map;
	}
}