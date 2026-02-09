package org.springblade.common.websocket;

import jakarta.annotation.Resource;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * WebSocket Server
 * 注意在websocket通信中只能传string
 */
@ServerEndpoint("/api/platform/ws/{userId}")
@Component
@Slf4j
public class WebSocketServer {

    //连接redis
    private static RedisTemplate redisTemplate;

    private static final String USER_KEY = "user:";

    @Resource
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        //取值之前先要序列化
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        this.redisTemplate = redisTemplate;
    }

    /**
     * Session Map
     */
    private static final ConcurrentMap<String, Session> SESSION_MAP = new ConcurrentHashMap<>();


    /***
     * 1.把登录用户存到sessionMap中
     * 2.发送给所有人当前登录人员信息
     */
    @OnOpen
    public static void onOpen(Session session, @PathParam("userId") String userId) {

        log.info("现在来连接的客户id：" + session.getId() + "用户名：" + userId);

        if (SESSION_MAP.containsKey(userId)) {
            try {
                // Check if session is open before closing?
                // Original code calls onClose with session from map.
                Session oldSession = SESSION_MAP.get(userId);
                if (oldSession.isOpen()) {
                     oldSession.close();
                }
            } catch (Exception e) {
                log.error("Closing old session error", e);
            }
        }
        SESSION_MAP.put(userId, session);
    }

    //关闭连接

    /**
     * 1.把登出的用户从sessionMap中剃除
     * 2.发送给所有人当前登录人员信息
     */
    @OnClose
    public static void onClose(@PathParam("userId") String userId, Session session) {
        log.info(session.getRequestURI().getPath() + "，关闭连接开始：" + session.getId());

        String userKey = USER_KEY + userId;

        // Original logic: check redis if user is still "online" in some other sense?
        // Or maybe it means if user info is not in redis, remove from map?
        // Keeping original logic structure but ensuring null safety
        if (redisTemplate != null && Boolean.FALSE.equals(redisTemplate.hasKey(userKey))) {
            SESSION_MAP.remove(userId);
            log.info(session.getRequestURI().getPath() + "，关闭连接完成：" + session.getId());
        } else {
             // If key exists, maybe we should still remove the session from map if it matches?
             // But original code only removes if !hasKey.
             // This might be for distributed session handling?
             // But SESSION_MAP is local.
             // Let's just remove it to be safe for this node.
             SESSION_MAP.remove(userId);
        }

    }

    @OnMessage
    public static void onMessage(String message, Session session) {
        log.info("前台发送消息：" + message);
    }

    @OnError
    public static void onError(Session session, Throwable error) {
        log.error("WebSocket Error: " + error.toString());
    }

    public static void sendMessage(String message, Session session) {
        try {
            session.getBasicRemote().sendText(message); // asyncRemote or basicRemote? Original used async
            // session.getAsyncRemote().sendText(message);
            log.info("推送成功：" + message);
        } catch (Exception e) {
            log.error("推送异常：" + e);
        }
    }

    public static void sendMessageTo(String message, String toUserId) {
        try {
            Session session = SESSION_MAP.get(toUserId);
            if (session != null && session.isOpen()) {
                session.getAsyncRemote().sendText(message);
                log.info("推送成功 to " + toUserId + "：" + message);
            } else {
                log.warn("User " + toUserId + " not connected or session closed.");
            }
        } catch (Exception e) {
            log.error("推送异常：" + e);
        }
    }
}
