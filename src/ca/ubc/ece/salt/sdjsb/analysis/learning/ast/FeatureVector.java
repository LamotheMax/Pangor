package ca.ubc.ece.salt.sdjsb.analysis.learning.ast;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import ca.ubc.ece.salt.sdjsb.analysis.learning.apis.KeywordDefinition;
import ca.ubc.ece.salt.sdjsb.analysis.learning.apis.KeywordDefinition.KeywordType;
import ca.ubc.ece.salt.sdjsb.analysis.learning.apis.KeywordUse;
import ca.ubc.ece.salt.sdjsb.analysis.learning.apis.KeywordUse.KeywordContext;

/**
 * Stores a feature vector (a row) of the repair pattern learning data set.
 * 
 * The feature vector includes meta information and keyword counts. The meta 
 * information (e.g., project, commit #, file names, etc.) are used to explore
 * the repair patterns once they are discovered through data mining. The 
 * keyword counts are used to discover the repair patterns.
 */
public class FeatureVector {
	
	/** A counter to produce unique IDs for each feature vector. **/
	private static int idCounter;
	
	/** The unique ID for the feature vector. **/
	public int id;
	
	/** The identifier for the project. **/
	public String projectID;
	
	/** The path to the source file where the bug is present. **/
	public String buggyFile;
	
	/** The path to the source file where the bug is repaired. **/
	public String repairedFile;
	
	/** The ID for the commit where the bug is present. **/
	public String buggyCommitID;
	
	/** The ID for the commit where the bug is repaired. **/
	public String repairedCommitID;
	
	/** The file path from which this feature vector was constructed. **/
	public String path;
	
	/** The function from which this feature vector was constructed. **/
	public String functionName;
	
	/** The buggy code **/
	public String sourceCode;
	
	/** The repaired code. **/
	public String destinationCode;
	
	/** The keyword counts in each fragment. **/
	public Map<KeywordUse, Integer> keywordMap;
	
	public FeatureVector() {
		this.id = FeatureVector.getNextID();
		this.keywordMap = new HashMap<KeywordUse, Integer>();
	}
	
	/**
	 * Joins a source feature vector with this (the destination) feature vector.
	 * @param source The source feature vector.
	 */
	public void join(FeatureVector source) {
		this.sourceCode = source.sourceCode;
		
		/* Insert the keywords form the source feature vector with change type
		 * REMOVED into this feature vector. */
		for(KeywordUse keyword : source.keywordMap.keySet()) {
			if(keyword.changeType == ChangeType.REMOVED) {
				this.keywordMap.put(keyword, source.keywordMap.get(keyword));
			}
		}
	}
	
	/**
	 * If the given token is a keyword, that keyword's count is incremented by
	 * one.
	 * @param token The string to check against the keyword list.
	 */
	public void addKeyword(KeywordUse keyword) {

		Integer count = this.keywordMap.containsKey(keyword) ? this.keywordMap.get(keyword) + 1 : 1;
		this.keywordMap.put(keyword,  count);
		
	}

	/**
	 * Add the keyword to the feature vector and set its count.
	 * @param token The string to check against the keyword list.
	 */
	public void addKeyword(KeywordUse keyword, Integer count) {

		this.keywordMap.put(keyword,  count);
		
	}
	
	/**
	 * This method serializes the feature vector. This is useful when writing
	 * a data set to the disk.
	 * @return The serialized version of the feature vector.
	 */
	public String serialize() {

		String serialized = id + "," + this.projectID + "," + this.buggyFile + "," + this.repairedFile 
				+ "," + this.buggyCommitID + "," + this.repairedCommitID + "," + this.functionName;
		
		for(KeywordUse keyword : this.keywordMap.keySet()) {
			Integer uses = this.keywordMap.get(keyword);
			serialized += "," + keyword.type + ":" + keyword.context + ":" + keyword.changeType + ":" + keyword.api.getName() + ":" + keyword.keyword + ":" + uses;
		}
		
		return serialized;
		
	}
	
	/**
	 * This method de-serializes a feature vector. This is useful when reading 
	 * a data set from the disk.
	 * @param serialized The serialized version of a feature vector.
	 * @return The feature vector represented by {@code serialized}.
	 */
	public static FeatureVector deSerialize(String serialized) throws Exception {
		
		String[] features = serialized.split(",");

		if(features.length < 7) throw new Exception("De-serialization exception. Serial format not recognized.");
		
		FeatureVector featureVector = new FeatureVector();
		featureVector.id = Integer.parseInt(features[0]);
		featureVector.projectID = features[1];
		featureVector.buggyFile = features[2];
		featureVector.repairedFile = features[3];
		featureVector.buggyCommitID = features[4];
		featureVector.repairedCommitID = features[5];
		featureVector.functionName = features[6];
		
		for(int i = 7; i < features.length; i++) {
			String[] feature = features[i].split(":");
			if(feature.length < 6) throw new Exception("De-serialization exception. Serial format not recognized.");
			KeywordUse keyword = new KeywordUse(KeywordType.valueOf(feature[0]), 
												KeywordContext.valueOf(feature[1]),
												feature[4], 
												ChangeType.valueOf(feature[2]), feature[3]);
			featureVector.addKeyword(keyword, Integer.parseInt(feature[5]));
		}

		return featureVector;
		
	}
	
	/**
	 * Prints the meta features and the specified keyword values in the order they are provided.
	 * @param keywords An ordered list of the keywords to print in the feature vector.
	 * @return the CSV row (the feature vector) as a string.
	 */
	public String getFeatureVector(Set<KeywordDefinition> keywords) {

		String vector = id + "," + this.projectID + "," + this.buggyFile + "," + this.repairedFile 
				+ "," + this.buggyCommitID + "," + this.repairedCommitID + "," + this.functionName;
		
		for(KeywordDefinition keyword : keywords) {
			if(this.keywordMap.containsKey(keyword)) vector += "," + this.keywordMap.get(keyword).toString();
			else vector += ",0";
		}
		
		return vector;

	}
	
	/**
	 * @return The source code for the alert.
	 */
	public String getSource() {
		return this.sourceCode;
	}
	
	/**
	 * @return The destination code for the alert.
	 */
	public String getDestination() {
		return this.destinationCode;
	}

	/**
	 * @return The next unique ID for a feature vector alert.
	 */
	private static int getNextID() {
		idCounter++;
		return idCounter;
	}
	
}