package br.com.shellcode.vraptorutils.helper;

public class MapHelper {
	private Object key;
	private Object value;

	public MapHelper() {
	}

	public MapHelper(Object key, Object value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * @return the key
	 */
	public Object getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(Object key) {
		this.key = key;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}
}
