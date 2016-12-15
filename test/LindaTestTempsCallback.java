package test;

import java.util.Date;

import linda.Linda;
import linda.Tuple;

/** Cette classe cherche à determiner le temps que met le serveur pour réveiller un process
 * lorsqu'un élément attendu est écrit sur le serveur et que de nombreux callbacks sont en attente.
 *
 */
public class LindaTestTempsCallback {

	public static void main(String[] args) {
		int nCallbacksMax = 10000;
		int nCallBackssPas = 500;
		long resu;
		
		System.out.println("Nb Callbacks       Tps(ms)");
		
		for (int nCallbacks=nCallBackssPas; nCallbacks<= nCallbacksMax; nCallbacks+= nCallBackssPas) {
			resu = test(nCallbacks);
			System.out.println(String.format("%12d%14d", nCallbacks, resu));
		}
	}
	
    public static long test(int nTuple) {    	
    	// Création de quelques tuples
    	Tuple t1 = new Tuple(5, 'a', "test", "extremité", 1);
    	Tuple t2 = new Tuple(7, 'b', "test", "extremité", 2);
    	Tuple t3 = new Tuple(2, "test", "milieu", 'c');
    	Tuple t4 = new Tuple(5, '8', 1, 'c', "toto");
    	Tuple t5 = new Tuple(3);
    	Tuple t6 = new Tuple("toto", "titi");

    	
    	
    	// Création du serveur
        // final Linda linda = new tshm.CentralizedLinda();
        final Linda linda = new shm.CentralizedLinda();
        // final Linda linda = new server.LindaClient("//localhost:4000/aaa");
        
        // On demande des données au serveur
        // TODO : faire faire les take par d'autres process
        // Sinon le main est bloqué
        linda.take(t6);
        for (int i=0; i<nTuple; i+=5) {
        	linda.take(t1);
        	linda.take(t2);
        	linda.take(t3);
        	linda.take(t4);
        	linda.take(t5);
        }
        
        // On fait le test
		long startTime = new Date().getTime();
		linda.write(t6);
		long endTime = new Date().getTime();
		
		return (endTime-startTime);
    }
}
