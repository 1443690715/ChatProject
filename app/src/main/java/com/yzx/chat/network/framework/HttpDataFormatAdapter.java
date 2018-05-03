package com.yzx.chat.network.framework;

import android.support.annotation.Nullable;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by YZX on 2017年10月15日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public interface HttpDataFormatAdapter {

    @Nullable
    String paramsToString(String url, Map<String, Object> params, String requestMethod);

    @Nullable
    Map<HttpParamsType, Map<String, Object>> multiParamsFormat(String url, Map<HttpParamsType, Map<String, Object>> params, String requestMethod);

    @Nullable
    Object responseToObject(String url, String httpResponse, Type genericType);

}
