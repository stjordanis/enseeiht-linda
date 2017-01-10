package shm;

import linda.AsynchronousCallback;
import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** Shared memory implementation of Linda. */
public class DecentralizedLinda implements Linda {

	public static final int DEFAULT_NSPACES = 8;
	private final int QUEUE_SIZE = 10;

	private List<Linda> lindas = new LinkedList<>();
	private Lock monitor = new ReentrantLock();
	private Map<Tuple, BlockingQueue<Condition>> waitingReads  = new HashMap<>();
	private Map<Tuple, BlockingQueue<Condition>> waitingTakes  = new HashMap<>();

	public DecentralizedLinda() {
		this(DEFAULT_NSPACES);
	}

	public DecentralizedLinda(int nSpaces) {
		for (int iLinda = 0; iLinda < nSpaces; iLinda++) {
			lindas.add(new CentralizedLinda());
		}
	}

	/**
	 * Adds a tuple t to the tuplespace.
	 *
	 * @param t
	 */
	@Override
	public void write(Tuple t) {
		monitor.lock();
		selectRandomLinda().write(t);
		notifyNewTuple(t);
		monitor.unlock();
	}

	/**
	 * Returns a tuple matching the template and removes it from the tuplespace.
	 * Blocks if no corresponding tuple is found.
	 *
	 * @param template
	 */
	@Override
	public Tuple take(Tuple template) {
		Tuple res = null;
		Condition c;
		monitor.lock();
		while((res = tryTake(template)) == null){
			c = addWaitingTake(template);
			try {
				c.await();
			}catch(InterruptedException e){}
		}
		monitor.unlock();
		return res;
	}

	private static Tuple tuple = null;
	/**
	 * Returns a tuple matching the template and leaves it in the tuplespace.
	 * Blocks if no corresponding tuple is found.
	 *
	 * @param template
	 */
	@Override
	public Tuple read(Tuple template) {
		Tuple res = null;
		Condition c;
		monitor.lock();
		while((res = tryRead(template)) == null){
			c = addWaitingRead(template);
			try {
				c.await();
			}catch(InterruptedException e){}
		}
		monitor.unlock();
		return res;
	}

	/**
	 * Returns a tuple matching the template and removes it from the tuplespace.
	 * Returns null if none found.
	 *
	 * @param template
	 */
	@Override
	public Tuple tryTake(Tuple template) {
		Tuple tuple = null;
		monitor.lock();
		for (Linda linda : lindas) {
			tuple = linda.tryTake(template);
			if (tuple != null) {
				break;
			}
		}
		monitor.unlock();
		return tuple;
	}

	/**
	 * Returns a tuple matching the template and leaves it in the tuplespace.
	 * Returns null if none found.
	 *
	 * @param template
	 */
	@Override
	public Tuple tryRead(Tuple template) {
		Tuple tuple = null;
		monitor.lock();
		for (Linda linda : lindas) {
			tuple = linda.tryRead(template);
			if (tuple != null) {
				break;
			}
		}
		monitor.unlock();
		return tuple;
	}

	/**
	 * Returns all the tuples matching the template and removes them from the tuplespace.
	 * Returns an empty collection if none found (never blocks).
	 * Note: there is no atomicity or consistency constraints between takeAll and other methods;
	 * for instance two concurrent takeAll with similar templates may split the tuples between the two results.
	 *
	 * @param template
	 */
	@Override
	public Collection<Tuple> takeAll(Tuple template) {
		monitor.lock();
		Collection<Tuple> tuples = new ArrayList<>();
		for (Linda linda : lindas) {
			tuples.addAll(linda.takeAll(template));
		}
		monitor.unlock();
		return tuples;
	}

	/**
	 * Returns all the tuples matching the template and leaves them in the tuplespace.
	 * Returns an empty collection if none found (never blocks).
	 * Note: there is no atomicity or consistency constraints between readAll and other methods;
	 * for instance (write([1]);write([2])) || readAll([?Integer]) may return only [2].
	 *
	 * @param template
	 */
	@Override
	public Collection<Tuple> readAll(Tuple template) {
		monitor.lock();
		Collection<Tuple> tuples = new ArrayList<>();
		for (Linda linda : lindas) {
			tuples.addAll(linda.readAll(template));
		}
		monitor.unlock();
		return tuples;
	}

	/**
	 * Registers a callback which will be called when a tuple matching the template appears.
	 * If the mode is Take, the found tuple is removed from the tuplespace.
	 * The callback is fired once. It may re-register itself if necessary.
	 * If timing is immediate, the callback may immediately fire if a matching tuple is already present; if timing is future, current tuples are ignored.
	 * Beware: a callback should never block as the calling context may be the one of the writer (see also {@link AsynchronousCallback} class).
	 * Callbacks are not ordered: if more than one may be fired, the chosen one is arbitrary.
	 * Beware of loop with a READ/IMMEDIATE re-registering callback !
	 *
	 * @param mode     read or take mode.
	 * @param timing   (potentially) immediate or only future firing.
	 * @param template the filtering template.
	 * @param callback the callback to call if a matching tuple appears.
	 */
	@Override
	public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {

	}

	/**
	 * To debug, prints any information it wants (e.g. the tuples in tuplespace or the registered callbacks), prefixed by <code>prefix</code.
	 *
	 * @param prefix
	 */
	@Override
	public void debug(String prefix) {
		monitor.lock();
		for (Linda linda : lindas) {
			linda.debug(prefix);
		}
		monitor.unlock();
	}

	private Linda selectRandomLinda() {
		int randomNum = ThreadLocalRandom.current().nextInt(0, lindas.size());
		return lindas.get(randomNum);
	}

	private void notifyNewTuple(Tuple t){
		boolean notified = false;
		Condition c;
		Callback cb;
		Iterator<Tuple> it;
		Tuple templateTake;
    	/* Réveil des reads en attente */
		for(Tuple template : waitingReads.keySet()){
			if(t.matches(template)){
				while((c = waitingReads.get(template).poll()) != null){
					c.signal();
				}
			}
		}
    	/* Réveil d'un take en attente (priorité au takes direct) */
		it = waitingTakes.keySet().iterator();
		while(!notified && it.hasNext()){
			templateTake = it.next();
			if(t.matches(templateTake)){
				Condition lock = waitingTakes.get(templateTake).poll();
				if (lock != null) {
					lock.signal();
				}
				notified = true;
			}
		}
	}

	private Condition addWaitingRead(Tuple template){
		Condition c = monitor.newCondition();
		waitingReads.putIfAbsent(template, new ArrayBlockingQueue<>(QUEUE_SIZE, true));
		waitingReads.get(template).add(c);
		return c;
	}

	private Condition addWaitingTake(Tuple template){
		Condition c = monitor.newCondition();
		waitingTakes.putIfAbsent(template, new ArrayBlockingQueue<>(QUEUE_SIZE, true));
		waitingTakes.get(template).add(c);
		return c;
	}

}
