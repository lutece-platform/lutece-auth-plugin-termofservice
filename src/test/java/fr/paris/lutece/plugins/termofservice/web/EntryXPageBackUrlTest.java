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

import java.lang.reflect.Proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.termofservice.business.Entry;
import fr.paris.lutece.plugins.termofservice.business.EntryHome;
import fr.paris.lutece.plugins.termofservice.business.UserAcceptedHome;
import fr.paris.lutece.portal.service.security.LuteceAuthentication;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityTokenService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.web.LocalVariables;
import fr.paris.lutece.portal.web.xpages.XPage;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.test.mocks.MockHttpServletRequest;
import fr.paris.lutece.test.mocks.MockHttpServletResponse;
import jakarta.enterprise.inject.spi.CDI;

/**
 * The back URL of the front office page of the terms of service is read through the AuthorizedUrlService of
 * plugin-verifybackurl, which checks it against the authorized domains of verifybackurl.properties: once the terms are
 * accepted, the page redirects to an authorized back URL only, and to the site otherwise.
 */
public class EntryXPageBackUrlTest extends LuteceTestCase
{
    private static final String USER_NAME = "tos-back-url-user";
    private static final String ACTION_MODIFY_TOS = "modifyEntry";
    /** Authorized by the default pattern http*://*.paris.mdp/** of verifybackurl.properties */
    private static final String AUTHORIZED_BACK_URL = "https://site.paris.mdp/demarche?step=2";
    /** Outside the authorized domains of verifybackurl.properties */
    private static final String OTHER_DOMAIN_BACK_URL = "https://example.org/demarche";

    private Entry _entry;

    /**
     * Response that keeps the target of the redirect.
     */
    private static final class RedirectResponse extends MockHttpServletResponse
    {
        private String _strRedirect;

        @Override
        public void sendRedirect( String strLocation )
        {
            _strRedirect = strLocation;
        }
    }

    /**
     * Front office user registered in the session, from an authentication service that only gives its name.
     */
    private static final class TestUser extends LuteceUser
    {
        private static final long serialVersionUID = 1L;

        TestUser( )
        {
            super( USER_NAME, (LuteceAuthentication) Proxy.newProxyInstance( LuteceAuthentication.class.getClassLoader( ),
                    new Class<?> [ ] {
                            LuteceAuthentication.class
                    }, ( proxy, method, args ) -> "getAuthServiceName".equals( method.getName( ) ) ? "test" : null ) );
        }
    }

    @AfterEach
    public void cleanUp( )
    {
        UserAcceptedHome.findByGuid( USER_NAME ).ifPresent( userAccepted -> UserAcceptedHome.remove( userAccepted.getId( ) ) );
        if ( _entry != null )
        {
            EntryHome.remove( _entry.getId( ) );
        }
        LocalVariables.setLocal( null, null, null );
    }

    /**
     * Accepts the terms with a back URL and returns the target of the redirect.
     *
     * @param strBackUrl
     *            the back URL posted with the form
     * @param request
     *            the request
     * @return the target of the redirect
     * @throws Exception
     *             if the action fails
     */
    private String acceptWithBackUrl( String strBackUrl, MockHttpServletRequest request ) throws Exception
    {
        _entry = new Entry( );
        _entry.setTitle( "CGU" );
        _entry.setText( "<p>CGU</p>" );
        _entry.setVersion( 1 );
        EntryHome.create( _entry );

        RedirectResponse response = new RedirectResponse( );
        LocalVariables.setLocal( null, request, response );
        request.setMethod( "POST" );
        request.getSession( true ).setAttribute( "lutece_user", new TestUser( ) );
        request.setParameter( "id", String.valueOf( _entry.getId( ) ) );
        request.setParameter( "accepted", "1" );
        request.setParameter( "back_url", strBackUrl );
        request.setParameter( "token", SecurityTokenService.getInstance( ).getToken( request, ACTION_MODIFY_TOS ) );

        XPage xpage = CDI.current( ).select( EntryXPage.class ).get( ).doModifyTOS( request );

        assertTrue( xpage.isSendRedirect( ), "The acceptance must redirect" );
        assertTrue( UserAcceptedHome.findByGuid( USER_NAME ).isPresent( ), "The acceptance must be recorded" );
        return response._strRedirect;
    }

    /**
     * An authorized back URL is followed once the terms are accepted.
     *
     * @throws Exception
     *             if the action fails
     */
    @Test
    public void testAcceptRedirectsToAnAuthorizedBackUrl( ) throws Exception
    {
        String strRedirect = acceptWithBackUrl( AUTHORIZED_BACK_URL, new MockHttpServletRequest( ) );

        assertTrue( strRedirect.startsWith( AUTHORIZED_BACK_URL ), strRedirect );
    }

    /**
     * A back URL outside the authorized domains is not followed: the page redirects to the site.
     *
     * @throws Exception
     *             if the action fails
     */
    @Test
    public void testAcceptRedirectsToTheSiteForABackUrlOutsideTheAuthorizedDomains( ) throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest( );
        String strRedirect = acceptWithBackUrl( OTHER_DOMAIN_BACK_URL, request );

        // The redirect of the MVC layer may add the parameter of its redirect scope to the URL of the site
        assertFalse( strRedirect.contains( "example.org" ), strRedirect );
        assertTrue( strRedirect.startsWith( AppPathService.getBaseUrl( request ) ), strRedirect );
    }
}
