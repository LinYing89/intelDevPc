package com.bairock.intelDevPc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bairock.intelDevPc.repository.LinkageRepository;
import com.bairock.iot.intelDev.linkage.Linkage;

@Service
public class LinkageService {

	@Autowired
	private LinkageRepository linkageRepository;
	
	public void addLinkage(Linkage linkage) {
		linkageRepository.saveAndFlush(linkage);
	}
	
	public void deleteLinkage(Linkage linkage) {
		linkageRepository.deleteById(linkage.getId());
	}
	
	public void updateLinkage(Linkage linkage) {
		linkageRepository.saveAndFlush(linkage);
	}
}
