import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.*;
import java.lang.reflect.Field;

public class CustomConfig {
    @SerializedName("本地接收连接所用的端口")
    public int serverPort = 16059;
    @SerializedName("主动连接所用的端口")
    public int connectPort = 16060;
    @SerializedName("文件发送所用端口")
    public int fileSendPort = 16061;
    @SerializedName("文件接收所用端口")
    public int fileRecvPort = 16062;
    @SerializedName("最大聊天记录保存条数")
    public int maxMagSave = 100;
    @SerializedName("最大聊天记录显示条数")
    public int maxMagShow = 50;
    @SerializedName("网络发现所用端口")
    public int findPort = 16063;
    @SerializedName("启用网络发现功能")
    public boolean findEnable = true;

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
                mergeObject(temp,this);
                in.close();
            } catch (Exception e) {
                SaveToFile();
                System.out.println("读取配置文件失败(配置文件非末行需要后加逗号)");
                e.printStackTrace();
            }
        } else {
            System.out.println("首次运行,创建配置文件LinuxChat_data/config.ini");
            SaveToFile();
        }
    }

    public void ShowPort() {
        String str =
                "-----------端口信息----------" +
                        "\n等待连接端口:" + serverPort +
                        "\n主动连接端口:" + connectPort +
                        "\n文件发送端口:" + fileSendPort +
                        "\n文件接收端口:" + fileRecvPort +
                        "\n网络发现端口:" + findPort +
                        "\n---------------------------";
        System.out.println(str);
    }

    public <CustomConfig> void mergeObject(CustomConfig origin, CustomConfig destination) {
        if (origin == null || destination == null)
            return;
        if (!origin.getClass().equals(destination.getClass()))
            return;
        Field[] fields = origin.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            try {
                fields[i].setAccessible(true);
                Object value = fields[i].get(origin);
                if (null != value) {
                    fields[i].set(destination, value);
                }
                fields[i].setAccessible(false);
            } catch (Exception e) {
            }
        }
    }
}

