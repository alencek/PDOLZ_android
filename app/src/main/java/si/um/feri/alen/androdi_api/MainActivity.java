package si.um.feri.alen.androdi_api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wonderkiln.camerakit.CameraKitEventCallback;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {
    private TextView mTextViewResult;  //izpisemo vrnjen rezultat
    private TextView tvQuery;  //JQuery string
    String url = "http://192.168.2.100:81/ORV.aspx?izbira=igra";  //getUrl
    String postUrl = "http://192.168.2.100:81/ORV-post.aspx"; //post url
    CameraView cameraView;
    Button btnPost;
    Bitmap slika;
    Bitmap bitmap;
    String GetOdgovor;


    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvQuery = findViewById((R.id.tvQuery));
        mTextViewResult = findViewById(R.id.text_view_result);
        cameraView = (CameraView) findViewById(R.id.camera);
        cameraView.setJpegQuality(100);
        btnPost = (Button) findViewById(R.id.btnPost);



    }

    public void getZahteva(){


/*
   cameraView.captureImage(new CameraKitEventCallback<CameraKitImage>() {
       @Override
       public void callback(CameraKitImage cameraKitImage) {


           slika = cameraKitImage.getBitmap();  //Bitmap slika
       }


   });

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    slika.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
    String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);


*/



        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.alen);
        // bitmap = slika;


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] b = baos.toByteArray();
        String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);




        OkHttpClient client = new OkHttpClient();

        RequestBody postData = new FormBody.Builder()
                .add("type", "json")
                .add("image", encodedImage)
                .add("imageName", "Alen")
                .add("namig", "neki namig")
                .add("tocke", "koliko tock je vredno")
                .add("latitude", String.valueOf(12323))
                .add("longitude", String.valueOf(898798))
                .build();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postData)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = response.body().string();

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTextViewResult.setText(myResponse);
                        }
                    });
                }
            }
        });



    }


    String imageDescriptives;
    String Vprasanje;
    String Odgovor;
    String OdgovorAlt1;
    String OdgovorAlt2;
    public void getMetoda(View view) {

        OkHttpClient client = new OkHttpClient();


        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = response.header("test").toString();

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            GetOdgovor = myResponse;
                            //mTextViewResult.setText(GetOdgovor);
                            try {
                                JSONArray readerA = new JSONArray(GetOdgovor);
                                JSONObject reader = readerA.getJSONObject(0);
                                //[{"imageDescriptives":"del1","Vprasanje":"Bo delalo?","Odgovor":"Bo delalo!","OdgovorAlt1":"Bo delalo!","OdgovorAlt2":"Bo delalo!"}]
                                imageDescriptives = reader.getString("imageDescriptives");
                                Vprasanje = reader.getString("Vprasanje");
                                Odgovor = reader.getString("Odgovor");
                                OdgovorAlt1 = reader.getString("OdgovorAlt1");
                                OdgovorAlt2 = reader.getString("OdgovorAlt1");
                                mTextViewResult.setText("Slika: " + imageDescriptives  +",\n Vprasanje je: " + Vprasanje + ",\n Odgovor: " +
                                        Odgovor + ",\n OdgovorAlt1: " + OdgovorAlt1 + ",\n OdgovorAlt2: " + OdgovorAlt2);
                            } catch (JSONException e) {
                                String napaka ="Napaka JSON" +  e.toString();
                                mTextViewResult.setText(napaka);

                            }
                        }
                    });
                }
            }
        });



    }

    public void postMetoda(View view) {
        //getZahteva();


        cameraView.captureImage(new CameraKitEventCallback<CameraKitImage>() {
            @Override
            public void callback(CameraKitImage cameraKitImage) {
                //TO HTTP!!! itImage.getBitmap(),"slika"+imageCounter+".jpg", "NAMIG 123", "t1",2,2).execute();
                //new SaveBitmapTask().execute(cameraKitImage.getBitmap());

                new SendImageToServer(cameraKitImage.getBitmap(), "testna_slika.jpg", (Double)213.2132, (Double)2.321321).execute();
            }
        });

    }



    public class SendImageToServer extends AsyncTask<String, Void, String> {
        Bitmap orgImage;
        String imageName;

        double latitude;
        double longitude;


        public SendImageToServer(Bitmap image, String imageName, double latitude, double longitude) {
            this.orgImage = image;
            this.imageName = imageName;

            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mTextViewResult.setText("Poslano");

        }

        @Override
        protected String doInBackground(String... strings) {
           orgImage = Bitmap.createScaledBitmap(orgImage, 64, 128, false);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            orgImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

            try
            {
                OkHttpClient client = new OkHttpClient();

                RequestBody postData = new FormBody.Builder()
                        .add("type", "json")
                        .add("image", encodedImage)
                        .add("imageName", imageName)

                        .add("latitude", String.valueOf(latitude))
                        .add("longitude", String.valueOf(longitude))
                        .build();

                Request request = new Request.Builder()
                        .url(postUrl)
                        .post(postData)
                        .build();

                Response response = client.newCall(request).execute();
                String result = response.body().string();
                return result;
            }
            catch (Exception e)
            {
                return null;
            }

        }
    }
    }
