package org.springblade.modules.ruleversionnotice.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;

@Data
@TableName("rule_version_notice")
@Schema(description = "RuleVersionNotice对象")
@EqualsAndHashCode(callSuper = true)
public class RuleVersionNoticeEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	private String moduleType;
	private String versionNo;
	private String noticeTitle;
	private String noticeContent;
	private java.util.Date publishAt;
	private java.util.Date effectiveAt;
	private Integer status;
}
