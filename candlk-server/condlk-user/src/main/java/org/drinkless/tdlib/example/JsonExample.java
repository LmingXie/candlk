//
// Copyright Aliaksei Levin (levlam@telegram.org), Arseny Smirnov (arseny30@gmail.com) 2014-2025
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
package org.drinkless.tdlib.example;

import org.drinkless.tdlib.JsonClient;

/**
 * Example class for TDLib usage from Java using JSON interface.
 */
public final class JsonExample {
    public static void main(String[] args) throws InterruptedException {
        // 设置日志消息处理程序，只处理致命错误(0)和普通日志消息（-1）
        JsonClient.setLogMessageHandler(0, new LogMessageHandler());

        // 禁用TDLib日志，并将致命错误和普通日志消息重定向到一个文件
        JsonClient.execute("{\"@type\":\"setLogVerbosityLevel\",\"new_verbosity_level\":0}");
        JsonClient.execute("{\"@type\":\"setLogStream\",\"log_stream\":{\"@type\":\"logStreamFile\",\"path\":\"tdlib.log\",\"max_file_size\":128000000}}");

        // 创建客户端标识符
        int clientId = JsonClient.createClientId();

        // 发送第一个请求来激活客户端
        JsonClient.send(clientId, "{\"@type\":\"getOption\",\"name\":\"version\"}");

        // main loop
        while (true) {
            String result = JsonClient.receive(100.0);
            if (result != null) {
                System.out.println(result);
            }
        }
    }

    private static class LogMessageHandler implements JsonClient.LogMessageHandler {
        @Override
        public void onLogMessage(int verbosityLevel, String message) {
            System.err.print(message);
            if (verbosityLevel == 0) {
                System.err.println("Receive fatal error; the process will crash now");
            }
        }
    }
}
