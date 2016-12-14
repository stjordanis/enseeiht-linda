package linda.test;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.After;

import linda.*;

public class LindaTryReadTest {
	private Linda linda;
	private Tuple motif;
	private Tuple tuple;
	private Tuple tupleVide;
	
	
	@BeforeClass
	public void setUpBeforeClass() throws Exception {
		linda = new linda.shm.CentralizedLinda();	
        // Linda linda = new linda.server.LindaClient("//localhost:4000/aaa");
		
		motif = new Tuple(Character.class, String.class, Integer.class);
		tuple = new Tuple('a', "toto", 4);
		tupleVide = new Tuple();
	}

	@After
	public void cleanUp() {
		linda.takeAll(motif);
		linda.takeAll(tupleVide);
	}
	
	@Test
	public void testTryReadTuple() {
		linda.write(tuple);
		assertTrue(linda.tryRead(tuple).equals(tuple));
	}

	@Test
	public void testTryReadMotif1() {
		linda.write(tuple);
		assertTrue(linda.tryRead(motif).equals(tuple));
	}
	
	@Test
	public void testTryReadMotif2() {
		linda.write(motif);
		assertTrue(linda.tryRead(motif).equals(motif));
	}
	
	@Test
	public void testTryReadVide() {
		linda.write(tupleVide);
		assertTrue(linda.tryRead(tupleVide).equals(tupleVide));
	}
	
	@Test
	public void testTryReadDontRemove() {
		linda.write(tuple);
		linda.tryRead(tuple);
		assertNotNull(linda.tryRead(tuple));
	}
	
	@Test
	public void testTryReadReturnNull() {
		assertNull(linda.tryRead(motif));
	}
	
	@Test
	public void testTryReadDontBlock() throws InterruptedException {
        Thread th = 
    	        new Thread() {
    	            public void run() {
    	                try {
    	                    linda.tryRead(new Tuple(motif));
    	                } catch (Exception e) {
    	                    ;
    	                }
    	            }
    	        };
            th.start();
            Thread.sleep(2);
            assertTrue(th.getState() == Thread.State.TERMINATED);
	}
	
	// TODO : verifier que l'exception renvoyée est bien censé être NullPointerException
	@Test(expected=NullPointerException.class)
	public void testTryReadNullThrowsException() {
		linda.tryRead(null);
	}

}
