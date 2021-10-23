public class HelpInfo {
static String Version = "V0.6";
static String Title =
            " ------------------------------------------------------------ "+"\n"+
            "|   _      _                       ____  _             _     |"+"\n"+
            "|  | |    (_) _ __   _   _ __  __ / ___|| |__    __ _ | |_   |"+"\n"+
            "|  | |    | || '_ \\ | | | |\\ \\/ /| |    | '_ \\  / _` || __|  |"+"\n"+
            "|  | |___ | || | | || |_| | >  < | |___ | | | || (_| || |_   |"+"\n"+
            "|  |_____||_||_| |_| \\__,_|/_/\\_\\ \\____||_| |_| \\__,_| \\__|  |";

public static void ShowTitle(){
    System.out.println(Title);
    System.out.println("| "+"Version:"+Version+"     输入 \"##H\" 获取帮助     Power By CroakFang|");
    System.out.println(" ------------------------------------------------------------ ");
}

public static void ShowHelp(){
   String str =
           "---------------------------帮助手册---------------------------"+
           "\n[开始]"+
           "\n程序初次启动时会在程序目录创建LinuxChat_Data文件夹以及文件夹内的配置文件config.ini"+
           "\n配置文件config.ini可以修改程序设置(端口禁止重复)"+
           "\n\n[建立连接]"+
           "\n程序启动时会使用配置接收端口(默认16059)等待连接，此时可以输入地址主动连接"+
           "\n输入格式:[地址]:[端口号(可选)]，如“192.168.1.1” “192.168.1.1:8080”"+
           "\n注：如果不使用端口号，将向配置接收端口(默认16059)进行连接"+
           "\n\n[指令列表]"+
           "\n{##H}：获取帮助手册"+
           "\n{##Q}：断开所有连接并退出程序"+
           "\n{##P}：查看当前配置的所有端口"+
           "\n{##R}：查看与本机连接过的用户地址"+
           "\n{##D}：删除用户的所有记录和文件，需要后接用户地址，如 “##D127.0.0.1”"+
           "\n{##S}：内容搜索与对方的聊天记录，仅在建立连接后可用，需要后接查找内容"+
           "\n使用示例：“##S你好” “##S再见”"+
           "\n{##T}：时间搜索与对方的聊天记录，仅在建立连接后可用，需要后接日期(年/月/日)"+
           "\n使用示例：“##T2021/12/25” “##T2021/5/9”"+
           "\n{##F}：向对方发送文件或文件夹，仅在建立连接后可用，需要后接路径"+
           "\n使用示例：“##F图片.jpg” “##F文件夹/图片.jpg” “##F文件夹”"+
           "\n注：文件接收目录为LinuxChat_Data下，以对方地址命名的文件夹下"+
           "\n文件夹会保存为zip压缩包格式"+
           "\n------------------------------------------------------------";
   System.out.println(str);
}

}

