package chatserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Konstant
 */
public class App {

    /**
     * @param args the command line arguments
     */
    ServerSocket server;
    ArrayList<Socket> clients;
    ArrayList<String> names;
    boolean is_working = true;
    int ccount = 0;
    public static void main(String[] args) {
        new App();
    }
    public App()
    {
        try {
            int port = 0;
            String str = System.getenv("PORT");
            while(str == null)
            {
                str = System.getenv("PORT");
            }
            port = Integer.parseInt(str);
            server = new ServerSocket(port);
            clients = new ArrayList(10);
            names = new ArrayList(10);
            System.out.println("\nServer started. Port = " + port + " .");
            while(is_working)
            {
                clients.add(server.accept());
                ccount++;
                new Thread(){
                    @Override
                    public void run()
                    {
                        SocketProcess(ccount);
                    }
                }.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void SocketProcess(int sn)
    {
        boolean is_first = true;
        boolean is_working = true;
        sn--;
        while(is_working)
        {
            try {
                if(clients.get(sn).getInputStream().available() != 0)
                {
                    if(is_first)
                    {
                        int lenght = clients.get(sn).getInputStream().read();
                        byte[] name = new byte[lenght];
                        clients.get(sn).getInputStream().read(name);
                        names.add(sn, new String(name));
                        System.out.println(new String(name) + " joined chat.");
                        for(int j = 0; j < ccount; j++)
                        {
                            if(j!=sn)
                            {
                                clients.get(j).getOutputStream().write(((byte)232));
                                clients.get(j).getOutputStream().write(names.get(sn).length());
                                clients.get(j).getOutputStream().write(names.get(sn).getBytes());
                                clients.get(j).getOutputStream().flush();
                            }
                        }
                        clients.get(sn).getOutputStream().write(((byte)231));
                        clients.get(sn).getOutputStream().write(((byte)ccount));
                        for(int k = 0; k < ccount; k++)
                        {
                            clients.get(sn).getOutputStream().write(names.get(k).length());
                            clients.get(sn).getOutputStream().write(names.get(k).getBytes());
                        }
                        is_first = false;
                    }
                    else
                    {
                        int lenght = clients.get(sn).getInputStream().read();
                        if(lenght == 255)
                        {
                            System.out.println(names.get(sn) + " left the chat.");
                            for(int j = 0; j < ccount; j++)
                            {
                                clients.get(j).getOutputStream().write(((byte)233));
                                clients.get(j).getOutputStream().write(names.get(sn).length());
                                clients.get(j).getOutputStream().write(names.get(sn).getBytes());
                                clients.get(j).getOutputStream().flush();
                            }
                            clients.remove(sn);
                            names.remove(sn);
                            ccount--;                       
                            is_working = false;
                        }
                        else
                        {
                        byte[] message = new byte[lenght];
                        clients.get(sn).getInputStream().read(message);
                        System.out.println(names.get(sn) + " : " + new String(message));
                        for(int j = 0; j < ccount; j++)
                        {
                            clients.get(j).getOutputStream().write((byte)(names.get(sn) + " : " + new String(message)).length());
                            clients.get(j).getOutputStream().write((names.get(sn) + " : " + new String(message)).getBytes());
                            clients.get(j).getOutputStream().flush();
                        }
                    }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}