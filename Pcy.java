/*
 * PCY Algorithm -- Finding Frequent Pairs
 * 
 * FIRST PASS:  When passing through the file, using a nested for loop and hash function, hash pairs
 * 				to buckets. This refinement from A-Priori uses wasted memory space from the first pass
 * 				so that the second pass can have less candidate pairs to go through. Also keep a count of
 * 				all items between passes.
 * SECOND PASS: After a bitVector was created to map buckets that are frequent (1) and infrequent (0), only
 * 				consider candidate pairs in frequent buckets when going through the second pass. Then
 * 				compare with support threshold to see if candidate pairs are frequent as well.
 * 
 * Author: Angelo Bortolin, 104256682
 * February 17, 2019
 */

import java.util.*;
import java.io.*;
import java.util.Map.Entry;

public class Pcy {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		if(args.length != 3){
			System.out.println("Usage of Pcy:\t<file> <support threshold %> <dataset size %>\nex.\t\t$ retail.txt 10 100");
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
		double support = basketsToUse * ((double)threshold_percent / 100);
		
		ArrayList<Integer> freq_items_array = new ArrayList<Integer>();
		HashMap<Integer, Integer> itemCount = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> bucketHashTable = new HashMap<Integer, Integer>();
		final long startTime1 = System.currentTimeMillis();
		
		// FIRST PASS
		bucketHashTable = bucket_hashing(fileName, itemCount, basketsToUse);
		
		// between first and second pass, making bitVector
		int[] bitVector = new int[88162];
		for(int i = 0; i < bitVector.length; i++){
			if(bucketHashTable.containsKey(i) && bucketHashTable.get(i) >= support)
				bitVector[i] = 1;
			else
				bitVector[i] = 0;
		}
		
		// list of frequent items
		System.out.printf("ITEMS\tFREQUENCY\n");
		itemCount.forEach((key, value)->{
			if(value > support){
				freq_items_array.add(key);
				System.out.printf("{%d}\t%d\n", key, value);
			}
		});
		
		// number of frequent items
		System.out.println("NUMBER OF FREQUENT ITEMS: "+ freq_items_array.size());
			
		// SECOND PASS
		HashMap<String, Integer> pairs_returned = new HashMap<String, Integer>();
		pairs_returned = find_freq_pairs(fileName, freq_items_array, bitVector, basketsToUse);
		
		// After the second pass, checking which pairs are frequent
		Iterator<Entry<String, Integer>> itr = pairs_returned.entrySet().iterator();
		while (itr.hasNext()){
			Entry<String, Integer> entry = itr.next();
			if(entry.getValue() < support)
				itr.remove();
		}
		
		System.out.printf("\nPAIRS\t\tFREQUENCY\n");
		
		pairs_returned.forEach((key, value) -> {
			System.out.printf("%8s\t%d\n", key, value);
		});
		System.out.println("Number of frequent pairs: " + pairs_returned.size());
		
		
		final long endTime = System.currentTimeMillis();
		System.out.println("\nSupport: " + (int)support);
		System.out.println("Total execution time: " + (endTime - startTime1));
		System.out.println("Baskets in dataset: "+ (int)basketsToUse);
		
	}
	
	// hash function used for buckets
	public static int pcy_hash_function(int i, int j){ 
		return (i * j) % 88162;
	}
	
	// FIRST PASS
	public static HashMap<Integer, Integer> bucket_hashing(String fileName, HashMap<Integer, Integer> itemCount, double dataSetSize) throws FileNotFoundException,IOException{
		
		HashMap<Integer, Integer> bucketHashTable = new HashMap<Integer, Integer>();
		int numBaskets = 0;
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		
		while(br.ready()){
			String line = br.readLine();
			if(++numBaskets >= dataSetSize)
				break;
			StringTokenizer st = new StringTokenizer(line);
			Vector<Integer> basket = new Vector<Integer>(); //vector for basket read
			
			while(st.hasMoreTokens()){
				int num = Integer.parseInt(st.nextToken());
				itemCount.put(num, itemCount.containsKey(num) ? itemCount.get(num) + 1 : 1); // adding 1 to items count
				basket.add(num); // setting up basket for nested for-loop
			}
			
			int basket_length = basket.size();
			for(int i = 0; i < basket_length; i++){
				for(int j = (i + 1); j < basket_length; j++){
					int hashReturn = pcy_hash_function(basket.elementAt(i),basket.elementAt(j));
					bucketHashTable.put(hashReturn, bucketHashTable.containsKey(hashReturn) ? bucketHashTable.get(hashReturn) + 1 : 1);
				}
			}	
		}
		br.close();
		return bucketHashTable;
		
	}
	
	// SECOND PASS
	public static HashMap<String, Integer> find_freq_pairs(String fileName, ArrayList<Integer> freq_items_array, int[] bitVector, double dataSetSize) throws FileNotFoundException,IOException{
		
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
					
					// checks if pairs map to a frequent bucket in bit vector, as well as if each item is a frequent item
					if(bitVector[pcy_hash_function(basket.elementAt(i),basket.elementAt(j))] == 1 && freq_items_array.contains(basket.get(i)) && freq_items_array.contains(basket.get(j))){
						String pair = "{"+basket.get(i)+","+basket.get(j)+"}";
//						System.out.println(pair); //DEBUG LINE
						freq_pair.put(pair, (freq_pair.containsKey(pair)) ? freq_pair.get(pair) + 1 : 1);
						
					}	
				}
			}		
		}
		br.close();
		
		return freq_pair;
	}

}
