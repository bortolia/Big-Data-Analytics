/*
 * Multihash (Refinement to PCY) Algorithm -- Finding Frequent Pairs
 * 
 * FIRST PASS:  When passing through the file, using a nested for loop and hash function, hash pairs
 * 				to buckets. This is done for both hash tables using their corresponding hash function
 * SECOND PASS: After bitVector1 and bitVector2 were created to map buckets that are frequent (1) and
 * 				infrequent (0), the second pass checks candidate pairs that are mapped to each bitVecotr
 * 				and that each item of the pairs are frequent items.
 * 
 * Author: Angelo Bortolin, 104256682
 * February 17, 2019
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Map.Entry;

public class Multihash {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		if(args.length != 3){
			System.out.println("Usage of Multihash:\t<file> <support threshold %> <dataset size %>\nex.\t\t$ retail.txt 10 100");
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
		HashMap<Integer, Integer> firstHashTable = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> secondHashTable = new HashMap<Integer, Integer>();
		final long startTime1 = System.currentTimeMillis();
		
		// FIRST PASS
		bucket_hashing(fileName, itemCount, basketsToUse, firstHashTable, secondHashTable);
		
		// making bitVector1 and bitVector2
		int[] bitVector1 = new int[44081];
		for(int i = 0; i < bitVector1.length; i++){
			if(firstHashTable.containsKey(i) && firstHashTable.get(i) >= support)
				bitVector1[i] = 1;
			else
				bitVector1[i] = 0;
		}
		
		int[] bitVector2 = new int[44081];
		for(int i = 0; i < bitVector2.length; i++){
			if(secondHashTable.containsKey(i) && secondHashTable.get(i) >= support)
				bitVector2[i] = 1;
			else
				bitVector2[i] = 0;
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
		pairs_returned = find_freq_pairs(fileName, freq_items_array, bitVector1, bitVector2, basketsToUse);
		
		// After the third pass, checking which pairs are frequent
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
	
	// hash functions used for buckets in order to make bitVectors half the size
	public static int pcy_hash_function1(int i, int j){ 
		return (i * j) % 44081;
	}
			
	public static int pcy_hash_function2(int i, int j){ 
		return (i + j) % 44081;
	}
	
	// FIRST PASS
	public static void bucket_hashing(String fileName, HashMap<Integer, Integer> itemCount, double dataSetSize, HashMap<Integer, Integer> firstBuckets, HashMap<Integer, Integer> secondBuckets) throws FileNotFoundException,IOException{
		
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
			
			// Hashing both tables in the first pass
			
			int basket_length = basket.size();
			for(int i = 0; i < basket_length; i++){
				for(int j = (i + 1); j < basket_length; j++){
					int hashReturn = pcy_hash_function1(basket.elementAt(i),basket.elementAt(j));
					firstBuckets.put(hashReturn, firstBuckets.containsKey(hashReturn) ? firstBuckets.get(hashReturn) + 1 : 1);
				}
			}
			
			for(int i = 0; i < basket_length; i++){
				for(int j = (i + 1); j < basket_length; j++){
					int hashReturn = pcy_hash_function2(basket.elementAt(i),basket.elementAt(j));
					secondBuckets.put(hashReturn, secondBuckets.containsKey(hashReturn) ? secondBuckets.get(hashReturn) + 1 : 1);
				}
			}
		}
		br.close();
	}
	
	// SECOND PASS
	public static HashMap<String, Integer> find_freq_pairs(String fileName, ArrayList<Integer> freq_items_array, int[] bitVector1, int[] bitVector2, double dataSetSize) throws FileNotFoundException,IOException{
		
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
					
					// checks if pairs map to a frequent bucket in both bitVectors, as well as if each item is a frequent item
					if(bitVector1[pcy_hash_function1(basket.elementAt(i),basket.elementAt(j))] == 1 && bitVector2[pcy_hash_function2(basket.elementAt(i),basket.elementAt(j))] == 1 && freq_items_array.contains(basket.get(i)) && freq_items_array.contains(basket.get(j))){
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
