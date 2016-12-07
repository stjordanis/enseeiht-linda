package linda.shm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {
	
	private List<Tuple> tuples;
	private Lock moniteur;
	private Map<Tuple, Condition> attente;

	public CentralizedLinda(){
		tuples = new ArrayList<Tuple>();
		attente = new HashMap<Tuple, Condition>();
		moniteur = new ReentrantLock();
	}

    /** Adds a tuple t to the tuplespace. */
    public void write(Tuple t){
    	moniteur.lock();
		tuples.add(t);
		moniteur.unlock();
		signalNewTuple(t);
	}

    /** Returns a tuple matching the template and removes it from the tuplespace.
     * Blocks if no corresponding tuple is found. */
    public Tuple take(Tuple template){
		moniteur.lock();
		Tuple res = read(template);
		tuples.remove(res);
		moniteur.unlock();
		return res;
	}

    /** Returns a tuple matching the template and leaves it in the tuplespace.
     * Blocks if no corresponding tuple is found. */
    public Tuple read(Tuple template){
		Tuple res = null;
		moniteur.lock();
		res = tryRead(template);
		while(res == null){
			res = waitFor(template);
		}
		moniteur.unlock();
		return res;
	}

    /** Returns a tuple matching the template and removes it from the tuplespace.
     * Returns null if none found. */
    public Tuple tryTake(Tuple template){
    	moniteur.lock();
		Tuple res = tryRead(template);
		if(res != null){
			tuples.remove(res);
		}
		moniteur.unlock();
		return res;
	}

    /** Returns a tuple matching the template and leaves it in the tuplespace.
     * Returns null if none found. */
    public Tuple tryRead(Tuple template){
		Tuple res = null;
		moniteur.lock();
		for(Tuple t : tuples){
			if(t.matches(template)){
				res = t;
			}
		}
		moniteur.unlock();
		return res;
	}

    /** Returns all the tuples matching the template and removes them from the tuplespace.
     * Returns an empty collection if none found (never blocks).
     * Note: there is no atomicity or consistency constraints between takeAll and other methods;
     * for instance two concurrent takeAll with similar templates may split the tuples between the two results.
     */
    public Collection<Tuple> takeAll(Tuple template){
		Collection<Tuple> res = new ArrayList<Tuple>();
		Tuple t = tryTake(template);
		while(t != null){
			res.add(t);
			t = tryTake(template);
		}
		return res;
	}

    /** Returns all the tuples matching the template and leaves them in the tuplespace.
     * Returns an empty collection if none found (never blocks).
     * Note: there is no atomicity or consistency constraints between readAll and other methods;
     * for instance (write([1]);write([2])) || readAll([?Integer]) may return only [2].
     */
    public Collection<Tuple> readAll(Tuple template){
		Collection<Tuple> res = new ArrayList<Tuple>();
		Tuple t = tryRead(template);
		while(t != null){
			res.add(t);
			t = tryRead(template);
		}
		return res;
	}

    /** Registers a callback which will be called when a tuple matching the template appears.
     * If the mode is Take, the found tuple is removed from the tuplespace.
     * The callback is fired once. It may re-register itself if necessary.
     * If timing is immediate, the callback may immediately fire if a matching tuple is already present; if timing is future, current tuples are ignored.
     * Beware: a callback should never block as the calling context may be the one of the writer (see also {@link AsynchronousCallback} class).
     * Callbacks are not ordered: if more than one may be fired, the chosen one is arbitrary.
     * Beware of loop with a READ/IMMEDIATE re-registering callback !
     *
     * @param mode read or take mode.
     * @param timing (potentially) immediate or only future firing.
     * @param template the filtering template.
     * @param callback the callback to call if a matching tuple appears.
     */
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback){
    	new Thread() {
            public void run() {
            	Tuple t = null;
            	if(mode == eventMode.READ && timing == eventTiming.IMMEDIATE){
            		t = read(template);
            	}else if(mode == eventMode.READ && timing == eventTiming.FUTURE){
            		t = waitFor(template);
            	}else if(mode == eventMode.TAKE && timing == eventTiming.IMMEDIATE){
            		t = take(template);
            	}else if(mode == eventMode.TAKE && timing == eventTiming.FUTURE){
            		moniteur.lock();
            		t = waitFor(template);
            		tuples.remove(t);
            		moniteur.unlock();
            	}
                callback.call(t);
            }
        }.start();
    	
	}

    /** To debug, prints any information it wants (e.g. the tuples in tuplespace or the registered callbacks), prefixed by <code>prefix</code. */
    public void debug(String prefix){
		for(Tuple t : tuples){
			System.out.println(prefix + t.toString());
		}
	}
    
    private void signalNewTuple(Tuple t){
    	Condition res = null;
    	moniteur.lock();
		for(Tuple template : attente.keySet()){
			if(t.matches(template)){
				res = attente.get(template);
			}
		}
		if(res != null){
			res.signal();
		}
    	moniteur.unlock();
    }
    
    private Tuple waitFor(Tuple template){
    	moniteur.lock();
    	if(attente.get(template) == null){
    		attente.put(template, moniteur.newCondition());
    	}
		try{
			attente.get(template).await();
		}catch(Exception e){}
		//attente.remove(template);
		signalNewTuple(template);
    	moniteur.unlock();
    	return null;
    }

    private class WaitingList {
    	
    	private Map<Tuple, Condition> cond;
    	private Map<Tuple, Integer> nbAttente;
    	
    	public WaitingList(){
    		cond = new HashMap<Tuple, Condition>();
    		nbAttente = new HashMap<Tuple, Integer>();
    	}
    	
    	public void add(Tuple t){
    		if(nbAttente.get(t) == null){
    			cond.put(t, moniteur.newCondition());
    			nbAttente.put(t, 1);
    		}else{
    			nbAttente.put(t, nbAttente.get(t) + 1);
    		}
    	}
    	
    	public void remove(Tuple t){
    		if(nbAttente.get(t) > 1){
    			nbAttente.put(t, nbAttente.get(t) - 1);
    		}else{
    			cond.remove(t);
    			nbAttente.remove(t);
    		}
    	}
    }
}
