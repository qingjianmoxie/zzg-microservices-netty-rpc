package com.zzg.user.remote;


import java.util.List;

import javax.annotation.Resource;

import com.zzg.netty.annotation.Remote;
import com.zzg.netty.util.ResponseUtil;
import com.zzg.user.model.User;
import com.zzg.user.service.UserService;

/**
 * 服务接口的实现
 */
@Remote
public class UserRemoteImpl implements UserRemote {

    @Resource
    private UserService userService;

    @Override
    public Object saveUser(User user) {
        userService.save(user);
        return ResponseUtil.createSuccessResult(user);
    }

    @Override
    public Object saveUsers(List<User> users) {
        userService.saveList(users);
        return ResponseUtil.createSuccessResult(users);
    }
}
