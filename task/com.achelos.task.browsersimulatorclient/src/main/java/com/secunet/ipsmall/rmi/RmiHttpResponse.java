package com.secunet.ipsmall.rmi;

import static com.secunet.ipsmall.rmi.HttpResponseUtil.getBody;
import static com.secunet.ipsmall.rmi.HttpResponseUtil.getContentLength;
import static com.secunet.ipsmall.rmi.HttpResponseUtil.getContentType;
import static com.secunet.ipsmall.rmi.HttpResponseUtil.getHeaders;
import static com.secunet.ipsmall.rmi.HttpResponseUtil.getLocale;
import static com.secunet.ipsmall.rmi.HttpResponseUtil.getProtocolVersion;
import static com.secunet.ipsmall.rmi.HttpResponseUtil.getReasonPhrase;
import static com.secunet.ipsmall.rmi.HttpResponseUtil.getStatusCode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpResponse;


/**
 * Simplified http response class which is serializable and transferable via RMI
 * so that evaluation of http response can be done by testbed instead of by
 * BrowserSimulator.
 * 
 * @author kersten.benjamin
 * 
 */
public class RmiHttpResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public int statusCode;
    public String body;
    public HashMap<String, String> headers;
    public String reasonPhrase;
    public String protocolVersion;
    public String contentType;
    public long contentLength;
    public Locale locale;
    
    public RmiHttpResponse(HttpResponse response) {       
        statusCode = getStatusCode(response);
        body = getBody(response);
        headers = getHeaders(response);
        reasonPhrase = getReasonPhrase(response);
        protocolVersion = getProtocolVersion(response);
        contentType = getContentType(response);
        contentLength = getContentLength(response);
        locale = getLocale(response);
    }
    
    public RmiHttpResponse() {
        
    }
    
    public void log() {
//        Logger.BrowserSim.logState("BrowserSimulator: Received HttpResponse");
        System.out.println("BrowserSimulator: Received HttpResponse");
        
        String msg = "statusCode: " + statusCode + System.lineSeparator();
        msg += "body: " + body + System.lineSeparator();
        msg += "reasonPhrase: " + reasonPhrase + System.lineSeparator();
        msg += "protocolVersion: " + protocolVersion + System.lineSeparator();
        msg += "contentType: " + contentType + System.lineSeparator();
        msg += "contentLength: " + contentLength + System.lineSeparator();
        msg += "locale: " + locale.toString() + System.lineSeparator();
        msg += "Headers {" + System.lineSeparator();
        Set<Entry<String, String>> keys = headers.entrySet();
        for (Entry<String, String> entry : keys) {
            msg += "   " + entry.getKey() + ": " + entry.getValue() + System.lineSeparator();
        }
        msg += "}" + System.lineSeparator();
        
//        Logger.BrowserSim.logProtocol(Protocols.HTTP.toString(), ProtocolDirection.received, "eID-Client", "BrowserSimulator", msg);
        System.out.println("HTTP message from eID-Client: "  + msg);
    }
}
