package linda;

import java.util.Collection;

/** Public interface to a Linda implementation.
 * @author philippe.queinnec@enseeiht.fr
 */
public class LindaMemoirePartagee implements Linda {

	private List<Tuple> tuples;
	private Lock moniteur;
	private Map<Tuple, Condition> attente;

	public LindaMemoirePartagee(){
		tuples = new ArrayList<Tuple>();
		attente = new HashMap<Tuple, Condition>();

		moniteur = new ReentrantLock();
	}

    /** Adds a tuple t to the tuplespace. */
    public void write(Tuple t){
		tuples.add(t);
		//moniteur.lock();
		cond.signal();
		//moniteur.unlock();
	}

    /** Returns a tuple matching the template and removes it from the tuplespace.
     * Blocks if no corresponding tuple is found. */
    public Tuple take(Tuple template){
		Tuple res = read(template);
		moniteur.lock();
		tuples.remove(res);
		moniteur.unlock();
		return res;
	}

    /** Returns a tuple matching the template and leaves it in the tuplespace.
     * Blocks if no corresponding tuple is found. */
    public Tuple read(Tuple template){
		moniteur.lock();
		if(tryRead(template) == null){
			attente.add(template, moniteur.newCondition());
			attente.get(template).await();
		}
		while(tryRead(template) == null){
			attente.get(template).await();
		}
		attente.remove(template);
		cond.signal();
		moniteur.unlock();
	}

    /** Returns a tuple matching the template and removes it from the tuplespace.
     * Returns null if none found. */
    public Tuple tryTake(Tuple template){
		Tuple res = tryRead(template);
		if(res != null){
			moniteur.lock();
			tuples.remove(res);
			moniteur.unlock();
		}
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

    public enum eventMode { READ, TAKE };
    public enum eventTiming { IMMEDIATE, FUTURE };

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

	}

    /** To debug, prints any information it wants (e.g. the tuples in tuplespace or the registered callbacks), prefixed by <code>prefix</code. */
    public void debug(String prefix){
		for(Tuple t : tuples){
			System.out.println(t.toString());
		}
	}

}
