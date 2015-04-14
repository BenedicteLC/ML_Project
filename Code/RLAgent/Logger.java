package RLAgent;

import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

import java.util.ArrayList;

public class Logger {
	
	int currentTrainIter;
	ArrayList<Integer> monsterKills, winLose, cellsPassed;
	
	public Logger(int currentTrainIter){
		this.currentTrainIter = currentTrainIter;
		
		this.monsterKills = new ArrayList<Integer>();
		this.winLose = new ArrayList<Integer>();
		this.cellsPassed = new ArrayList<Integer>();
	}
	
	public void log(Environment environment){
		
		// Total number of monsters killed.
		this.monsterKills.add(environment.getEvaluationInfo().killsTotal);
		// Will need to check if the entries are "marioStatus == Mario.STATUS_WIN" 
		this.winLose.add(environment.getEvaluationInfo().marioStatus); 
		// Fraction on cells passed.
		this.cellsPassed.add(environment.getEvaluationInfo().distancePassedCells / environment.getEvaluationInfo().levelLength);
	}
	
	public void writeToFile(){
		// TODO: Create file and APPEND.
		// Write currentTrainIter
		// For each array, write the mean and the stdev to a file.
		// Desired format: currentTrainIter mean1 mean2 mean3 stdev1 stdev2 stdev3
	}
}
