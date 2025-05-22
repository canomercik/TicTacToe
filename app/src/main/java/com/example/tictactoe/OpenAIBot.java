package com.example.tictactoe;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OpenAIBot {
    private static final String TAG = "OpenAIBot";
    private static final String URL = "https://api.openai.com/v1/chat/completions";
    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    // 10s connect, 30s read, 5s overall call timeout
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .callTimeout(5, TimeUnit.SECONDS)
            .build();

    public interface MoveCallback {
        void onMove(int row, int col);
        void onError(Exception e);
    }

    /**
     * boardJson: örn "[["X"," "," "],[" ", "O"," "],...]" formatında
     */
    public void requestNextMove(String boardJson, MoveCallback callback) {
        try {
            // 1) Mesajları hazırla
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject()
                    .put("role", "system")
                    .put("content",
                            "You are a expert Tic-Tac-Toe AI. "
                                    + "First block opponent win, then take your win."));

            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content",
                            "Board state: " + boardJson
                                    + " Play as 'O'. Reply ONLY JSON: {\"row\":r,\"col\":c}."));

            JSONObject bodyJson = new JSONObject()
                    .put("model", "gpt-3.5-turbo")
                    .put("messages", messages);

            RequestBody body = RequestBody.create(JSON, bodyJson.toString());

            Request request = new Request.Builder()
                    .url(URL)
                    .addHeader("Authorization",
                            "Bearer " + BuildConfig.OPENAI_API_KEY)
                    .post(body)
                    .build();

            Log.d(TAG, "Sending OpenAI request...");

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "API call failed", e);
                    callback.onError(e);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    if (!response.isSuccessful()) {
                        String err = "";
                        try {
                            err = response.body() != null
                                    ? response.body().string()
                                    : "empty body";
                        } catch (IOException ignored){}
                        Log.e(TAG,
                                "API error " + response.code() + ": " + err);
                        callback.onError(
                                new IOException("OpenAI " + response.code()));
                        return;
                    }

                    try {
                        String resp = response.body().string();
                        Log.d(TAG, "API response: " + resp);
                        JSONObject msg = new JSONObject(resp)
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message");
                        String content = msg.getString("content").trim();
                        JSONObject move = new JSONObject(content);
                        int row = move.getInt("row");
                        int col = move.getInt("col");
                        Log.d(TAG, "Parsed move: " + row + "," + col);
                        callback.onMove(row, col);
                    } catch (Exception ex) {
                        Log.e(TAG, "Parse error", ex);
                        callback.onError(ex);
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Request setup error", e);
            callback.onError(e);
        }
    }
}
