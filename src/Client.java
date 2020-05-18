import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.*;

public class Client {
    public static void main(String[] args){
        new Client_so("127.0.0.1",8888).start();
    }
}

class Client_so{
    private String path;
    private int port;
    private Scanner scanner;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    public Client_so(String path,int port){
        this.path=path;
        this.port=port;
        scanner=new Scanner(System.in);
    }
    public void start(){
        try{
            socket=new Socket(path,port);
            in=new DataInputStream(socket.getInputStream());
            out=new DataOutputStream(socket.getOutputStream());
            System.out.println("enter your account:");
            String str1 = scanner.nextLine();
            out.writeUTF(str1);
            System.out.println("enter your password:");
            String str2 = scanner.nextLine();
            out.writeUTF(str2);

            String str3 =in.readUTF();


            if(str3.equals("success_login")){
                System.out.println("success login");
                success_login();
            }else if(str3.equals("error_passord")){
                System.out.println("error_passord");
            }else if(str3.equals("no_account")){
                System.out.println("no_account");
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally{
            try{
                in.close();
                out.close();
                socket.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public void success_login(){
        try{
            //set online list
            System.out.println("online list");
            System.out.println("-----------------");
            String online_list=in.readUTF();
            System.out.println(online_list);
            System.out.println("-----------------");

            while(true){

                System.out.println("chat 1,file 2,quit ctrl+c");
                String choice=scanner.nextLine();
                if(choice.equals("1")){
                    out.writeUTF("chat");

                    //send another user
                    System.out.println("choose whom you want to chat with");
                    String another=scanner.nextLine();
                    out.writeUTF(another);

                    System.out.println("-----------------");
                    System.out.println("chat begin:(say bye to quit)");



                    Thread chat_reader_thread =new Thread(new chat_reader(in));
                    Thread chat_writer_thread =new Thread(new chat_writer(out));

                    chat_reader_thread.start();
                    chat_writer_thread.start();


                    chat_writer_thread.join();
                    chat_reader_thread.join();

                }else if(choice.equals("2")){
                    out.writeUTF("file");

                    //send file to another user
                    System.out.println("choose whom you want to send file to or recieve from,for example user2");
                    String another=scanner.nextLine();
                    out.writeUTF(another);

                    Thread file_reader_thread =new Thread(new file_reader(in));
                    Thread file_writer_thread =new Thread(new file_writer(out));

                    file_reader_thread.start();
                    file_writer_thread.start();


                    //let the file operation do first
                    file_writer_thread.join();
                    file_reader_thread.join();

                }


                else{
                    System.out.println("please try again");
                }

            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
class file_reader implements Runnable{
    private String fileName;
    private DataInputStream in;
    public file_reader(DataInputStream in){
        this.in=in;
    }
    public void run(){
        try{
            String[] temp=in.readUTF().split("==");

            fileName=temp[1];
            System.out.println(fileName);
            if(fileName.equals("yes")){
                System.out.println("the file has received");
            }
            else{
                System.out.println(temp[0]+" send file "+fileName+" to you ");
                File f=new File(fileName);
                if (!f.exists()){
                    f.createNewFile();
                }

                int file_length=in.readInt();

                FileOutputStream os= new FileOutputStream(f);//write data into f
                byte[] b= new byte[1000];
                int i=0;
                int b_sum=0;
                while(b_sum!=file_length&&(i=in.read(b))!=-1){
                    b_sum=b_sum+i;
                    os.write(b, 0, i);
                }
                os.close();
                System.out.println("finish");

            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
class file_writer implements Runnable{
    private String fileName;
    private DataOutputStream out;
    private Scanner scanner;

    public  file_writer(DataOutputStream out){
        this.out=out;
        scanner=new Scanner(System.in);

    }
    public void run(){
        try{
            //enter the name
            System.out.println("please input fileName. if you have received file please say yes");
            String fileName=scanner.nextLine();
            if(fileName.equals("yes")){
                out.writeUTF(fileName);
            }else{
                out.writeUTF(fileName);

                File f = new File(fileName);
                //put the file in to this process
                FileInputStream in= new FileInputStream(f);
                if(f.exists()){
                    int file_length=in.available();
                    out.writeInt(file_length);
                    byte[] b= new byte[1000];
                    int i=0;
                    while((i=in.read(b))!=-1){
                        out.write(b, 0, i);
                    }

                    in.close();
                    System.out.println("Upload finish!");
                }

            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}



class chat_reader implements Runnable{
    private  String message;
    private DataInputStream in;
    public chat_reader(DataInputStream in){
        this.in=in;
    }
    public void run(){
        try{
            while(true){

                message=in.readUTF();
                System.out.println(message);
                if(message.contains("bye")){
                    System.out.println("The another user has finished chatting,say bye!");
                    break;
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

class chat_writer implements Runnable{
    private String message;
    private DataOutputStream out;
    private Scanner scanner;

    public  chat_writer(DataOutputStream out){
        this.out=out;
        scanner=new Scanner(System.in);

    }
    public void run(){
        try{
            while(true){

                message=scanner.nextLine();
                out.writeUTF(message);
                if(message.contains("bye")){
                    System.out.println("you have finished chatting");
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}