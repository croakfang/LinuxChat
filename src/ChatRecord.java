import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;

public class ChatRecord {

    public ChatRecord(String id, ArrayList<ChatMessage> Record){
        ID = id;
        records = Record;
    }
    public String ID;
    ArrayList<ChatMessage> records;

    public static ChatRecord GetChatRecord(String id){
        File file = new File("LinuxChat_data/"+id+"/ChatRecord.txt");
        if(file.exists()){
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                BufferedReader in = new BufferedReader(new FileReader("LinuxChat_data/"+id+"/ChatRecord.txt"));
                String inStr = "";
                int num = 0;
                char ch;
                while ((num = in.read()) != -1) {
                    ch = (char) num;
                    inStr += ch;
                }
                ChatRecord recordList =  gson.fromJson(inStr,new TypeToken<ChatRecord>(){}.getType());
                if(recordList.records != null)
                return recordList;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return new ChatRecord(id, new ArrayList<>());
    }

    public void SaveChatRecord(String id,boolean isMe,String content){
        records.add(new ChatMessage(new Date(System.currentTimeMillis()),isMe,content));
        while (records.size()>50){
            records.remove(0);
        }
        File file = new File("LinuxChat_data/"+id+"/ChatRecord.txt");
        if(!file.exists())file.getParentFile().mkdirs();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            String str = gson.toJson(this);
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(str);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    class ChatMessage{
        public ChatMessage(Date date, boolean IsMe, String Content){
            msgDate = date;
            isMe = IsMe;
            content = Content;
        }
        Date msgDate;
        Boolean isMe;
        String content;
    }
}
