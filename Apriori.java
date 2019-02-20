/*
 * A-Priori Algorithm -- Finding Frequent Pairs
 * 
 * FIRST PASS:  Count frequency of each item in the file to compare with support threshold
 * 				to obtain frequent items.
 * SECOND PASS: Using a nested for loop, develop candidate pairs within each basket. Check if
 * 				these candidate pairs are frequent items and consider them frequent pairs if their
 * 				frequency as a pair is greater than or equal to the support threshold.
 * 
 * Author: Angelo Bortolin, 104256682
 * February 17, 2019
 */

import java.util.*;
import java.io.*;
import java.util.Map.Entry;

public class Apriori {
	
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub

		if(args.length != 3){
			System.out.println("Usage of Apriori: <file> <support threshold %> <dataset size %>\nex.\t\t$ retail.txt 10 100");
			return;
		}
		
		String fileName = args[0];
		int threshold_percent = Integer.parseInt(args[1]);
		int dataSetPercentage = Integer.parseInt(args[2]);
		int numBaskets = 0;
		
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		while(br.ready()){
			br.readLine();
			numBaskets++;
		}
		br.close();
		
		double basketsToUse = numBaskets * ((double)dataSetPercentage / 100);
		ArrayList<Integer> freq_items_array = new ArrayList<Integer>();
		
		// starting time
		final long startTime1 = System.currentTimeMillis();
		
		// FIRST PASS, finding the frequent items in the dataset
		find_freq_items(fileName, freq_items_array, threshold_percent, basketsToUse);
		
		// SECOND PASS, Frequent pairs now
		find_freq_pairs(fileName, freq_items_array, threshold_percent, basketsToUse);
		
		final long endTime = System.currentTimeMillis();
		System.out.println("Total execution time: " + (endTime - startTime1));
		System.out.println("Baskets in dataset: "+ (int)basketsToUse);
		
	}
	
	// FIRST PASS 
	public static void find_freq_items(String fileName, ArrayList<Integer> set_of_freq_items, int threshold, double dataSetSize) throws FileNotFoundException,IOException{
		
		HashMap<Integer, Integer> itemCount = new HashMap<Integer, Integer>();
		int numBaskets = 0;
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		
		while(br.ready()){
			String line = br.readLine();
			if(++numBaskets >= dataSetSize)
				break;
			StringTokenizer st = new StringTokenizer(line);
			
			while(st.hasMoreTokens()){
				int num = Integer.parseInt(st.nextToken());
				itemCount.put(num, itemCount.containsKey(num) ? itemCount.get(num) + 1 : 1);
			}
		}
		
		double support = numBaskets * ((double)threshold / 100);
		System.out.printf("ITEMS\tFREQUENCY\n");
		
//		Between first and second pass, compare against support and add to set_of_freq_items if it is a frequent item		
		itemCount.forEach((key, value)->{
			if(value > support){
				set_of_freq_items.add(key);
				System.out.printf("{%d}\t%d\n", key, value);
			}
		});
		System.out.println("Number of frequent items: " + set_of_freq_items.size());
		
		br.close();
	}
	
	// SECOND PASS
	public static void find_freq_pairs(String fileName, ArrayList<Integer> freq_items, int threshold, double dataSetSize) throws FileNotFoundException,IOException{
		
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		HashMap<String, Integer> freq_pair = new HashMap<String, Integer>();
		int numBaskets = 0;
		
		while(br.ready()){
			String line = br.readLine();
			if(++numBaskets >= dataSetSize)
				break;
			StringTokenizer st = new StringTokenizer(line);
			Vector<Integer> basket = new Vector<Integer>(); //vector for basket read in
			
			while(st.hasMoreTokens()){
				int num = Integer.parseInt(st.nextToken());
				basket.add(num);			
			}

			int basket_length = basket.size();
			for(int i = 0; i < basket_length; i++){
				for(int j = (i + 1); j < basket_length; j++){
					
//					System.out.println("["+basket.get(i)+","+basket.get(j)+"]"); //DEBUG LINE
//					checks if each each candidate pair is made up of the frequent items previously found					
					if(freq_items.contains(basket.get(i)) && freq_items.contains(basket.get(j))){
						String pair = "{"+basket.get(i)+","+basket.get(j)+"}";
//						System.out.println(pair); //DEBUG LINE
						freq_pair.put(pair, (freq_pair.containsKey(pair)) ? freq_pair.get(pair) + 1 : 1);
						
					}
				}
			}
		}
		
		
		// checking which pairs are frequent after the second pass
		double support = numBaskets * ((double)threshold / 100);
	
		Iterator<Entry<String, Integer>> itr = freq_pair.entrySet().iterator();
		while (itr.hasNext()){
			Entry<String, Integer> entry = itr.next();
			if(entry.getValue() < support)
				itr.remove();
		}		

		
		System.out.printf("\nPAIRS\t\tFREQUENCY\n");
		
		freq_pair.forEach((key, value) -> {
			System.out.printf("%8s\t%d\n", key, value);
		});
		System.out.println("Number of frequent pairs: " + freq_pair.size());
		
		br.close();
		
		System.out.println("\nSupport: " + (int)support);
	}
}
