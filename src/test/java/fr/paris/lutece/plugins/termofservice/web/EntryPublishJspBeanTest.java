/*
 * Copyright (c) 2002-2026, City of Paris
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

import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.termofservice.business.Entry;
import fr.paris.lutece.plugins.termofservice.business.EntryHome;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.security.SecurityTokenHandler;
import fr.paris.lutece.portal.web.cdi.mvc.event.MvcEvent.ControllerInvocationType;
import fr.paris.lutece.test.AdminUserUtils;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.test.mocks.MockHttpServletRequest;
import fr.paris.lutece.test.mocks.MockHttpServletResponse;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Checks that the publication of a terms of service entry passes the admin security token filter: the list posts the
 * publication in a form, and the list view generates the security token of the publication, that the core adds to its forms.
 */
public class EntryPublishJspBeanTest extends LuteceTestCase
{
    private static final String SERVLET_PATH = "/jsp/admin/plugins/termofservice/ManageEntrys.jsp";
    private static final String RIGHT = "TERMOFSERVICE_MANAGEMENT";
    private static final String ACTION_PUBLISH = "publishEntry";

    @Inject
    private EntryJspBean _instance;
    @Inject
    private SecurityTokenHandler _securityTokenHandler;

    private static Entry createEntry( String strTitle )
    {
        Entry entry = new Entry( );
        entry.setTitle( strTitle );
        entry.setText( "<p>" + strTitle + "</p>" );
        entry.setVersion( 1 );
        EntryHome.create( entry );
        return entry;
    }

    @Test
    public void testPublishWithTheTokenOfTheList( ) throws Exception
    {
        Entry entry = createEntry( "publication" );
        try
        {
            MockHttpServletRequest request = new MockHttpServletRequest( );
            request.setMethod( "GET" );
            request.setServletPath( SERVLET_PATH );
            AdminUserUtils.registerAdminUserWithRight( request, new AdminUser( ), RIGHT );
            String strHtml = _instance.processController( request, new MockHttpServletResponse( ) );
            assertTrue( strHtml.contains( "name=\"action_" + ACTION_PUBLISH + "\"" ), "The list must post the publication" );
            assertFalse( strHtml.contains( "action=" + ACTION_PUBLISH ), "The publication must not be a link" );

            // The token the security token filter generates when the list is displayed
            _securityTokenHandler.handle( request );
            _securityTokenHandler.handleToken( request, ControllerInvocationType.DEFAULT_VIEW,
                    EntryJspBean.class.getMethod( "getManageEntrys", HttpServletRequest.class ) );
            String strToken = _securityTokenHandler.resolveTokenValue( request );
            assertTrue( strToken != null, "The list must generate the token of the publication" );

            // The publish button of the row, posted in the same session
            request.setMethod( "POST" );
            request.setParameter( "action", ACTION_PUBLISH );
            request.setParameter( "id", String.valueOf( entry.getId( ) ) );
            request.setParameter( SecurityTokenHandler.MARK_CSRF_TOKEN, strToken );
            assertFalse( _securityTokenHandler.shouldNotFilter( request ), "The publication must be checked by the security token filter" );
            assertTrue( _securityTokenHandler.validate( request, _securityTokenHandler.resolveTokenAction( request, ACTION_PUBLISH ) ) );

            _instance.processController( request, new MockHttpServletResponse( ) );
            assertEquals( entry.getId( ), EntryHome.findPublishedEntry( ).map( Entry::getId ).orElse( -1 ) );
        }
        finally
        {
            EntryHome.remove( entry.getId( ) );
        }
    }
}
