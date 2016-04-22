package com.magnetic.hackathon.dogeideasapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

public class GridViewActivity extends AppCompatActivity {
    private static final String TAG = GridViewActivity.class.getSimpleName();

    // Oracle connection info
    private static final String JDBC_URL = "jdbc:oracle:thin:@10.150.1.38:1521:devqa";
    private static final String DB_USER = "qa";
    private static final String DB_PASSWORD = "password";

    // Oracle queries
    private static final String GRID_ITEM_QUERY = "select m_display_name, m_price, m_image_url, m_detail_url from ETLPREP.DOGE";
    private static final String GRID_ITEM_QUERY_CAT = "select m_display_name, m_price, m_image_url, m_detail_url from ETLPREP.CATS";

    private enum State {Dogs, Cat}
    State state;

    static boolean showPrice = true;

    private GridView mGridView;
    private ProgressBar mProgressBar;

    private FloatingActionButton dogButton;
    private FloatingActionButton catButton;
    private FloatingActionButton profileButton;
    private int dogPosition = 0;
    private int catPosition = 0;


    private GridViewAdapter mGridAdapter;
    private List<GridItem> mGridData = new ArrayList<>(1000);
    private List<GridItem> mGridDataCat = new ArrayList<>(1000);

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_grid_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.show_price:
                switchPriceView(item);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void switchPriceView(MenuItem item) {
        if (!showPrice) {
            showPrice = true;
            item.setIcon(R.drawable.ic_pets_white_24dp);
        } else {
            // showPrice is PriceState.ON
            showPrice = false;
            item.setIcon(R.drawable.ic_pets_black_24dp);
        }
        mGridAdapter.notifyDataSetChanged();
        mGridView.invalidateViews();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gridview);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitle("Magne-Doge");
        myToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        myToolbar.inflateMenu(R.menu.menu_grid_activity);
        setSupportActionBar(myToolbar);

        state = State.Dogs;

        dogButton = (FloatingActionButton) findViewById(R.id.button2);
        catButton = (FloatingActionButton) findViewById(R.id.button3);

        catButton.setVisibility(View.GONE);

        dogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation slideLeft = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.side_left);
                catButton.setAnimation(slideLeft);
                catButton.setVisibility(View.VISIBLE);
            }
        });

        catButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state == State.Dogs) {
                    mGridAdapter.setGridData(mGridDataCat);
                    mGridAdapter.notifyDataSetChanged();
                    mGridView.invalidateViews();
                    dogPosition = mGridView.getFirstVisiblePosition();
                    mGridView.smoothScrollToPosition(catPosition);
                    state = State.Cat;
                    catButton.setImageResource(R.drawable.ic_doge);
                    dogButton.setImageResource(R.drawable.ic_catz);
                } else {
                    mGridAdapter.setGridData(mGridData);
                    mGridAdapter.notifyDataSetChanged();
                    mGridView.invalidateViews();
                    catPosition = mGridView.getFirstVisiblePosition();
                    mGridView.smoothScrollToPosition(dogPosition);
                    state = State.Dogs;
                    catButton.setImageResource(R.drawable.ic_catz);
                    dogButton.setImageResource(R.drawable.ic_doge);
                }

                catButton.setVisibility(View.GONE);
            }
        });

        mGridView = (GridView) findViewById(R.id.gridView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mGridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, mGridData);
        mGridView.setAdapter(mGridAdapter);

        //Grid view click event
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //Get item at position
                GridItem item = (GridItem) parent.getItemAtPosition(position);
                if (state == State.Dogs)
                    openUrl(mGridData.get(position).getSiteURL());
                else
                    openUrl(mGridDataCat.get(position).getSiteURL());
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
            Connection con= null;
            Statement st= null;
            try {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                con = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
                st = con.createStatement();
                ResultSet rs = st.executeQuery(GRID_ITEM_QUERY);
                while (rs.next()) {
                    GridItem item = new GridItem();
                    item.setTitle(rs.getString(1));
                    item.setPrice(rs.getDouble(2));
                    item.setImageURL(rs.getString(3));
                    item.setSiteURL(rs.getString(4));

                    mGridData.add(item);
                }

                rs = st.executeQuery(GRID_ITEM_QUERY_CAT);
                while (rs.next()) {
                    GridItem item = new GridItem();
                    item.setTitle(rs.getString(1));
                    item.setPrice(rs.getDouble(2));
                    item.setImageURL(rs.getString(3));
                    item.setSiteURL(rs.getString(4));

                    mGridDataCat.add(item);
                }

                isSuccessful = true;

            } catch (ClassNotFoundException e) {
                isSuccessful = false;
                Log.e(TAG, "Unable to find the driver", e);
            }
            catch (SQLException e) {
                isSuccessful = false;
                Log.e(TAG, "Unable to query the data", e);
            }
            finally {
                try {
                    st.close();
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
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
