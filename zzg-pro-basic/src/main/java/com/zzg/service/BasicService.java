package com.zzg.service;

import org.junit.Test;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.zzg.client.annotation.RemoteInvoke;
import com.zzg.user.model.User;
import com.zzg.user.remote.UserRemote;

@Service
public class BasicService {

	/**
	 * 远程服务的接口注入
	 */
	@RemoteInvoke
	private UserRemote userRemote;

	@Test
	public void testSaveUser() {
		User u = new User();
		u.setId(1);
		u.setName("张三");
		Object response = userRemote.saveUser(u);
		System.out.println(JSONObject.toJSONString(response));
	}

}
