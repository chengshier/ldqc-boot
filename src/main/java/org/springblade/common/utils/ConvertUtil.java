package org.springblade.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 对象转换工具类
 *
 * @author BladeX
 */
public class ConvertUtil {
    private static Logger logger = LoggerFactory.getLogger(ConvertUtil.class);

    /**
     * 单个对象转换
     */
    public static <T> T sourceToTarget(Object source, Class<T> target){
        if(source == null){
            return null;
        }
        T targetObject = null;
        try {
            targetObject = target.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, targetObject);
        } catch (Exception e) {
            logger.error("convert error ", e);
        }

        return targetObject;
    }

    /**
     * 列表对象转换
     */
    public static <T> List<T> sourceToTarget(List<?> sourceList, Class<T> target){
        if(sourceList == null){
            return null;
        }

        List<T> targetList = new ArrayList<>(sourceList.size());
        try {
            for(Object source : sourceList){
                T targetObject = target.getDeclaredConstructor().newInstance();
                BeanUtils.copyProperties(source, targetObject);
                targetList.add(targetObject);
            }
        }catch (Exception e){
            logger.error("convert error ", e);
        }

        return targetList;
    }
}

