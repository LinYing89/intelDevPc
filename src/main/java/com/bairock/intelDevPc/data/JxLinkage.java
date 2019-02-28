package com.bairock.intelDevPc.data;

import com.bairock.iot.intelDev.linkage.Linkage;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class JxLinkage{

	private Linkage linkage;
	private BooleanProperty enableProperty = null;
	
	public JxLinkage() {}
	
	public JxLinkage(Linkage linkage) {
		this.linkage = linkage;
	}
	
	public Linkage getLinkage() {
		return linkage;
	}

	public void setLinkage(Linkage linkage) {
		this.linkage = linkage;
	}

	public final BooleanProperty enableProperty() {
		if(null == enableProperty) {
			enableProperty = new SimpleBooleanProperty(this, "enable", linkage.isEnable());
			enableProperty.addListener((ena)-> linkage.setEnable(enableProperty.get()));
		}
		return enableProperty;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isEnable() {
		return null == enableProperty ? linkage.isEnable() : enableProperty.get();
	}
	
	/**
	 * 
	 * @param enable
	 */
	public void setEnable(boolean enable) {
		//hibernate保存时不走setter, 所以要保证属性实时
		this.linkage.setEnable(enable);
		if(null != enableProperty) {
			this.enableProperty.set(enable);
		}
	}
}
