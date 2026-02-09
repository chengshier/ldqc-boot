package org.springblade.modules.resource.service;

public interface IDmService {
    void sendDm(String toAddress, String content) throws Exception;
}