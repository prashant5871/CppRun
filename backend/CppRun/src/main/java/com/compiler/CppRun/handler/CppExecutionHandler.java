package com.compiler.CppRun.handler;

import java.io.File;
import java.io.FileWriter;
import java.util.UUID;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class CppExecutionHandler extends TextWebSocketHandler {

    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Connection established succesfully...");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("Payload is : " + payload);

        if(payload.startsWith("code:"))
        {
            executeCode(session,payload.substring(5));
        }
    }

    void executeCode(WebSocketSession session,String code)
    {
        String randomString = UUID.randomUUID().toString();
        randomString = "./frontend/public/" + randomString;
        String fileName = randomString + ".cpp";
        // fileName = "./frontend/public/"+fileName;
        File file = new File(fileName);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(code);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("file Created succesfully...");

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Connection get closed...");
    }
}
