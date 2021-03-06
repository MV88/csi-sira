/*
 *  GeoServer Security Provider plugin used for doing authentication and authorization operations using CSI-Piemonte IRIDE Service.
 *  Copyright (C) 2016  Regione Piemonte (www.regione.piemonte.it)
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.geoserver.security.iride;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.io.IOUtils;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.security.impl.RoleCalculator;
import org.geoserver.security.iride.config.IrideSecurityServiceConfig;
import org.geoserver.security.iride.util.factory.roleservice.IrideRoleServiceFactory;
import org.geoserver.security.iride.util.factory.template.freemarker.FreeMarkerConfigurationDefaultFactory;
import org.geoserver.security.iride.util.factory.template.freemarker.FreeMarkerTemplateEngineFactory;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 *
 * @author "Mauro Bartolomeoli - mauro.bartolomeoli@geo-solutions.it"
 * @author "Simone Cornacchia - seancrow76@gmail.com, simone.cornacchia@consulenti.csi.it (CSI:71740)"
 */
public class IrideRoleServiceTest extends TestCase {

    private static final String BASE_SAMPLE_USER = "AAAAAA00A11D000L/DEMO 23/CSI PIEMONTE/CSI_NUOVACA/20131112095654/8/52paOWJH3ukdZkuV0A1ffg==";
    private static final String MAJOR_SAMPLE_USER = "AAAAAA00A11F000N/DEMO 25/CSI PIEMONTE/CSI_NUOVACA/20131112095654/8/52paOWJH3ukdZkuV0A1ffg==";
    private static final String SUPER_SAMPLE_USER = "AAAAAA00A11E000M/DEMO 24/CSI PIEMONTE/CSI_NUOVACA/20131112095654/8/52paOWJH3ukdZkuV0A1ffg==";
    private static final String USER_WITH_EXTRA_PARTS = "AAAAAA00A11D000L/DEMO 23/CSI PIEMONTE/CSI_NUOVACA/20131112095654/8/52paOWJH3/kdZkuV0A1ffg==";

    File tempFolder;
    GeoServerSecurityManager securityManager;
    IrideRoleServiceFactory irideRoleServiceFactory;
    FreeMarkerTemplateEngineFactory freeMarkerTemplateEngineFactory;
    IrideSecurityProvider securityProvider;
    IrideSecurityServiceConfig config;

    Pattern lookForMac = Pattern.compile("<mac[^>]*?>\\s*(.*?)\\s*<\\/mac>", Pattern.CASE_INSENSITIVE);
    Pattern lookForInterna = Pattern.compile("<rappresentazioneInterna[^>]*?>\\s*(.*?)\\s*<\\/rappresentazioneInterna>", Pattern.CASE_INSENSITIVE);

    @Override
    public void setUp() throws Exception {
        tempFolder = File.createTempFile("ldap", "test");
        tempFolder.delete();
        tempFolder.mkdirs();
        GeoServerResourceLoader resourceLoader = new GeoServerResourceLoader(tempFolder);
        securityManager = new GeoServerSecurityManager(new GeoServerDataDirectory(resourceLoader));
        irideRoleServiceFactory = new IrideRoleServiceFactory(
        	FreeMarkerTemplateEngineFactory.createTemplateEngine(
        		FreeMarkerConfigurationDefaultFactory.createConfiguration(),
        		"xml"
        	)
        );
        securityProvider = new IrideSecurityProvider(securityManager, irideRoleServiceFactory);
        config = new IrideSecurityServiceConfig();
        config.setApplicationName("SIIG");
        config.setAdminRole("SUPERUSER_SIIG");
        config.setClassName(IrideRoleService.class.getName());
        config.setName("iride");
        config.setServerURL("http://localhost:8085/iride2simApplIridepepWsfad/services/iride2simIridepep");
    }

    @Override
    public void tearDown() throws Exception {
        tempFolder.delete();
    }

    public void testGetRolesForBaseUserWithInvalidServerURL() throws IOException {
    	config.setServerURL(null);
    	try {
    		IrideRoleService roleService = wrapRoleService(createRoleService(), "base");
			roleService.getRolesForUser(BASE_SAMPLE_USER);
		} catch (IllegalArgumentException e) {
			assertEquals(e.getMessage(), "'null' is not a valid IRIDE server URL ");
		}
    }

