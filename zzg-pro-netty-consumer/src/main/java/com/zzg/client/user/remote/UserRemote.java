package com.zzg.client.user.remote;

import java.util.List;

import com.zzg.client.param.Response;
import com.zzg.client.user.bean.User;

/**
 * 访问远程服务的接口
 */
public interface UserRemote {
	public Response saveUser(User user);
	public Response saveUsers(List<User> users);
}
