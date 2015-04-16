package net.learn2develop.NetworkingText;

import android.app.Activity;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.AsyncTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.learn2develop.Networking.R;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class NetworkingActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final EditText wordSearch = (EditText)findViewById(R.id.wordText);

        Button searchButton = (Button)findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AccessWebServiceTask().execute(wordSearch.getText().toString());
            }
        });

        // ---access a Web Service using GET---
        //new AccessWebServiceTask().execute("apple");
    }

    private InputStream OpenHttpConnection(String urlString)
            throws IOException {
        InputStream in = null;
        int response = -1;

        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();

        if (!(conn instanceof HttpURLConnection))
            throw new IOException("Not an HTTP connection");
        try {
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            response = httpConn.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
            }
        } catch (Exception ex) {
            Log.d("Networking", ex.getLocalizedMessage());
            throw new IOException("Error connecting");
        }
        return in;
    }

    private String WordDefinition(String word) {

        InputStream in = null;
        String strDefinition = "";

        try {
            in = OpenHttpConnection("http://services.aonaware.com/DictService/DictService.asmx/Define?word="
                    + word);
            XmlPullParser parser = Xml.newPullParser();
            try {
                parser.setInput(in, null);
                int type = parser.getEventType();
                String text = "";
                while (type != XmlPullParser.END_DOCUMENT) {
                    String tagName = parser.getName();
                    switch (type) {
                        case XmlPullParser.TEXT:
                            text = parser.getText();
                            break;
                        case XmlPullParser.END_TAG:
                            if (tagName.equalsIgnoreCase("WordDefinition") && parser.getDepth() == 4 ) {
                                strDefinition = text;
                            }
                            break;
                    }
                    type = parser.next();
                }
            } catch (XmlPullParserException ex) {
                ex.printStackTrace();
            }
            in.close();
        } catch (IOException e1) {
            Log.d("NetworkingActivity", e1.getLocalizedMessage());
        }
        return strDefinition;
    }

    private class AccessWebServiceTask extends
            AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls) {
            return WordDefinition(urls[0]);
        }

        protected void onPostExecute(String result) {
            TextView tv = (TextView) findViewById(R.id.textView2);
            tv.setText(result);
        }
    }

}