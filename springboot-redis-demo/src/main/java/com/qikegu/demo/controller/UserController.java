package com.qikegu.demo.controller;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.qikegu.demo.model.User;
import com.qikegu.demo.service.UserService;

@RestController
@EnableAutoConfiguration
@RequestMapping("/user")
public class UserController {
	
	// 注入service类
    @Resource
    private UserService userService;
    
    // 注入RedisTemplate
    @Resource
    private RedisTemplate<String, Object> redis;
	
    // 读取用户信息，测试缓存使用：除了首次读取，接下来都应该从缓存中读取
    @RequestMapping(value="{id}", method=RequestMethod.GET, produces="application/json")
    public User getUser(@PathVariable long id) throws Exception {
    	
        User user = this.userService.getUserById(id);
        
        return user;
    }
    
    // 修改用户信息，测试删除缓存
    @RequestMapping(value = "/{id}/change-nick", method = RequestMethod.GET, produces="application/json")
    public User changeNickname(@PathVariable long id) throws Exception{
    	String uid = "user-nick-name-"+id;
        User user = (User)redis.opsForValue().get(uid);
        if(user == null){
        	System.out.println("**********来到这里说明没有从redis查询缓存数据***********");
        	String nick = "abc-" + Math.random();
        	user = this.userService.updateUserNickname(id, nick);
        	redis.opsForValue().set(uid, user);
        	redis.opsForValue().set("obj-json", JSON.toJSONString(user));
        }
        return user;
    }
    
    // 使用RedisTemplate访问redis服务器
    @RequestMapping(value="/redis", method=RequestMethod.GET, produces="application/json")
    public String redis() throws Exception {
        String uuid = UUID.randomUUID().toString();
        // 设置键"project-name"，值"qikegu-springboot-redis-demo"
        redis.opsForValue().set("project-name", "qikegu-springboot-redis-demo");
        redis.opsForValue().set("project-expire", uuid, 30, TimeUnit.SECONDS);
        String value = (String) redis.opsForValue().get("project-expire");
        Long expire = redis.getExpire("project-expire");
        return value +expire;
    }
}