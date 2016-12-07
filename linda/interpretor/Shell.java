package linda.interpretor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import linda.Linda;

public class Shell {

	InputStream in;
	Map<String, Process> processes = new HashMap<>();
	
	public Shell(InputStream in) {
		this.in = in;
	}
	
	public void run() {
		boolean exit = false;

		Scanner console = new Scanner(in);
		final Linda linda = new linda.shm.CentralizedLinda();

		// <processID> <action> [options...]
		// exit ou create <processID> are special actions
		while (!exit) {
			System.out.print("linda> ");
			String commandLine = console.nextLine();

			String[] tokens = commandLine.split("\\s");
			if (tokens == null || tokens.length == 0) {
				continue;
			}

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
					System.err.println(e.getMessage());
				}
			}
		}

		console.close();
	}
	
	private void performAction(String[] action) throws ActionInvalidException {
		Process process = processes.get(action[0]);
		String[] actionProcess = {action[1], ""};
		for (int iChaine = 2; iChaine < action.length; iChaine++) {
			actionProcess[1] += action[iChaine] + (iChaine != action.length ? " " : "");
		}
		process.setAction(actionProcess);
	}
	
}
