package net.davidbourguignon.monvelib;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Date;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import net.davidbourguignon.monvelib.model.*;

public class ListeStationVelibActivity extends ListActivity {

    private static final String TAG = ListeStationVelibActivity.class.getName();

    private static final long ONE_WEEK_MILLIS
        = 1000 * 60 * 60 * 24 * 7; // nombre de millisecondes dans une semaine

    private ListeDesStationsVelib stations = null;
    List<StationVisitee> listeStationsVisitees = new ArrayList<StationVisitee>();
    private SharedPreferences settings = null;

    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      Log.i(TAG,"onCreate");

      setContentView(R.layout.main);
      setListAdapter(new ArrayAdapter<String>(this,
                                              android.R.layout.simple_list_item_1,
                                              new ArrayList<String>()));
      Global gs = (Global) getApplication();
      settings = getSharedPreferences(gs.getPrefsFilename(), MODE_PRIVATE);

      new AddStringTask().execute();
    }

    protected void onPause() {
        super.onPause();
        Log.i(TAG,"onPause");

        // sanity check
        for (String nom : stations.noms()) {
            int nombreFois = settings.getInt(nom, 0);
            if (nombreFois > 0) {
                Log.i(TAG,"station " + nom + " visitee " + nombreFois + " fois.");
            }
        }
    }

    private class StationVisitee {
        // public
        String nom = "";
        int nombreFois = 0;

        public StationVisitee(String str, int n) {
            nom = str;
            nombreFois = n;
        }
    }

    private class ComparaisonStations implements Comparator<StationVisitee> {
        public int compare(StationVisitee st1, StationVisitee st2) {
            int n1 = st1.nombreFois;
            int n2 = st2.nombreFois;
            return (n1 > n2 ? -1 : (n1 == n2 ? 0 : 1)); // les stations les plus visitees en premier
        }
    }

    private class AddStringTask extends AsyncTask<Void, String, Void> {
        private ProgressDialog mProgressDialog;

        protected void onPreExecute() {
            Log.i(TAG,"ListeStationVelibActivity AddStringTask onPreExecute()...");
            mProgressDialog = ProgressDialog.show(ListeStationVelibActivity.this,
                                                  "Patience...",
                                                  "Nous recherchons la liste des stations...",
                                                  true);
        }

        protected Void doInBackground(Void... unused) {
          try {
            Global gs = (Global) getApplication();
            SharedPreferences settings = getSharedPreferences(gs.getPrefsFilename(),
                                                              MODE_PRIVATE);
            long currentDateMillis = new Date().getTime();
            long previousDateMillis = settings.getLong(gs.getDateKey(),
                                                        currentDateMillis); // par defaut la date courante
            boolean isDownloaded = false;
            if (currentDateMillis == previousDateMillis || // initialisation
                currentDateMillis - previousDateMillis > ONE_WEEK_MILLIS) { // une semaine s'est ecoulee
                isDownloaded = true;
            }
            stations = new ListeDesStationsVelib(ListeStationVelibActivity.this, isDownloaded);
            gs.setValue(stations);

            // enregistrer la date courante dans les preferences
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong(gs.getDateKey(), currentDateMillis);
            editor.commit();
          } catch (Exception e) {
            e.printStackTrace();
          }

          // tri des stations par nombre de visites de l'utilisateur
          int nombreVisites = 0;
          for (String nom : stations.noms()) {
              nombreVisites = settings.getInt(nom, 0); // 0 si pas de preference disponible
              listeStationsVisitees.add(new StationVisitee(nom, nombreVisites));
          }
          Collections.sort(listeStationsVisitees, new ComparaisonStations());

          // affichage des stations triees
          for (StationVisitee st : listeStationsVisitees) {
              String nom = st.nom;
              int nombreFois = st.nombreFois;
              if (nombreFois > 0) {
                  Log.i(TAG,"station " + nom + " visitee " + nombreFois + " fois.");
              }
              publishProgress(nom);
          }

          return (null);
        }

        protected void onProgressUpdate(String... item) {
          ((ArrayAdapter)getListAdapter()).add(item[0]);
        }

        protected void onPostExecute(Void unused) {
          Log.i(TAG,"ListeStationVelibActivity AddStringTask onPostExecute()...");
          Toast.makeText(ListeStationVelibActivity.this, "Done!", Toast.LENGTH_SHORT).show();
          ListView listV = (ListView)ListeStationVelibActivity.this.findViewById(android.R.id.list);
          listV.setOnItemClickListener(new ItemClickListener());
          mProgressDialog.dismiss();
        }

    }

    private class ItemClickListener implements OnItemClickListener {

      public void onItemClick(AdapterView<?> parent,
                               View arg1,
                               int position,
                               long arg3) {
        String item = (String) parent.getItemAtPosition(position);
        Intent intent = new Intent(getBaseContext(),MapStationActivity.class);
        intent.putExtra("item", item);

        // enregistrer la station selectionnee dans les preferences
        SharedPreferences.Editor editor = settings.edit();
        int nombreVisites = settings.getInt(item, 0);
        editor.putInt(item, ++nombreVisites); // incrementer le nombre de visites
        editor.commit();

        startActivity(intent);
      }

    }

}
