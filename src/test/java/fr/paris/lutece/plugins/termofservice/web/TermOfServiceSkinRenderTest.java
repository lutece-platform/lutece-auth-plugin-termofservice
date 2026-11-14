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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.termofservice.business.Entry;
import fr.paris.lutece.portal.service.editor.RichTextContentMethod;
import fr.paris.lutece.test.LuteceTestCase;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

/**
 * Renders the front office page of the terms of service (skin manage_entrys.html) with HTML auto-escaping (LUT-33153),
 * in its two layouts: the default theme and the theme that provides theme_parisfr_commons.html. The text of the terms
 * is rich text entered by an administrator, written with richText(): its formatting is kept and its scripts removed by
 * the sanitizer of the core, hence the CDI container of LuteceTestCase. The back URL is written as text.
 */
public class TermOfServiceSkinRenderTest extends LuteceTestCase
{
    private static final String TEMPLATES_ROOT = "webapp/WEB-INF/templates";
    private static final String TEMPLATE_PAGE = "skin/plugins/termofservice/manage_entrys.html";
    private static final String THEME_PARISFR = "skin/theme_parisfr_commons.html";
    /** Stand-ins of the core macros the page calls: the fields write their name and value, the containers their body. */
    private static final String MACROS = "termofservice_test_macros.ftl";
    private static final String MACROS_SOURCE = "<#macro box rest...><#nested></#macro><#macro boxHeader rest...></#macro>"
            + "<#macro boxBody rest...><#nested></#macro><#macro tform rest...><form><#nested></form></#macro>"
            + "<#macro messages rest...></#macro><#macro formGroup rest...><#nested></#macro><#macro checkBox rest...></#macro>"
            + "<#macro button id='' rest...><button id=\"${id}\"></button></#macro>"
            + "<#macro input name value='' rest...><input name=\"${name}\" value=\"${value}\"></#macro>"
            + "<#macro cContainer rest...><#nested></#macro><#macro cRow rest...><#nested></#macro><#macro cCol rest...><#nested></#macro>"
            + "<#macro cTitle rest...><#nested></#macro><#macro cSection class='' rest...><section class=\"${class}\"><#nested></section></#macro>"
            + "<#macro cForm rest...><form><#nested></form></#macro><#macro cFormRow rest...><#nested></#macro><#macro cCheckbox rest...></#macro>"
            + "<#macro cBtn rest...></#macro><#macro cInput name value='' rest...><input name=\"${name}\" value=\"${value}\"></#macro>";

    private static final String TITLE = "<h2>Article 1</h2>";
    private static final String LIST = "<ul><li>Compte</li><li>Profil</li></ul>";
    private static final String STYLED = "<p><strong>Usage</strong> &amp; <em>contact</em></p>";
    private static final String HOSTILE = "<script>alert(1)</script><p onclick=\"alert(2)\">Contact</p>";
    private static final String TEXT = TITLE + LIST + STYLED + HOSTILE;
    private static final String BACK_URL = "https://example.org/demarche?a=1&b=\"x\"<y>";
    private static final String BACK_URL_ESCAPED = "value=\"https://example.org/demarche?a=1&amp;b=&quot;x&quot;&lt;y&gt;\"";

    /**
     * The configuration of AbstractFreeMarkerTemplateService for HTML templates, with the richText() method of the core.
     *
     * @param bThemeParisFr
     *            true to provide theme_parisfr_commons.html, which selects the second layout of the page
     * @return the configuration
     * @throws IOException
     *             if the templates directory cannot be read
     */
    private static Configuration configuration( boolean bThemeParisFr ) throws IOException
    {
        StringTemplateLoader macros = new StringTemplateLoader( );
        macros.putTemplate( MACROS, MACROS_SOURCE );
        if ( bThemeParisFr )
        {
            macros.putTemplate( THEME_PARISFR, "" );
        }
        Configuration cfg = new Configuration( Configuration.VERSION_2_3_0 );
        cfg.setOutputFormat( HTMLOutputFormat.INSTANCE );
        cfg.setAutoEscapingPolicy( Configuration.ENABLE_IF_DEFAULT_AUTO_ESCAPING_POLICY );
        cfg.setTemplateLoader( new MultiTemplateLoader( new TemplateLoader [ ] {
                new FileTemplateLoader( new File( TEMPLATES_ROOT ) ), macros
        } ) );
        cfg.addAutoInclude( MACROS );
        cfg.setSharedVariable( "richText", new RichTextContentMethod( ) );

        return cfg;
    }

    /**
     * Renders the page for an entry and a back URL.
     *
     * @param bThemeParisFr
     *            true for the layout of theme_parisfr_commons.html
     * @return the rendering
     * @throws IOException
     *             if the template cannot be read
     * @throws TemplateException
     *             if the rendering fails
     */
    private static String render( boolean bThemeParisFr ) throws IOException, TemplateException
    {
        Entry entry = new Entry( );
        entry.setId( 1 );
        entry.setTitle( "CGU" );
        entry.setText( TEXT );
        entry.setVersion( 1 );
        Map<String, Object> model = new HashMap<>( );
        model.put( "entry", entry );
        model.put( "back_url", BACK_URL );
        model.put( "token", "token" );
        model.put( "errors", new ArrayList<>( ) );

        StringWriter writer = new StringWriter( );
        configuration( bThemeParisFr ).getTemplate( TEMPLATE_PAGE ).process( model, writer );

        return writer.toString( );
    }

    /**
     * Checks that the formatting of the text is kept, that its scripts are removed and that the back URL is text.
     *
     * @param strHtml
     *            the rendering
     */
    private static void assertTextKeptAndBackUrlEscaped( String strHtml )
    {
        for ( String strKept : new String [ ] {
                TITLE, LIST, STYLED, "<p>Contact</p>", BACK_URL_ESCAPED
        } )
        {
            assertTrue( strHtml.contains( strKept ), strKept + " expected in " + strHtml );
        }
        assertFalse( strHtml.contains( "<script>alert" ), strHtml );
        assertFalse( strHtml.contains( "alert(" ), strHtml );
        assertFalse( strHtml.contains( "onclick" ), strHtml );
        assertFalse( strHtml.contains( "&lt;h2" ), strHtml );
    }

    /**
     * The default layout keeps the rich text and writes the back URL as text.
     *
     * @throws Exception
     *             if the rendering fails
     */
    @Test
    public void testDefaultLayoutKeepsTheRichTextAndEscapesTheBackUrl( ) throws Exception
    {
        assertTextKeptAndBackUrlEscaped( render( false ) );
    }

    /**
     * The layout of theme_parisfr_commons.html keeps the rich text, in the scrolled section styled by the plugin CSS, and
     * writes the back URL as text.
     *
     * @throws Exception
     *             if the rendering fails
     */
    @Test
    public void testParisFrLayoutKeepsTheRichTextAndEscapesTheBackUrl( ) throws Exception
    {
        String strHtml = render( true );

        assertTextKeptAndBackUrlEscaped( strHtml );
        assertTrue( strHtml.contains( "class=\"py-3 overflow-auto termofservice-text\"" ), strHtml );
    }
}
