package app;
/*
 * Decompiled with CFR 0_114.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;

public class WazeListener
implements Runnable {
    private static final String USER_AGENT = "Mozilla/5.0";
    private HashMap<String, JSONObject> events = new HashMap<String, JSONObject>();
    private String path;
    private boolean active;
    private long tiempo;

    private String sendGet(String url) throws Exception {
        String inputLine;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection)obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.ISO_8859_1));
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(new String(inputLine.getBytes("ISO-8859-1"), "UTF-8"));
        }
        in.close();
        return response.toString();
    }

    private void readResponse(String alerts) throws Exception {
        JSONObject json = new JSONObject(alerts);
        JSONArray alerts_jsonarray = json.getJSONArray("alerts");
        System.out.println("[INFO] " + this.events.size() + " alertas existentes previamente");
        System.out.println("[INFO] " + alerts_jsonarray.length() + " alertas a agregar");
        int pisadas = 0;
        int nuevas = 0;
        int i = 0;
        while (i < alerts_jsonarray.length()) {
            JSONObject alert_json = alerts_jsonarray.getJSONObject(i);
            String id = alert_json.getString("id");
            if (id != null) {
                if (this.events.containsKey(id)) {
                    ++pisadas;
                } else {
                    ++nuevas;
                }
                this.events.put(id, alert_json);
            }
            ++i;
        }
        System.out.println("[INFO] " + pisadas + " alertas estaban repetidas");
        System.out.println("[INFO] " + nuevas + " alertas nuevas");
        System.out.println("[INFO] " + this.events.size() + " alertas existentes ahora");
    }

    private void saveToFile() {
        String pathfile = String.valueOf(this.path) + File.separator + "Waze_" + new SimpleDateFormat("dd-MM-yyyy").format(new Date()) + ".json";
        String pathfileaux = String.valueOf(this.path) + File.separator + "Waze_" + System.currentTimeMillis() + "_aux.json";
        try {
            PrintWriter auxwriter = new PrintWriter(pathfileaux, "UTF-8");
            JSONArray jsonarray = new JSONArray();
            for (JSONObject object : this.events.values()) {
                jsonarray.put(object);
            }
            auxwriter.print(jsonarray.toString());
            auxwriter.close();
            PrintWriter writer = new PrintWriter(pathfile, "UTF-8");
            writer.print(jsonarray.toString());
            writer.close();
            File auxfile = new File(pathfileaux);
            auxfile.delete();
        }
        catch (FileNotFoundException e) {
            System.out.println("[ERROR] El archivo " + e.getMessage() + " no se pudo escribir");
        }
        catch (UnsupportedEncodingException e) {
            System.out.println("[ERROR] No se soporta el encode " + e.getMessage());
        }
    }

    public WazeListener(String path, long tiempo) {
        this.setActive(true);
        this.setPath(path);
        this.tiempo = tiempo * 1000;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setPath(String path) {
        this.path = path;
        try {
            String pathfile = String.valueOf(path) + File.separator + "Waze_" + new SimpleDateFormat("dd-MM-yyyy").format(new Date()) + ".json";
            String content = this.readFile(pathfile, StandardCharsets.UTF_8);
            JSONArray alerts_jsonarray = new JSONArray(content);
            System.out.println("[INFO] " + alerts_jsonarray.length() + " alertas leidas del archivo ya existente");
            int i = 0;
            while (i < alerts_jsonarray.length()) {
                JSONObject alert_json = alerts_jsonarray.getJSONObject(i);
                String id = alert_json.getString("id");
                if (id != null) {
                    this.events.put(id, alert_json);
                }
                ++i;
            }
        }
        catch (JSONException | IOException e) {
            System.out.println("[INFO] El archivo " + e.getMessage() + " no se pudo leer o no existia");
        }
    }

    private String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path, new String[0]));
        return new String(encoded, encoding);
    }

    @Override
    public void run() {
        while (this.active) {
            try {
                System.out.println("------------------------");
                String alerts = this.sendGet("https://www.waze.com/row-rtserver/web/TGeoRSS?ma=800&left=-58.5324&right=-58.3031&bottom=-34.7075&top=-34.5329&types=alerts");
                this.readResponse(alerts);
                this.saveToFile();
            }
            catch (Exception e) {
                System.out.println("[ERROR] No se pudo obtener o interpretar la informacion del servidor");
            }
            try {
                Thread.sleep(this.tiempo);
                continue;
            }
            catch (InterruptedException e) {
                System.out.println("------------------------");
                System.out.println("[INFO] Thread interrumpido");
            }
        }
    }
}

