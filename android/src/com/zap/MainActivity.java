package com.zap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zap.main.Comm;
import com.zap.main.Player;

public class MainActivity extends Activity {

    Button newGame;

    private static AssetManager assetManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // This call will result in better error messages if you
        // try to do things in the wrong thread.

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText et = (EditText) findViewById(R.id.RecvdMessage);
        et.setKeyListener(null);
        et = (EditText) findViewById(R.id.error_message_box);
        et.setKeyListener(null);

        // Set up a timer task. We will use the timer to check the
        // input queue every 500 ms

        TCPReadTimerTask tcp_task = new TCPReadTimerTask();
        Timer tcp_timer = new Timer();
        tcp_timer.schedule(tcp_task, 3000, 500);

        assetManager = getAssets();

        addButtonListenerNewGame();

        Player.activity = this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public static BufferedReader readCardTxt() {
        InputStream inputStream;
        try {
            inputStream = assetManager.open("cards.txt");
            return new BufferedReader(new InputStreamReader(inputStream));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    // Route called when the user presses "connect"

    public void openSocket(View view) {
        MyApplication app = (MyApplication) getApplication();
        Comm.setApp(app);

        TextView msgbox = (TextView) findViewById(R.id.error_message_box);

        // Make sure the socket is not already opened

        if (app.sock != null && app.sock.isConnected() && !app.sock.isClosed()) {
            msgbox.setText("Socket already open");
            return;
        }

        // open the socket. SocketConnect is a new subclass
        // (defined below). This creates an instance of the subclass
        // and executes the code in it.

        new SocketConnect().execute((Void) null);
    }

    // Called when the user wants to send a message

    public void sendMessage(View view) {
        MyApplication app = (MyApplication) getApplication();
        app.setPlayer(new Player());
        for (int i = 0; i < 7; i++) {
            app.getPlayer().initOpponent(i, 1, "ASDF");
        }
        // 7 is a magic number
        Comm.tellDE2Connected(7);
    }

    public void sendMessage2(View view) {
        // Get the message from the box
        EditText et = (EditText) findViewById(R.id.MessageText);
        String msg = et.getText().toString();

        MyApplication app = (MyApplication) getApplication();
        app.getPlayer().zapOpponent(Integer.valueOf(msg));
    }

    public void sendMessage3(View view) {
        MyApplication app = (MyApplication) getApplication();
        app.getPlayer().zapAll();
    }

    public void sendMessage4(View view) {
        MyApplication app = (MyApplication) getApplication();
        app.getPlayer().goToSaloon();
    }

    public void sendMessage5(View view) {
        MyApplication app = (MyApplication) getApplication();
        app.getPlayer().drinkBeer();
    }

    public void sendMessage6(View view) {
        EditText et = (EditText) findViewById(R.id.MessageText);
        String msg = et.getText().toString();

        MyApplication app = (MyApplication) getApplication();
        app.getPlayer().throwInJail(Integer.valueOf(msg), 72);
    }

    public void sendMessage7(View view) {
        EditText et = (EditText) findViewById(R.id.MessageText);
        String msg = et.getText().toString();

        MyApplication app = (MyApplication) getApplication();
        app.getPlayer().drawCards(Integer.valueOf(msg));
    }

    public void sendMessage8(View view) {
    }

    public void sendMessage9(View view) {
        EditText et = (EditText) findViewById(R.id.MessageText);
        String msg = et.getText().toString();

        MyApplication app = (MyApplication) getApplication();
        app.getPlayer().panicOpponent(Integer.valueOf(msg));
    }

    public void sendMessage10(View view) {
        EditText et = (EditText) findViewById(R.id.MessageText);
        String msg = et.getText().toString();

        MyApplication app = (MyApplication) getApplication();
        app.getPlayer().catBalouOpponent(Integer.valueOf(msg));
    }

    public void sendMessage11(View view) {
        EditText et = (EditText) findViewById(R.id.MessageText);
        String msg = et.getText().toString();

        MyApplication app = (MyApplication) getApplication();
        app.getPlayer().duelOpponent(Integer.valueOf(msg));
    }

    public void sendMessage12(View view) {
        MyApplication app = (MyApplication) getApplication();
        app.getPlayer().releaseTheAliens();
    }

    public void sendMessage13(View view) {
        MyApplication app = (MyApplication) getApplication();
        app.getPlayer().generalStore();
    }

    public void sendMessage14(View view) {
        MyApplication app = (MyApplication) getApplication();
        app.getPlayer().endTurn();
    }

    public void sendMessage15(View view) {
        EditText et = (EditText) findViewById(R.id.MessageText);
        String msg = et.getText().toString();

        Comm.sendMessage(msg);
    }

    public void setupGame() {
        Intent setupIntent = new Intent(getBaseContext(), GameActivity.class);
        startActivity(setupIntent);
    }

    // Called when the user closes a socket

    public void closeSocket(View view) {
        MyApplication app = (MyApplication) getApplication();
        Socket s = app.sock;
        try {
            s.getOutputStream().close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Construct an IP address from the four boxes

    public String getConnectToIP() {
        String addr = "";
        EditText text_ip;
        text_ip = (EditText) findViewById(R.id.ip1);
        addr += text_ip.getText().toString();
        text_ip = (EditText) findViewById(R.id.ip2);
        addr += "." + text_ip.getText().toString();
        text_ip = (EditText) findViewById(R.id.ip3);
        addr += "." + text_ip.getText().toString();
        text_ip = (EditText) findViewById(R.id.ip4);
        addr += "." + text_ip.getText().toString();
        return addr;
    }

    // Gets the Port from the appropriate field.

    public Integer getConnectToPort() {
        Integer port;
        EditText text_port;

        text_port = (EditText) findViewById(R.id.port);
        port = Integer.parseInt(text_port.getText().toString());

        return port;
    }

    // This is the Socket Connect asynchronous thread. Opening a socket
    // has to be done in an Asynchronous thread in Android. Be sure you
    // have done the Asynchronous Tread tutorial before trying to understand
    // this code.

    public class SocketConnect extends AsyncTask<Void, Void, Socket> {

        // The main parcel of work for this thread. Opens a socket
        // to connect to the specified IP.

        protected Socket doInBackground(Void... voids) {
            Socket s = null;
            String ip = getConnectToIP();
            Integer port = getConnectToPort();

            try {
                s = new Socket(ip, port);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return s;
        }

        // After executing the doInBackground method, this is
        // automatically called, in the UI (main) thread to store
        // the socket in this app's persistent storage

        protected void onPostExecute(Socket s) {
            MyApplication myApp = (MyApplication) MainActivity.this.getApplication();
            myApp.sock = s;
        }
    }

    // This is a timer Task. Be sure to work through the tutorials
    // on Timer Tasks before trying to understand this code.

    public class TCPReadTimerTask extends TimerTask {
        public void run() {
            MyApplication app = (MyApplication) getApplication();
            if (app.sock != null && app.sock.isConnected() && !app.sock.isClosed()) {

                try {
                    InputStream in = app.sock.getInputStream();

                    // See if any bytes are available from the Middleman

                    int bytes_avail = in.available();
                    if (bytes_avail > 0) {
                        // If so, read them in and create a string

                        byte buf[] = new byte[bytes_avail];
                        in.read(buf);

                        final String s = Comm.bth(buf);
                        Log.i("colin", "Message from Middleman: " + s);

                        Comm.receiveInterpretDE2(buf, app.getPlayer());

                        // As explained in the tutorials, the GUI can not be
                        // updated in an asynchronous task. So, update the GUI
                        // using the UI thread.

                        runOnUiThread(new Runnable() {
                            public void run() {
                                EditText et = (EditText) findViewById(R.id.RecvdMessage);
                                et.setText(s);
                            }
                        });

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addButtonListenerNewGame() {
        newGame = (Button) findViewById(R.id.buttonNewGame);
        newGame.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setupGame(view);
            }
        });
    }

    public void setupGame(View view) {
        Intent setupIntent = new Intent(getBaseContext(), GameActivity.class);
        // Intent setupIntent = new Intent(getBaseContext(),
        // PlayerActivity.class);
        // String value = "A string value to pass.";
        // setupIntent.putExtra("key", value);
        startActivity(setupIntent);
    }

}
