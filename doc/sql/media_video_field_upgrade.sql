-- 图片动态表 / 达人动态表 视频媒体字段补充
-- 执行前请先确认表结构与现网字段一致，建议先在测试库验证。

ALTER TABLE `t_img_detail`
  ADD COLUMN `media_type` varchar(20) DEFAULT 'image' COMMENT '媒体类型[image,video]' AFTER `imgs_url`,
  ADD COLUMN `media_url` varchar(500) DEFAULT NULL COMMENT '媒体地址' AFTER `media_type`,
  ADD COLUMN `poster_url` varchar(500) DEFAULT NULL COMMENT '媒体封面/首帧地址' AFTER `media_url`,
  ADD COLUMN `duration` int DEFAULT NULL COMMENT '媒体时长(秒)' AFTER `poster_url`,
  ADD COLUMN `file_size` bigint DEFAULT NULL COMMENT '文件大小(字节)' AFTER `duration`,
  ADD COLUMN `width` int DEFAULT NULL COMMENT '媒体宽度' AFTER `file_size`,
  ADD COLUMN `height` int DEFAULT NULL COMMENT '媒体高度' AFTER `width`;

ALTER TABLE `ldqc_talent_post`
  ADD COLUMN `poster_url` varchar(500) DEFAULT NULL COMMENT '媒体封面/首帧地址' AFTER `cover_image`,
  ADD COLUMN `duration` int DEFAULT NULL COMMENT '媒体时长(秒)' AFTER `media_type`,
  ADD COLUMN `file_size` bigint DEFAULT NULL COMMENT '文件大小(字节)' AFTER `duration`;

-- 可选回填：把历史图片动态标记为 image，避免前端新逻辑判断为空
UPDATE `t_img_detail`
SET `media_type` = 'image'
WHERE `media_type` IS NULL OR `media_type` = '';

UPDATE `ldqc_talent_post`
SET `media_type` = 'image'
WHERE `media_type` IS NULL OR `media_type` = '';
