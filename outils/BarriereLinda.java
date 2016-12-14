package linda.outils;

import linda.Linda;
import linda.Tuple;

public class BarriereLinda {

	private Linda linda;
	private Tuple t;
	private int nbProcess;
	private int nbWaiting;
	
	public BarriereLinda(int nbProcess) {
		linda = new linda.shm.CentralizedLinda();	
		
		t = new Tuple();
		
		this.nbProcess = nbProcess;
		this.nbWaiting = 0;
	}
	
	public void access() {
		nbWaiting++;
		if (nbWaiting==nbProcess) {
			for (int i=0; i<nbProcess; i++) {
				linda.write(t);
			}
			nbWaiting = 0;
		} else {
			linda.take(t);
		}
	}
	
}
