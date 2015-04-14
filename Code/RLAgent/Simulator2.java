package RLAgent;

import java.io.IOException;
import java.util.Random;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.*;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.mario.environments.MarioEnvironment;
import ch.idsia.benchmark.tasks.RLBasicTask;
//import ch.idsia.benchmark.tasks.RLBasicTask;
import ch.idsia.benchmark.tasks.LearningTask;
import ch.idsia.tools.MarioAIOptions;
import competition.gic2010.learning.sergeykarakovskiy.*;

public class Simulator2 {
	
	public static void main(String[] args) throws CloneNotSupportedException
	{		
		final int ITERATIONS = 1500;
		int winCount = 0;
		int gameCount = 0;
		Random rand = new Random();
		
		//final String argsString = "-vis off"; // -vis: show display
	    final MarioAIOptions marioAIOptions = new MarioAIOptions(args);
	    final RLAgent2 agent = new RLAgent2();
	    final RLBasicTask basicTask = new RLBasicTask(marioAIOptions);
	    final Logger logger = new Logger();
	    
	    int seed = 0;
	    int difficulty;
	    
	    for (int i = 0; i < ITERATIONS; ++i)
	    {	
	    	difficulty = rand.nextInt(1);        	        
	        
            marioAIOptions.setLevelDifficulty(difficulty);
            marioAIOptions.setLevelRandSeed(seed++);
            marioAIOptions.setAgent(agent);
            basicTask.setOptionsAndReset(marioAIOptions);
            
            // Run the stage. This is where the agent gets the state
            // and performs an action.
            basicTask.runSingleEpisode(1);
            Environment environment = basicTask.getEnvironment();
                        
            System.out.println("Iteration: " + i + " difficulty: " + difficulty);
            System.out.println(basicTask.getEnvironment().getEvaluationInfoAsString());
            
            // Log the current model results every 100 iterations.
            if(i % 100 == 0){            	
            	testModel(marioAIOptions, basicTask, agent, logger);   
            }
            
            
            // Count wins in the last 1000 iters.
            if(ITERATIONS - i <= 1000){ 
	            gameCount++;
	            if(basicTask.getEnvironment().getEvaluationInfo().marioStatus == Mario.STATUS_WIN)
	            	winCount++;
            }
	    }
	    System.out.println("\n" + winCount + "/" + gameCount + " wins");
	    
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
	
	static void testModel(MarioAIOptions marioAIOptions, RLBasicTask basicTask, RLAgent2 agent, Logger logger){
		int seed = 0;
	    int difficulty;
	    Random rand = new Random();
	    
	    // This is ugly, but creating a clone of agent was a mess.
	    double backupAlpha = agent.alpha;
	    double backupAlphaMin = agent.alpha_min;
	    double backupAlphaDecay = agent.alpha_decay;
	    double backupEpsilon = agent.epsilon;
	    double backupEpsilonMin = agent.epsilon_min;
	    double backupEpsilonDecay = agent.epsilon_decay;
	
	    // Prevent learning and pick greedy policy.
		agent.alpha = 0;
    	agent.alpha_min = 0;
    	agent.alpha_decay = 0;
    	agent.epsilon = 0;
    	agent.epsilon_min = 0;
    	agent.epsilon_decay = 0;
    	
    	for (int i = 0; i < 100; ++i)
 	    {	
 	    	 difficulty = rand.nextInt(1);        	        
 	        
             marioAIOptions.setLevelDifficulty(difficulty);
             marioAIOptions.setLevelRandSeed(seed++);
             marioAIOptions.setAgent(agent);
             basicTask.setOptionsAndReset(marioAIOptions);
             
             // Run the stage. This is where the agent gets the state
             // and performs an action.
             basicTask.runSingleEpisode(1);
             Environment environment = basicTask.getEnvironment();
             logger.log(environment);
 	    }
    	
    	// Restore the parameters.
    	agent.alpha = backupAlpha;
    	agent.alpha_min = backupAlphaMin;
    	agent.alpha_decay = backupAlphaDecay;
    	agent.epsilon = backupEpsilon;
    	agent.epsilon_min = backupEpsilonMin;
    	agent.epsilon_decay = backupEpsilonDecay;
	}
}
