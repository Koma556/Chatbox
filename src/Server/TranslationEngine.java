package Server;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class TranslationEngine {

    public static String translateThis(String query, String fromLanguage, String toLanguage){
        String translation = "COULDN'T CONTACT REST API";
        try {
            JSONParser parser = new JSONParser();
            String translation_service = "https://api.mymemory.translated.net/get?q=";
            String languagePair = "&langpair=" + fromLanguage + "|" + toLanguage;
            URL mymemory = new URL( translation_service + URLEncoder.encode(query,"UTF-8") + languagePair);
            HttpURLConnection connection = (HttpURLConnection) mymemory.openConnection();
            connection.connect();
            String[] contentType = connection.getContentType().split(";");
            if (contentType[0].contains("json")) {
                String encoding = "UTF-8";
                if (contentType.length > 1)
                    encoding = contentType[1].substring(9);
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), encoding))) {
                    JSONObject response = (JSONObject) parser.parse(in);
                    JSONObject results = (JSONObject) (response.get("responseData"));
                    translation = (String) (results.get("translatedText"));
                }
            }else{}

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ParseException e1) {
            e1.printStackTrace();
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return translation;
    }
}
