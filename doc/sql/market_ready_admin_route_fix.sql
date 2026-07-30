-- market-ready-phase1 管理端动态路由修复
-- 适用：MySQL 5.7 / BladeX Saber Vue3
-- 原因：前端根据 menu.path 直接拼接为 views + path；路径必须与 src/views 下文件名及大小写完全一致。
-- 执行后需要退出管理端重新登录或清理菜单缓存。

SET NAMES utf8mb4;

UPDATE blade_menu
   SET path = '/training/courseManager',
       is_deleted = 0
 WHERE code = 'trainingCourseManager';

UPDATE blade_menu
   SET path = '/training/courseWorkbench',
       is_deleted = 0
 WHERE code = 'trainingCourseWorkbench';

UPDATE blade_menu
   SET path = '/mall/productManager',
       is_deleted = 0
 WHERE code = 'mallProductManager';

UPDATE blade_menu
   SET path = '/mall/fulfillmentWorkbench',
       is_deleted = 0
 WHERE code = 'mallFulfillmentWorkbench';

-- 其余本批新增页面路径与实际文件一致，统一恢复启用状态。
UPDATE blade_menu SET path = '/content/moderation', is_deleted = 0 WHERE code = 'contentModeration';
UPDATE blade_menu SET path = '/content/auditExceptions', is_deleted = 0 WHERE code = 'contentAuditExceptions';
UPDATE blade_menu SET path = '/venueapply/venueApply', is_deleted = 0 WHERE code = 'venueApplyAudit';

SELECT id, parent_id, code, name, path, category, is_open, is_deleted
  FROM blade_menu
 WHERE code IN (
     'trainingOperation','trainingCourseManager','trainingCourseWorkbench',
     'mallOperation','mallProductManager','mallFulfillmentWorkbench',
     'contentModeration','contentAuditExceptions','venueApplyAudit'
 )
 ORDER BY parent_id, sort, id;
