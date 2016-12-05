package linda.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import linda.Linda;

public class LindaReadTest {

	@BeforeClass
	public void setUpBeforeClass() throws Exception {
		final Linda linda = new linda.shm.CentralizedLinda();	
        // final Linda linda = new linda.server.LindaClient("//localhost:4000/aaa");
	}
	
	@Before
	public void setUp() throws Exception {
		
	}

	@Test
	public void testCentralizedLinda() {
		fail("Not yet implemented");
	}

}

