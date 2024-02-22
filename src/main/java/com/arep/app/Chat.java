package com.arep.app;

import java.net.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Chat {
    public static void main(String[] args) throws IOException, URISyntaxException, ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        ServerSocket serverSocket = null;
        try { 
            serverSocket = new ServerSocket(37000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 37000.");
            System.exit(1);
        }

        Socket clientSocket = null;
        boolean running = true;
        while(running){
        try {
            System.out.println("Listo para recibir ...");
            clientSocket = serverSocket.accept();
            handleConnection(clientSocket);
        } catch (IOException e) {
            System.err.println("Accept failed.");
            System.exit(1);
        }
        }
        serverSocket.close(); 
    }

    public static void handleConnection(Socket client) throws IOException, URISyntaxException, ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        PrintWriter out = new PrintWriter(
            client.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
                                new InputStreamReader(client.getInputStream()));
        String inputLine, outputLine, query;
        String fLine = "";
        while ((inputLine = in.readLine()) != null) {
            if(fLine.isEmpty()){
                fLine = inputLine.split(" ")[1];
            }
            if (!in.ready()) {break; }
        }

        if(fLine.startsWith("/compreflex")){
            URI uri = new URI(fLine);
            query = uri.getQuery();
            query = query.split("=")[1];
            outputLine = "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: application/json\r\n"
                        + "\r\n";
            outputLine += reflexCallProcedure(query);
        }else{
            outputLine = getErrorPage();
        }
        
        out.println(outputLine);
        out.close(); 
        in.close(); 
        client.close();
    }

    public static String getErrorPage(){
        return "HTTP/1.1 403 Bad request\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n"+"<!DOCTYPE html>\r\n" + //
                        "<html>\r\n" + //
                        "    <head>\r\n" + //
                        "        <title>Form Example</title>\r\n" + //
                        "        <meta charset=\"UTF-8\">\r\n" + //
                        "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n" + //
                        "    </head>\r\n" + //
                        "    <h1>Error, your request failed :c</h1>\r\n" + //
                        "</html>";
    }

    public static String reflexCallProcedure(String query) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        System.out.println(query);
        String function = query.split("\\(")[0];
        String values = query.split("\\(")[1];
        values = values.split("\\)")[0];
        String[] vals = values.split(",");
        String ans = "";
        Class<?> c = Class.forName(vals[0]);
        
        switch (function) {
            case "class":
                ans += "{\"fields\":[";
                for(Field f : c.getDeclaredFields()){
                    ans += "\"" + f.getName() + "\",";
                }
                ans = ans.substring(0, ans.length() - 1); 
                ans += "],\"methods\":[";
                for(Method m: c.getDeclaredMethods()){
                    ans += "\"" + m.getName() + "\",";
                }
                ans = ans.substring(0, ans.length() - 1);
                ans += "]}";
                System.out.println(ans);
                break;
            case "invoke":
                Method m1;
                try{
                    m1 = c.getDeclaredMethod(vals[1], String.class);
                } catch (Exception e) {
                    try {
                        
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
                
                try {
                    m1 = c.getDeclaredMethod(vals[1], Integer.TYPE);
                } try{
                    m1 = c.getDeclaredMethod(vals[1], Double.TYPE);
                } 
                ans = "{\"method\":\"" + m1.toString() + "\"}";
                break;
            case "unaryInvoke":
                Method m2;
                if (vals[2].equals("string")){
                    m2 = c.getMethod(vals[1], String.class);
                    String args = vals[3];
                    ans = "{\"answer\":\"" + m2.invoke(null, args) + "\"}";
                }
                if(vals[2].equals("int")){
                    m2 = c.getMethod(vals[1], Integer.TYPE);
                    Integer args = Integer.parseInt(vals[3]);
                    ans = "{\"answer\":\"" + m2.invoke(null, args) + "\"}";
                }
                if(vals[2].equals("double")){
                    m2 = c.getMethod(vals[1], Double.TYPE);
                    Double args = Double.parseDouble(vals[3]);
                    ans = "{\"answer\":\"" + m2.invoke(null, args) + "\"}";
                }
                break;
            case "binaryInvoke":
                Method m3;
                if (vals[2].equals("string")){
                    m3 = c.getMethod(vals[1], String.class, String.class);
                    String arg1 = vals[3];
                    String arg2 = vals[5];
                    ans = "{\"answer\":\"" + m3.invoke(null, arg1, arg2) + "\"}";
                }
                if(vals[2].equals("int")){
                    m2 = c.getMethod(vals[1], Integer.TYPE, Integer.TYPE);
                    Integer arg1 = Integer.parseInt(vals[3]);
                    Integer arg2 = Integer.parseInt(vals[5]);
                    ans = "{\"answer\":\"" + m2.invoke(null, arg1, arg2) + "\"}";
                }
                if(vals[2].equals("double")){
                    m2 = c.getMethod(vals[1], Double.TYPE, Double.TYPE);
                    Double arg1 = Double.parseDouble(vals[3]);
                    Double arg2 = Double.parseDouble(vals[5]);
                    ans = "{\"answer\":\"" + m2.invoke(null, arg1, arg2) + "\"}";
                }
                break;
            default:
                break;
        }
        return ans;
    }
}

// return "{\"fields\":[1,2], \"methods\":[1,2]}";