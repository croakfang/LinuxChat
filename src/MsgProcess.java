import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Scanner;

public class MsgProcess {
    private static Scanner scanner = new Scanner(System.in);
    public static String inputNext;
    public static int inputLevel = -1;

    public static void toZip(File sFile, OutputStream out) throws RuntimeException {
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(out);
            compress(sFile, zos, sFile.getName());
        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils", e);
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void compress(File sourceFile, ZipOutputStream zos, String name) throws Exception {
        byte[] buf = new byte[1024];
        if (sourceFile.isFile()) {
            zos.putNextEntry(new ZipEntry(name));
            int len;
            FileInputStream in = new FileInputStream(sourceFile);
            while ((len = in.read(buf, 0, buf.length)) != -1) {
                zos.write(buf, 0, len);
            }
            zos.closeEntry();
            in.close();
        } else {
            File[] listFiles = sourceFile.listFiles();
            if (listFiles == null || listFiles.length == 0) {
                zos.putNextEntry(new ZipEntry(name + "/"));
                zos.closeEntry();
            } else {
                for (File file : listFiles) {
                    compress(file, zos, name + "/" + file.getName());
                }
            }
        }
    }

    public static void SearchByChat(String str,ChatRecord chatRecord) {
        if (str.equals("")) {
            System.out.println("请输入有效内容");
            return;
        }
        ;
        System.out.println("-----查找结果-----");
        SimpleDateFormat format = new SimpleDateFormat("[yyyy/MM/dd HH:mm]");
        for (ChatRecord.ChatMessage msg : chatRecord.records) {
            if (msg.content.contains(str)) {
                System.out.println(format.format(msg.Date) + (msg.isMe ? "你：" : "对方：") + msg.content);
            }
        }
        System.out.println("------------------");
    }

    public static void SearchByTime(String str,ChatRecord chatRecord) {
        System.out.println("-----查找结果-----");
        SimpleDateFormat Format = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat format = new SimpleDateFormat("[yyyy/MM/dd HH:mm]");
        try {
            Date date = Format.parse(str);
            for (ChatRecord.ChatMessage msg : chatRecord.records) {
                if (Duration.between(msg.Date.toInstant(), date.toInstant()).toDays() < 1) {
                    System.out.println(format.format(msg.Date) + (msg.isMe ? "你：" : "对方：") + msg.content);
                }
            }
        } catch (ParseException e) {
            System.out.println("请输入有效日期");
        }

        System.out.println("------------------");
    }

    public static int GetStrLength(String str) {
        String tmp = str.replaceAll("[^\\x00-\\xff]", "aa");
        return tmp.length();
    }

    public static String GetNextInput(int level) {
        while (MsgProcess.inputLevel < 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }
        if (MsgProcess.inputLevel <= level) {
            MsgProcess.inputLevel = -1;
            return MsgProcess.inputNext;
        }
        return "";
    }

    public static void GetInput(MySocket mySocket) {
        while (!Thread.interrupted()) {
            inputNext = scanner.nextLine();
            if (inputNext.matches("^##H")) {
                HelpInfo.ShowHelp();
            } else if (inputNext.matches("^##P")) {
                mySocket.config.ShowPort();
            } else if (inputNext.matches("^##Q")) {
                mySocket.CloseConnect();
            } else if (inputNext.matches("^##R")) {
                ChatRecord.GetUserList();
            } else if (inputNext.matches("^##D.*")) {
                ChatRecord.RemoveUserList(inputNext.substring(3));
            } else
                inputLevel = (mySocket.CurSocket == null ? 1 : 2);
        }
    }
}
