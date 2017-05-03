package net.justtim.sotw;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableRow.LayoutParams;
import android.graphics.Typeface;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    TableLayout myTableLayout = null;
    int seg;
    long lastUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Switch from logo theme to proper theme...

        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);
        this.setTitle("ARCC Segment of the week");
    }

    /**
     * Called when we get back to the front, check whether we need to
     * refresh the data or not
     */
    @Override
    public void onResume() {
        super.onResume();
        // Called when activity comes to front, check if due to reload data?
        long now = new Date().getTime();
        long timeLag;
        timeLag = now - lastUpdated;

        if ((now - lastUpdated) > 30000) {
            doRefresh();
        }
    }

    /**
     * Called when the refresh / sync button is clicked
     *
     * @param view
     */
    public void refreshData(View view) {
        doRefresh();
    }

    private void doRefresh() {
        long now = new Date().getTime();
        if ((now - lastUpdated) > 30000) {
            Toast.makeText(getApplicationContext(), "Refreshing data...", Toast.LENGTH_SHORT).show();
            new RetrieveFeedTask().execute();  // Fire off the fetch data thread
        }
    }

    /**
     * Called by the fetch data thread when it has got the data
     *
     * @param data
     */
    public void gotData(JSONObject data) {
        seg = 0;



        if (myTableLayout == null) {
            myTableLayout = (TableLayout) findViewById(R.id.maintable);
        }

        myTableLayout.removeAllViews();

        // See what segments we've got...
        long seg1ID;
        lastUpdated = new Date().getTime();
        try {
            if (data == null) {
                showNoDataMsg();
            } else {
                if (data.getJSONObject("segment1").getString("Name").equals("-")) {
                    showNoSegmentMsg();
                } else {
                    JSONObject s1 = data.getJSONObject("segment1");
                    String seg1Name = s1.getString("Name");
                    if (seg1Name.equals("-") == false) {
                        seg1ID = s1.getLong("ID");
                        String seg1Gradient = s1.getString("Gradient");
                        String seg1Distance = s1.getString("Distance");
                        addSegment(seg1ID, seg1Name, seg1Gradient, seg1Distance);
                        if (data.getJSONArray("Items").length() == 0) {
                            showNoAttemptsMsg();
                        } else {
                            addHeaders();
                            scanArray(data, seg1ID);

                            JSONObject s2 = data.getJSONObject("segment2");
                            String seg2Name = s2.getString("Name");
                            if (seg2Name.equals("-") == false) {
                                long seg2ID = s2.getLong("ID");
                                String seg2Gradient = s2.getString("Gradient");
                                String seg2Distance = s2.getString("Distance");

                                addSegment(seg2ID, seg2Name, seg2Gradient, seg2Distance);
                                addHeaders();

                                scanArray(data, seg2ID);

                                JSONObject s3 = data.getJSONObject("segment3");
                                String seg3Name = s3.getString("Name");

                                if (seg3Name.equals("-") == false) {
                                    long seg3ID = s3.getLong("ID");
                                    String seg3Gradient = s3.getString("Gradient");
                                    String seg3Distance = s3.getString("Distance");

                                    addSegment(seg3ID, seg3Name, seg3Gradient, seg3Distance);
                                    addHeaders();

                                    scanArray(data, seg3ID);
                                }
                            }
                        }
                    } else {
                        showNoSegmentsMsg();
                    }

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * When returned JSON object is null
     */
    private void showNoDataMsg() {
        Toast.makeText(getApplicationContext(), "No data returned, please try later", Toast.LENGTH_LONG).show();
        return;
    }

    private void showNoSegmentsMsg() {
        int blackColor = getResources().getColor(R.color.colorRed);

        TableRow tr = new TableRow(this);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.span = 3;

        tr.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        TableRow tr1 = new TableRow(this);
        tr1.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        /** Creating a TextView to add to the row **/
        TextView gradTV = new TextView(this);
        gradTV.setTextSize(24);
        gradTV.setText("No segment set (yet).");
        gradTV.setTextColor(blackColor);
        gradTV.setLayoutParams(lp);
        tr1.addView(gradTV);

        // Add the TableRow to the TableLayout
        myTableLayout.addView(tr1, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        addDivider(1);
        return;
    }

    /**
     * A general message in case we got no attempts.
     */
    private void showNoAttemptsMsg() {
        int blackColor = getResources().getColor(R.color.colorRed);

        TableRow tr = new TableRow(this);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.span = 3;

        tr.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        TableRow tr1 = new TableRow(this);
        tr1.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        /** Creating a TextView to add to the row **/
        TextView gradTV = new TextView(this);
        gradTV.setTextSize(24);
        gradTV.setText("No attempts found (yet).");
        gradTV.setTextColor(blackColor);
        gradTV.setLayoutParams(lp);
        tr1.addView(gradTV);

        // Add the TableRow to the TableLayout
        myTableLayout.addView(tr1, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        addDivider(1);
        return;
    }

    /**
     * A general message in case segment1 is "-".
     */
    private void showNoSegmentMsg() {
        Toast.makeText(getApplicationContext(), "No segments set yet, please try later", Toast.LENGTH_LONG).show();
        return;
    }

    /**
     * This one is aware of whether the effort needs to be greyed out or not
     *
     * @param rank
     * @param name
     * @param athleteID
     * @param time
     * @param activityID
     * @param greyed
     */
    public void addData(String rank, String name, long athleteID, String time, long activityID, boolean greyed) {

        int blackColor = getResources().getColor(R.color.colorBlack);
        int linkColor = getResources().getColor(R.color.colorLink);

        int greyColor = getResources().getColor(R.color.colorGrey);
        int greyLinkColor = getResources().getColor(R.color.colorGreyLink);

        /** Create a TableRow dynamically **/
        TableRow tr = new TableRow(this);
        LayoutParams lpRow = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        tr.setLayoutParams(lpRow);

        /** Creating a TextView to add to the row **/
        TextView rankTV = new TextView(this);
        rankTV.setText(rank);
        rankTV.setTextSize(16);
        if (greyed) {
            rankTV.setTextColor(greyColor);
        } else {
            rankTV.setTextColor(blackColor);
        }

        LayoutParams lpRank = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lpRank.weight = 1;
        rankTV.setLayoutParams(lpRank);
        rankTV.setPadding(5, 5, 5, 25);
        tr.addView(rankTV);

        /** Creating another textview **/
        TextView nameTV = new TextView(this);
        nameTV.setLinksClickable(true);
        nameTV.setTextSize(16);
        if (greyed) {
            nameTV.setTextColor(greyColor);
            nameTV.setLinkTextColor(greyColor);
        } else {
            nameTV.setTextColor(linkColor);
            nameTV.setLinkTextColor(linkColor);
        }

        nameTV.setMovementMethod(LinkMovementMethod.getInstance());

        if (greyed) {
            nameTV.setText(name);
        } else {
            nameTV.setText(Html.fromHtml("<a href='https://www.strava.com/athletes/" + athleteID + "'>" + name + "</a>"));
        }

        LayoutParams lpName = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lpName.weight = 2;
        nameTV.setLayoutParams(lpName);
        nameTV.setPadding(5, 5, 5, 25);
        tr.addView(nameTV); // Adding textView to tablerow.

        /** Creating another textview **/
        TextView timeTV = new TextView(this);
        timeTV.setLinksClickable(true);
        timeTV.setTextSize(16);
        timeTV.setMovementMethod(LinkMovementMethod.getInstance());
        timeTV.setText(Html.fromHtml("<a href='https://www.strava.com/activities/" + activityID + "'>" + time + "</a>"));
        if (greyed) {
            timeTV.setLinkTextColor(greyLinkColor);
            timeTV.setTextColor(greyLinkColor);
        } else {
            timeTV.setLinkTextColor(linkColor);
            timeTV.setTextColor(linkColor);
        }

        LayoutParams lpTime = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lpTime.weight = 2;
        timeTV.setLayoutParams(lpTime);
        timeTV.setPadding(5, 5, 5, 25);
        tr.addView(timeTV); // Adding textView to tablerow.

        // Add the TableRow to the TableLayout
        myTableLayout.addView(tr, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    /**
     * This function add the headers to the table
     **/
    public void addHeaders() {

        /** Create a TableRow dynamically **/
        TableRow tr = new TableRow(this);

        LayoutParams lpRow = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        tr.setLayoutParams(lpRow);

        /** Creating a TextView to add to the row **/
        TextView rankTV = new TextView(this);
        rankTV.setText("Rank");
        //rankTV.setTextColor(Color.GRAY);
        rankTV.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LayoutParams lpRank = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lpRank.weight = 1;
        rankTV.setLayoutParams(lpRank);
        rankTV.setPadding(5, 5, 5, 0);
        tr.addView(rankTV);  // Adding textView to tablerow.

        /** Creating another textview **/
        TextView nameTV = new TextView(this);
        nameTV.setText("Name");

        LayoutParams lpName = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lpName.weight = 2;
        nameTV.setLayoutParams(lpName);
        nameTV.setPadding(5, 5, 5, 0);
        nameTV.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tr.addView(nameTV); // Adding textView to tablerow.

        /** Creating another textview **/
        TextView timeTV = new TextView(this);
        timeTV.setText("Time");
        LayoutParams lpTime = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lpTime.weight = 2;
        timeTV.setLayoutParams(lpTime);
        timeTV.setPadding(5, 5, 5, 0);
        timeTV.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tr.addView(timeTV); // Adding textView to tablerow.

        // Add the TableRow to the TableLayout
        TableLayout.LayoutParams lpToAdd = new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        myTableLayout.addView(tr, lpToAdd);

        addDivider(1);
    }

    /**
     * This function adds a segment header, describing the name gradient and length
     *
     * @param id       Segment id, so we can create a link to it
     * @param name     The name
     * @param gradient The average gradient
     * @param length   The length in metres
     */
    public void addSegment(long id, String name, String gradient, String length) {

        int blackColor = getResources().getColor(R.color.colorBlack);
        int linkColor = getResources().getColor(R.color.colorLink);

        if (seg != 0) {
            addDivider(2);
        }
        seg++;


        TableRow tr = new TableRow(this);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.span = 3;

        /** Create a TableRow dynamically **/
        //tr = new TableRow(this);
        LayoutParams lpRow = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        tr.setLayoutParams(lpRow);

        /** Creating a TextView to add to the row **/
        TextView titleTV = new TextView(this);
        titleTV.setLinksClickable(true);
        titleTV.setTextSize(18);
        titleTV.setMovementMethod(LinkMovementMethod.getInstance());
        titleTV.setText(Html.fromHtml("<a href='https://www.strava.com/segments/" + String.valueOf(id) + "'>" + name + "</a>"));
        titleTV.setTextColor(linkColor);
        titleTV.setLinkTextColor(linkColor);
        titleTV.setTypeface(null, Typeface.BOLD);
        titleTV.setLayoutParams(lp);
        titleTV.setPadding(5, 5, 5, 5);
        tr.addView(titleTV);

        // Add the TableRow to the TableLayout
        myTableLayout.addView(tr, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        TableRow tr1 = new TableRow(this);
        tr1.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        /** Creating a TextView to add to the row **/
        TextView gradTV = new TextView(this);
        gradTV.setTextSize(18);
        gradTV.setText("Average gradient: " + gradient + " %");
        gradTV.setTextColor(blackColor);
        gradTV.setLayoutParams(lp);
        gradTV.setPadding(5, 5, 5, 5);
        tr1.addView(gradTV);

        // Add the TableRow to the TableLayout
        myTableLayout.addView(tr1, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        TableRow tr2 = new TableRow(this);
        tr2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        /** Creating a TextView to add to the row **/
        TextView lengthTV = new TextView(this);
        lengthTV.setTextSize(18);


        double lengthMetres = Double.valueOf(length);
        if(lengthMetres < 1500) {
            length = length + " metres";
        } else {
            if(lengthMetres < 5000) {
                length = round(lengthMetres, 3);
            } else {
                if(lengthMetres > 15000) {
                    length = round(lengthMetres, 1);
                } else {
                    length = round(lengthMetres, 2);
                }
            }
        }


        lengthTV.setText("Distance: " + length);
        lengthTV.setTextColor(blackColor);
        lengthTV.setLayoutParams(lp);
        lengthTV.setPadding(5, 5, 5, 5);
        tr2.addView(lengthTV);

        // Add the TableRow to the TableLayout
        myTableLayout.addView(tr2, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        addDivider(1);
        return;
    }

    /**
     * This processes the array, getting results for those attempts in the data which
     * are for the specified segment.
     *
     * @param data
     * @param segID
     */
    private void scanArray(JSONObject data, long segID) {
        //long[] athletes = new long[120];
        ArrayList athletes = new ArrayList<Long>();

        int rankVal = 0;
        double lastTime = 0;
        try {
            for (int i = 0; i < data.getJSONArray("Items").length(); i++) {

                String elapsed = "";

                JSONObject attempt = data.getJSONArray("Items").getJSONObject(i);
                double minutes = Math.floor(attempt.getDouble("elapsed_time") / 60);
                double seconds = attempt.getDouble("elapsed_time") - minutes * 60;
                if(seconds < 10) {
                    elapsed = (int) minutes + ":0" + (int) seconds;
                } else {
                    elapsed = (int) minutes + ":" + (int) seconds;
                }
                if (attempt.getLong("segment") == segID) {
                    if (athletes.contains(attempt.getLong("athleteID"))) { // This person has already featured so 'grey them out'
                        addData("-", attempt.getString("name"), attempt.getLong("athleteID"), elapsed, attempt.getLong("activityID"), true);
                    } else {
                        if (lastTime < attempt.getDouble("elapsed_time")) { // Is this a slower time, i.e. not a tied result
                            rankVal++;
                        }
                        athletes.add(attempt.getLong("athleteID"));




                        addData(String.valueOf(rankVal), attempt.getString("name"), attempt.getLong("athleteID"), elapsed, attempt.getLong("activityID"), false);
                        lastTime = attempt.getDouble("elapsed_time");
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a dividing line, weight as specified.
     *
     * @param weight
     */
    private void addDivider(int weight) {
        int blackColor = getResources().getColor(R.color.colorBlack);
        TableRow tr = new TableRow(this);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.span = 3;
        tr.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        ImageView iv = new ImageView(this);
        iv.setBackgroundColor(blackColor);
        iv.setPadding(0, 0, 0, weight);
        iv.setLayoutParams(lp);
        tr.addView(iv);

        // Add the TableRow to the TableLayout
        myTableLayout.addView(tr, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    private String round(double metres, int places) {
        DecimalFormat df2;
        String fString = ".#######";

        if(places == 3) {
            fString = ".###";
        } else {
            if(places == 2) {
                fString = ".##";
            } else {
                if(places == 1) {
                    fString = ".#";
                }
            }
        }
        df2 = new DecimalFormat(fString);
        String result = df2.format(metres/1000) + " km";
        return result;
    }

    /**
     * Private inner class here, so we're off the main thread for network activity...
     */
    private class RetrieveFeedTask extends AsyncTask<String, Void, JSONObject> {

        protected JSONObject doInBackground(String... urls) {
            JSONObject theObject = null;

            try {
                URL url = new URL("https://8hc4h5psj6.execute-api.eu-west-1.amazonaws.com/prod");

                // Here we use the URL we've created:
                HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

                //con.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String resultString = response.toString();
                theObject = new JSONObject(resultString);

            } catch (MalformedURLException e) {
                Toast.makeText(getApplicationContext(), "MalformedURLException", Toast.LENGTH_SHORT).show();
            } catch (IOException ex) {
                Toast.makeText(getApplicationContext(), "IOException", Toast.LENGTH_SHORT).show();
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), "Exception", Toast.LENGTH_SHORT).show();
            }
            return theObject;
        }

        /**
         * Here's where we're called when we have the data
         *
         * @param data
         */
        protected void onPostExecute(JSONObject data) {
            gotData(data);
        }
    }
}
