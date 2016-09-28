package com.jash.myutils.utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.Locale;

public class NetUtils {
    public static<T> T getInstance(Class<T> type) {
        Object o = Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, new MyHandler());
        return (T) o;
    }

    private static class MyHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            UrlString urlString = method.getAnnotation(UrlString.class);
            ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();
            if (urlString != null && returnType.getRawType().equals(NetworkTask.class)) {
                String url = String.format(Locale.CHINA, urlString.value(), args);
                Class type = (Class) returnType.getActualTypeArguments()[0];
                return new NetworkTask<>(url, type);
            } else {
                if (urlString == null) {
                    throw new RuntimeException(method.getName() + "方法没有加UrlString的注解");
                } else {
                    throw new RuntimeException(method.getName() + "方法返回值类型不为NetworkTask");
                }

            }
        }
    }
}
