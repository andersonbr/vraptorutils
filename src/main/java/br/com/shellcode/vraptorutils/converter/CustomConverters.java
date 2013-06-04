package br.com.shellcode.vraptorutils.converter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.shellcode.vraptorutils.helper.MapHelper;
import br.com.shellcode.vraptorutils.jpa.JPAUtil.OP;
import br.com.shellcode.vraptorutils.jpa.JPAUtil.OR;

public class CustomConverters {
	public static Map<String, OP> getOperationMap(List<MapHelper> list) {
		return getMap(list, String.class, OP.class);
	}
	public static Map<String, OR> getOrderMap(List<MapHelper> list) {
		return getMap(list, String.class, OR.class);
	}

	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> getMap(List<MapHelper> list,
			Class<K> classK, Class<V> classV) {
		Map<K, V> map = new HashMap<K, V>();
		if (list != null) {
			for (MapHelper h : list) {
				Object en = h.getValue();
				if (en instanceof String) {
					String svalue = (String) en;
					try {
						Method m = classV.getDeclaredMethod("valueOf",
								String.class);
						en = m.invoke(classV, svalue);
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
					map.put((K) h.getKey(), (V) en);
				}
			}
		}
		return map;
	}
}
