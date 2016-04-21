package com.magnetic.hackathon.dogeideasapp;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GridViewActivity extends Activity {
    private static final String TAG = GridViewActivity.class.getSimpleName();

    // Oracle connection info
    private static final String JDBC_URL = "jdbc:oracle:thin:@10.150.1.38:1521:devqa";
    private static final String DB_USER = "qa";
    private static final String DB_PASSWORD = "password";

    // Oracle queries
    private static final String GRID_ITEM_QUERY = "select m_display_name, m_price, m_image_url, m_detail_url from ETLPREP.DOGE";

    private GridView mGridView;
    private ProgressBar mProgressBar;


    private GridViewAdapter mGridAdapter;
    private List<GridItem> mGridData = new ArrayList<>(1000);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gridview);

        mGridView = (GridView) findViewById(R.id.gridView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mGridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, mGridData);
        mGridView.setAdapter(mGridAdapter);

        //Grid view click event
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //Get item at position
                GridItem item = (GridItem) parent.getItemAtPosition(position);
                openUrl(item.getSiteURL());
            }
        });
        new ProductFetcher().execute();
        mProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * open system browser and load the url
     * @param url
     */
    public void openUrl(String url) {
        String HTTP = "http://";
        String HTTPS = "https://";
        Uri webPage;

        if (!url.startsWith(HTTP) && !url.startsWith(HTTPS))
            url = "http://" + url;

        webPage = Uri.parse(url);

        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, webPage);
            startActivity(browserIntent);
        } catch (ActivityNotFoundException e) {
            ;
        }
    }

    /**
     * Fetches products from the db asynchronously
     */
    public class ProductFetcher extends AsyncTask<String, Void, Boolean> {

        /**
         * Fetches products from the db
         * @param params
         * @return
         */
        @Override
        protected Boolean doInBackground(String... params) {
            boolean isSuccessful = false;

            // Connect to Oracle Database
            try {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                Connection con = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(GRID_ITEM_QUERY);
                while (rs.next()) {
                    GridItem item = new GridItem();
                    item.setTitle(rs.getString(1));
                    item.setPrice(rs.getDouble(2));
                    item.setImageURL(rs.getString(3));
                    item.setSiteURL(rs.getString(4));

                    mGridData.add(item);
                }

                isSuccessful = true;

                st.close();
                con.close();
            } catch (ClassNotFoundException e) {
                isSuccessful = false;
                Log.e(TAG, "Unable to find the driver", e);
            }
            catch (SQLException e) {
                isSuccessful = false;
                Log.e(TAG, "Unable to query the data", e);
            }

            return isSuccessful;
        }

        @Override
        protected void onPostExecute(Boolean isSuccessful) {
            // Download complete. Lets update UI
            if (isSuccessful) {
                mGridAdapter.setGridData(mGridData);
            } else {
                Toast.makeText(GridViewActivity.this, "Failed to fetch data from DB!", Toast.LENGTH_SHORT).show();
            }
            //Hide progressbar
            mProgressBar.setVisibility(View.GONE);
        }
    }

}
