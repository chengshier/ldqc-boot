-- -----------------------------------
-- 新增路由字段
-- -----------------------------------
ALTER TABLE [blade_top_menu] ADD [path] nvarchar(255)
GO

EXEC sp_addextendedproperty
    'MS_Description', N'顶部菜单路由',
    'SCHEMA', N'dbo',
    'TABLE', N'blade_top_menu',
    'COLUMN', N'path';
