-- 户外活动新增截止日期字段
-- 作用：支持活动报名截止，并配合定时任务自动结束报名

ALTER TABLE `ldqc_outdoor`
  ADD COLUMN `end_time` datetime NULL DEFAULT NULL COMMENT '活动截止日期' AFTER `cover_image`;
