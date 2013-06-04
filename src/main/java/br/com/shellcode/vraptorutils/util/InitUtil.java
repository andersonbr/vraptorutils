package br.com.shellcode.vraptorutils.util;

public class InitUtil {
	@SuppressWarnings("unchecked")
	public static <T> T setIfNull(T obj, Object defaultValue) {
		if (obj == null)
			obj = (T) defaultValue;
		return obj;
	}

	public static Integer setIfNullWithMax(Integer obj, int defaultValue, int maxValue) {
		if (obj == null)
			obj = defaultValue;
		if (obj > maxValue)
			obj = maxValue;
		return obj;
	}
}
