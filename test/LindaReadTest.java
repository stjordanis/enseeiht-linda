package linda.test;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.After;

import linda.*;

public class LindaReadTest {
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
	public void testReadTuple() {
		linda.write(tuple);
		assertTrue(linda.read(tuple).equals(tuple));
	}

	@Test
	public void testReadMotif1() {
		linda.write(tuple);
		assertTrue(linda.read(motif).equals(tuple));
	}
	
	@Test
	public void testReadMotif2() {
		linda.write(motif);
		assertTrue(linda.read(motif).equals(motif));
	}
	
	@Test
	public void testReadVide() {
		linda.write(tupleVide);
		assertTrue(linda.read(tupleVide).equals(tupleVide));
	}
	
	@Test
	public void testReadDontRemove() {
		linda.write(tuple);
		linda.read(tuple);
		assertNotNull(linda.tryRead(tuple));
	}
	
	@Test
	public void testReadBlock() throws InterruptedException {
        Thread th = 
    	        new Thread() {
    	            public void run() {
    	                try {
    	                    linda.read(new Tuple(motif));
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
	public void testReadUnblock() throws InterruptedException {
        Thread th = 
    	        new Thread() {
    	            public void run() {
    	                try {
    	                    assertTrue(linda.read(new Tuple(motif)).equals(tuple));
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
	public void testReadNullThrowsException() {
		linda.read(null);
	}
	
	@Test
	public void testReadNullDontBlock() throws InterruptedException {
        Thread th = 
	        new Thread() {
	            public void run() {
	                try {
	                    linda.read(null);
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
