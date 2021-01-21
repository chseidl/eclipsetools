package de.christophseidl.util.java.util;

public class DefaultFormattingDelegate implements FormattingDelegate<Object> {
	@Override
	public String format(Object value) {
		if (value == null) {
			return null;
		}
		
		return value.toString();
	}
}