    public void testGetRolesForBaseUser() throws IOException {
        //config.setServerURL("http://localhost:8085/iride2simApplIridepepWsfad/services/iride2simIridepep");
        IrideRoleService roleService = wrapRoleService(createRoleService(), "base");
        SortedSet<GeoServerRole> roles = roleService.getRolesForUser(BASE_SAMPLE_USER);
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("BASEUSER_SIIG", roles.iterator().next().toString());
    }

    /**
     * @param string
     * @return
     * @throws IOException
     */
    private IrideRoleService createRoleService() throws IOException {
        IrideRoleService roleService = (IrideRoleService) securityProvider.createRoleService(config);
        roleService.setHttpClient(new HttpClient() {
            @Override
            public int executeMethod(HttpMethod method) throws IOException,
                    HttpException {
                return 200;
            }

        });
        return roleService;
    }

    public void testGetRolesForMajorUser() throws IOException {
        IrideRoleService roleService = wrapRoleService(createRoleService(), "major");
        SortedSet<GeoServerRole> roles = roleService.getRolesForUser(MAJOR_SAMPLE_USER);
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("MAJORUSER_SIIG", roles.iterator().next().toString());
    }

    public void testGetRolesForSuperUser() throws IOException {
        IrideRoleService roleService = wrapRoleService(createRoleService(), "super");
        SortedSet<GeoServerRole> roles = roleService.getRolesForUser(SUPER_SAMPLE_USER);
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("SUPERUSER_SIIG", roles.iterator().next().toString());
        //assertEquals(GeoServerRole.ADMIN_ROLE, roles.iterator().next());

        RoleCalculator roleCalc = new RoleCalculator(roleService);
        roles = roleCalc.calculateRoles(SUPER_SAMPLE_USER);
        assertNotNull(roles);
        assertEquals(3, roles.size());
        boolean foundAdmin = false;
        for(GeoServerRole role : roles) {
            if(role.equals(GeoServerRole.ADMIN_ROLE)) {
                foundAdmin = true;
            }
        }
        assertTrue(foundAdmin);
    }

    public void testExtraPartsInUserName() throws IOException {
        IrideRoleService roleService = wrapRoleService(createRoleService(), "major");

        SortedSet<GeoServerRole> roles = roleService.getRolesForUser(USER_WITH_EXTRA_PARTS);
        assertNotNull(roles);
        Matcher m = lookForMac.matcher(roleService.getHttpClient().getState().toString());
        assertTrue(m.find());
        assertEquals("52paOWJH3/kdZkuV0A1ffg==", m.group(1));
        m = lookForInterna.matcher(roleService.getHttpClient().getState().toString());
        assertTrue(m.find());
        assertEquals("AAAAAA00A11D000L/DEMO 23/CSI PIEMONTE/CSI_NUOVACA/20131112095654/8/52paOWJH3/kdZkuV0A1ffg==", m.group(1));
    }

    /**
     * @param createRoleService
     * @return
     * @throws UnsupportedEncodingException
     */
    private IrideRoleService wrapRoleService(final IrideRoleService roleService, final String roleName) throws UnsupportedEncodingException {
        IrideRoleService wrapped = spy(roleService);
        when(wrapped.createHttpMethod(anyString())).thenAnswer(new Answer<HttpMethod>() {

            @Override
            public HttpMethod answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                final String requestXml = args[0].toString();
                IrideRoleService mock = (IrideRoleService) invocation.getMock();
                mock.getHttpClient().setState(new HttpState() {
                    @Override
                    public synchronized String toString() {
                        return requestXml;
                    }

                });
                return new HttpMethodBase() {

                    @Override
                    public String getName() {
                        return "FileMethod";
                    }

                    @Override
                    public String getResponseBodyAsString() throws IOException {
                        InputStream in = getClass().getResource("/" + roleName + ".xml").openStream();

                        try {
                          return IOUtils.toString(in);
                        } finally {
                          IOUtils.closeQuietly(in);
                        }
                    }

                };
            }

        });
        return wrapped;
    }

}
