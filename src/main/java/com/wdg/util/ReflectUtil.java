package com.wdg.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ReflectUtil {

    /**
     *
     * @param souce
     * @param target
     * @return
     * @throws Exception
     */
    public static Object setPropertiesToObj(Object souce,Object target) throws Exception {

        Field[] declaredFields = target.getClass().getDeclaredFields();
        List<String> values = (List<String>) souce;
        for (int i = 0; i < declaredFields.length; i++) {
            String sourceName = declaredFields[i].getName();
            Class<?> type = declaredFields[i].getType();
            Method method = null;
            method = target.getClass().getDeclaredMethod("set" + sourceName.substring(0, 1).toUpperCase() + sourceName.substring(1), type);
            method.invoke(target,values.get(i));
        }
        return target;
    }
}
