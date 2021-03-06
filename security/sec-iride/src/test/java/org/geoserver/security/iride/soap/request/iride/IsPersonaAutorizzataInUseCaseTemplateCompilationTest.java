/*
 *  GeoServer Security Provider plugin with which doing authentication and authorization operations using CSI-Piemonte IRIDE Service.
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
package org.geoserver.security.iride.soap.request.iride;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import it.csi.iride2.policy.entity.UseCase;

import java.io.IOException;
import java.util.Map;

import org.geoserver.security.iride.identity.IrideIdentity;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * <code>IRIDE</code> service <code>SOAP</code> <em>isPersonaAutorizzataInUseCase</em> request template compilation (using <a href="http://freemarker.org/">FreeMarker</a>) <code>JUnit</code>.
 *
 * @author "Simone Cornacchia - seancrow76@gmail.com, simone.cornacchia@consulenti.csi.it (CSI:71740)"
 */
public final class IsPersonaAutorizzataInUseCaseTemplateCompilationTest extends AbstractIrideSoapRequestTemplateCompilationTest {

    /**
     * Test method for {@link Template#process(Object, java.io.Writer)}.
     */
    @Test
    public void test() throws TemplateException, IOException {
        final String result = this.processTemplate();

        assertThat(result, not(isEmptyOrNullString()));

        final IrideIdentity irideIdentity = (IrideIdentity) this.getDataModel().get("irideIdentity");

        assertThat(result, hasXPath("/soapenv:Envelope/soapenv:Body/int:isPersonaAutorizzataInUseCase/in0/codFiscale", equalTo(irideIdentity.getCodFiscale())));
        assertThat(result, hasXPath("/soapenv:Envelope/soapenv:Body/int:isPersonaAutorizzataInUseCase/in0/nome", equalTo(irideIdentity.getNome())));
        assertThat(result, hasXPath("/soapenv:Envelope/soapenv:Body/int:isPersonaAutorizzataInUseCase/in0/cognome", equalTo(irideIdentity.getCognome())));
        assertThat(result, hasXPath("/soapenv:Envelope/soapenv:Body/int:isPersonaAutorizzataInUseCase/in0/idProvider", equalTo(irideIdentity.getIdProvider())));
        assertThat(result, hasXPath("/soapenv:Envelope/soapenv:Body/int:isPersonaAutorizzataInUseCase/in0/timestamp", equalTo(irideIdentity.getTimestamp())));
        assertThat(result, hasXPath("/soapenv:Envelope/soapenv:Body/int:isPersonaAutorizzataInUseCase/in0/livelloAutenticazione", equalTo(String.valueOf(irideIdentity.getLivelloAutenticazione()))));
        assertThat(result, hasXPath("/soapenv:Envelope/soapenv:Body/int:isPersonaAutorizzataInUseCase/in0/mac", equalTo(irideIdentity.getMac())));
        assertThat(result, hasXPath("/soapenv:Envelope/soapenv:Body/int:isPersonaAutorizzataInUseCase/in0/rappresentazioneInterna", equalTo(irideIdentity.toInternalRepresentation())));

        final UseCase useCase = (UseCase) this.getDataModel().get("useCase");

        assertThat(result, hasXPath("/soapenv:Envelope/soapenv:Body/int:isPersonaAutorizzataInUseCase/in1/appId/id", equalTo(useCase.getAppId().getId())));
        assertThat(result, hasXPath("/soapenv:Envelope/soapenv:Body/int:isPersonaAutorizzataInUseCase/in1/id", equalTo(useCase.getId())));
    }

    /*
     * (non-Javadoc)
     * @see org.geoserver.security.iride.soap.request.iride.AbstractIrideSoapRequestTemplateCompilationTest#setDataModel(java.util.Map)
     */
    @Override
    @Value("#{irideIdentityAndUseCase}")
    protected void setDataModel(Map<String, Object> dataModel) {
        super.setDataModel(dataModel);
    }

    /*
     * (non-Javadoc)
     * @see org.geoserver.security.iride.soap.request.iride.AbstractIrideSoapRequestTemplateCompilationTest#setTemplateName(java.lang.String)
     */
    @Override
    @Value("#{'isPersonaAutorizzataInUseCase'}")
    protected void setTemplateName(String templateName) {
        super.setTemplateName(templateName);
    }

}
