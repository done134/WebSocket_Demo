package com.yinzhendong.websocket;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btnStart, btnSendMsg;
    private TextView tvOutput;
    private LinearLayout chatLayout;
    private EditText ipAddress, port, messageInput;
    private WebSocket mSocket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnStart = (Button) findViewById(R.id.start);
        tvOutput = (TextView) findViewById(R.id.output);
        ipAddress = (EditText) findViewById(R.id.ip_address);
        btnSendMsg = (Button) findViewById(R.id.btn_send_message);
        messageInput = (EditText) findViewById(R.id.message_input);
        port = (EditText) findViewById(R.id.port);
        chatLayout = (LinearLayout) findViewById(R.id.chat_layout);
        btnStart.setOnClickListener(this);
        btnSendMsg.setOnClickListener(this);
    }

    private void start() {
        String ipStr = ipAddress.getText().toString().trim();
        String portStr = port.getText().toString().trim();
        if (TextUtils.isEmpty(ipStr)) {
            Toast.makeText(this, "请输入IP地址", Toast.LENGTH_LONG).show();
        }
        if (TextUtils.isEmpty(portStr)) {
            Toast.makeText(this, "请输入端口", Toast.LENGTH_LONG).show();
        }
        OkHttpClient mOkHttpClient = new OkHttpClient.Builder()
                .readTimeout(3000, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(3000, TimeUnit.SECONDS)//设置写的超时时间
                .connectTimeout(3000, TimeUnit.SECONDS)//设置连接超时时间
                .build();
        Request request = new Request.Builder().url("ws://echo.websocket.org").build();
//        Request request = new Request.Builder().url("ws://"+ipStr+":"+portStr).build();
        EchoWebSocketListener socketListener = new EchoWebSocketListener();
        mOkHttpClient.newWebSocket(request, socketListener);

        mOkHttpClient.dispatcher().executorService().shutdown();
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send_message://发送消息:
                String message = messageInput.getText().toString().trim();
                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(this, "请输入内容", Toast.LENGTH_LONG).show();
                    return;
                }
                mSocket.send(message);
                messageInput.setText("");
                break;
            case R.id.start://开始连接
                start();
                break;

        }
    }


    private final class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 5000;

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);
            onOpenStatus();
            mSocket = webSocket;
            webSocket.send("hello!");
            webSocket.send("how are you!");
            webSocket.send(ByteString.decodeHex("deadbeef"));
//            webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
            output("onOpen");

        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            super.onMessage(webSocket, bytes);
            output("receive bytes:" + bytes.hex());
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            super.onMessage(webSocket, text);
            output("receive text:" + text);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            super.onClosed(webSocket, code, reason);
            output("closed:" + reason);
            onCloasedStatus();
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            super.onClosing(webSocket, code, reason);
            output("closing:" + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            super.onFailure(webSocket, t, response);
            output("failure:" + t.getMessage());
        }

    }

    private void onOpenStatus() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatLayout.setVisibility(View.VISIBLE);
                btnStart.setVisibility(View.GONE);
            }
        });
    }

    private void onCloasedStatus() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatLayout.setVisibility(View.GONE);
                btnStart.setVisibility(View.VISIBLE);
            }
        });
    }
    private void output(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvOutput.setText(tvOutput.getText().toString()
                        + "\n\n" + text);
            }
        });
    }
}
