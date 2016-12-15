package outils.interpretor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import linda.*;

public class Shell {

	InputStream in;
	Map<String, Process> processes = new HashMap<>();
	final private Linda linda = new linda.tshm.CentralizedLinda();
	
	public Shell(InputStream in) {
		this.in = in;
	}
	
	public void run() {
		boolean exit = false;

		Scanner console = new Scanner(in);

		// <processID> <action> [options...]
		// exit ou create <processID> are special actions
		while (!exit) {
			System.out.print("linda> ");
			String commandLine = console.nextLine();

			if (commandLine.isEmpty()) {
				continue;
			}
			
			String[] requetes = commandLine.split("\\|\\|");

			for (String requete : requetes) {
				String[] tokens = requete.trim().split("\\s");
				commandLine(tokens);
			}
		}

		console.close();
	}
	
	private void commandLine(String[] tokens) {
		switch (tokens[0]) {
		case "create":
			for (int iProcess = 1; iProcess < tokens.length; iProcess++) {
				Process process = new Process(tokens[iProcess], linda);
				processes.put(tokens[iProcess], process);
				process.start();
			}
			break;
		case "debug":
			linda.debug("<user>");
			break;
		case "exit":
			for (Thread t : processes.values()) {
				t.interrupt();
			}
			return;
		default:
			try {
				performAction(tokens);
			} catch (ActionInvalidException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	private void performAction(String[] action) throws ActionInvalidException {
		Process process = processes.get(action[0]);
		
		if (process == null) {
			System.out.println("Process \"" + action[0] + "\" does not exists");
			return;
		}
		
		String[] actionProcess = {action[1], ""};
		for (int iChaine = 2; iChaine < action.length; iChaine++) {
			actionProcess[1] += action[iChaine] + (iChaine != action.length ? " " : "");
		}
		process.setAction(actionProcess);
	}
	
}
