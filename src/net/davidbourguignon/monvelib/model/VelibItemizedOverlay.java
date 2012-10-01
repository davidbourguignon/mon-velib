package net.davidbourguignon.monvelib.model;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.OverlayItem;

public class VelibItemizedOverlay extends ItemizedOverlay<OverlayItem> {

    private List<OverlayItem> overlays = new ArrayList<OverlayItem>();
    private Context context;

    public VelibItemizedOverlay(Drawable defaultMarker, MapActivity context) {
        super(boundCenterBottom(defaultMarker));
        this.context = context;
    }

    public void addOverlay(OverlayItem overlay) {
        overlays.add(overlay);
        populate();
    }

    protected OverlayItem createItem(int i) {
      return overlays.get(i);
    }

    public int size() {
      return overlays.size();
    }

    protected boolean onTap(int index) {
      OverlayItem item = overlays.get(index);
      if (!item.getSnippet().equals("")) {
          AlertDialog.Builder dialog = new AlertDialog.Builder(context);
          dialog.setTitle(item.getTitle());
          dialog.setMessage(item.getSnippet());
          dialog.show();
      }
      return true;
    }

}
