package br.com.shellcode.vraptorutils.helper;

import java.util.List;

public class ResultGridHelper<T> {

	private List<T> list;
	private Integer total = 1;

	/**
	 * 
	 * @param rows
	 *            Rows from current page
	 */
	public ResultGridHelper(List<T> list) {
		this.setList(list);
	}

	/**
	 * 
	 * @param rows
	 *            Rows from current page
	 * @param total
	 *            Total of pages
	 */
	public ResultGridHelper(List<T> list, Integer total) {
		this.setList(list);
		this.total = total;
	}

	public List<T> getList() {
		return list;
	}

	public Integer getTotal() {
		return total;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}
}