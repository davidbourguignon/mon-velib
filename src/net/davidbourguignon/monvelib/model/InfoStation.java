package net.davidbourguignon.monvelib.model;

import java.net.URL;
import java.io.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.util.Log;

public class InfoStation implements Serializable {

  private static final long serialVersionUID = 5263116456578091144L;

  private static final String TAG = InfoStation.class.getName();
  private static final String URL_VELIB_INFO = "http://www.velib.paris.fr/service/stationdetails/paris/";

//   <station>
//     <available>21</available>
//     <free>10</free>
//     <total>31</total>
//     <ticket>1</ticket>
//   </station>

  private long time;  /* pourquoi faire ? une clef unique (ici un
                        * "timestamp") passee en extra pour recuperer
                        * l'objet dans une HashMap commune ?
                        */
  private int available;
  private int free;
  private int total;
  private boolean ticket;

  private static Context context;

  public InfoStation(StationVelib st) {
    this(st.getNumber());
  }

  public InfoStation(int numeroDeStation) {
    this.time = System.currentTimeMillis(); // pourquoi ? voir ci-dessus.
    try {
      URL url = new  URL(URL_VELIB_INFO + numeroDeStation);
      new ParserXML(url.openStream());
    } catch (Exception e) {
       Log.e(TAG,"InfoStation en mode degrade...");
       setAvailable(20);
       setFree(10);
       setTotal(30);
    }
  }

  public int getAvailable(){
    return available;
  }
  public int getFree(){
    return free;
  }
  public int getTotal(){
    return total;
  }
  public boolean getTicket(){
    return ticket;
  }
  public void setAvailable(int available){
    this.available = available;
  }
  public void setFree(int free){
    this.free = free;
  }
  public void setTotal(int total){
    this.total = total;
  }
  public void setTicket(boolean ticket){
    this.ticket = ticket;
  }
  public String toString() {
    return "<" + getAvailable() + "," + getFree() + ">";
  }

  private class ParserXML extends DefaultHandler implements Serializable {
    private StringBuffer current;

    public ParserXML(InputStream in) {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            xr.setContentHandler(this);
            xr.parse(new InputSource(in));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startElement(String uri,
                              String localName,
                              String qName,
                              Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        current = new StringBuffer();
    }

    public void characters(char[] ch,
                            int start,
                            int length) throws SAXException {
        super.characters(ch, start, length);
        current.append(new String(ch, start, length));
    }

    public void endElement(String uri,
                            String localName,
                            String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (qName.equals("available")) {
            available = Integer.parseInt(current.toString());
        } else if (qName.equals("free")) {
            free = Integer.parseInt(current.toString());
        } else if (qName.equals("total")) {
            total = Integer.parseInt(current.toString());
        } else if (qName.equals("ticket")) {
            int val = Integer.parseInt(current.toString());
            if (val == 1) {
                ticket = true;
            } else if (val == 0) {
                ticket = false;
            } else {
                Log.e(TAG,"Unknown ticket value");
            }
        } else {
            // unparsed elements: <open> et <updated>
        }
    }
  }
}
