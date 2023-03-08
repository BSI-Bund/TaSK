package com.secunet.ipsmall.rmi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;

/**
 * Helper methods for easier HttpResponse-information
 * typically accessed when evaluating browser simulator results.
 * 
 * @author kersten.benjamin
 *
 */
public class HttpResponseUtil {

	
	public static int getStatusCode(HttpResponse response){
		return response.getStatusLine().getStatusCode();
	}
	
	public static String getReasonPhrase(HttpResponse response){
		return response.getStatusLine().getReasonPhrase();
	}
	
	public static String getProtocolVersion(HttpResponse response){
		return response.getProtocolVersion().getProtocol();
	}
	
	public static HashMap<String, String> getHeaders(HttpResponse response){
		Header[] headers = response.getAllHeaders();
		HashMap<String, String> result = new HashMap<>(headers.length);
		for (Header header : headers) {
			result.put(header.getName(), header.getValue());
		}
		return result;
	}
	
	public static Locale getLocale(HttpResponse response){
		return response.getLocale();
	}
	
	public static String getBody(HttpResponse response){
		String body = null;
		HttpEntity entity = response.getEntity();
		if (entity != null) {
		    try {
				body = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return body;
	}
	
	public static String getContentType(HttpResponse response){
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			Header contentType = entity.getContentType();
			if( contentType != null ){
				return contentType.getValue();
			}
		}
		return null;
	}
	
	public static long getContentLength(HttpResponse response){
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			return entity.getContentLength();
		}
		return -1;
	}
	
}
