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
 	
 
package fr.paris.lutece.plugins.termofservice.web;

import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.binding.BindingResult;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.ModelAttribute;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.util.mvc.utils.MVCUtils;
import fr.paris.lutece.portal.web.cdi.mvc.Models;
import fr.paris.lutece.portal.web.util.IPager;
import fr.paris.lutece.portal.web.util.Pager;
import fr.paris.lutece.util.url.UrlItem;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.plugins.termofservice.business.Entry;
import fr.paris.lutece.plugins.termofservice.business.EntryHome;
import fr.paris.lutece.plugins.termofservice.util.TOSConstants;

/**
 * This class provides the user interface to manage Entry features ( manage, create, modify, remove )
 */
@RequestScoped
@Named
@Controller( controllerJsp = "ManageEntrys.jsp", controllerPath = "jsp/admin/plugins/termofservice/", right = "TERMOFSERVICE_MANAGEMENT", securityTokenEnabled = true )
public class EntryJspBean extends MVCAdminJspBean
{
    private static final long serialVersionUID = 1L;

    // Templates
    private static final String TEMPLATE_MANAGE_ENTRYS = "/admin/plugins/termofservice/manage_entrys.html";
    private static final String TEMPLATE_CREATE_ENTRY = "/admin/plugins/termofservice/create_entry.html";
    private static final String TEMPLATE_MODIFY_ENTRY = "/admin/plugins/termofservice/modify_entry.html";

    // Parameters
    private static final String PARAMETER_ID_ENTRY = "id";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_ENTRYS = "termofservice.manage_entrys.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_ENTRY = "termofservice.modify_entry.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_ENTRY = "termofservice.create_entry.pageTitle";

    // Markers
    private static final String MARK_ENTRY_LIST = "entry_list";
    private static final String MARK_ENTRY = "entry";
    private static final String MARK_FEATURE_TOS_PUBLICATION = "featureTOSPublication";

    // Properties
    private static final String MESSAGE_CONFIRM_REMOVE_ENTRY = "termofservice.message.confirmRemoveEntry";
    private static final String PROPERTY_DEFAULT_LIST_ITEM_PER_PAGE = "termofservice.listItems.itemsPerPage";

    // Views
    private static final String VIEW_MANAGE_ENTRYS = "manageEntrys";
    private static final String VIEW_CREATE_ENTRY = "createEntry";
    private static final String VIEW_MODIFY_ENTRY = "modifyEntry";

    // Actions
    private static final String ACTION_CREATE_ENTRY = "createEntry";
    private static final String ACTION_MODIFY_ENTRY = "modifyEntry";
    private static final String ACTION_REMOVE_ENTRY = "removeEntry";
    private static final String ACTION_CONFIRM_REMOVE_ENTRY = "confirmRemoveEntry";
    private static final String ACTION_PUBLISH_ENTRY = "publishEntry";

    // Infos
    private static final String INFO_ENTRY_CREATED = "termofservice.info.entry.created";
    private static final String INFO_ENTRY_UPDATED = "termofservice.info.entry.updated";
    private static final String INFO_ENTRY_REMOVED = "termofservice.info.entry.removed";
    private static final String INFO_ENTRY_PUBLISHED = "termofservice.info.entry.published";
    
    // Errors
    private static final String ERROR_RESOURCE_NOT_FOUND = "Resource not found";
    
    @Inject
    @Pager( listBookmark = MARK_ENTRY_LIST, defaultItemsPerPage = PROPERTY_DEFAULT_LIST_ITEM_PER_PAGE)
    private IPager<Integer, Entry> _pager;
    @Inject 
    private Models model;
    private Entry _entry;
    
