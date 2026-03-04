/*
 * Copyright (c) 2002-2022, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.termofservice.business;


/**
 * 
 * EntryDto
 *
 */
public class EntryDto {
    private int nId;  
    private String strTitle;
    private String strText;
    private int nVersion;  
    private boolean bPublished;
    private boolean bExistsUserAcceptedEntry;
    
    /**
     * Constructor
     */
    public EntryDto() {
    	//Do nothing
    }
    
    /**
     * Constructor
     * @param entry
     * @param existsUserAcceptedEntry
     */
    public EntryDto(Entry entry, boolean existsUserAcceptedEntry) {
    	this.nId = entry.getId();
    	this.strTitle = entry.getTitle();
    	this.strText = entry.getText();
    	this.bPublished = entry.isPublished();
    	this.nVersion = entry.getVersion();
    	this.bExistsUserAcceptedEntry = existsUserAcceptedEntry;
    }

	/**
	 * @return the nId
	 */
	public int getId() {
		return nId;
	}

	/**
	 * @param nId the nId to set
	 */
	public void setId(int nId) {
		this.nId = nId;
	}

	/**
	 * @return the strTitle
	 */
	public String getTitle() {
		return strTitle;
	}

	/**
	 * @param strTitle the strTitle to set
	 */
	public void setTitle(String strTitle) {
		this.strTitle = strTitle;
	}

	/**
	 * @return the strText
	 */
	public String getText() {
		return strText;
	}

	/**
	 * @param strText the strText to set
	 */
	public void setText(String strText) {
		this.strText = strText;
	}

	/**
	 * @return the nVersion
	 */
	public int getVersion() {
		return nVersion;
	}

	/**
	 * @param nVersion the nVersion to set
	 */
	public void setVersion(int nVersion) {
		this.nVersion = nVersion;
	}

	/**
	 * @return the bPublished
	 */
	public boolean isPublished() {
		return bPublished;
	}

	/**
	 * @param bPublished the bPublished to set
	 */
	public void setPublished(boolean bPublished) {
		this.bPublished = bPublished;
	}

	/**
	 * @return the bExistsUserAcceptedEntry
	 */
	public boolean isExistsUserAcceptedEntry() {
		return bExistsUserAcceptedEntry;
	}

	/**
	 * @param bExistsUserAcceptedEntry the bExistsUserAcceptedEntry to set
	 */
	public void setExistsUserAcceptedEntry(boolean bExistsUserAcceptedEntry) {
		this.bExistsUserAcceptedEntry = bExistsUserAcceptedEntry;
	}

}
