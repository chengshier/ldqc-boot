package org.springblade.modules.resource.service.impl;

import com.aliyun.tea.TeaException;
import org.springblade.modules.resource.service.IDmService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DmServiceImpl implements IDmService {

    @Value("${aliyun.dm.accessKeyId:}")
    private String accessKeyId;

    @Value("${aliyun.dm.accessKeySecret:}")
    private String accessKeySecret;

    @Value("${aliyun.dm.accountName:xiaozhao@ccxzvideo.top}")
    private String accountName;

    @Value("${aliyun.dm.fromAlias:ccvideo}")
    private String fromAlias;

    public static com.aliyun.dm20151123.Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret);
        config.endpoint = "dm.aliyuncs.com";
        return new com.aliyun.dm20151123.Client(config);
    }

    @Override
    public void sendDm(String toAddress, String content) throws Exception {
        com.aliyun.dm20151123.Client client = createClient(accessKeyId, accessKeySecret);
        com.aliyun.dm20151123.models.SingleSendMailRequest singleSendMailRequest = new com.aliyun.dm20151123.models.SingleSendMailRequest()
                .setAccountName(accountName)
                .setAddressType(1)
                .setToAddress(toAddress)
                .setReplyToAddress(true)
                .setSubject("邮件发送测试")
                .setFromAlias(fromAlias)
                .setHtmlBody(content);
        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
        try {
            client.singleSendMailWithOptions(singleSendMailRequest, runtime);
        } catch (TeaException error) {
            com.aliyun.teautil.Common.assertAsString(error.message);
            throw error;
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            com.aliyun.teautil.Common.assertAsString(error.message);
            throw _error;
        }
    }
}