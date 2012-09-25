package ch.unizh.geo.webgen.tools;

/**
 * Diese Klasse erleichtert das Einlesen von der Standardeingabe,
 * indem sie daf&uuml;r einige Methoden zur Verfuegung stellt.
 */

// Importieren der Klassen für Ein-/Ausgabe und Eingabe-Fehlerbehandlung.
import java.io.*;

public class ConsoleInput {

  /**
   * Die folgenden Methoden <em>typ</em>Einlesen lesen jeweils eine
   * Variable vom Typ <em>typ</em> von der Standardeingabe ein und
   * behandeln dabei die Fehler, die beim Einlesen und Umwandeln des
   * Eingabestrings in den Typ <em>typ</em> enstehen k&ouml;nnen.
   * Die Methoden bekommen jeweils einen String als Parameter
   * (aufforderungsString) &uuml;bergeben. Dieser String wird
   * auf die Standardausgabe ausgegeben und dient dazu, dem Benutzer
   * mitzuteilen, welche Eingabe erwartet wird. Das Einlesen
   * wird jeweils solange wiederholt, bis beim Einlesen kein Fehler
   * mehr auftritt, insbesondere die Eingabe vom Typ <em>typ</em> ist.
  */

  public static String stringEinlesen ( String aufforderungsString ) {

    // Deklaration und Initialisierung der Variablen-Felder
    String eingabeString = "";
    boolean fehler; // fehler bekommt den Wert true falls ein Fehler
                    // beim Einlesen auftritt, andernfalls false
    // Eingabe-Stream console liest von der Standardeingabe (Tastatur)
    BufferedReader console =
      new BufferedReader( new InputStreamReader( System.in ) );

    // Einlesen einer Zeile wird wiederholt, bis dabei kein Fehler mehr auftritt
    do {
      fehler = false;
      // Aufforderung zur Eingabe eines Strings auf der Standardeingabe
      System.out.print( aufforderungsString );
      // Tritt ein Fehler beim Einlesen auf?
      try {
        eingabeString = console.readLine( ); // Einlesen einer Zeile
      }
      // ... so wird dieser hier entsprechend behandelt
      catch (IOException ioexception) {
        fehler = true;
        System.out.println( "Fehler beim Einlesen aufgetreten: "
                            + ioexception.getMessage() );
      }
    }
    while (fehler);
    return eingabeString;
  }

  public static int intEinlesen ( String aufforderungsString ) {

    // Deklaration und Initialisierung der Variablen-Felder
    String eingabeString = "";
    int eingabeZahl = 0;
    boolean fehler; // fehler bekommt den Wert true falls ein Fehler
                    // beim Einlesen auftritt, andernfalls false
    // Eingabe-Stream console liest von der Standardeingabe (Tastatur)
    BufferedReader console =
      new BufferedReader( new InputStreamReader( System.in ) );

    // Einlesen einer Zeile wird wiederholt, bis dabei kein Fehler mehr auftritt
    // und der eingegebene String tats&auml;chlich eine Zahl darstellt
    do {
      fehler = false;
      // Aufforderung zur Eingabe eines Strings auf der Standardeingabe
      System.out.print( aufforderungsString );
      // Tritt ein Fehler beim Einlesen auf oder
      // handelt es sich bei der Eingabe um keine Zahl?
      try {
        eingabeString = console.readLine();
        eingabeZahl = Integer.parseInt( eingabeString );
      }
      // ... so wird dies hier entsprechend behandelt
      catch (IOException ioexception) {
        fehler = true;
        System.out.println("Fehler beim Einlesen aufgetreten: "
                           + ioexception.getMessage() );
      }
      catch (NumberFormatException numberformatexception) {
        fehler = true;
        System.out.println("Sie muessen eine ganze Zahl eingeben, deren " +
          "Betrag kleiner oder gleich " + Integer.MAX_VALUE + " ist!");
      }
    }
    while (fehler);
    return eingabeZahl;
  }



  public static double doubleEinlesen ( String aufforderungsString ) {

    // Deklaration und Initialisierung der Variablen-Felder
    String eingabeString = "";
    double eingabeZahl = 0;
    boolean fehler; // fehler bekommt den Wert true falls ein Fehler
                    // beim Einlesen auftritt, andernfalls false
    // Eingabe-Stream console liest von der Standardeingabe (Tastatur)
    BufferedReader console =
      new BufferedReader( new InputStreamReader( System.in ) );

    // Einlesen einer Zeile wird wiederholt, bis dabei kein Fehler mehr auftritt
    // und der eingegebene String tats&auml;chlich eine Kommazahl darstellt
    do {
      fehler = false;
      // Aufforderung zur Eingabe eines Strings auf der Standardeingabe
      System.out.print( aufforderungsString );
      // Tritt ein Fehler beim Einlesen auf oder
      // handelt es sich bei der Eingabe um keine Kommazahl?
      try {
        eingabeString = console.readLine();
        eingabeZahl = Double.parseDouble( eingabeString );
      }
      // ... so wird dies hier entsprechend behandelt
      catch (IOException ioexception) {
        fehler = true;
        System.out.println("Fehler beim Einlesen aufgetreten: "
                           + ioexception.getMessage() );
      }
      catch (NumberFormatException numberformatexception) {
        fehler = true;
        System.out.println("Sie muessen eine Zahl eingeben, deren " +
          "Betrag kleiner oder gleich " + Double.MAX_VALUE + " ist!");
      }
    }
    while (fehler);
    return eingabeZahl;
  }
}