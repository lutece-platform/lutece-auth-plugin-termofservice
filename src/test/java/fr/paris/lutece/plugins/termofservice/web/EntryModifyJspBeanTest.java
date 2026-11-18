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
import fr.paris.lutece.portal.service.security.SecurityTokenService;
import fr.paris.lutece.portal.util.mvc.binding.BindingErrorImpl;
import fr.paris.lutece.portal.util.mvc.binding.BindingResultImpl;
import fr.paris.lutece.test.AdminUserUtils;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.test.mocks.MockHttpServletRequest;
import fr.paris.lutece.test.mocks.MockHttpServletResponse;
import jakarta.inject.Inject;

/**
 * Checks that a modification refused by the validation shows the modification form again, not the creation form, and that
 * a modification keeps the publication of the entry.
 */
public class EntryModifyJspBeanTest extends LuteceTestCase
{
    private static final String RIGHT = "TERMOFSERVICE_MANAGEMENT";

    @Inject
    private EntryJspBean _instance;

    @Test
    public void testRefusedModificationShowsTheModificationForm( ) throws Exception
    {
        Entry entry = new Entry( );
        entry.setTitle( "modification" );
        entry.setText( "<p>modification</p>" );
        entry.setVersion( 1 );
        EntryHome.create( entry );
        try
        {
            // A refused title, given as a binding result of its own: the one of the request scope is shared by the tests
            BindingResultImpl bindingResult = new BindingResultImpl( );
            bindingResult.addBindingError( new BindingErrorImpl( "title", "#i18n{termofservice.validation.entry.Title.notEmpty}", "" ) );
            Entry entryPosted = new Entry( );
            entryPosted.setId( entry.getId( ) );
            entryPosted.setTitle( "" );
            entryPosted.setText( entry.getText( ) );
            MockHttpServletRequest request = new MockHttpServletRequest( );
            AdminUserUtils.registerAdminUserWithRight( request, new AdminUser( ), RIGHT );

            String strHtml = _instance.doModifyEntry( entryPosted, bindingResult, request );

            assertTrue( strHtml != null && strHtml.contains( "action_modifyEntry" ), "The modification form is expected" );
            assertFalse( strHtml.contains( "action_createEntry" ), "The creation form would create another entry" );
        }
        finally
        {
            EntryHome.remove( entry.getId( ) );
        }
    }

    @Test
    public void testModificationKeepsThePublication( ) throws Exception
    {
        Entry entry = new Entry( );
        entry.setTitle( "published" );
        entry.setText( "<p>published</p>" );
        entry.setVersion( 1 );
        EntryHome.create( entry );
        EntryHome.unpublishAllEntries( );
        EntryHome.publishEntry( entry.getId( ) );
        try
        {
            // The modification form has no publication field
            MockHttpServletRequest request = new MockHttpServletRequest( );
            request.setMethod( "POST" );
            request.addParameter( "action", "modifyEntry" );
            request.addParameter( "id", String.valueOf( entry.getId( ) ) );
            request.addParameter( "title", "published and modified" );
            request.addParameter( "text", "<p>modified</p>" );
            request.addParameter( "version", "2" );
            request.addParameter( "token", SecurityTokenService.getInstance( ).getToken( request, "modifyEntry" ) );
            AdminUserUtils.registerAdminUserWithRight( request, new AdminUser( ), RIGHT );

            _instance.processController( request, new MockHttpServletResponse( ) );

            Entry entryStored = EntryHome.findByPrimaryKey( entry.getId( ) ).orElseThrow( );
            assertEquals( "published and modified", entryStored.getTitle( ) );
            assertEquals( "<p>modified</p>", entryStored.getText( ) );
            assertEquals( 2, entryStored.getVersion( ) );
            assertTrue( entryStored.isPublished( ), "A modification must keep the entry published" );
        }
        finally
        {
            EntryHome.remove( entry.getId( ) );
        }
    }

    @Test
    public void testVersionNotANumberGivesAReadableMessage( ) throws Exception
    {
        // The conversion error the binder gives for the version "1.0"
        BindingResultImpl bindingResult = new BindingResultImpl( );
        bindingResult.addBindingError( new BindingErrorImpl( "version", "Error converting from 'String' to 'Integer' For input string: \"1.0\"", "1.0" ) );
        Entry entryPosted = new Entry( );
        entryPosted.setTitle( "version" );
        entryPosted.setText( "<p>version</p>" );
        MockHttpServletRequest request = new MockHttpServletRequest( );
        AdminUserUtils.registerAdminUserWithRight( request, new AdminUser( ), RIGHT );

        String strHtml = _instance.doCreateEntry( entryPosted, bindingResult, request );

        assertFalse( strHtml.contains( "Error converting" ), strHtml );
        // The message key, localized with the admin page
        assertTrue( strHtml.contains( "termofservice.validation.entry.Version.notNumber" ) || strHtml.contains( "whole number" ), strHtml );
    }
}
