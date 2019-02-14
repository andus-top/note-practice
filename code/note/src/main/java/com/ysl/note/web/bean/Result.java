package com.ysl.note.web.bean;
import java.io.Serializable;

/**
 * 用于给前端返回json数据的封装类
 * @author YSL
 * 2018-09-05 15:26
 */
public class Result implements Serializable {
	
	private static final long serialVersionUID = -5674931711357734810L;

	private boolean status = false; // 本次操作结果，成功与否

	private int statusCode; // 本次操作的状态码

	private String msg; // 提示信息

	private Object data; // 返回的数据

	public Result() {
	}

	public Result(boolean status, String msg) {
		this.status = status;
		this.msg = msg;
	}

	public Result(boolean status, Object data) {
		this.status = status;
		this.data = data;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "Result [status=" + status + ", statusCode=" + statusCode + ", msg=" + msg + ", data=" + data + "]";
	}
	
	
}
