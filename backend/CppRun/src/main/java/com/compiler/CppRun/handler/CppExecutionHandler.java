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

    private final Map<WebSocketSession, BufferedWriter> sessionWriterMap = new HashMap<>();
    private final Map<WebSocketSession, BufferedReader> sessionReaderMap = new HashMap<>();
    private final Map<WebSocketSession, Process> sessionProcessMap = new HashMap<>();

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
        } else {
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
        Process compileProcess = compileBuiler.start();
        int exitCode = compileProcess.waitFor();

        if (exitCode != 0) {
            String errorOutput = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()))
                    .lines().reduce("", (acc, line) -> acc + line + "\n");
            session.sendMessage(new TextMessage("Compilation failed:\n" + errorOutput));
            return;
        }

        String executablePath = "./backend/CppFiles/" + exeString;
        ProcessBuilder processBuilder = new ProcessBuilder(executablePath);
        Process process = processBuilder.start();

        // store the process and session in map
        sessionProcessMap.put(session, process);

        BufferedWriter processWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        BufferedReader processReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        // store writer and reader in map
        sessionWriterMap.put(session, processWriter);
        sessionReaderMap.put(session, processReader);

        File secondFile = new File(executablePath + ".exe");
        readProcessOutput(session, processReader, file, secondFile);
        System.out.println("after readProcessOutput...");

    }

    private void readProcessOutput(WebSocketSession session, BufferedReader processReader, File f1, File f2) {
        Thread t;
        new Thread(() -> {
            try {
                String line;
                while ((line = processReader.readLine()) != null) {
                    session.sendMessage(new TextMessage(line));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                closeProcess(session, f1, f2);
            }
        }).start();
    }

    private void closeProcess(WebSocketSession session, File f1, File f2) {
        try {

            Process process = sessionProcessMap.remove(session);
            if (process != null) {
                process.destroy();
            }

            BufferedWriter processWriter = sessionWriterMap.remove(session);
            if (processWriter != null) {
                processWriter.close();
            }

            BufferedReader processReader = sessionReaderMap.remove(session);
            if (processReader != null) {
                processReader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            f1.delete();
            if (f2.delete()) {
                System.out.println("sucess");
            } else {
                System.out.println("failure");
            }
            // f2.delete();
        }

    }

    private void sendInputToProcess(WebSocketSession session, String payload) throws Exception {
        BufferedWriter processWriter = sessionWriterMap.get(session);

        if (processWriter == null) {
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
