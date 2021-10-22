import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class CustomConfig {
    private final String Note_1 = "本地接收连接所用的端口";
    public int serverPort = 16059;
    private final String Note_2 = "主动连接所用的端口";
    public int connectPort = 16060;
    private final String Note_3 = "文件发送所用端口";
    public int fileSendPort = 16061;
    private final String Note_4 = "文件接收所用端口";
    public int fileRecvPort = 16062;
    private final String Note_5 = "以下是聊天记录";
    public ArrayList<ChatRecord> chatRecord = new ArrayList<ChatRecord>();

    public boolean HasConfig(){
        return new File("config.txt").exists();
    }

    public void SaveToFile(){
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String OutStr = gson.toJson(this);
            BufferedWriter out = new BufferedWriter(new FileWriter("config.txt"));
            out.write(OutStr);
            out.flush();
        } catch (IOException e) {
            System.out.println("保存配置文件失败");
            e.printStackTrace();
        }
    }

    public void GetConfig(){
        if(HasConfig()) {
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                BufferedReader in = new BufferedReader(new FileReader("config.txt"));
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
                this.chatRecord = temp.chatRecord;

            } catch (Exception e) {
                SaveToFile();
                System.out.println("读取配置文件失败");
                e.printStackTrace();
            }
        }
        else {
            System.out.println("首次运行,创建配置文件config.txt");
            SaveToFile();
        }
    }

    public ArrayList<ChatMessage> GetChatRecord(String id){
        for(int i=0;i<chatRecord.size();i++){
            if(chatRecord.get(i).ID .equals(id))
            return chatRecord.get(i).record;
        }
        return new ArrayList<ChatMessage>();
    }

    public void SaveChatRecord(String id,boolean isMe,String content){
        ChatMessage msg= new ChatMessage(new Date(System.currentTimeMillis()),isMe,content);
        for(int i=0;i<chatRecord.size();i++){
            if(chatRecord.get(i).ID.equals(id) ){
                while (chatRecord.get(i).record.size()>50)
                    chatRecord.get(i).record.remove(0);
                chatRecord.get(i).record.add(msg);
                SaveToFile();
                return;
            }
        }
        chatRecord.add(new ChatRecord(id,new ArrayList<ChatMessage>()));
        SaveToFile();
    }

    class ChatRecord{
        public ChatRecord(String id,ArrayList<ChatMessage> Record){
            ID = id;
            record = Record;
        }
        public String ID;
        ArrayList<ChatMessage> record;
    }

    class ChatMessage{
        public ChatMessage(Date date,boolean IsMe,String Content){
            msgDate = date;
            isMe = IsMe;
            content = Content;
        }
        Date msgDate;
        Boolean isMe;
        String content;
    }
}

