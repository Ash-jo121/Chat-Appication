
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient {
    private final String serverName;
    private final int serverPort; 
    private Socket socket;
    private InputStream serverIn;
    private OutputStream serverOut;
    private BufferedReader bufferedIn;
    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    private ArrayList<MessageListener> messageListeners = new ArrayList<>();

    public ChatClient(String serverName,int serverPort){
        this.serverName = serverName;
        this.serverPort = serverPort;
    }
    public static void main(String[] args) {
        ChatClient client = new ChatClient("localhost",8818);
        client.addUserStatusListener(new UserStatusListener(){
            @Override
            public void online(String login){
                System.out.println("ONLINE: "+login);
            }
            @Override
            public void offline(String login){
                System.out.println("OFFLINE: "+login);
            }
        });

        client.addMessageListener(new MessageListener(){
            @Override
            public void onMessage(String fromLogin,String msgBody){
                System.out.println("You got a message from " + fromLogin + "===>" + msgBody);
            }
        });
        if(!client.connect()){
            System.out.println("Connection falied");   
        }else{
            System.out.println("Connection successful");
            try{
                if(client.login("guest","guest")){
                    System.out.println("Login successful...");
                    client.msg("jim","Hello world!");
                }else{
                    System.out.println("Login failed....");
                }
            }catch(IOException e){
                e.printStackTrace();
            }
            //client.logoff();
        }
    }
    public void msg(String sendTo,String msgBody){
        String cmd = "msg " + sendTo + " " + msgBody +"\n";
        try{
            serverOut.write(cmd.getBytes());
        }catch(IOException e){
            e.printStackTrace();
        }

    }
    public boolean login(String login,String password)throws IOException{
        String cmd = "login " + login + " " + password + "\n";
        serverOut.write(cmd.getBytes());
        String response = bufferedIn.readLine();
        System.out.println("Response line:" + response);
        if("ok login".equalsIgnoreCase(response)){
            startMessageReader();
            return true;
        }else{
            return false;
        }

    }
    public void logoff() throws IOException{
        String cmd = "logoff\n";
        serverOut.write(cmd.getBytes());
    }
    private void readMessageLoop(){
        try{
        String line;
        while((line = bufferedIn.readLine()) != null){
            String[] tokens = line.split(" ");
            if(tokens!=null && tokens.length>0){
                String cmd = tokens[0];
                if("online".equalsIgnoreCase(cmd)){
                    handleOnline(tokens);
                }else if("offline".equalsIgnoreCase(cmd)){
                    handleOffline(tokens);
                }else if("msg".equalsIgnoreCase(cmd)){
                    String[] tokenMsg = line.split(" ",3);
                    handleMessage(tokenMsg);
                }
            }
        }}catch(Exception e){
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException ex) {
                //TODO: handle exception
                ex.printStackTrace();
            }
        }
    }
    private void handleMessage(String[] tokenMsg){
        String login = tokenMsg[1];
        String msgBody = tokenMsg[2];
        for(MessageListener listener : messageListeners){
            listener.onMessage(login,msgBody);
        }
    }
    private void handleOffline(String[] tokens){
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListeners ){
            listener.offline(login);
        }
    }

    private void handleOnline(String[] tokens){
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListeners ){
            listener.online(login);
        }
    }
    public boolean connect(){
        try {
            this.socket = new Socket(serverName,serverPort);
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException e) {
            //TODO: handle exception
            e.printStackTrace();
        }
        return false;
    }
    public void addUserStatusListener(UserStatusListener listener){
        userStatusListeners.add(listener);
    }
    public void removeUserListener(UserStatusListener listener){
        userStatusListeners.remove(listener);
    }
    private void startMessageReader(){
        Thread t =new Thread(){
            @Override
            public void run(){
                readMessageLoop();
            }
        };
        t.start();
    }
    public void addMessageListener(MessageListener listener){
        messageListeners.add(listener);
    }
    public void removeMessageListener(MessageListener listener){
        messageListeners.remove(listener);
    }

}