//v1 1/1/17 (PM)

class Sequence implements java.io.Serializable {
    private static final long serialVersionUID = 001L;

// Séquence = PDB ID, Chain ID, Nom Macromolecule, Sequence, Longueur séquence
    private String pbdId;
    private Character chainId;
    private String nomMolecule;
    private String seq;
    private Integer tailleSeq;

    public Sequence(String p, Character c, String n, String s, Integer t) {
        pbdId = p;
        chainId = c;
        nomMolecule = n;
        seq = s;
        tailleSeq = t;
    }

    public static int correspondance(char a, char b) {
        /* Score basique.
         * Il est possible de raffiner un peu en utilisant des matrices,
         * mais ce n'est pas essentiel ici.
         */
        if (a==b) {
            return 2;
        } else {
            return -1;
        }
    }

    public static int suppression(char a) {
        /* Même remarque : score basique.
        * Il est possible de raffiner, mais ce n'est pas essentiel ici.
        */
        return -2;
    }

    public static int insertion(char a) {
        /* Même remarque : score basique.
        * Il est possible de raffiner, mais ce n'est pas essentiel ici.
        */
        return -2;
    }

    public String lirePbdId() {
        return pbdId;
    }

    public void écrirePbdId(String s) {
        pbdId = s;
    }

    public Character lireChainId() {
        return chainId;
    }
    public void écrireChainId(Character s) {
        chainId = s;
    }

    public String lireNomMolecule() {
        return nomMolecule;
    }

    public void écrireNomMolecule(String s) {
        nomMolecule = s;
    }

    public String lireSéquence() {
        return seq;
    }

    public void écrireSéquence(String s) {
        seq = s;
    }

    public Integer lireTailleSeq() {
        return tailleSeq;
    }

    public void écrireTailleSeq(Integer s) {
        tailleSeq = s;
    }

    public String afficher() {
        return "["+pbdId+"-"+chainId+"/"+nomMolecule+" : "+seq+" ("+tailleSeq+")]";
    }
}