    /**
     * Build the Manage View
     * @param request The HTTP request
     * @return The page
     */
    @View( value = VIEW_MANAGE_ENTRYS, defaultView = true )
    public String getManageEntrys( HttpServletRequest request )
    {
        List<Integer> _listIdEntrys = EntryHome.getIdEntrysList(  );
        _pager.withBaseUrl( getHomeUrl( request ) )
            .withIdList( _listIdEntrys ).populateModels( request, model, ( l ) -> getItemsFromIds( l ), getLocale( ) );
        
        model.put( MARK_FEATURE_TOS_PUBLICATION, TOSConstants.PROPERTY_FEATURE_TOS_PUBLICATION_ENABLED );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_ENTRYS, TEMPLATE_MANAGE_ENTRYS, model );
    }

	/**
     * Get Items from Ids list
     * @param listIds
     * @return the populated list of items corresponding to the id List
     */
	List<Entry> getItemsFromIds( List<Integer> listIds ) 
	{
		List<Entry> listEntry = EntryHome.getEntrysListByIds( listIds );
		
		// keep original order
        return listEntry.stream()
                 .sorted(Comparator.comparingInt( notif -> listIds.indexOf( notif.getId())))
                 .collect(Collectors.toList());
	}
    
    /**
     * Returns the form to create a entry
     *
     * @param request The Http request
     * @return the html code of the entry form
     */
    @View( VIEW_CREATE_ENTRY )
    public String getCreateEntry( HttpServletRequest request )
    {
        _entry = ( _entry != null ) ? _entry : new Entry( );

        model.put( MARK_ENTRY, _entry );
        return getPage( PROPERTY_PAGE_TITLE_CREATE_ENTRY, TEMPLATE_CREATE_ENTRY, model );
    }

    /**
     * Process the data capture form of a new entry
     *
     * @param request The Http Request
     * @return The Jsp URL of the process result
     * @throws AccessDeniedException
     */
    @Action( ACTION_CREATE_ENTRY )
    public String doCreateEntry( @Valid @ModelAttribute Entry entry, BindingResult bindingResult, HttpServletRequest request ) throws AccessDeniedException
    {
        if ( bindingResult.isFailed( ) )
        {
            model.put( MVCUtils.MARK_ERRORS, bindingResult.getAllErrors( ) );
            model.put( MARK_ENTRY, entry );
            return getPage( PROPERTY_PAGE_TITLE_CREATE_ENTRY, TEMPLATE_CREATE_ENTRY, model );
        }

        EntryHome.create( entry );

        addInfo( INFO_ENTRY_CREATED, getLocale() );
        return redirectView( request, VIEW_MANAGE_ENTRYS );
    }

    /**
     * Manages the removal form of a entry whose identifier is in the http
     * request
     *
     * @param request The Http request
     * @return the html code to confirm
     */
    @View( value = ACTION_CONFIRM_REMOVE_ENTRY, securityTokenAction = ACTION_REMOVE_ENTRY )
    public String getConfirmRemoveEntry( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_ENTRY ) );
        UrlItem url = new UrlItem( getActionUrl( ACTION_REMOVE_ENTRY ) );
        url.addParameter( PARAMETER_ID_ENTRY, nId );

        String strMessageUrl = AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_ENTRY, url.getUrl(  ), AdminMessage.TYPE_CONFIRMATION );

        return redirect( request, strMessageUrl );
    }

    /**
     * Handles the removal form of a entry
     *
     * @param request The Http request
     * @return the jsp URL to display the form to manage entrys
     */
    @Action( ACTION_REMOVE_ENTRY )
    public String doRemoveEntry( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_ENTRY ) );
        
        EntryHome.remove( nId );
        addInfo( INFO_ENTRY_REMOVED, getLocale(  ) );

        return redirectView( request, VIEW_MANAGE_ENTRYS );
    }

    /**
     * Returns the form to update info about a entry
     *
     * @param request The Http request
     * @return The HTML form to update info
     */
    @View( VIEW_MODIFY_ENTRY )
    public String getModifyEntry( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_ENTRY ) );

        if ( _entry == null || ( _entry.getId(  ) != nId ) )
        {
            Optional<Entry> optEntry = EntryHome.findByPrimaryKey( nId );
            _entry = optEntry.orElseThrow( ( ) -> new AppException(ERROR_RESOURCE_NOT_FOUND ) );
        }

        model.put( MARK_ENTRY, _entry );
        return getPage( PROPERTY_PAGE_TITLE_MODIFY_ENTRY, TEMPLATE_MODIFY_ENTRY, model );
    }

    /**
     * Process the change form of a entry
     *
     * @param request The Http request
     * @return The Jsp URL of the process result
     * @throws AccessDeniedException
     */
    @Action( ACTION_MODIFY_ENTRY )
    public String doModifyEntry(  @Valid @ModelAttribute Entry entry, BindingResult bindingResult, HttpServletRequest request ) throws AccessDeniedException
    {   
        if ( bindingResult.isFailed( ) )
        {
            model.put( MVCUtils.MARK_ERRORS, bindingResult.getAllErrors( ) );
            model.put( MARK_ENTRY, entry );
            return getPage( PROPERTY_PAGE_TITLE_CREATE_ENTRY, TEMPLATE_CREATE_ENTRY, model );
        }

        EntryHome.update( entry );
        addInfo( INFO_ENTRY_UPDATED, getLocale(  ) );

        return redirectView( request, VIEW_MANAGE_ENTRYS );
    }
    
    @Action( ACTION_PUBLISH_ENTRY )
    public String doPublishEntry( HttpServletRequest request )
    {
        if( TOSConstants.PROPERTY_FEATURE_TOS_PUBLICATION_ENABLED )
        {
            String strIdEntry = request.getParameter( PARAMETER_ID_ENTRY );
            
            if( StringUtils.isNumeric( strIdEntry ) )
            {
                EntryHome.unpublishAllEntries( );
                EntryHome.publishEntry( Integer.parseInt( strIdEntry ) );
            }
            
            addInfo( INFO_ENTRY_PUBLISHED, getLocale(  ) );
        }
        return redirectView( request, VIEW_MANAGE_ENTRYS );
    }

}
