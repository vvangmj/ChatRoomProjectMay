import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    public static void main(String[] args){
        new ServerR(8888).start();
    }
}
class ServerR{
    private int port;
    ServerSocket serverSocket;
    public static Map<String, String> map_infor;//record user infor
    public static Map<String,Socket> map_online;//record online infor
    public ServerR(int port){
        this.port=port;
        map_infor = new HashMap<String,String>();
        map_online = new HashMap<String,Socket>();
        //<account,password>
        map_infor.put("user1", "1");

        map_infor.put("user2", "1");
    }
    public void start(){
        try{
            serverSocket=new ServerSocket(port);
        }catch (Exception e){
            e.printStackTrace();
        }
        while(true){
            try{
                Socket socket=serverSocket.accept();
                //loop for waiting socket
                new Thread(new ClientR(socket)).start();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
class ClientR implements Runnable{
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private String account;
    public ClientR(Socket socket){
        this.socket=socket;
        try{
            this.in=new DataInputStream(socket.getInputStream());
            this.out=new DataOutputStream(socket.getOutputStream());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void run(){

        try{
            //login operation，to check if the accounts are existed.
            String str_account=in.readUTF();
            String str_password=in.readUTF();
            if(ServerR.map_infor.containsKey(str_account)){
                if(ServerR.map_infor.get(str_account).equals(str_password)){
                    //use equals not ==
                    System.out.println("success login");
                    out.writeUTF("success_login");

                    account=str_account;
                    success_login();
                }
                else{
                    System.out.println("error password");
                    out.writeUTF("error_password");
                }
            }else{
                System.out.println("no account");
                out.writeUTF("no_account");
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
        ServerR.map_online.put(account,socket);//bind the account_name with socket
        Set<String> set_account;
        String choice;
        String another;
        //Scanner scanner=new Scanner(System.in);
        try{

            //send online list info
            set_account=ServerR.map_online.keySet();
            if(set_account!=null){
                out.writeUTF(set_account.toString());
            }else{
                out.writeUTF("null");
            }
            //transit info
            while(true){
                choice=in.readUTF();
                if(choice.equals("chat")){

                    //get another user (socket)
                    another=in.readUTF();
                    System.out.println(another);

                    Socket another_socket=ServerR.map_online.get(another);
                    DataOutputStream another_out=new DataOutputStream(
                            another_socket.getOutputStream()
                    );//

                    while(true){
                        // send message
                        String message=in.readUTF();

                        another_out.writeUTF(account+":"+message);

                        if(message.equals("bye")){
                            break;
                        }
                    }
                }
                if(choice.equals("file")){

                    //get another user
                    another=in.readUTF();

                    //get filename
                    String fileName=in.readUTF();

                    Socket another_socket=ServerR.map_online.get(another);
                    DataOutputStream another_out=new DataOutputStream(
                            another_socket.getOutputStream()
                    );

                    if(fileName.equals("yes")){
                        another_out.writeUTF(account+"=="+fileName);
                    }else{
                        another_out.writeUTF(account+"=="+fileName);
                        int file_length=in.readInt();
                        System.out.println(file_length);

                        byte[] b= new byte[1000];//read 1KB each time

                        int i=0;
                        File f=new File(fileName);
                        if (!f.exists()){
                            f.createNewFile();
                        }
                        FileOutputStream fos = new FileOutputStream(fileName);
                        int b_sum=0;
                        while(b_sum!=file_length &&(i=in.read(b))!=-1){//b is inputoutput parameter
                            b_sum=b_sum+i;
                            fos.write(b, 0, i);
                        }
                        fos.close();

                        //put the  received file in to this sending process
                        FileInputStream fis = new FileInputStream(fileName);

                        another_out.writeInt(fis.available());
                        while((i=fis.read(b))!=-1){
                            another_out.write(b, 0, i);
                        }
                        fis.close();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
