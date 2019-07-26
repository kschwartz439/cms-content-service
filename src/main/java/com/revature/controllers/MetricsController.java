package com.revature.controllers;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.revature.entities.Module;
import com.revature.services.ContentService;
import com.revature.services.ModuleService;
import com.revature.services.SearchService;
import com.revature.services.TimegraphService;
import com.revature.util.MetricsData;
import com.revature.util.TimeGraphData;


@CrossOrigin
@RestController
@RequestMapping("/metrics")
public class MetricsController {
	// reports mapping for metrics
	@Autowired
	ModuleService moduleService;
	@Autowired
	ContentService contentService;
	@Autowired
	SearchService searchService;
	@Autowired
	TimegraphService timegraphService;
		
	
	
	@PostMapping("/obtain/{timeFrame}")
	public MetricsData getMetrics(@PathVariable("timeFrame") long timeRange, 
								  @RequestBody Map<String, Object> ids) {
		System.out.println(ids	);
		//formats for codeCount
		String[] formats = new String[] {"Code", "Document", "Powerpoint"};
		Map<String, Integer> contentFormats = contentService.getContentByFormat(formats);
		System.out.println(contentFormats.toString());

		
		//numDiffMods
		Set<Module> modules = (Set<Module>) moduleService.getAllModules();
		int modSize = modules.size(); //num diff modules
		
		
		//avg size
		@SuppressWarnings("unchecked")
		ArrayList<Integer> idsIn = (ArrayList<Integer>) ids.get("modules");
		double avgMods =  moduleService.getAverageByModuleIds(idsIn);
		
		
		TimeGraphData timeGraphData = timegraphService.findByCreatedBetween(timeRange);
		
		Integer numCode = 0;
		if(contentFormats.containsKey("Code"))
			numCode = contentFormats.get("Code");
		
		Integer numDoc = 0;
		if(contentFormats.containsKey("Document"))
			numDoc = contentFormats.get("Document");
		
		Integer numPpt = 0;
		if(contentFormats.containsKey("Powerpoint"))
			numPpt = contentFormats.get("Powerpoint");
		
		MetricsData gatheredMetrics = new MetricsData(
				numCode, numDoc, numPpt, 
				modSize, avgMods, timeGraphData);
		 
		 System.out.println("METRICS DATA OBJECT: " + gatheredMetrics.toString());
		 
		 return gatheredMetrics;
	}
	
	
	
	/*
	 * Returns the number of Contents with format set to code
	 * @return count of code formats 
	 * */
	@GetMapping("/codeCount")
	public ArrayList<Integer> getCountCodeEx(){
		String[] formats = new String[] {"Code", "Document", "Powerpoint"};
		
		Map<String, Integer> contentMap = contentService.getContentByFormat(formats);
		ArrayList<Integer> codeCount = new ArrayList<Integer>();
		
		for(String key : contentMap.keySet()) {
			codeCount.add(contentMap.get(key));
		}
		
		return codeCount;
	}
	
	
	/*
	 * Returns the number of different modules in DB
	 * @return number of modules 
	 * */
	@GetMapping("/numDiffMods")
	public int getNumDiffMod() {
		Set<Module> modules = (Set<Module>) moduleService.getAllModules();
		return modules.size();
	}
	
	
	/*
	 * Return average count of resources covered by each Module object from DB
	 * @returns average number of links
	 * */
	@PostMapping("/averageRecs")
	public double getAvgRec(@RequestBody Map<String, Object> ids) {
		System.out.println(ids);
		@SuppressWarnings("unchecked")
		ArrayList<Integer> idsIn = (ArrayList<Integer>) ids.get("modules");
		return moduleService.getAverageByModuleIds(idsIn);
	}
}