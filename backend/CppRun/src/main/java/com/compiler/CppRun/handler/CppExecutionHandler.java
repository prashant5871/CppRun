package com.compiler.CppRun.handler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class CppExecutionHandler extends TextWebSocketHandler {

    private final Map<WebSocketSession,BufferedWriter> sessionWriterMap = new HashMap<>();
    private final Map<WebSocketSession,BufferedReader> sessionReaderMap = new HashMap<>();
    private final Map<WebSocketSession,Process> sessionProcessMap = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Connection established succesfully...");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("Payload is : " + payload);

        if (payload.startsWith("code:")) {
            System.out.println("executig the code...");
            executeCode(session, payload.substring(5));
            System.out.println("Code executed...");
        }else{
            sendInputToProcess(session, payload);
        }
    }

    void executeCode(WebSocketSession session, String code) throws Exception {
        String randomString = UUID.randomUUID().toString();
        String exeString = randomString;
        randomString = "./backend/CppFiles/" + randomString;
        String fileName = randomString + ".cpp";
        // fileName = "./frontend/public/"+fileName;
        File file = new File(fileName);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(code);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // System.out.println("file Created succesfully...");

        ProcessBuilder compileBuiler = new ProcessBuilder("g++", fileName, "-o", randomString);
        Process compilProcess = compileBuiler.start();
        int exitCode = compilProcess.waitFor();

        if (exitCode != 0) {
            System.out.println("error while compiling..");
            session.sendMessage(new TextMessage("Error while compiling..."));
            return;
        }

        String executablePath = "./backend/CppFiles/" + exeString;
        ProcessBuilder processBuilder = new ProcessBuilder(executablePath);
        Process process = processBuilder.start();

        //store the process and session in map
        sessionProcessMap.put(session, process);

        BufferedWriter processWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        BufferedReader processReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        //store writer and reader in map
        sessionWriterMap.put(session, processWriter);
        sessionReaderMap.put(session, processReader);

        readProcessOutput(session, processReader);

        file.delete();
        Thread.sleep(500);
        File secondFile = new File(executablePath + ".exe");
        if(secondFile.delete())
        {
            System.out.println("exe file deleted succesfully...");
        }else{
            System.out.println("can not delete exe file...");
        }
        

    }

    private void readProcessOutput(WebSocketSession session, BufferedReader processReader) {
        new Thread(() -> {
            try {
                String line;
                while ((line = processReader.readLine()) != null) {
                    session.sendMessage(new TextMessage(line));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void sendInputToProcess(WebSocketSession session,String payload) throws Exception
    {
        BufferedWriter processWriter = sessionWriterMap.get(session);

        if(processWriter == null)
        {
            session.sendMessage(new TextMessage("No active program to send input"));
            return;
        }

        try {
            processWriter.write(payload);
            processWriter.newLine();
            processWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Connection get closed...");
    }
}
