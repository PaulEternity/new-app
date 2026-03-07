package com.paul.appgen.langgraph4j.demo;

import org.bsc.langgraph4j.action.NodeAction;

import java.util.List;
import java.util.Map;

// Node that adds a greeting
public class GreeterNode implements NodeAction<SimpleState> {
    @Override    // 标记重写父类方法
    public Map<String, Object> apply(SimpleState state) {    // 定义一个接收SimpleState参数并返回Map<String, Object>的方法
        System.out.println("GreeterNode executing. Current messages: " + state.messages());    // 打印执行信息，包含当前消息
        return Map.of(SimpleState.MESSAGES_KEY, "Hello from GreeterNode!");    // 返回一个包含消息的Map，键为SimpleState.MESSAGES_KEY，值为"Hello from GreeterNode!"
    }
}

