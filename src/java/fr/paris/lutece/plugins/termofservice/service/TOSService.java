/*
 * Copyright (c) 2002-2025, City of Paris
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
package fr.paris.lutece.plugins.termofservice.service;

import java.util.Optional;

import fr.paris.lutece.plugins.termofservice.business.Entry;
import fr.paris.lutece.plugins.termofservice.business.EntryHome;
import fr.paris.lutece.plugins.termofservice.business.UserAccepted;
import fr.paris.lutece.plugins.termofservice.business.UserAcceptedHome;
import fr.paris.lutece.plugins.termofservice.rs.Constants;
import fr.paris.lutece.plugins.termofservice.util.TOSConstants;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

/**
 * 
 * TOSService
 *
 */
public class TOSService
{    
    /**
     * Private constructor
     */
    private TOSService()
    {
        //Do nothing
    }
    
    /**
     * Retrieves the Terms of Service (TOS) accepted by the specified user.
     * <p>
     * If the TOS publication feature is enabled, this method returns the acceptance record
     * associated with the currently published TOS.
     * Otherwise, it returns the user's general TOS acceptance record.
     *
     * @param strGuid the unique identifier of the user
     * @return an {@link Optional} containing the {@link UserAccepted} object if the user has accepted the TOS,
     *         or an empty {@link Optional} if no record is found
     */
    public static Optional<UserAccepted> getUserAcceptedTOS ( String strGuid )
    {
        if( TOSConstants.PROPERTY_FEATURE_TOS_PUBLICATION_ENABLED )
        {
            return getUserAcceptedPublishedTOS( strGuid );
        } else
        {
            return UserAcceptedHome.findByGuid( strGuid,AppPropertiesService.getPropertyBoolean(Constants.PROPERTY_USED_REMOTE, false) );
        }        
    }
    
    /**
     * Retrieves the Terms of Service (TOS) accepted by the specified user when the TOS publication feature is enabled.
     * <p>
     * This method looks for the currently published TOS entry and returns the record of acceptance
     * corresponding to that published version, if it exists.
     *
     * @param strGuid the guid
     * @return an {@link Optional} containing the {@link UserAccepted} object if the user has accepted
     *         the published TOS, or an empty {@link Optional} if no record is found
     */
    public static Optional<UserAccepted> getUserAcceptedPublishedTOS ( String strGuid )
    {
        Optional<Entry> publishedEntry = EntryHome.findPublishedEntry( );
        
        Optional<UserAccepted> userAccepted = Optional.empty( );
        if( publishedEntry.isPresent( ) )
        {
            userAccepted = UserAcceptedHome
                    .findByGuidAndEntryAndVersion( strGuid, publishedEntry.get( ).getId( ), publishedEntry.get( ).getVersion( ) );
            
        }
        
        return userAccepted;        
    }
    
    /**
     * Retrieves the current Terms of Service (TOS) entry.
     * <p>
     * If the TOS publication feature is enabled, this method returns the currently published entry.
     * Otherwise, it returns the last available version.
     *
     * @return an {@link Optional} containing the current {@link Entry} if found, or an empty {@link Optional} otherwise
     */
    public static Optional<Entry> getCurrentEntry ( )
    {
        if( TOSConstants.PROPERTY_FEATURE_TOS_PUBLICATION_ENABLED )
        {
            return EntryHome.findPublishedEntry( ); 
        } else
        {
            return EntryHome.findByLastVersion( );
        }
    }
}
