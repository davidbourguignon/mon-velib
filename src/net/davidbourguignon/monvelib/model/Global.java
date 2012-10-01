package net.davidbourguignon.monvelib.model;

import java.util.Map;
import java.util.TreeMap;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

public class Global extends Application {

   private static final String TAG = Global.class.getName();

   public static final String PREFS_FILENAME = "netDavidBourguignonGoogleLibPrefsFilename";
   public static final String DATE_KEY = "netDavidBourguignonGoogleLibDateKey";

   private ListeDesStationsVelib value = null;

   public void setValue(ListeDesStationsVelib liste) {
     value = liste;
   }

   public ListeDesStationsVelib getValue(){
     if (value == null) {
         Log.e(TAG, "Pas de stations disponibles");
     }
     return value;
   }

   public String getPrefsFilename() {
       return PREFS_FILENAME;
   }

   public String getDateKey() {
       return DATE_KEY;
   }

}
