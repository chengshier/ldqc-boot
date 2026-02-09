package org.springblade.common.utils;



import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 树形结构工具类
 *
 * @author BladeX
 */
public class TreeUtil {

    /**
     * 构建树节点 - 专门用于CategoryVo
     */
//    public static List<CategoryVo> build(List<CategoryVo> treeNodes) {
//        List<CategoryVo> result = new ArrayList<>();
//
//        //list转map
//        Map<Long, CategoryVo> nodeMap = new LinkedHashMap<>(treeNodes.size());
//        for(CategoryVo treeNode : treeNodes){
//            nodeMap.put(treeNode.getId(), treeNode);
//        }
//
//        for(CategoryVo node : nodeMap.values()) {
//            CategoryVo parent = nodeMap.get(node.getPid());
//            if(parent != null && !(node.getId().equals(parent.getId()))){
//                parent.getChildren().add(node);
//                continue;
//            }
//
//            result.add(node);
//        }
//
//        return result;
//    }
}

