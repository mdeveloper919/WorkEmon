package com.workemon.melvin;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyActivity extends ActionBarActivity {

    private static final Object IO = null;
    private SearchView searchView;
    private ListView listView;
    private AdView mAdView;
    private SimpleAdapter adapter;
    static String[] urlArray ;
    static String[] dateArray;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        //--------------icon adding---------------------2016.7.28------------------
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.splash);


        // Initialize the Mobile Ads SDK.
        //    MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
        MobileAds.initialize(this, "ca-app-pub-1896414122247902~7539162229");

        // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
        // values/strings.xml.
        searchView = (SearchView) findViewById(R.id.searchView);

        searchView.setIconifiedByDefault(false);

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                return true;
            }

            public boolean onQueryTextSubmit(String query) {
                try {

                    getJobs(query);
                    //--------keyboard hidden-------------------------------------------
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);


                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);



        mAdView = (AdView) findViewById(R.id.ad_view);

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Called when leaving the activity */
    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }
    // HTTP GET request
    public void getJobs(String urlQuery) throws Exception {
        final String USER_AGENT = "Mozilla/5.0";

//http connection---------------------------------------------------
        urlQuery = urlQuery.replaceAll(" ", "%20");

        String urlString = "http://api.indeed.com/ads/apisearch?publisher=5572911862581719&format=json&userip=127.0.0.1&filter=1&limit=15&co=us&useragent=Mozilla/5.0%20(iPhone;%20CPU%20iPhone%20OS%209_1%20like%20Mac%20OS%20X)%20AppleWebKit/601.1.46%20(KHTML,%20like%20Gecko)%20Version/9.0%20Mobile/13B143%20Safari/601.1&v=2&q=" + urlQuery;

        URL url = new URL(urlString);

        //   System.out.println(url);

        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
//--------------------results get in jsonarray ----------------------------------------------------------------------------------

        JSONObject jo = new JSONObject(response.toString());
        JSONArray resultarr = jo.getJSONArray("results");

        String[] jobtitleArray = new String[resultarr.length()];
        String[] companyArray = new String[resultarr.length()];
        String[] formattedLocation = new String[resultarr.length()];
        String[] snippetArray = new String[resultarr.length()];
        String[] formattedRelativeTime = new String[resultarr.length()];
        urlArray = new String[resultarr.length()];
        dateArray = new String[resultarr.length()];

        for (int i = 0; i < resultarr.length(); i++)
        {

            JSONObject jObjresult = resultarr.getJSONObject(i);
            jobtitleArray[i] = jObjresult.getString("jobtitle");
            companyArray[i] = jObjresult.getString("company") + " - " + jObjresult.getString("formattedLocation");
            snippetArray[i] = jObjresult.getString("snippet").replaceAll("\\<[^>]*>","");
            formattedRelativeTime[i] = jObjresult.getString("formattedRelativeTime");
            urlArray[i] = jObjresult.getString("url");
            dateArray[i] = jObjresult.getString("date");
        }
//--------------------------display in listview-----------------------------------------------------------------------
        ListView listView = (ListView) findViewById(R.id.listView);
        String[] from = new String[] {"job", "company","snippet", "time"};
        int[] to = new int[] { R.id.jobtitle_view, R.id.company_view, R.id.snippet_view, R.id.time_view};

        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();

        for(int i = 0; i < jobtitleArray.length; i++){
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("job", "" + jobtitleArray[i]);
            map.put("company", "" + companyArray[i]);
            map.put("snippet", "" + snippetArray[i]);
            map.put("time", "" + formattedRelativeTime[i]);
            fillMaps.add(map);
        }

        adapter = new SimpleAdapter(this, fillMaps, R.layout.list_item, from, to);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new ListClickHandler());//--------------item click action---------------

    }

    //---------when item clicked, display in other view    -----------------------2016.7.28
    public class ListClickHandler implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {

            // TODO Auto-generated method stub

            TextView jobText = (TextView) view.findViewById(R.id.jobtitle_view);
            String jobtitle = jobText.getText().toString();
            TextView comText = (TextView) view.findViewById(R.id.company_view);
            String company = comText.getText().toString();

            // create intent to start another activity
            Intent intent = new Intent(MyActivity.this, detailView.class);
            // add the selected text item to our intent.
            intent.putExtra("jobtitle", jobtitle);
            intent.putExtra("company", company);
            intent.putExtra("url", urlArray[position]);
            intent.putExtra("date", dateArray[position]);

            startActivity(intent);

        }

    }
}
