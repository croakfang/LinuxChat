import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class MySocket {
    public Thread ServerThread;
    public Thread ConnectThread;
    public Socket CurSocket;
    public Scanner scanner = new Scanner(System.in);
    public int inputLevel = -1;
    public String inputNext;
    private int connectPort = 16059;
    private int serverPort = 16060;
    private int fileSendPort = 16061;
    private int fileRecvPort = 16062;
    private CustomConfig config;
    private ChatRecord chatRecord;
    private boolean isFileSend;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
    Date LastChatDate = null;


    public void Initialization() {
        config = new CustomConfig();
        config.GetConfig();
        serverPort = config.serverPort;
        connectPort = config.connectPort;
        fileSendPort = config.fileSendPort;
        fileRecvPort = config.fileRecvPort;
        ServerThread = new Thread(this::StartServer);
        ConnectThread = new Thread(this::Connect);
        ServerThread.start();
        ConnectThread.start();
        System.out.println("正在等待连接(端口：" + serverPort + ")...");
        System.out.println("你也可以输入IP主动连接:");
        System.out.println("(可以带端口号 如127.0.0.1:8080)");
        GetInput();
    }

    public void CloseConnect() {
        try {
            CurSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ServerThread = null;
        ConnectThread = null;
        chatRecord = null;
        String DesIP = CurSocket.getInetAddress().getHostAddress();
        System.out.println("与" + DesIP + "的聊天已结束");
        CurSocket = null;
        System.exit(0);
    }

    public void StartServer() {
        while (CurSocket == null) {
            while (CurSocket == null) {
                try {
                    ServerSocket server = new ServerSocket(serverPort);
                    Socket socket = server.accept();
                    if (CurSocket == null) {
                        CurSocket = socket;
                        Accept();
                    } else {
                        server.close();
                        Thread.sleep(1000);
                    }
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("服务器初始化失败");
                }
            }
        }
    }

    public void Connect() {
        while (CurSocket == null) {
            try {
                String[] strArr = GetNextInput(1).split(":");
                if (CurSocket != null) return;

                int port = strArr.length > 1 ? Integer.parseInt(strArr[1]) : serverPort;
                if (serverPort == port) {
                    System.out.println("该端口是服务器端口，无法连接");
                    continue;
                }

                System.out.println("正在尝试与[" + strArr[0] + ":" + port + "]" + "进行连接");
                Socket socket = new Socket(strArr[0], port, null, connectPort);

                if (CurSocket == null) {
                    CurSocket = socket;
                    Accept();
                    return;
                }
            } catch (Exception e) {
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }
                System.out.println("连接失败,请重新输入");
            }
        }
    }

    public void Accept() {
        String DesIP = CurSocket.getInetAddress().getHostAddress();
        System.out.print("来自 " + DesIP + "的用户想要建立连接，确认吗？(y/n)");
        if (GetNextInput(2).equals("y")) {
            System.out.println("-------------" + DesIP + "-------------");
            CheckChatRecord();
            CheckTimeDur(LastChatDate, GetCurTime());
            LastChatDate = GetCurTime();
            Chat();
        } else CloseConnect();
    }

    public String GetNextInput(int level) {
        while (inputLevel < 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        if (inputLevel <= level) {
            inputLevel = -1;
            return inputNext;
        }
        return "";
    }

    private void GetInput() {
        while (true) {
            inputNext = scanner.nextLine();
            inputLevel = CurSocket == null ? 1 : 2;
        }
    }

    public void Chat() {
        Thread ListenThread = new Thread(this::Listen);
        ListenThread.start();
        try {
            BufferedWriter os = new BufferedWriter
                    (new OutputStreamWriter(CurSocket.getOutputStream(), StandardCharsets.UTF_8));
            while (true) {
                String temp = GetNextInput(2);
                if (temp.matches("^##f.*")) {
                    String name = FileSend(temp.substring(3));
                    if (!name.equals("")) {
                        os.write("[发送文件：" + name + "]" + "\n");
                        os.write("##f" + name + "\n");
                        os.flush();
                        chatRecord.SaveChatRecord(CurSocket.getInetAddress().getHostAddress(), true, "[发送文件：" + name + "]");
                    } else System.out.println("文件不存在,或已有文件发送/接收中");
                } else {
                    os.write(temp + "\n");
                    os.flush();
                    chatRecord.SaveChatRecord(CurSocket.getInetAddress().getHostAddress(), true, temp);
                }
            }
        } catch (IOException e) {
            CloseConnect();
        }

    }

    public void Listen() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader
                    (CurSocket.getInputStream(), StandardCharsets.UTF_8));
            while (true) {
                String mess = br.readLine();
                if (mess.matches("^##f.*")) {
                    FileRecv(mess.substring(3));
                } else {
                    CheckTimeDur(LastChatDate, GetCurTime());
                    ShowMessage(mess, false);
                    chatRecord.SaveChatRecord(CurSocket.getInetAddress().getHostAddress(), false, mess);
                }
            }
        } catch (Exception e) {
            CloseConnect();
        }
    }

    private void ShowMessage(String mess, boolean isMe) {
        LastChatDate = GetCurTime();
        if (isMe) {
            System.out.println(mess);
        } else {
            for (int i = 0; i < 30 - GetStrLength(mess); i++) System.out.print(" ");
            System.out.print(mess);
            System.out.println(":对方");
        }

    }

    private Date GetCurTime() {
        return new Date(System.currentTimeMillis());
    }

    private void CheckTimeDur(Date star, Date end) {
        if (star == null || Duration.between(star.toInstant(), end.toInstant()).toMinutes() > 5) {
            System.out.println("\n    [" + formatter.format(end) + "]");
        }
    }

    private void CheckChatRecord() {
        chatRecord = ChatRecord.GetChatRecord(CurSocket.getInetAddress().getHostAddress());
        Date tempLastDate = null;
        ArrayList<ChatRecord.ChatMessage> records = chatRecord.records;
        for (int i = 0; i < records.size(); i++) {
            CheckTimeDur(tempLastDate, records.get(i).msgDate);
            tempLastDate =records.get(i).msgDate;
            ShowMessage(records.get(i).content, records.get(i).isMe);
        }
    }

    private String FileSend(String info) {
        File file = new File(info);
        if (file.exists() && !isFileSend) {
            new Thread(() -> SendThread(file, file.isDirectory())).start();
            return file.getName() + (file.isDirectory() ? ".zip" : "");
        }
        return "";
    }

    private void FileRecv(String name) {
        if (!isFileSend) {
            new Thread(() -> RecvThread(name)).start();
        } else System.out.println("已有文件发送/接收中");
    }

    private void RecvThread(String name) {
        byte[] inputByte = new byte[1024];
        int length = 0;
        DataInputStream din = null;
        FileOutputStream fout = null;
        Socket socket = null;
        isFileSend = true;
        try {
            try {
                File file = new File("LinuxChat_data/"+CurSocket.getInetAddress().getHostAddress()+"/"+name);
                if(!file.exists())file.getParentFile().mkdirs();
                socket = new Socket(CurSocket.getInetAddress().getHostAddress(), fileSendPort, null, fileRecvPort);
                din = new DataInputStream(socket.getInputStream());
                fout = new FileOutputStream(file);
                while (true) {
                    length = din.read(inputByte, 0, inputByte.length);
                    if (length == -1) {
                        break;
                    }
                    fout.write(inputByte, 0, length);
                    fout.flush();
                }
                System.out.println("文件(" + name + ")已接收");
                chatRecord.SaveChatRecord(CurSocket.getInetAddress().getHostAddress(), true, "文件(" + name + ")已接收");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                new BufferedWriter(new OutputStreamWriter(CurSocket.getOutputStream(),
                        StandardCharsets.UTF_8)) {{
                    write("文件已接收\n");
                    flush();
                }};
                if (fout != null) fout.close();
                if (din != null) din.close();
                if (socket != null) socket.close();
            }
        } catch (Exception e) {
            System.out.println("接收失败");
        }
        isFileSend = false;
    }

    private void SendThread(File file, boolean isDir) {
        int length;
        byte[] sendByte = new byte[1024];
        ServerSocket server = null;
        Socket socket = null;
        DataOutputStream out = null;
        FileInputStream fin = null;
        isFileSend = true;
        try {
            try {
                server = new ServerSocket(fileSendPort);
                socket = server.accept();
                if(isDir){
                    toZip(file,socket.getOutputStream());
                }else {
                    out = new DataOutputStream(socket.getOutputStream());
                    fin = new FileInputStream(file);
                    while ((length = fin.read(sendByte, 0, sendByte.length)) > 0) {
                        out.write(sendByte, 0, length);
                        out.flush();
                    }
                }
            } finally {
                if (out != null) out.close();
                if (fin != null) fin.close();
                if (socket != null) socket.close();
                if (server != null) server.close();
            }

        } catch (Exception e) {
            isFileSend = false;
            System.out.println("文件发送失败");
        }
        isFileSend = false;
    }

    private int GetStrLength(String str) {
        String tmp = str.replaceAll("[^\\x00-\\xff]", "aa");
        return tmp.length();
    }

    public void toZip(File sFile, OutputStream out)throws RuntimeException {
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
            while ((len = in.read(buf,0,buf.length)) != -1) {
                zos.write(buf, 0, len);
            }
            zos.closeEntry();
            in.close();
        }
        else {
            File[] listFiles = sourceFile.listFiles();
            if (listFiles == null || listFiles.length == 0) {
                zos.putNextEntry(new ZipEntry(name + "/"));
                zos.closeEntry();
            }
            else {
                for (File file : listFiles){compress(file, zos, name + "/" + file.getName()); }
            }
        }
    }
}
