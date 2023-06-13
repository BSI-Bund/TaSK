package com.secunet.ipsmall.browser.simulator;

import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import com.secunet.ipsmall.rmi.IBrowserSimulator;
import com.secunet.ipsmall.rmi.RmiHttpResponse;

/**
 * Handler to send http request, usually triggered by RmiHandler.
 * 
 * @author kersten.benjamin
 * 
 */
public class HttpHandler {
    
    HttpClient defaultHttpClient;
    
    public HttpHandler() {
        defaultHttpClient = new DefaultHttpClient();
        
        // global User-Agent param did not work
        // defaultHttpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
        // IBrowserSimulator.HEADER_VALUE_USER_AGENT);
    }
    
    /**
     * Send a request
     * 
     * @param url
     *            - the url to request
     * @param responseHandler
     *            a responseHandler to process expected results or null if not
     *            required, depends on usecase
     * @param trustedCerts
     *            X509 certs the browsersimulator trusts. May be null.
     * @throws IOException
     * @throws ClientProtocolException
     */
    public RmiHttpResponse sendRequest(String url, final X509Certificate[] trustedCerts, boolean followRedirects)
            throws Exception {
        
        // log incoming request
        log("");
        log("");
        log("---------- REQUEST -----------");
        log("URL: " + url);
        
        try {
            
            // usage of apache-http vs plain java vs sockets?
            RmiHttpResponse response;
            
            if (trustedCerts != null) {
                response = viaApacheHttpSSL(url, trustedCerts, followRedirects);
            } else {
                response = viaApacheHttpDefault(url, followRedirects);
            }
            // plain java URL currently not used -> does not handle HTTP
            // StatusCode 102 (WebDAV processing)
            // response = viaJava(url);
            
            // delay for a second
            log("");
            log("");
            log("---------- DELAY 1 SECOND -----------");
            Thread.sleep(1000);
            
            // log response to show information on browser simulator side...
            log("");
            log("");
            log("---------- RESPONSE -----------");
            log("for request URL: " + url);
            log("status code: " + response.statusCode);
            
            // ... and pass through to testbed
            return response;
            
        } catch (Exception e) {
            // log exception to show information on browser simulator side...
            log("");
            log("");
            log("---------- EXCEPTION -----------");
            log("for request URL: " + url);
            log("Type: " + e.getClass().getName());
            log("Message: " + e.getMessage());
            // ... and still throw to pass through to testbed
            throw (e);
        }
        
    }
    
    private RmiHttpResponse viaApacheHttpDefault(String url, boolean followRedirects) throws Exception {
        
        HttpGet request = new HttpGet(url);
        
        // by default, hc-apache client follows redirect, which is usually not
        // desired for ecard-testbed-scenario, but we want to be notified on
        // redirects instead
        HttpParams params = new BasicHttpParams();
        if (!followRedirects) {
            // disable redirects
            params.setParameter("http.protocol.handle-redirects", false);
            log("Redirects: do not follow");
        } else {
            log("Redirects: follow");
        }
        // User-Agent does not work yet
        params.setParameter(HTTP.USER_AGENT, IBrowserSimulator.HEADER_VALUE_USER_AGENT);
        request.setParams(params);
        
        HttpResponse response = defaultHttpClient.execute(request);
        // create rmiResponse before closing the socket
        RmiHttpResponse rmiResponse = new RmiHttpResponse(response);
        // shutdown vs pooled?
        defaultHttpClient.getConnectionManager().shutdown();
        return rmiResponse;
    }
    
    /**
     * Try to get a response for the passed URL with hc-apache and some SSL for
     * certificates that the browsersimulator should trust
     * 
     * @param url
     * @param trustedCerts
     * @return
     * @throws Exception
     */
    private RmiHttpResponse viaApacheHttpSSL(String url, final X509Certificate[] trustedCerts, boolean followRedirects)
            throws Exception {
        // TODO SSL Cert, see hc-apache ClientCustomSSL
        TrustStrategy trustStrategy = new TrustStrategy() {
            
            @Override
            public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                for (X509Certificate cert : chain) {
                    for (X509Certificate trustedCert : trustedCerts) {
                        if (cert.equals(trustedCert)) {
                            log("Trusting X509Certificate:");
                            log(cert);
                            return true;
                        }
                    }
                }
                return false;
            }
        };
        
        KeyStore keystore = null;
        SSLSocketFactory sslsf = new SSLSocketFactory("TLS", null, null, keystore, null, trustStrategy,
                new AllowAllHostnameVerifier());
        Scheme https = new Scheme("https", 443, sslsf);
        defaultHttpClient.getConnectionManager().getSchemeRegistry().register(https);
        
        // ssl is now configured for defaultHttpClient, now delegate to
        // default method (which would also be executed for usual http)
        return viaApacheHttpDefault(url, followRedirects);
    }
    
    // /**
    // * Try to get a response for the passed URL with plain Java-URL.
    // * Currently not used. AA uses StatusCode 102 (WebDAV processing, see
    // * RFC2518), which is not automatically handled with the implementation
    // * below.
    // *
    // * @param url
    // * @return
    // * @throws Exception
    // */
    // private RmiHttpResponse viaJava(String url) throws Exception {
    // URL obj = new URL(url);
    // URLConnection conn = obj.openConnection();
    //
    // //get all headers
    // Map<String, List<String>> map = conn.getHeaderFields();
    // for (Map.Entry<String, List<String>> entry : map.entrySet()) {
    // log("Key : " + entry.getKey() + " ,Value : " + entry.getValue());
    // }
    //
    // //get header by 'key'
    // /*String server =*/ conn.getHeaderField("Server");
    //
    // RmiHttpResponse r = new RmiHttpResponse();
    // //r.responseHeaders = map; // convert
    // return r;
    // }
    
    /*
    public interface IHttpResponseHandler {
    	
    	public void handleResponse(HttpResponse response);
    	public void handleException(Exception e);
    	
    }
    */
    
    private void log(String s) {
        // anything else than System.out? => change here
        System.out.println(s);
    }
    
    private void log(X509Certificate cert) {
        System.out.println(cert);
    }
    
}
