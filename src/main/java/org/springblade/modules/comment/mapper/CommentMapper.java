package org.springblade.modules.comment.mapper;

import org.apache.ibatis.annotations.Select;
import org.springblade.modules.comment.pojo.entity.CommentEntity;
import org.springblade.modules.comment.pojo.vo.CommentVO;
import org.springblade.modules.comment.excel.CommentExcel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 评论表 Mapper 接口
 *
 * @author BladeX
 * @since 2026-01-27
 */
public interface CommentMapper extends BaseMapper<CommentEntity> {

/**
 * 自定义分页
 *
 * @param page 分页参数
 * @param comment 查询参数
 * @return List<CommentVO>
 */
List<CommentVO> selectCommentPage(IPage page, CommentVO comment);


/**
 * 获取导出数据
 *
 * @param queryWrapper 查询条件
 * @return List<CommentExcel>
 */
List<CommentExcel> exportComment(@Param("ew") Wrapper<CommentEntity> queryWrapper);

@Select("<script>" +
"SELECT " +
"c.id, c.content, c.create_time, c.uid, u.name as username, u.avatar, " +
"c.reply_id, ru.name as reply_name, null as reply_uid, null as reply_content, " +
"i.id as mid, i.cover as cover " +
"FROM t_comment c " +
"JOIN t_img_detail i ON c.mid = i.id " +
"LEFT JOIN blade_user u ON c.uid = u.id " +
"LEFT JOIN blade_user ru ON c.reply_uid = ru.id " +
"WHERE i.user_id = #{uid} and c.uid != #{uid} " +
"AND (c.pid IS NULL OR c.pid = 0) " +
"UNION " +
"SELECT DISTINCT " +
"c.id, c.content, c.create_time, c.uid, u.name as username, u.avatar, " +
"c.reply_id, ru.name as reply_name, s.uid as reply_uid, s.content AS reply_content, " +
"i.id as mid, i.cover as cover " +
"FROM t_comment c " +
"INNER JOIN t_img_detail i ON c.mid = i.id " +
"LEFT JOIN t_comment s ON c.reply_id = s.id " +
"LEFT JOIN blade_user u ON c.uid = u.id " +
"LEFT JOIN blade_user ru ON c.reply_uid = ru.id " +
"WHERE i.user_id = #{uid} and c.uid != #{uid} " +
"AND c.pid != 0 " +
"UNION " +
"SELECT " +
"s.id, s.content, s.create_time, s.uid, u.name as username, u.avatar, " +
"s.reply_id, ru.name as reply_name, c.uid as reply_uid, c.content AS reply_content, " +
"i.id as mid, i.cover as cover " +
"FROM t_comment c " +
"LEFT JOIN t_comment s ON s.reply_id = c.id " +
"LEFT JOIN t_img_detail i on s.mid = i.id " +
"LEFT JOIN blade_user u ON s.uid = u.id " +
"LEFT JOIN blade_user ru ON s.reply_uid = ru.id " +
"WHERE c.uid = #{uid} and s.uid != #{uid} " +
"ORDER BY create_time DESC" +
"</script>")
List<CommentVO> getAllReplyComment(IPage<CommentVO> page, @Param("uid") Long uid);

    /**
     * 获取热门评论
     */
    List<CommentVO> getAllTrendCommentByImage(IPage<CommentVO> page, @Param("mid") Long mid);

}