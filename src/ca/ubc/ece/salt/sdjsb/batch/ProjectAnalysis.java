package ca.ubc.ece.salt.sdjsb.batch;

import java.io.IOException;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import ca.ubc.ece.salt.sdjsb.checker.Alert;

public class ProjectAnalysis {
	
	public static final String CHECKOUT_DIR =  new String("repositories");

	/**
	 * Inspects bug fixing commits to classify repairs.
	 * @param args Usage: ProjectAnalysis /path/to/project/.git
	 * @throws IOException
	 * @throws GitAPIException
	 */
	public static void main(String[] args) throws Exception {

		ProjectAnalysisOptions options = new ProjectAnalysisOptions();
		CmdLineParser parser = new CmdLineParser(options);

		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			ProjectAnalysis.printUsage(e.getMessage(), parser);
			return;
		}
		
		/* Print the help page. */
		if(options.getHelp()) {
			ProjectAnalysis.printHelp(parser);
			return;
		}
		
        GitProjectAnalysis gitProjectAnalysis;

		/* A project folder was given. */
		if(options.getGitDirectory() != null) {

			try {
                gitProjectAnalysis = GitProjectAnalysis.fromDirectory(options.getGitDirectory());
			} 
			catch(GitProjectAnalysisException e) {
                ProjectAnalysis.printUsage(e.getMessage(), parser);
                return;
			}

		}
		/* A URI was given. */
		else if(options.getURI() != null) {

			try {
                gitProjectAnalysis = GitProjectAnalysis.fromURI(options.getURI(), ProjectAnalysis.CHECKOUT_DIR);
			} 
			catch(GitProjectAnalysisException e) {
                ProjectAnalysis.printUsage(e.getMessage(), parser);
                return;
			}

		}
		else {
			System.out.println("No repository given.");
			ProjectAnalysis.printUsage("No repository given.", parser);
			return;
		}

		/* Perform the analysis (may take some time). */
		gitProjectAnalysis.analyze();
		
		List<Alert> alerts = gitProjectAnalysis.getAlerts();
		
		/* Print the results. */
		System.out.print("There were " + gitProjectAnalysis.getBugFixingCommits() + " bug fixing commits analyzed out of " + gitProjectAnalysis.getTotalCommits() + " total commits.");
		System.out.println("Alerts (" + alerts.size() + "):");
		for(Alert alert : alerts) {
			System.out.println("\t" + alert.getShortDescription());
		}
		
	}

	/**
	 * Prints the help file for main.
	 * @param parser The args4j parser.
	 */
	private static void printHelp(CmdLineParser parser) {
        System.out.print("Usage: ProjectAnalysis ");
        parser.setUsageWidth(Integer.MAX_VALUE);
        parser.printSingleLineUsage(System.out);
        System.out.println("\n");
        parser.printUsage(System.out);
        System.out.println("");
        return;
	}
	
	/**
	 * Prints the usage of main.
	 * @param error The error message that triggered the usage message.
	 * @param parser The args4j parser.
	 */
	private static void printUsage(String error, CmdLineParser parser) {
        System.out.println(error);
        System.out.print("Usage: ProjectAnalysis ");
        parser.setUsageWidth(Integer.MAX_VALUE);
        parser.printSingleLineUsage(System.out);
        System.out.println("");
        return;
	}
	
}