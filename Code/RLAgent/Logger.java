package RLAgent;

import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;

public class Logger {
	
	int currentTrainIter;
	// Arrays storing the results of many games (for each X test iteration).
	ArrayList<Double> monsterKills, winLose, cellsPassed;
	
	public Logger(int currentTrainIter){
		this.currentTrainIter = currentTrainIter;
		
		this.monsterKills = new ArrayList<Double>();
		this.winLose = new ArrayList<Double>();
		this.cellsPassed = new ArrayList<Double>();
	}
	
	// Add the results of one game to the arrays.
	public void log(Environment environment){
		
		// Total number of monsters killed.
		this.monsterKills.add((double)environment.getEvaluationInfo().killsTotal);
		// Label wins as 1.
		this.winLose.add(environment.getEvaluationInfo().marioStatus == Mario.STATUS_WIN ? 1.0 : 0.0); 
		// Fraction on cells passed.
		this.cellsPassed.add((double)environment.getEvaluationInfo().distancePassedCells / environment.getEvaluationInfo().levelLength);
	}
	
	// Write the means and stdev of each array to a csv file.
	public void writeToFile(){
		
		double averageMonster = this.getAverage(this.monsterKills);
		double averageWins = this.getAverage(this.winLose);
		double averageCells = this.getAverage(this.cellsPassed);
		
		double stdevMonster = this.getStdDev(averageMonster, this.monsterKills);
		double stdevWins = this.getStdDev(averageWins, this.winLose);
		double stdevCells = this.getStdDev(averageCells, this.cellsPassed);
		
		// Write the data to a csv file.
		// Format: currentTrainIter mean1 mean2 mean3 stdev1 stdev2 stdev3
		FileWriter writer;
		try {
			// True = append mode
			writer = new FileWriter("Results.csv", true);

		    writer.write(String.valueOf(this.currentTrainIter));
		    writer.append(',');
		    writer.write(String.valueOf(averageMonster));
		    writer.append(',');
		    writer.write(String.valueOf(averageWins));
		    writer.append(',');
		    writer.write(String.valueOf(averageCells));
		    writer.append(',');
		    writer.write(String.valueOf(stdevMonster));
		    writer.append(',');
		    writer.write(String.valueOf(stdevWins));
		    writer.append(',');	 
		    writer.write(String.valueOf(stdevCells));
		    writer.append('\n');	 	
		    
		    writer.flush();
		    writer.close();
		    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private double getAverage(ArrayList<Double> data)
    {
        double sum = 0.0;
        for(double a : data)
            sum += a;
        return sum/data.size();
    }
	
	private double getVariance(double mean, ArrayList<Double> data)
    {
        double temp = 0;
        for(double a : data)
            temp += (mean-a)*(mean-a);
        return temp/data.size();
    }

	private double getStdDev(double mean, ArrayList<Double> data)
    {
        return Math.sqrt(this.getVariance(mean, data));
    }
}
