package uniol.apt.analysis.product;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Iterator;

import org.testng.annotations.Test;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

/**
 * 
 * @author Jonas Prellberg
 *
 */
public class ProductTest {

	@Test
	public void testDeterministicSyncProduct() {
		TransitionSystem ts1 = new TransitionSystem();
		State s0 = ts1.createState();
		State s1 = ts1.createState();
		State s2 = ts1.createState();
		State s3 = ts1.createState();
		State s4 = ts1.createState();
		ts1.setInitialState(s0);
		ts1.createArc(s0, s1, "a");
		ts1.createArc(s0, s2, "b");
		ts1.createArc(s1, s4, "a");
		ts1.createArc(s2, s3, "a");
		
		TransitionSystem ts2 = new TransitionSystem();
		State p0 = ts2.createState();
		State p1 = ts2.createState();
		State p2 = ts2.createState();
		State p3 = ts2.createState();
		ts2.setInitialState(p0);
		ts2.createArc(p0, p1, "b");
		ts2.createArc(p0, p2, "c");
		ts2.createArc(p1, p3, "a");
		
		Product product = new Product(ts1, ts2);
		TransitionSystem result = product.getSyncProduct();
		
		// Result should look like this:
		// s0p0 --(b)--> s2p2 --(a)--> s3p3
		assertEquals(result.getNodes().size(), 3);
		assertEquals(result.getEdges().size(), 2);
		assertEquals(result.getInitialState().getPostsetEdges().size(), 1);
		Arc fstArc = result.getInitialState().getPostsetEdges().iterator().next();
		assertEquals(fstArc.getTarget().getPostsetEdges().size(), 1);
		Arc sndArc = fstArc.getTarget().getPostsetEdges().iterator().next();
		assertEquals(fstArc.getLabel(), "b");
		assertEquals(sndArc.getLabel(), "a");
	}
	
	@Test
	public void testNondeterministicSyncProduct() {
		TransitionSystem ts1 = new TransitionSystem();
		State s0 = ts1.createState();
		State s1 = ts1.createState();
		State s2 = ts1.createState();
		ts1.setInitialState(s0);
		ts1.createArc(s0, s1, "a");
		ts1.createArc(s0, s2, "a");
		
		TransitionSystem ts2 = new TransitionSystem();
		State p0 = ts2.createState();
		State p1 = ts2.createState();
		ts2.setInitialState(p0);
		ts2.createArc(p0, p1, "a");
		
		Product product = new Product(ts1, ts2);
		TransitionSystem result = product.getSyncProduct();
		
		// Result should look like this:
		// s0p0 --(a)--> s1p1
		//   \----(a)--> s2p1
		assertEquals(result.getNodes().size(), 3);
		assertEquals(result.getEdges().size(), 2);
		assertEquals(result.getInitialState().getPostsetEdges().size(), 2);
		Iterator<Arc> iter = result.getInitialState().getPostsetEdges().iterator();
		Arc fstArc = iter.next();
		Arc sndArc = iter.next();
		assertEquals(fstArc.getLabel(), "a");
		assertEquals(sndArc.getLabel(), "a");
	}
	
	@Test
	public void testEmptySyncProduct() {
		TransitionSystem ts1 = new TransitionSystem();
		State s0 = ts1.createState();
		State s1 = ts1.createState();
		ts1.setInitialState(s0);
		ts1.createArc(s0, s1, "a");
		
		TransitionSystem ts2 = new TransitionSystem();
		State p0 = ts2.createState();
		ts2.setInitialState(p0);
		
		Product product = new Product(ts1, ts2);
		TransitionSystem result = product.getSyncProduct();
		
		// Result should look like this:
		// s0p0
		assertEquals(result.getNodes().size(), 1);
		assertEquals(result.getEdges().size(), 0);
	}
	
}
