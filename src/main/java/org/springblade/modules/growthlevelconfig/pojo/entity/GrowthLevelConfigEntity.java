package org.springblade.modules.growthlevelconfig.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;

@Data
@TableName("growth_level_config")
@Schema(description = "成长等级配置")
@EqualsAndHashCode(callSuper = true)
public class GrowthLevelConfigEntity extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "等级")
    private Integer levelNo;

    @Schema(description = "等级名称")
    private String levelName;

    @Schema(description = "达到该等级需要累计获得绿豆")
    private Integer minEarnedPoints;

    @Schema(description = "图标")
    private String iconUrl;

    @Schema(description = "权益说明")
    private String privilegeDesc;

    @Schema(description = "状态 1启用 0停用")
    private Integer status;
}
