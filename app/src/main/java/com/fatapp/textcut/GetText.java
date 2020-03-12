package com.fatapp.textcut;

import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EncodingUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;

public class GetText {
    private static int lineNumber;
    private static int onLine;
    private static String text;
    private static Boolean done;

    /**
     * @返回 TXT文件的所有字符 用String 接收
     */
    public static String GetLog(String path) {
        String txt;
        int current;
        File file = new File(path);
        ByteArrayBuffer bb = new ByteArrayBuffer(500);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while ((current = reader.read()) != -1) {
                bb.append(current);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        txt = EncodingUtils.getString(bb.toByteArray(), "GB2312");
        return txt;
    }

    public static BufferedReader convertCodeAndGetText(String str_filepath) {// 转码

        File file = new File(str_filepath);
        BufferedReader reader = null;


        try {
            LineNumberReader lnr = new LineNumberReader(new FileReader(file));
            lnr.skip(Long.MAX_VALUE);

            // FileReader f_reader = new FileReader(file);
            // BufferedReader reader = new BufferedReader(f_reader);
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream in = new BufferedInputStream(fis);
            in.mark(4);
            byte[] first3bytes = new byte[3];
            in.read(first3bytes);//找到文档的前三个字节并自动判断文档类型。
            in.reset();
            if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB
                    && first3bytes[2] == (byte) 0xBF) {// utf-8

                reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            } else if (first3bytes[0] == (byte) 0xFF
                    && first3bytes[1] == (byte) 0xFE) {

                reader = new BufferedReader(
                        new InputStreamReader(in, "unicode"));
            } else if (first3bytes[0] == (byte) 0xFE
                    && first3bytes[1] == (byte) 0xFF) {

                reader = new BufferedReader(new InputStreamReader(in,
                        StandardCharsets.UTF_16BE));
            } else if (first3bytes[0] == (byte) 0xFF
                    && first3bytes[1] == (byte) 0xFF) {

                reader = new BufferedReader(new InputStreamReader(in,
                        StandardCharsets.UTF_16LE));
            } else {

                reader = new BufferedReader(new InputStreamReader(in, "GBK"));
            }


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return reader;
    }


    public static int getFileLineNumber(BufferedReader reader) throws IOException {
        lineNumber = 0;
        //读入文件数据
        LineNumberReader lnr = new LineNumberReader(reader);
        //开始一个字符一个字符的跳过 一直到最后一个字符。读取完成
        lnr.skip(Long.MAX_VALUE);
        //有一个换行符
        lineNumber = lnr.getLineNumber() + 1;
        lnr.close();
        return lineNumber;

    }

    public static String getText(BufferedReader reader) {
        text = "";
        onLine = 1;

        try {
            String str = reader.readLine();
            while (str != null) {
                onLine++;
                text = text + str + "\n";
                str = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        done = true;
        return text;
    }

    public static int getLineNumber() {
        return lineNumber;
    }

    private static void setLineNumber(int n) {
        lineNumber = n;
    }

    public static int getOnLine() {
        return onLine;
    }

    public static void setOnLine(int n) {
        onLine = n;
    }

    public static String getText() {
        return text;
    }

    public static void setText(String t) {
        text = t;
    }

    public static Boolean getDone() {
        return done;
    }

    public static void setDone(Boolean b) {
        done = b;
    }
}
