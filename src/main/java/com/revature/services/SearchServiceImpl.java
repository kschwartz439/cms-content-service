package com.revature.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.revature.entities.Content;
import com.revature.entities.Link;
import com.revature.repositories.LinkRepository;
import com.revature.repositories.ContentRepository;
import com.revature.repositories.ModuleRepository;
import com.revature.util.LogException;

@Service
public class SearchServiceImpl implements SearchService {
	
	@Autowired
	ContentRepository cr;
	@Autowired
	ModuleRepository mr;
	@Autowired
	LinkRepository lr;
	@Autowired
	ContentService csi;

	@Override
	@LogException
	public Set<Content> filterContentByTitle(String title) {
		Set<Content> temp = cr.findByTitle(title); 
		return temp;
	}

	@Override
	@LogException
	public Set<Content> filterContentByFormat(String format) {
		return cr.findByFormat(format); 
	}

	/**
	 * filterContentBySubjects takes in a list of integers that represent
	 * module ids. It then stores all the related links of that module
	 * in a temporary set.
	 * then it gets all the content that relates to the selected links.
	 * The method uses AND logic. 
	 */
	@Override
	@LogException
	public Set<Content> filterContentBySubjects(List<Integer> moduleIds) {
		/**
		 * a set of content that will be returned
		 */
		Set<Content> contents = new HashSet<>();
		/**
		 * Temporary sets to hold integers while searching
		 */
		Set<Integer> ids = new HashSet<>();
		Set<Integer> idsTemp = new HashSet<>();
		
		/**
		 * create a set of links that have a moduleID of the first element
		 * of the list that is passed into the method and adds it to 
		 * a temporary set of ids.
		 */
		Set<Link> linksByModuleID = lr.findByModuleId(moduleIds.get(0));
		for(Link link : linksByModuleID) {
			ids.add(link.getContentId());
		}
		/**
		 * loop over the array that was passed in getting all the links
		 * assosiated to each element and only keeping those who share 
		 * a that of the list that was already made.
		 */
		for(int i = 1; i < moduleIds.size(); i++) {
			linksByModuleID = lr.findByModuleId(moduleIds.get(i));
			for(Link link : linksByModuleID) {
				idsTemp.add(link.getContentId());
			}
			ids.retainAll(idsTemp);
		}
		
		/**
		 * this finds all the content by id for each element in the 
		 * temp list of ids and adds them to the content set that will 
		 * be returned.
		 */
		cr.findAllById(ids).forEach(contents :: add);
		
		return contents;	
	}
	
	/**
	 * getContentByModuleID takes in a int that is a module id and 
	 * returns a set of content that have the inputed value as 
	 * the moduleID.
	 */
	@Override
	@LogException
	public Set<Content> getContentByModuleId(int moduleId) {
		Set<Link> links = lr.findByModuleId(moduleId);
		int contentId = links.iterator().next().getContentId();
		return cr.findById(contentId);
	}
	/**
	 * Filter takes a content title, content format and/or
	 * a list of Integers that represent module ids.
	 * It will then return a set of content that contains all content that 
	 * matches all 3 inputs using AND logic.
	 * If an input is empty it is ignored and is not part of the logic.
	 */
	@Override
	@LogException
	public Set<Content> filter(String title, String format, List<Integer> modules) {
		
		Set<Content> selectedContent;
		Set<Content> tempSet = new HashSet<>();
		/**
		 * check if the array passed in was empty and populating the initial 
		 * set of content
		 */
		if (modules.isEmpty()) {
			selectedContent = (Set<Content>) csi.getAllContent();
		}
		else {
			selectedContent = this.filterContentBySubjects(modules);
		}
		/**
		 * check if title is empty if it isn't it will remove any content 
		 * from the set that does not match the title.
		 */
		if (!title.equalsIgnoreCase("")) {
			for(Content c: selectedContent)
			{
				if(c.getTitle().toLowerCase().contains(title.toLowerCase())) tempSet.add(c);
			}

			selectedContent = new HashSet<Content>();
			selectedContent.addAll(tempSet);
			System.out.println(selectedContent);
		}

		tempSet.clear();
		
		/**
		 * check if the format is empty, if it isn't remove all content
		 * that the format does not match what is passed in. 
		 */
		if (!format.equalsIgnoreCase("")) {
			for (Content c: selectedContent)
			{
				System.out.println(c);
				if(c.getFormat().toLowerCase().contains(format.toLowerCase())) tempSet.add(c);
			}

			selectedContent = tempSet;
			System.out.println(selectedContent);
		}
		
		return selectedContent;
	}

	@Override
	public Set<Content> filterContent(Set<Content> contents, Map<String, Object> filters) {
		// The Set of Content we are going to be returning
		Set<Content> filtCont = new HashSet<Content>();
		
		/*
		 * Filter the content based on the Modules in the filters
		 */
		try {
			ArrayList<Integer> ids = (ArrayList<Integer>)filters.get("modules");
			if(ids.size() != 0) {
				for(Content c : contents) {
					for(Link l : c.getLinks()) {
						if(ids.contains(l.getModuleId())) {
							filtCont.add(c);
						}
					}
				}
			}
		} catch(NullPointerException e) {
			
		} catch(Exception e) {
			
		}
		
		/*
		 * Filter the content based on the Title in the filters
		 */
		try {
			String title = (String)filters.get("title");
			if(!(title.equals(""))) {
				for(Content c : contents) {
					if(c.getTitle().equals(title)) {
						filtCont.add(c);
					}
				}
			}
		} catch(Exception e) {
			
		}
		
		/*
		 * Filter the content based on the Format in the filters
		 */
		try {
			String format = (String)filters.get("format");
			if(!(format.equals(""))) {
				for(Content c : contents) {
					if(c.getFormat().contentEquals(format)) {
						filtCont.add(c);
					}
				}
			}
		} catch(Exception e) {
			
		}
		
		// If there was no filters then just return the full collection
		if(filtCont.isEmpty()) {
			return contents;
		}
		// Else return the filtered set
		return filtCont;
	}
}

	
