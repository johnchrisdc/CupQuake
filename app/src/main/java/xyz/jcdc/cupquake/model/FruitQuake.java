package xyz.jcdc.cupquake.model;

/**
 * Created by jcdc on 4/29/17.
 */

import android.os.AsyncTask;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import xyz.jcdc.cupquake.Variables;

import static xyz.jcdc.cupquake.MainActivity.FILTER_ALL;
import static xyz.jcdc.cupquake.MainActivity.FILTER_LAST_DAY;
import static xyz.jcdc.cupquake.MainActivity.FILTER_LAST_MONTH;
import static xyz.jcdc.cupquake.MainActivity.FILTER_LAST_WEEK;
import static xyz.jcdc.cupquake.MainActivity.FILTER_LAST_YEAR;
import static xyz.jcdc.cupquake.MainActivity.FILTER_TODAY;

public class FruitQuake {

    public interface FruitQuakeListener {
        void onStartQuaking();
        void onQuake(FruitQuake fruitQuake);
    }

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("metadata")
    @Expose
    private Metadata metadata;
    @SerializedName("features")
    @Expose
    private List<Feature> features = null;
    @SerializedName("bbox")
    @Expose
    private List<Double> bbox = null;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public List<Double> getBbox() {
        return bbox;
    }

    public void setBbox(List<Double> bbox) {
        this.bbox = bbox;
    }

    public static FruitQuake getFruitQuake(int filter) throws Exception {

        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();

        calendar.setTime(date);

        String start_date = "";

        switch (filter) {

            case FILTER_TODAY:
                calendar.add(Calendar.DATE, 0);
                break;

            case FILTER_LAST_DAY:
                calendar.add(Calendar.DATE, -1);
                break;

            case FILTER_LAST_WEEK:
                calendar.add(Calendar.DATE, -7);
                break;

            case FILTER_LAST_MONTH:
                calendar.add(Calendar.DATE, -30);
                break;

            case FILTER_LAST_YEAR:
                calendar.add(Calendar.DATE, -365);
                break;

            case FILTER_ALL:
                calendar.add(Calendar.DATE, -696969); //HIHIHIHI
                break;

        }

        start_date = dateFormat.format(calendar.getTime());

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .build();

        Request request = new Request.Builder()
                .url(Variables.API_QUAKE + start_date)
                .build();

        Log.d("FruitQuake", "URL: " + Variables.API_QUAKE + start_date);

        Response response = client.newCall(request).execute();
        String json = response.body().string();

        Log.d("FruitQuake", json);

        return new Gson().fromJson(json, FruitQuake.class);
    }

    public static class GetFruitQuake extends AsyncTask<Void, Void, FruitQuake> {

        private int filter;
        private FruitQuakeListener fruitQuakeListener;

        public GetFruitQuake(int filter, FruitQuakeListener fruitQuakeListener) {
            this.filter = filter;
            this.fruitQuakeListener = fruitQuakeListener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            fruitQuakeListener.onStartQuaking();
        }

        @Override
        protected FruitQuake doInBackground(Void... voids) {
            try {
                return getFruitQuake(filter);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(FruitQuake fruitQuake) {
            super.onPostExecute(fruitQuake);
            fruitQuakeListener.onQuake(fruitQuake);
        }
    }

}
