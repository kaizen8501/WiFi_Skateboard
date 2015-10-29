package com.example.administrator.myskateboard;

import android.support.v7.app.ActionBarActivity;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.app.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.*;

public class Skate_MainActivity extends ActionBarActivity {
    private Socket socket;
    private String hostname = "192.168.100.2";
    //String hostname = "222.98.173.202";
    int port = 5000;

    public final static String START_MSG         = "START";
    public final static String END_MSG           = "END";
    public final static String CONTROL_UP       = "UPXX";
    public final static String CONTROL_DOWN     = "DOWN";
    public final static String CONTROL_CRUISER = "CRUI";
    public final static String CONTROL_STOP     = "STOP";

    public final static String STATUS_CMD_OK   = "OKXX";
    public final static String STATUS_CMD_FAIL = "FAIL";

    ConnectThread TCPClient_Thread;
    //ImageView status_image;
    Button connect_btn;
    SeekBar speed_sb;
    boolean isConnected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skate_main);

        Log.d("tag", "Application Start");

        connect_btn = (Button)findViewById(R.id.button_connect);
        connect_btn.setOnClickListener(mClickListener);
        findViewById(R.id.button_speed_up).setOnClickListener(mClickListener);
        findViewById(R.id.button_speed_down).setOnClickListener(mClickListener);
        findViewById(R.id.button_stop).setOnClickListener(mClickListener);

        //status_image = (ImageView)findViewById(R.id.imageView_con_status);
        speed_sb = (SeekBar)findViewById(R.id.seekBar_Speed);
        speed_sb.setProgress(50);
        speed_sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(isConnected == true)
                {
                    String crui_pkt = String.format("START,CRUI,%03d,END",speed_sb.getProgress());
                    Log.d("tag","TEST "+crui_pkt);
                    TCPClient_Thread.Send_Data(crui_pkt);
                }
            }
        });

    }

    Button.OnClickListener mClickListener = new View.OnClickListener(){
      public void onClick(View v){
          switch(v.getId()){
              case R.id.button_connect:
                  Log.d("tag","TEST5 " + isConnected );
                  if(isConnected != true){
                      TCPClient_Thread = new ConnectThread(hostname,port);
                      TCPClient_Thread.start();
                      connect_btn.setText("Disconnect");
                  }else{
                      try{
                          TCPClient_Thread.sock.close();
                      }catch (IOException e){
                          e.printStackTrace();
                      }
                      connect_btn.setText("Connect");
                  }

                  break;

              case R.id.button_speed_up:
                  TCPClient_Thread.Send_Data("START,UPXX,000,END");
                  break;
              case R.id.button_speed_down:
                  TCPClient_Thread.Send_Data("START,DOWN,000,END");
                  break;
              case R.id.button_stop:
                  TCPClient_Thread.Send_Data("START,STOP,000,END");
                  break;
          }
      }
    };

    class ConnectThread extends Thread{
        String m_hostname;
        int m_port;
        BufferedReader socket_in;
        PrintWriter socket_out;
        String[] status_pkt;
        EditText speed_value;

        String data;
        Socket sock;

        public ConnectThread(String addr, int port){
            m_hostname = addr;
            m_port = port;
        }

        public void Send_Data(String message) {
            socket_out.println(message);
        }

        public void Close(){
            isConnected = false;
            try{
                sock.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        public void run(){
            try {
                int port = 5000;

                sock = new Socket(m_hostname, m_port);
                socket_out = new PrintWriter(sock.getOutputStream(), true);
                socket_in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

                Thread.sleep(1000);

                if(sock.isConnected()) {
                    isConnected = true;
                    connect_btn.setText("Disconnect");
                    //status_image.setImageResource(R.drawable.connected);
                }
            } catch(Exception ex){
                ex.printStackTrace();
            }
            try{
                while(true){
                    data = socket_in.readLine();
                    status_pkt = data.split(",");

                    if(status_pkt.length == 4)
                    {
                        if(status_pkt[0].equals(START_MSG) && status_pkt[3].equals(END_MSG))
                        {
                            int speed_sb_value = Integer.parseInt(status_pkt[1]);
                            speed_sb.setProgress(speed_sb_value);
                        }
                    }
                }
            }catch(Exception e){
                Log.d("tag","TEST4 :"+e);
            }
        }
    }
}
