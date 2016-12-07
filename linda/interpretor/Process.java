package linda.interpretor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import linda.Callback;
import linda.Linda;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.Tuple;

public class Process extends Thread implements Callback {

	private String[] action;
	private String name;
	private Linda linda;

	public Process(String name, Linda linda) {
		super();
		this.name = name;
		this.linda = linda;
	}

	private volatile Boolean running;

    public void run() {
        running = true;
        while(running) {
            running = executeAction();
            if (Thread.interrupted()) {
                return;
            }
        }
    }

    public boolean executeAction() {
    	if (action == null)
    		return true;
    	
    	List<Tuple> tuples = new ArrayList<>();
		Tuple tuple = Tuple.valueOf(action[1]);
    	switch (action[0]) {
    	case "write":
    		linda.write(tuple);
    		System.out.println(name + ": Ecriture de " + tuple + "effectuee");
    		break;
    	case "take":
    		tuples.add(linda.take(tuple));
    		break;
    	case "read":
    		tuples.add(linda.read(tuple));
    		break;
    	case "tryTake":
    		tuples.add(linda.tryTake(tuple));
    		break;
    	case "tryRead":
    		tuples.add(linda.tryRead(tuple));
    		break;
    	case "takeAll":
    		tuples.addAll(linda.takeAll(tuple));
    		break;
    	case "readAll":
    		tuples.addAll(linda.readAll(tuple));
    		break;
    	case "eventRegister":
    		String[] tokens = action[1].split("\\s");
    		eventMode mode = tokens[0].toLowerCase() == "read" ? eventMode.READ : eventMode.TAKE;
    		eventTiming timing = tokens[1].toLowerCase() == "future" ? eventTiming.FUTURE : eventTiming.IMMEDIATE;
    		String tupleString = String.join(" ", Arrays.copyOfRange(tokens, 2, tokens.length-1));
    		tuple = Tuple.valueOf(String.join(" ", tupleString));
    		linda.eventRegister(mode, timing, tuple, this);
    		System.out.println(name + ": Demande de " + tuple + "effectuee");
    		break;
    	case "terminate":
    		System.out.println("Processus termine");
    		return false;
    	};
    	
    	for (Tuple curTuple : tuples) {
    		System.out.println(name + ": " + curTuple);
    	}
    	action = null;
    	return true;
    	
    }
    
    public void wakeUp() {
    	synchronized (running) {
    		running.notify();
    	}
    }
    
    public void setAction(String[] action) {
    	this.action = action;
    }

	@Override
	public void call(Tuple tuple) {
		System.out.println("<callback> " + name + ": " + tuple);
	}
	
}
