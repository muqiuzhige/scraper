package com.telegram.hunter.dto;

import java.util.List;

import lombok.Data;

@Data
public class KnifeDTO {
	
	private List<String> groups;
	private List<String> users;
	private String dest;
	private String interval;
	
}
