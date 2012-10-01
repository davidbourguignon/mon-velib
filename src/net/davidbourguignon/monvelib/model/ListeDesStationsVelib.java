package net.davidbourguignon.monvelib.model;

import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import java.net.URL;
import java.net.URLConnection;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.io.File;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.Attributes;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.os.Environment;

public class ListeDesStationsVelib {

  private static final String TAG
    = ListeDesStationsVelib.class.getName();

  private static final String URL_VELIB_ALL
    = "http://www.velib.paris.fr/service/carto";

  public static final String STATIONS_DIRECTORY_NAME = "tp6GoogleLibStations";
  public static final String STATIONS_FILENAME = "tp6GoogleLibStations.xml";

  private File path = null;

  private Map<Integer, StationVelib> stations = new HashMap<Integer, StationVelib>(); // fastest
  private Map<String, Integer> noms = new TreeMap<String, Integer>(); // slower but sorted
  private Map<Position, Integer> positions = new HashMap<Position, Integer>(); // fastest

  public ListeDesStationsVelib(Context ctxt, boolean isDownloaded) {
    // initialisation du chemin
    if (path == null) {
        path = Environment.getExternalStoragePublicDirectory(STATIONS_DIRECTORY_NAME);
        path.mkdirs(); // cree eventuellement le(s) repertoire(s)
        Log.i(TAG, "Repertoire de sauvegarde des stations : " + path.getAbsolutePath());
    }
    try {
      InputStream is = null;
      if (isDownloaded) {
          // ouvrir le fichier sur le Web
          URL url = new  URL(URL_VELIB_ALL);
          is = url.openStream();

          // enregistrer le fichier en local
          try{
              // recuperer le contenu du fichier (non binaire)
              Log.i(TAG, "Recuperation du contenu du fichier distant...");
              URLConnection connection = url.openConnection();
              BufferedReader in
                  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
              String resultat = new String();
              String inputLine = in.readLine();
              while (inputLine != null) {
                  resultat += inputLine;
                  inputLine = in.readLine();
              }
              in.close();

              // ecrire le fichier
              Log.i(TAG, "Ecriture locale du fichier distant...");
              OutputStream os = new FileOutputStream(new File(path, STATIONS_FILENAME));
              os.write(resultat.getBytes());
              os.close();
          } catch(Exception e) {
              Log.e(TAG, e.getMessage());
          }
      } else {
          // ouvrir le fichier en local
          Log.i(TAG,"Lecture du fichier local...");
          is = new FileInputStream(new File(path, STATIONS_FILENAME));
      }
      new ParserXML(is);
    } catch (Exception e1) {
      // en mode degrade
      Log.e(TAG,"ListeDesStations Velib en mode degrade...");
      try {
        InputStream is = ctxt.getAssets().open("stations.xml");
        new ParserXML(is);
      } catch (Exception e2) {
          e2.printStackTrace();
      }
    }
  }

  public StationVelib lireStation(String nom) {
      StationVelib st = null;
      try {
          st = stations.get(noms.get(nom));
      } catch (Exception e) {
          e.printStackTrace();
      }
      return st;
  }

  public InfoStation info(StationVelib st) {
      InfoStation infost = null;
      try {
          infost = new InfoStation(st);
      } catch (Exception e) {
          e.printStackTrace();
      }
      return infost;
  }

  public Set<String> noms() {
      return noms.keySet();
  }

  public Set<StationVelib> voisines(StationVelib st, double rayon) {
      Set<StationVelib> voisines = new HashSet<StationVelib>(); // fastest
      double latitude = st.getLatitude();
      double longitude = st.getLongitude();

      Log.i(TAG, "station : " + st.toString());
      Log.i(TAG, "positions des stations voisines :");
      for (Position pos : positions.keySet()) {
          if (Distance.between(longitude, latitude,
                                 pos.longitude, pos.latitude) < rayon) {
              Log.i(TAG, "lng : " + pos.longitude + " ; lat : " + pos.latitude);
              voisines.add(stations.get(positions.get(pos)));
          }
      }
      return voisines;
  }

  private class Position {
      // public
      double latitude = 0.0;
      double longitude = 0.0;

      public Position(double lat, double lng) {
          latitude = lat;
          longitude = lng;
      }
  }

  private class ParserXML extends DefaultHandler implements Serializable {

    public ParserXML(InputStream in) throws Exception {
      Log.i(TAG, "Analyse (parsing) du fichier des stations...");
      SAXParserFactory spf = SAXParserFactory.newInstance();
      SAXParser sp = spf.newSAXParser();
      XMLReader xr = sp.getXMLReader();
      xr.setContentHandler(this);
      xr.parse(new InputSource(in));
    }

    public void startElement(String uri,
                              String localName,
                              String qName,
                              Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (qName.equals("marker")) {
            StationVelib station = new StationVelib();

            station.setName(attributes.getValue("name"));
            station.setNumber(Integer.parseInt(attributes.getValue("number")));
            station.setAddress(attributes.getValue("address"));
            station.setLatitude(Double.parseDouble(attributes.getValue("lat")));
            station.setLongitude(Double.parseDouble(attributes.getValue("lng")));

            if (attributes.getValue("bonus").equals("1"))
                station.setBonus(true);
            else
                station.setBonus(false);

            if (attributes.getValue("open").equals("1"))
                station.setOpen(true);
            else
                station.setOpen(false);

            stations.put(station.getNumber(), station);
            noms.put(station.getName(), station.getNumber());
            positions.put(new Position(station.getLatitude(),
                                        station.getLongitude()),
                          station.getNumber());

            // Idealement, on utiliserait un K-d tree
            // avec l'algorithme des k nearest neighbors...
            // Mais ici on se contente de la force brute en O(n^2).
            // (Pour plus d'infos, voir http://java-ml.sourceforge.net/
            // ou http://blog.afewguyscoding.com/2010/05/nearest-neighbors-java/)
        }
    }
  }
}
