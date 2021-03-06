package wot.jdt.tds.json_ld;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.mgt.Explain.InfoLevel;
import org.junit.Assert;
import org.junit.Test;

import com.apicatalog.jsonld.JsonLdError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.google.gson.JsonObject;

import wot.jtd.JTD;
import wot.jtd.model.Thing;

public class LinkSmartTest {


	
	@Test
	public void test1() throws JsonProcessingException, JsonLdError {
		ARQ.setExecutionLogging(InfoLevel.NONE);
		JTD.setShowExternalValuesWarnings(false);
		JsonObject td = readJsonFile(new File("./src/test/resources/linksmart/td-1.json"));
		
		Thing thing = null;
		try {
			thing =  Thing.fromJson(td);		
		}catch (UnrecognizedPropertyException e) {
			e.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		Assert.assertTrue(thing!=null && thing.isEquivalent(td));

	}
	

	private static JsonObject readJsonFile(File myObj) {
		StringBuilder builder = new StringBuilder(); 
		try {
		      Scanner myReader = new Scanner(myObj);
		      while (myReader.hasNextLine()) {
		        String data = myReader.nextLine();
		        builder.append(data);
		      }
		      myReader.close();
		    } catch (FileNotFoundException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }
		return JTD.parseJson(builder.toString());
	}
	
}
