package de.christophseidl.util.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.christophseidl.util.java.util.FormattingDelegate;

public class CollectionsUtil {
	
	public static <T> void addAll(T[] array, Collection<T> collection) {
		if (array != null) {
			for (T element : array) {
				collection.add(element);
			}
		}
	}
	
	public static <T> List<T> toList(T[] array) {
		List<T> list = new ArrayList<T>();
		
		addAll(array, list);
		return list;
	}
	
	public static <T> String toString(Collection<T> values) {
		return StringUtil.implode(values, StringUtil.defaultImplodeSeparator, StringUtil.defaultFormattingDelegate);
	}
	
	public static <T> String toString(Collection<? extends T> values, FormattingDelegate<T> formattingDelegate) {
		return StringUtil.implode(values, StringUtil.defaultImplodeSeparator, formattingDelegate);
	}
	
	public static <T extends U, U> void filter(Collection<U> rawList, Collection<T> filteredList, Class<T> type) {
		if (rawList == null) {
			return;
		}
		
		filteredList.clear();
		
		for (U rawElement : rawList) {
			try {
				T typedElement = type.cast(rawElement);
				filteredList.add(typedElement);
			} catch(ClassCastException e) {
				//Do nothing.
				continue;
			}
		}
	}
	
	public static <T extends U, U> List<T> filter(List<U> rawList, Class<T> type) {
		if (rawList == null) {
			return null;
		}
		
		List<T> filteredList = new ArrayList<T>();
		
		filter(rawList, filteredList, type);
		
		return filteredList;
	}
}
