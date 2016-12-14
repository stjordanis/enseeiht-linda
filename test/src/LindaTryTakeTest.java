package linda.test;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.After;

import linda.*;

public class LindaTryTakeTest {
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
	public void testTryTakeTuple() {
		linda.write(tuple);
		assertTrue(linda.tryTake(tuple).equals(tuple));
	}

	@Test
	public void testTryTakeMotif1() {
		linda.write(tuple);
		assertTrue(linda.tryTake(motif).equals(tuple));
	}
	
	@Test
	public void testTryTakeMotif2() {
		linda.write(motif);
		assertTrue(linda.tryTake(motif).equals(motif));
	}
	
	@Test
	public void testTryTakeVide() {
		linda.write(tupleVide);
		assertTrue(linda.tryTake(tupleVide).equals(tupleVide));
	}
	
	@Test
	public void testTryTakeRemove() {
		linda.write(tuple);
		linda.tryTake(tuple);
		assertNull(linda.tryRead(tuple));
	}
	
	@Test
	public void testTryTakeReturnNull() {
		assertNull(linda.tryTake(motif));
	}
	
	@Test
	public void testTryTakeDontBlock() throws InterruptedException {
        Thread th = 
    	        new Thread() {
    	            public void run() {
    	                try {
    	                    linda.tryTake(new Tuple(motif));
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
	public void testTryTakeNullThrowsException() {
		linda.tryTake(null);
	}

}
