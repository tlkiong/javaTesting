package mainTest;

import org.junit.Test;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class TestGuava {
	
	@Test
	public void TestBiMap(){
		BiMap<String,String> britishToAmerican = HashBiMap.create();
		BiMap<String,String> americanToBritish = britishToAmerican.inverse();
		
		// Initialise and use just like a normal map
		britishToAmerican.put("aubergine","eggplant");
		britishToAmerican.put("courgette","zucchini");
		britishToAmerican.put("jam","jelly");
		System.out.println(americanToBritish.get("jelly")); // jam
		britishToAmerican.put("jam","aa");
		
		System.out.println(britishToAmerican.get("aubergine")); // eggplant
		System.out.println(britishToAmerican.get("jam")); // aa
		
		System.out.println(americanToBritish.get("jelly")); // null
		System.out.println(americanToBritish.get("eggplant")); // aubergine
		System.out.println(americanToBritish.get("zucchini")); // courgette
	}
}
