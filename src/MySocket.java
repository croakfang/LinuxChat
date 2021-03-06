import com.sun.corba.se.spi.activation.Server;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

@SuppressWarnings("BusyWait")
public class MySocket {
    public Thread ServerThread;
    public Thread ConnectThread;
    public Socket CurSocket;
    public CustomConfig config;

    protected ChatRecord chatRecord;
    protected boolean isFileSend;
    protected Date LastChatDate = null;


    public void Initialization() {
        config = new CustomConfig();
        config.GetConfig();
        ServerThread = new Thread(this::StartServer) {{
            start();
        }};
        ConnectThread = new Thread(this::Connect) {{
            start();
        }};
        new Thread(this::NetFind).start();
        System.out.println("正在等待连接,可以输入IP主动连接:");
        MsgProcess.GetInput(this);
    }

    public void NetFind() {
        if (!config.findEnable) return;
        ArrayList<String> findList = GetLocalIP();
        try {
            InetAddress findIP = InetAddress.getByName(config.findAddr);
            new Thread(() -> NetBeFind(findIP)).start();
            while (CurSocket == null) {
                String data = "##N" + config.serverPort;
                try {
                    InetAddress.getByName(config.findAddr);
                    for (String s : findList) {
                        InetAddress tmp = InetAddress.getByName(s);
                        DatagramSocket datagramSocket = new DatagramSocket(config.findPort, tmp);
                        DatagramPacket pak = new DatagramPacket(data.getBytes(StandardCharsets.UTF_8), data.length(), findIP, config.findPort);
                        datagramSocket.send(pak);
                        datagramSocket.close();
                    }
                    Thread.sleep(3000);
                } catch (Exception ignored) {
                }
            }

        } catch (Exception ignored) {
        }

    }

