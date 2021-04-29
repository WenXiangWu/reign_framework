package com.reign.framework.core.mvc.view;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: ViewManager
 * @Description: 视图管理器
 * @Author: wuwx
 * @Date: 2021-04-19 17:49
 **/
public class ViewManager {

    private ResponseView defaultView;

    private Map<String, ResponseView> viewMap = new HashMap<>();

    public ViewManager(ResponseView defaultView) {
        this.defaultView = defaultView;
    }

    public ResponseView getView(String viewName) {
        ResponseView view = viewMap.get(viewName);
        return null == view ? defaultView : view;
    }

    public void addView(String viewName, ResponseView view) {
        viewMap.put(viewName, view);
    }
}
