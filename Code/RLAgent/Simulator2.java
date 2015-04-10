package RLAgent;

import java.io.IOException;
import java.util.Random;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.*;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.mario.environments.MarioEnvironment;
import ch.idsia.benchmark.tasks.RLBasicTask;
//import ch.idsia.benchmark.tasks.RLBasicTask;
import ch.idsia.benchmark.tasks.LearningTask;
import ch.idsia.tools.MarioAIOptions;
import competition.gic2010.learning.sergeykarakovskiy.*;

public class Simulator2 {
	
	public static void main(String[] args)
	{		
		final int ITERATIONS = 15000;
		Random rand = new Random();
		
		//final String argsString = "-vis off"; // -vis: show display
	    final MarioAIOptions marioAIOptions = new MarioAIOptions(args);
	    final RLAgent2 agent = new RLAgent2();
	    final RLBasicTask basicTask = new RLBasicTask(marioAIOptions);
	    
	    int seed = 0;
	    int difficulty;
	    
	    for (int i = 0; i < ITERATIONS; ++i)
	    {	
	    	difficulty = rand.nextInt(2);        	        
	        
            marioAIOptions.setLevelDifficulty(difficulty);
            marioAIOptions.setLevelRandSeed(seed++);
            marioAIOptions.setAgent(agent);
            basicTask.setOptionsAndReset(marioAIOptions);
            
            // Run the stage. This is where the agent gets the state
            // and performs an action.
            basicTask.runSingleEpisode(1);
            Environment environment = basicTask.getEnvironment();
            
            // Print every X iteration.
            System.out.println("Iteration: " + i + " difficulty: " + difficulty);
            System.out.println(basicTask.getEnvironment().getEvaluationInfoAsString());	
           
	    }
	    
	    Runtime rt = Runtime.getRuntime();
	    try
	    {
	    	//Process proc = rt.exec("/usr/local/bin/mate " + marioTraceFileName);
	        Process proc = rt.exec("python hello.py");
	    } catch (IOException e)
	    {
	        e.printStackTrace();
	    }
	    System.exit(0);

	}
}
