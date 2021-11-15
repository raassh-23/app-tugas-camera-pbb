package com.raassh.simplecamera;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {
    Button btnCam;
    Button btnList;
    ImageButton btnSend;
    ImageView imgView;
    EditText etServer;

    private static final int kodeKamera = 222;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCam = findViewById(R.id.btnCam);
        btnList = findViewById(R.id.btnList);
        btnSend = findViewById(R.id.btnSend);
        imgView = findViewById(R.id.imageView);
        etServer = findViewById(R.id.etServer);

        btnCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(it, kodeKamera);
            }
        });

        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://" + etServer.getText().toString() +"/api/image/list";

                Intent it = new Intent(MainActivity.this, ListFoto.class);
                it.putExtra("url", url);
                startActivity(it);
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bm = ((BitmapDrawable)imgView.getDrawable()).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                String base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP);

                JSONObject requestJson = new JSONObject();
                try {
                    requestJson.put("image", "data:image/png;base64,"+base64String);
                } catch (JSONException e) {
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("MAIN", "JSONException: " + e.getMessage());
                }

                String url = "http://" + etServer.getText().toString() + "/api/image/upload";
                JsonObjectRequest uploadRequest = new JsonObjectRequest
                        (Request.Method.POST, url, requestJson, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("MAIN", "Response: " + response.toString());

                                Toast.makeText(getBaseContext(), "Gambar berhasil tersimpan di server", Toast.LENGTH_SHORT).show();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getBaseContext(), error.toString(), Toast.LENGTH_LONG).show();
                                Log.e("MAIN", "VolleyError: " + error.toString());
                            }
                        });

                Toast.makeText(getBaseContext(), "Mengirim gambar ke server", Toast.LENGTH_SHORT).show();
                RequestQueueSingleton.getInstance(getBaseContext()).addToRequestQueue(uploadRequest);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case kodeKamera:
                    prosesKamera(data);
                    break;
            }
        }
    }

    private void prosesKamera(Intent datanya) {
        Bitmap bm = (Bitmap) datanya.getExtras().get("data");
        imgView.setImageBitmap(bm);

        btnSend.setEnabled(true);

        Toast.makeText(this, "Gambar sudah terload ke Imageview", Toast.LENGTH_SHORT).show();
    }
}