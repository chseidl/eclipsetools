package de.christophseidl.util.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.christophseidl.util.java.util.DefaultFormattingDelegate;
import de.christophseidl.util.java.util.FormattingDelegate;

public class StringUtil {
	protected static final String defaultImplodeSeparator = ", ";
	protected static final FormattingDelegate<Object> defaultFormattingDelegate = new DefaultFormattingDelegate();
	protected static final String defaultExplodePattern = "\\s*,\\s*";
	
	
	public static <T> String implode(T[] values) {
		return implode(values, defaultImplodeSeparator);
	}
	
	public static <T> String implode(T[] values, String separator) {
		return doImplode(values, separator, defaultFormattingDelegate);
	}
	
	//TODO: Duplication: bad, bad, bad - FIX!
	protected static <T> String doImplode(T[] values, String separator, FormattingDelegate<T> formattingDelegate) {
		if (formattingDelegate == null) {
			return "";
		}
		
		boolean isFirst = true;
		String imploded = "";
		
		for (T value : values) {
			if (!isFirst) {
				imploded += separator;
			}
			
			imploded += formattingDelegate.format(value);
			
			isFirst = false;
		}
		
		return imploded;
	}
	
	//Utility methods
	public static <T> String implode(Collection<T> values) {
		return implode(values, defaultImplodeSeparator);
	}
	
	public static <T> String implode(Collection<T> values, String separator) {
		return implode(values, separator, defaultFormattingDelegate);
	}
	
	public static <T> String implode(Collection<? extends T> values, String separator, FormattingDelegate<T> formattingDelegate) {
		if (formattingDelegate == null) {
			return "";
		}
		
		boolean isFirst = true;
		String imploded = "";
		
		for (T value : values) {
			if (!isFirst) {
				imploded += separator;
			}
			
			imploded += formattingDelegate.format(value);
			
			isFirst = false;
		}
		
		return imploded;
	}
	
	public static List<String> explode(String valuesString) {
		return explode(valuesString, defaultExplodePattern);
	}
	
	public static List<String> explode(String valuesString, String splitPattern) {
		List<String> values = new ArrayList<String>();
		explode(valuesString, splitPattern, values);
		return values;
	}
	
	public static void explode(String valuesString, String splitPattern, Collection<String> collection) {
		explode(valuesString, splitPattern, collection, true);
	}
	
	public static void explode(String valuesString, String splitPattern, Collection<String> collection, boolean clearCollection) {
		if (clearCollection) {
			collection.clear();
		}
		
		if (valuesString == null || valuesString.isEmpty()) {
			return;
		}
		
		String[] splitValues = valuesString.split(splitPattern);
		
		for (String splitValue : splitValues) {
			collection.add(splitValue);
		}
	}
	
	public static String firstToUpper(String value) {
		if (value == null || value.isEmpty()) {
			return value;
		}
		
		String firstLetter = value.substring(0, 1);
		String upperFirstLetter = firstLetter.toUpperCase();
		
		if (value.length() == 1) {
			return upperFirstLetter;
		}
		
		String remainingLetters = value.substring(1);
		
		return upperFirstLetter + remainingLetters;
	}
	
	public static String firstToLower(String value) {
		if (value == null || value.isEmpty()) {
			return value;
		}
		
		String firstLetter = value.substring(0, 1);
		String lowerFirstLetter = firstLetter.toLowerCase();
		
		if (value.length() == 1) {
			return lowerFirstLetter;
		}
		
		String remainingLetters = value.substring(1);
		
		return lowerFirstLetter + remainingLetters;
	}
	
	public static String removePrefix(String value) {
		//Check if the string starts with more than one capital letter. If so,
		//remove all initial capital letters except for the last one.
		return value.replaceFirst("([A-Z]+)([A-Z])([a-z0-9_])(.+)$", "$2$3$4");
	}
	
	public static String toCamelCase(String input) {
		//Letter at first position is capitalized
		//All letters after a special character are capitalized
		//Special characters are removed.
		String output = "";
		
		//Initialized with true to capitalize the first letter no matter what
		boolean previousWasSpecialCharacter = true;
		
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			
			if (Character.isAlphabetic(c) || Character.isDigit(c)) {
				if (previousWasSpecialCharacter) {
					c = Character.toUpperCase(c);
				}
				
				output += c;
				previousWasSpecialCharacter = false;
			} else {
				previousWasSpecialCharacter = true;
			}
		}
		
		return output;
	}
}
