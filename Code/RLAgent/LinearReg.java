package RLAgent;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

public class LinearReg {
	double [][] weights;
	double [][] elig;
	int output_size;
	int input_size;
	String filename_out;
	int num_rounds = 0;
	
	public LinearReg(int in_size, int out_size, String filename){
		output_size = out_size;
		input_size = in_size;
		filename_out = filename;
		init_weights();
	}
	
	public LinearReg(int in_size, int out_size, String filename, String filename_in){
		output_size = out_size;
		input_size = in_size;
		filename_out = filename;
		get_weights(filename_in);
	}
	
	public double[] matrix_mult(double[] A, double[][] B){
		double[] o = new double[B[0].length];
		for (int i=0; i<(B.length); i++){
			for (int j=0; j<(B[0].length); j++){
				o[j] += weights[i][j] * A[i];
			}
		}
		return o;
	}
	
	public void init_weights(){
		weights = new double[input_size][output_size];
		for (int i=0; i<input_size; i++){
			for (int j=0; j<output_size; j++){
				//weights[i][j] = 0.0;
				weights[i][j] = randInt (-10,10);
			}
		}
		reset_traces();
	}
	
	public double[] get_output(double[] inputs){
		double[] outputs = matrix_mult(inputs, weights);
		return outputs;
	}
	
	public void add_elig(double[] inputs, int action_num){
		for (int j=0; j<weights.length; j++){
			elig[j][action_num] = inputs[j];
		}
	}
	
	public void print_w(){
		for (int i=0; i<weights.length; i++){
			for (int j=0; j<weights[0].length; j++){
				System.out.print(weights[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public void get_weights(String filename){
		try {
			get_weights1(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void get_weights1(String filename) throws FileNotFoundException, IOException, ClassNotFoundException{
		ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(filename));
		double[][] temp_weights = (double[][])inputStream.readObject();
		System.out.println(temp_weights.length+" "+temp_weights[0].length+" "+input_size+" "+output_size);
		if ((input_size == temp_weights.length) && (output_size == temp_weights[0].length)){
			weights = new double[input_size][output_size];
			weights = temp_weights.clone();
			reset_traces();
			System.out.println("Loaded weights!");
		}else{
			System.out.println("Weight dimensions didn't match. Initialized from scratch.");
			init_weights();
		}
		inputStream.close();
	}
	
	public void save_weights1(String filename) throws FileNotFoundException, IOException, ClassNotFoundException{
		ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(filename));
		outputStream.writeObject(weights);
		outputStream.close();
		//System.out.println("Saved weights in file: "+filename);
	}
	
	public void save_weights(String filename){
	try {
		save_weights1(filename);
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	}
	
	public void reset_traces(){
		elig = new double[input_size][output_size];
		if (num_rounds > 0) save_weights(filename_out);
		num_rounds ++;
		for (int i=0; i<input_size; i++){
			for (int j=0; j<output_size; j++){
				elig[i][j] = 0.0;
			}
		}
	}
	
	public void train(int action, double alpha, double error, double lambda, double dec){
		for (int i=0; i<weights.length; i++){
			for (int j=0; j<weights[0].length; j++){
				weights[i][j] += elig[i][j]*alpha*error;
				//System.out.printf("%.2f ", elig[i][j]*alpha*error);
				elig[i][j] *= lambda*dec;
				if (elig[i][j] < 0.0001) elig[i][j] = 0;
				if (elig[i][j] > 1) elig[i][j] = 1;
			}
		}
		//System.out.println();
		//print_w();
	}

	public static int randInt(int min, int max) {

	    // NOTE: Usually this should be a field rather than a method
	    // variable so that it is not re-seeded every call.
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
}