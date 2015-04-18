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
	double [] bias;
	double [][] weights;
	double [][] elig;
	int output_size;
	int input_size;
	String filename_out;
	int num_rounds = 0;
	Random rand = new Random();
	
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
		bias = new double[output_size];
		for (int i=0; i<input_size; i++){
			for (int j=0; j<output_size; j++){
				weights[i][j] = (rand.nextDouble()-0.5)*0.10;
			}
		}
		reset_traces();
	}
	
	public double[] get_output(double[] inputs, boolean t){
		double[] outputs = matrix_mult(inputs, weights);
		for (int i=0;i<output_size;i++) outputs[i] += bias[i];
		return outputs;
	}

	
	public void add_elig(double[] inputs, int action_num){
		for (int j=0; j<weights.length; j++){
			if (inputs[j] > 0)
				elig[j][action_num] = inputs[j]; // Replacing traces.
		}
	}

	
	public void get_weights(String filename){
		try {
			get_weights1(filename);
			get_biases1(filename);
		} catch (FileNotFoundException e) {
			init_weights();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void get_weights1(String filename) throws FileNotFoundException, IOException, ClassNotFoundException{
		ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(filename));
		bias = new double[output_size];
		double[][] temp_weights = (double[][])inputStream.readObject();
		//System.out.println(temp_weights.length+" "+temp_weights[0].length+" "+input_size+" "+output_size);
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
	
	public void get_biases1(String filename_before) throws FileNotFoundException, IOException, ClassNotFoundException{
		String filename = filename_before.substring(0, filename_before.length()-4)
				+"b"+ filename_before.substring(filename_before.length()-4);
		ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(filename));
		double[] temp_weights = (double[])inputStream.readObject();
		//System.out.println(temp_weights.length+" "+output_size);
		if (output_size == temp_weights.length){
			bias = new double[output_size];
			bias = temp_weights.clone();
			reset_traces();
			System.out.println("Loaded biases!");
		}else{
			System.out.println("Weight dimensions didn't match. Initialized from scratch.");
			init_weights();
		}
		inputStream.close();
	}
	
	public void save_biases1(String filename_before) throws FileNotFoundException, IOException, ClassNotFoundException{
		String filename = filename_before.substring(0, filename_before.length()-4)
				+"b"+ filename_before.substring(filename_before.length()-4);
		ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(filename));
		outputStream.writeObject(bias);
		outputStream.close();
		//System.out.println("Saved weights in file: "+filename);
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
		save_biases1(filename);
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	}
	
	public void reset_traces_only(){
		elig = new double[input_size][output_size];
	}
	
	public void reset_traces(){
		elig = new double[input_size][output_size];
		if (num_rounds > 0 && num_rounds%30 == 0) save_weights(filename_out);
		num_rounds ++;
	}
	
	public void print_sum(){
		double s1 = 0;
		for (int i=0; i<weights.length; i++){
			for (int j=0; j<weights[0].length; j++){
				s1 += weights[i][j];
			}
		}
		System.out.println(s1);
	}
	
	public void print_biases(){
		for (double i : bias) System.out.print(((int)i)+" ");
		System.out.println();
	}
	
	public void train(int action, double alpha, double error, double lambda, double dec, double reg, boolean reset_trace){
		double s = 0;
		double s1 = 0;
		if (reset_trace) lambda = 0;
		for (int i=0; i<weights.length; i++){
			for (int j=0; j<weights[0].length; j++){
				//if (elig[i][j]>0) 
				//	System.out.println(weights[i][j]+" "+elig[i][j]*alpha*error+" "+alpha+" "+error+" "+elig[i][j]);
				//s1 += elig[i][j];
				weights[i][j] += elig[i][j]*alpha*error - (alpha*weights[i][j]*reg);
				elig[i][j] *= lambda*dec;
				if (elig[i][j] < 0.001) elig[i][j] = 0;
				//s += weights[i][j];
			}
		}
		for (int i=0; i<weights[0].length; i++){
			bias[i] -= (alpha*bias[i]*reg);
		}
		bias[action] += alpha*error;
		//System.out.println(alpha*error);
		//print_biases();
		//System.out.println(s+" "+error+" "+s1);
		//System.out.println();
		//print_w();
	}
}
