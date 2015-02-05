package com.example.timebank;

public class Utils {
	
	public static String getSkillLevel(double value){
		
		if(value == 0.0)
			return "No knowledge";
		if(value == 0.5)
			return "Beginner";
		if(value == 1.0)
			return "Basic";
		if(value == 1.5)
			return "Average";
		if(value == 2.0)
			return "Medium";
		if(value == 2.5)
			return "Skilled";
		if(value == 3.0)
			return "Advanced";
		if(value == 3.5)
			return "Experienced";
		if(value == 4.0)
			return "Professional";
		if(value == 4.5)
			return "Specialist";
		if(value == 5.0)
			return "Expert";
		
		return null;		
	}

}
