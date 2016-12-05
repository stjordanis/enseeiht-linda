package linda.test;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.After;

import linda.*;

public class LindaWriteTest {
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
	public void testMultiEnsemble() {
		linda.write(tuple);
		linda.write(tuple);
		assertTrue(linda.takeAll(motif).size() == 2);
	}
	
	@Test
	public void testWriteTuple() {
		linda.write(tuple);
		assertTrue(linda.read(tuple).equals(tuple));
	}

	@Test
	public void testWriteMotif() {
		linda.write(motif);
		assertTrue(linda.read(motif).equals(motif));	}
	
	@Test
	public void testWriteVide() {
		linda.write(tupleVide);
		assertTrue(linda.read(tupleVide).equals(tupleVide));
	}
	
	@Test(expected=Exception.class)
	public void testWriteNullThrowsException() {
		linda.write(null);
	}
	
	@Ignore
	@Test
	public void testWriteNull() {
		try {
			linda.write(null);
		} catch (Exception e) {
			;
		}
		// TODO : Verifier que rien n'a été enregistré sur le serveur
		
	}
}
