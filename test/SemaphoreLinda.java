package linda.test;

import linda.*;

public class SemaphoreLinda {

	private Linda linda;
	private Tuple t;
	
	public SemaphoreLinda(Integer nbJetons) {
		linda = new linda.shm.CentralizedLinda();	
		
		t = new Tuple();
		
		for (int i=0; i<nbJetons; i++) {
			this.V();
		}
	}

	public SemaphoreLinda() {
		this(0);		
	}
	
	public void V() {
		linda.write(t);
	}
	
	public void P() {
		linda.take(t);
	}
}
