package com.idealist.stocks;

import android.app.Activity;

import android.util.Log;

import org.json.JSONArray;

import org.json.JSONException;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import java.util.List;

import io.socket.client.IO;

import io.socket.client.Socket;

import io.socket.emitter.Emitter;

public class SServer {
    public interface ServerCallBack{
        void onConnect();
        void  onError(String text);
        void onDisconnect();
    }

    public SServer(Activity activity, String address) {
        this.address = address;
        this.activity = activity;
    }

    Activity activity;
    String address;
    Socket socket;

    public void connect(final ServerCallBack callBack, final String login){
        try {
            socket = IO.socket(address).open().connect();
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    socket.emit("login",login);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callBack.onConnect();
                        }
                    });
                }
            });
            socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callBack.onError(String.valueOf(args[0]));
                        }
                    });
                }
            });
            socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callBack.onDisconnect();
                        }
                    });
                }
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
            callBack.onError("BAD URL");
        }
    }
    public void sendData(String key,String msg){
        if(!socket.connected()) {
            Log.i("SSSS", "sendData: " + "disconnected");
            return;
        }
        socket.emit(key,msg);
    }
    public interface ListenerCallBack{
        void call(String res);
    }
    public void addListener(String action, final ListenerCallBack callBack){
        if(!socket.connected()) {
            Log.i("SSSS", "addListener: " + "disconnected");
            return;
        }
        socket.on(action, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callBack.call(String.valueOf(args[0]));
                    }
                });
            }
        });
    }
    public boolean isConnected(){
        if(socket!= null && socket.connected()) return true;
        return false;
    }
    public void disconnect() throws URISyntaxException {
        socket.disconnect();
        socket.close();
        socket = IO.socket(address).open().connect();
    }
    String TAG = "USER";
    //---------------------------------------------------—
    //JSON parse
    public interface OnUsersReadyListener{
        void onUsersReady(List<User> users);
    }
    public void getTopPlayers(final OnUsersReadyListener listener){
        socket.on("top", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        listener.onUsersReady(parseUsers(String.valueOf(args[0])));
                    }
                });
            }
        });
    }

    public List<User> parseUsers(String json){
        List<User> users = new ArrayList<>();
        try {
            Log.i(TAG, "parseUsers: " + json);
            JSONObject mainj = new JSONObject(json);
            for (int i = 0; i < mainj.length(); i++) {
                JSONArray jsonArray = mainj.getJSONArray(String.valueOf(i));
                Log.i(TAG, "parseUsers: " + jsonArray);
                User user = new User();
                Log.i(TAG, "parseUsers: " + jsonArray.getString(0));
                user.setName(jsonArray.getString(0));
                user.setBalance(jsonArray.getDouble(1));
                user.setActionsCount(jsonArray.getInt(2));
                users.add(user);

            }
            return users;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
