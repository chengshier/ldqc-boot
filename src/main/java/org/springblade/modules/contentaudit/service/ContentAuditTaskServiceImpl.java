package org.springblade.modules.contentaudit.service;

import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.modules.contentaudit.mapper.ContentAuditTaskMapper;
import org.springblade.modules.contentaudit.pojo.entity.ContentAuditTask;
import org.springframework.stereotype.Service;

@Service
public class ContentAuditTaskServiceImpl extends BaseServiceImpl<ContentAuditTaskMapper, ContentAuditTask> implements IContentAuditTaskService {
}
