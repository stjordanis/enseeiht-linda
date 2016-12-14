package linda.test;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.After;

import linda.*;

public class LindaTakeAllTest {
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
	public void testTakeTuple() {
		linda.write(tuple);
		assertTrue(linda.take(tuple).equals(tuple));
	}

	@Test
	public void testTakeMotif1() {
		linda.write(tuple);
		assertTrue(linda.take(motif).equals(tuple));
	}
	
	@Test
	public void testTakeMotif2() {
		linda.write(motif);
		assertTrue(linda.take(motif).equals(motif));
	}
	
	@Test
	public void testTakeVide() {
		linda.write(tupleVide);
		assertTrue(linda.take(tupleVide).equals(tupleVide));
	}
	
	@Test
	public void testTakeRemove() {
		linda.write(tuple);
		linda.take(tuple);
		assertNull(linda.tryRead(tuple));
	}
	
	@Test
	public void testTakeBlock() throws InterruptedException {
        Thread th = 
    	        new Thread() {
    	            public void run() {
    	                try {
    	                    linda.take(new Tuple(motif));
    	                } catch (Exception e) {
    	                    ;
    	                }
    	            }
    	        };
            th.start();
            Thread.sleep(2);
            assertTrue(th.getState() == Thread.State.BLOCKED);
	}

	@Test
	public void testTakeUnblock() throws InterruptedException {
        Thread th = 
    	        new Thread() {
    	            public void run() {
    	                try {
    	                    assertTrue(linda.take(new Tuple(motif)).equals(tuple));
    	                } catch (Exception e) {
    	                    ;
    	                }
    	            }
    	        };
            th.start();
            Thread.sleep(2);
            linda.write(tuple);
            Thread.sleep(2);
            assertTrue(th.getState() == Thread.State.TERMINATED);
	}
	
	// TODO : verifier que l'exception renvoyée est bien censé être NullPointerException
	@Test(expected=NullPointerException.class)
	public void testTakeNullThrowsException() {
		linda.take(null);
	}
	
	@Test
	public void testTakeNullDontBlock() throws InterruptedException {
        Thread th = 
	        new Thread() {
	            public void run() {
	                try {
	                    linda.take(null);
	                } catch (Exception e) {
	                    ;
	                }
	            }
	        };
        th.start();
        Thread.sleep(2);
        assertTrue(th.getState() == Thread.State.TERMINATED);
	}
}
