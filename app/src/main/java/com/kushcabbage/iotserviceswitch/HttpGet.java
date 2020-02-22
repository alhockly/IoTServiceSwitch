package com.kushcabbage.iotserviceswitch;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpGet extends AsyncTask<Void,Void,Void> {

    OkHttpClient client = new OkHttpClient();
    int responsecode;
    Context con;
    String URL;
    public HttpGet(String url, Context context){
        URL=url;
        con=context;
    }

    @Override
    protected Void doInBackground(Void... voids) {



        try {
            URL="http://"+URL;
            run(URL);
            Log.d("Debug",URL);
        } catch (IOException e) {
            Log.d("Debug","OkHTTP GET failed");
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.d("Debug","Http request finished : "+responsecode);

    }

    void run(String url) throws IOException,RuntimeException {
        Request request = new Request.Builder()
                .url(url)
                .method("GET",null)
                .build();

        try (Response response = client.newCall(request).execute()) {
            responsecode = response.code();
        }
    }



}
