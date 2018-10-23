package com.zzg;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class TestGetIP {
	public static void main(String[] args) throws UnknownHostException {
		String ip = getIp();
		System.out.println(ip);
	}
	public static String getIp() throws UnknownHostException{
		InetAddress address = InetAddress.getLocalHost();//获取的是
		return address.getHostAddress();
	}
	
}
