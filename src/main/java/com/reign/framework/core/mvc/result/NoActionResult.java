package com.reign.framework.core.mvc.result;

/**
 * @ClassName: NoActionResult
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-19 17:52
 **/
public class NoActionResult implements Result<String> {

    private String command;

    public NoActionResult(String command) {
        this.command = command;
    }

    @Override
    public String getViewName() {
        return "noAction";
    }

    @Override
    public String getResult() {
        return command;
    }

    @Override
    public int getBytesLength() {
        return 0;
    }
}
