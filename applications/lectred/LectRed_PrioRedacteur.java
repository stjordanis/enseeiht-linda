package applications.lectred;
// Time-stamp: <08 Apr 2008 11:35 queinnec@enseeiht.fr>

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import applications.lectred.Synchro.Assert;

/** Lecteurs/rédacteurs
 * stratégie d'ordonnancement: priorité aux rédacteurs,
 * implantation: avec un moniteur. */
public class LectRed_PrioRedacteur implements LectRed
{
    private Lock moniteur = new ReentrantLock();
    private Condition acces;
    private Condition lecture;
    private boolean ecriture;
    private int nbRedacteursAttente;
    private int nbLecteurs;

    public LectRed_PrioRedacteur() {
        acces = moniteur.newCondition();
        lecture = moniteur.newCondition();
    }

    public void demanderLecture() throws InterruptedException {
        moniteur.lock();
        while (ecriture || nbRedacteursAttente > 0) {
            lecture.await();
        }
        nbLecteurs++;
        moniteur.unlock();
    }

    public void terminerLecture() throws InterruptedException {
        moniteur.lock();
        nbLecteurs--;
        if (nbLecteurs == 0) {
            acces.signal();
        }
        moniteur.unlock();
    }

    public void demanderEcriture() throws InterruptedException {
        moniteur.lock();
        while (nbLecteurs > 0 || ecriture) {
            nbRedacteursAttente++;
            acces.await();
            nbRedacteursAttente--;
        }
        ecriture = true;
        moniteur.unlock();
    }

    public void terminerEcriture() throws InterruptedException {
        moniteur.lock();
        ecriture = false;
        acces.signal();
        if (nbRedacteursAttente == 0) {
            lecture.signalAll();
        }
        moniteur.unlock();
    }

    public String nomStrategie() {
        return "Stratégie: Priorité Rédacteurs.";
    }
}
