package com.zzg.user.remote;

import java.util.List;

import com.zzg.user.model.User;

/**
 * 访问远程服务的接口
 */
public interface UserRemote {
	public Object saveUser(User user);
	public Object saveUsers(List<User> users);
}
