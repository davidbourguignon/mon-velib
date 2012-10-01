package net.davidbourguignon.monvelib;

import java.util.List;
import java.util.Set;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.graphics.drawable.Drawable;
import android.widget.Toast;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.GeoPoint;

import net.davidbourguignon.monvelib.model.*;

public class MapStationActivity extends MapActivity {

        private static final String TAG = MapStationActivity.class.getName();

        private MapController mapController;
        private MapView mapView;

        private ListeDesStationsVelib stations = null;
        private StationVelib station = null;

        // preferences
        private SharedPreferences settings = null;
        private double rayonRecherche = 0.0; // rayon de recherche des stations voisines
        private int niveauAgrandissement = 0; // pour le zoom

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.i(TAG,"onCreate");

            // initialisation
            setContentView(R.layout.mapstation);
            mapView = (MapView) findViewById(R.id.mapview);
            mapView.setBuiltInZoomControls(true);

            // recuperer la liste des stations
            Global gs = (Global) getApplication();
            stations = gs.getValue();

            // recuperer les preferences de l'utilisateur
            settings = getSharedPreferences(gs.getPrefsFilename(),
                                            MODE_PRIVATE);
            rayonRecherche
                = (double) (settings.getFloat("rayonRecherche",
                                               (float) 300)); // 300m si pas de preference disponible
            Log.i(TAG, "rayon de recherche " + rayonRecherche);
            niveauAgrandissement
                = settings.getInt("niveauAgrandissement", 18); // 18 si pas de preference disponible
            Log.i(TAG, "niveau d'agrandissement " + niveauAgrandissement);

            // recuperer la station choisie
            Bundle bundle = getIntent().getExtras();
            String item = bundle.getString("item");
            station = stations.lireStation(item);
            Log.i(TAG,"Station choisie : " + station.toString());
            Double latitude = station.getLatitude() * 1E6;
            Double longitude = station.getLongitude() * 1E6;

            // zoomer dessus
            GeoPoint point = new GeoPoint(latitude.intValue(), longitude.intValue());
            mapController = mapView.getController();

            ZoomButtonsController zoomButtonsControl = mapView.getZoomButtonsController();
            zoomButtonsControl.setOnZoomListener(new OnZoomListener() {
                public void onZoom(boolean zoomIn) {
                    final double FACTEUR = 1.5;
                    if (zoomIn) {
                        mapController.zoomIn();
                        rayonRecherche /= FACTEUR;
                        niveauAgrandissement++;
                    } else {
                        mapController.zoomOut();
                        rayonRecherche *= FACTEUR;
                        niveauAgrandissement--;
                    }
                }
                public void onVisibilityChanged(boolean visible) {
                    // rien
                }
            });

            mapController.animateTo(point);
            mapController.setZoom(niveauAgrandissement);

            // rechercher des infos sur les autres stations
            new SearchInfoStationTask().execute();
        }

        protected void onPause() {
            super.onPause();
            Log.i(TAG,"onPause");

            // enregistrer les preferences
            SharedPreferences.Editor editor = settings.edit();
            editor.putFloat("rayonRecherche", (float) rayonRecherche);
            Log.i(TAG, "rayon de recherche " + rayonRecherche);
            editor.putInt("niveauAgrandissement", niveauAgrandissement);
            Log.i(TAG, "niveau d'agrandissement " + niveauAgrandissement);
            editor.commit();
        }

        protected boolean isRouteDisplayed() {
            return false;
        }

        private class SearchInfoStationTask extends AsyncTask<Void, Void, Void> {
            private ProgressDialog mProgressDialog;

            protected void onPreExecute() {
                Log.i(TAG,"MapStationActivity SearchInfoStationTask onPreExecute()...");
                mProgressDialog = ProgressDialog.show(MapStationActivity.this,
                                                      "Patience...",
                                                      "Nous recherchons des informations sur les stations voisines...",
                                                      true);
            }

            protected Void doInBackground(Void... unused) {
                // determiner les stations voisines dans un rayon predefini
                Set<StationVelib> stationsVoisines
                    = stations.voisines(station, rayonRecherche);

                // afficher toutes les stations et recuperer leurs infos
                Drawable drawable
                    = MapStationActivity.this.getResources().getDrawable(R.drawable.cycling);
                VelibItemizedOverlay itemizedoverlay
                    = new VelibItemizedOverlay(drawable,
                                                MapStationActivity.this);

                Double latitude = 0.0;
                Double longitude = 0.0;
                GeoPoint point = null;
                InfoStation info = null;
                OverlayItem overlayitem = null;
                for (StationVelib st : stationsVoisines) {
                    info = stations.info(st);

                    latitude = st.getLatitude() * 1E6;
                    longitude = st.getLongitude() * 1E6;
                    point = new GeoPoint(latitude.intValue(), longitude.intValue());
                    String snippet = info.getFree() + " emplacements libres et " +
                                     info.getAvailable() + " vélos disponibles.";
                    overlayitem = new OverlayItem(point, st.toString(), snippet);
                    Log.i(TAG, st.toString() + "\n" + snippet);

                    itemizedoverlay.addOverlay(overlayitem);
                }
                List<Overlay> mapOverlays = mapView.getOverlays();
                mapOverlays.add(itemizedoverlay);

                // marquer la station choisie
                Drawable selectedDrawable
                    = MapStationActivity.this.getResources().getDrawable(R.drawable.tick_circle);
                VelibItemizedOverlay selectedItemizedoverlay
                    = new VelibItemizedOverlay(selectedDrawable, MapStationActivity.this);

                latitude = station.getLatitude() * 1E6;
                longitude = station.getLongitude() * 1E6;
                point = new GeoPoint(latitude.intValue(), longitude.intValue());
                overlayitem = new OverlayItem(point, "", ""); // pas de message donc pas d'AlertDialog
                selectedItemizedoverlay.addOverlay(overlayitem);

                mapOverlays.add(selectedItemizedoverlay);

                return (null);
            }

            protected void onProgressUpdate(Void... unused) {
              // rien a faire
            }

            protected void onPostExecute(Void unused) {
              Log.i(TAG,"MapStationActivity SearchInfoStationTask onPostExecute()...");
              Toast.makeText(MapStationActivity.this, "Done!", Toast.LENGTH_SHORT).show();
              mProgressDialog.dismiss();
              mapView.invalidate(); // forcer le redraw
            }

        }

}
