//v0 1/1/17 (PM)
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Iterator;

public interface AccesBD {
    
	public void lier(String chemin) throws IOException, FileNotFoundException;
    /* Initialement, la base de données associée à AccèsBD est supposée vide 
     * (ne pas contenir de séquence).
	 * L'opération lier(-) associe à Acces BD la base de données contenue dans le fichier 
     * dont le chemin d'accès est fourni en paramètre.
     * Si le chemin est null ou incorrect, lier(-) affiche un message et n'affecte pas la
     * base de données précédemment associée à AccesBD.
     */
    
    public int taille();
	// nombre de séquences de la base de données

    public boolean estVide();
	// vrai si la base de données ne contient aucune séquence
	
    public Sequence lire(int position) throws IndexOutOfBoundsException ;
    /* Fournit la ième séquence de la base. 
     * Les indices commencent à 0.
     * L'exception IndexOutOfBoundsException est levée si la position demandée
     * est strictement négative ou supérieure à la taille de la base de données - 1
     */

    public Iterator<Sequence> itérateur();
    /* Itérateur permettant de parcourir en séquence la base de données 
     * L'opération remove() n'est (volontairement) pas implémentée.
     */
}