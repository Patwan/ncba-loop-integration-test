package org.example.crudservice.components;

import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import lombok.extern.slf4j.Slf4j;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Set;

@Slf4j
public class SoapLogsHandler implements SOAPHandler<SOAPMessageContext> {

    @Override
    public Set<QName> getHeaders() {
        return Collections.emptySet();
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        try {
            SOAPMessage msg = context.getMessage();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            msg.writeTo(out);
            String xml = out.toString();
            if (Boolean.TRUE.equals(outbound)) {
                log.debug("SOAP REQUEST >>>\n{}", xml);
            } else {
                log.debug("SOAP RESPONSE <<<\n{}", xml);
            }
        } catch (Exception e) {
            log.warn("Failed to log SOAP message: {}", e.getMessage());
        }
        return true;   // continue processing
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return handleMessage(context);
    }

    @Override
    public void close(MessageContext context) {
        // no-op
    }
}