    public void NetBeFind(InetAddress findIP){
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket(config.findPort);
            socket.setTimeToLive(128);
            socket.joinGroup(findIP);
            socket.setLoopbackMode(false);
            ArrayList<String> findList = GetLocalIP();
            for (int i = 0; i < findList.size(); i++)
                findList.set(i, findList.get(i) + ":" + config.serverPort);
            byte[] arb = new byte[12];
            DatagramPacket pak = new DatagramPacket(arb, arb.length);
            while (CurSocket == null) {
                socket.receive(pak);
                if (new String(pak.getData()).contains("##N")) {
                    String tmp = pak.getAddress().getHostAddress() + ":" +
                            new String(pak.getData()).substring(3).trim();
                    if (!findList.contains(tmp)) {
                        findList.add(tmp);
                        System.out.println("[发现局域用户:" + tmp + "]");
                    }
                }
            }
            socket.leaveGroup(findIP);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            if(socket!=null)socket.close();
        }
    }

    public void CloseConnect() {
        try {
            if (CurSocket != null) {
                String DesIP = CurSocket.getInetAddress().getHostAddress();
                System.out.println("与" + DesIP + "的聊天已结束");
                CurSocket.close();
                CurSocket = null;
            }
        } catch (IOException ignored) {
        }
        ServerThread = null;
        ConnectThread = null;
        chatRecord = null;
        config.SaveToFile();

        System.exit(0);
    }

    public void StartServer() {
        while (CurSocket == null) {
            while (CurSocket == null) {
                try {
                    ServerSocket server = new ServerSocket(config.serverPort);
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
                    System.out.println("服务器初始化失败,请检查端口占用");
                    CloseConnect();
                }
            }
        }
    }

    public void Connect() {
        while (CurSocket == null) {
            try {
                String[] strArr = MsgProcess.GetNextInput(1).split(":");
                if (CurSocket != null) return;

                int port = strArr.length > 1 ? Integer.parseInt(strArr[1]) : config.serverPort;
                ArrayList<String> findList = GetLocalIP();
                if (findList.contains(strArr[0] + ":" + port)) {
                    System.out.println("地址或端口无效,请重新输入");
                    continue;
                }


                System.out.println("正在尝试与[" + strArr[0] + ":" + port + "]" + "进行连接");
                Socket socket = new Socket(strArr[0], port);

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
        if (MsgProcess.GetNextInput(2).equals("y")) {
            System.out.println("-----------------------" + DesIP + "-----------------------");
            MsgProcess.CheckChatRecord(this);
            MsgProcess.CheckTimeDur(LastChatDate, MsgProcess.GetCurTime());
            LastChatDate = MsgProcess.GetCurTime();
            Chat();
        } else CloseConnect();
    }

    public void Chat() {
        new Thread(this::Listen) {{
            start();
        }};

        try {
            BufferedWriter os = new BufferedWriter
                    (new OutputStreamWriter(CurSocket.getOutputStream(), StandardCharsets.UTF_8));
            while (!Thread.interrupted()) {
                String temp = MsgProcess.GetNextInput(2);

                if (temp.matches("^##F.*")) {
                    String name = FileSend(temp.substring(3));
                    if (!name.equals("")) {
                        String tmp = "[发送文件：" + name + "]";
                        os.write(tmp + "\n" + "##F" + name + "\n");
                        os.flush();
                        System.out.println(tmp);
                        chatRecord.SaveChatRecord(CurSocket.getInetAddress().getHostAddress(), true, tmp, config);
                    } else System.out.println("文件不存在,或已有文件发送/接收中");
                } else if (temp.matches("^##S.*")) {
                    MsgProcess.SearchByChat(temp.substring(3), chatRecord);

                } else if (temp.matches("^##T.*")) {
                    MsgProcess.SearchByTime(temp.substring(3), chatRecord);

                } else {
                    os.write(temp + "\n");
                    os.flush();
                    chatRecord.SaveChatRecord(CurSocket.getInetAddress().getHostAddress(), true, temp, config);
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
            while (!Thread.interrupted()) {
                String mess = br.readLine();
                if (mess.matches("^##F.*")) {
                    FileRecv(mess.substring(3));
                } else {
                    MsgProcess.CheckTimeDur(LastChatDate, MsgProcess.GetCurTime());
                    MsgProcess.ShowMessage(mess, false);
                    LastChatDate = MsgProcess.GetCurTime();
                    chatRecord.SaveChatRecord(CurSocket.getInetAddress().getHostAddress(), false, mess, config);
                }
            }
        } catch (Exception e) {
            CloseConnect();
        }
    }

    private String FileSend(String info) {
        File file = new File(info);
        boolean isDir = file.isDirectory();
        if (file.exists() && !isFileSend) {
            new Thread(() -> {
                int length;
                byte[] sendByte = new byte[1024];
                ServerSocket server = null;
                Socket socket = null;
                DataOutputStream out = null;
                FileInputStream fin = null;
                isFileSend = true;
                try {
                    try {
                        server = new ServerSocket(config.fileSendPort);
                        socket = server.accept();
                        if (isDir) {
                            MsgProcess.toZip(file, socket.getOutputStream());
                        } else {
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
            }).start();
            return file.getName() + (file.isDirectory() ? ".zip" : "");
        }
        return "";
    }

    private void FileRecv(String name) {
        if (!isFileSend) {
            new Thread(() -> {
                byte[] inputByte = new byte[1024];
                int length;
                DataInputStream din = null;
                FileOutputStream fout = null;
                Socket socket = null;
                isFileSend = true;
                try {
                    try {
                        File file = new File("LinuxChat_data/" + CurSocket.getInetAddress().getHostAddress() + "/" + name);
                        if (!file.exists()) file.getParentFile().mkdirs();

                        socket = new Socket(CurSocket.getInetAddress().getHostAddress(), config.fileSendPort);

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
                        String tmp = "[文件" + name + "已接收]";
                        System.out.println(tmp);
                        chatRecord.SaveChatRecord(CurSocket.getInetAddress().getHostAddress(), true, tmp, config);

                    } finally {
                        new BufferedWriter(new OutputStreamWriter(CurSocket.getOutputStream(),
                                StandardCharsets.UTF_8)) {{
                            write("[文件" + name + "已接收]\n");
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
            }).start();
        } else System.out.println("已有文件发送/接收中");
    }

    public ArrayList<String> GetLocalIP() {
        ArrayList<String> out = new ArrayList<>();
        Enumeration<NetworkInterface> allNetworkInterfaces =
                null;
        try {
            allNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            return new ArrayList<>();
        }
        NetworkInterface networkInterface = null;

        while (allNetworkInterfaces.hasMoreElements()) {
            networkInterface = allNetworkInterfaces.nextElement();
            Enumeration<InetAddress> allInetAddress =
                    networkInterface.getInetAddresses();

            InetAddress ipAddress = null;

            while (allInetAddress.hasMoreElements()) {
                ipAddress = allInetAddress.nextElement();
                if (ipAddress instanceof Inet4Address) {
                    out.add(ipAddress.getHostAddress());
                }
            }
        }
        return out;
    }

}
