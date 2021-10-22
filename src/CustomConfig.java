import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.*;

public class CustomConfig {
    @SerializedName("本地接收连接所用的端口")
    public int serverPort = 16059;
    @SerializedName("主动连接所用的端口")
    public int connectPort = 16060;
    @SerializedName("文件发送所用端口")
    public int fileSendPort = 16061;
    @SerializedName("文件接收所用端口")
    public int fileRecvPort = 16062;

    public boolean HasConfig() {
        return new File("LinuxChat_data/config.ini").exists();
    }

    public void SaveToFile() {
        try {
            File file = new File("LinuxChat_data/config.ini");
            if (!file.exists()) file.getParentFile().mkdirs();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String OutStr = gson.toJson(this);
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(OutStr);
            out.flush();
            out.close();
        } catch (IOException e) {
            System.out.println("保存配置文件失败");
            e.printStackTrace();
        }
    }

    public void GetConfig() {
        if (HasConfig()) {
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                BufferedReader in = new BufferedReader(new FileReader("LinuxChat_data/config.ini"));
                String inStr = "";
                int num = 0;
                char ch;
                while ((num = in.read()) != -1) {
                    ch = (char) num;
                    inStr += ch;
                }
                CustomConfig temp = gson.fromJson(inStr, this.getClass());
                this.serverPort = temp.serverPort;
                this.connectPort = temp.connectPort;
                this.fileSendPort = temp.fileSendPort;
                this.fileRecvPort = temp.fileRecvPort;
                in.close();
            } catch (Exception e) {
                SaveToFile();
                System.out.println("读取配置文件失败");
                e.printStackTrace();
            }
        } else {
            System.out.println("首次运行,创建配置文件config.ini");
            SaveToFile();
        }
    }


}

