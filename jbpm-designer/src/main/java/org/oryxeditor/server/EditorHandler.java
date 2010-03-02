/***************************************
 * Copyright (c) 2008
 * Philipp Berger 2009
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
****************************************/

package org.oryxeditor.server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditorHandler extends HttpServlet {

    private static final Logger _logger = LoggerFactory.getLogger(EditorHandler.class);
	/**
	 * 
	 */
	private static final String oryx_path = "/designer/";
	private static final String defaultSS="stencilsets/bpmn2.0/bpmn2.0.json";
	private static final long serialVersionUID = 1L;
	private Collection<String> availableProfiles;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		availableProfiles=getAvailableProfileNames();
//		if(availableProfiles.size()==0)
//			 defaultHandlerBehaviour();
		String uuid = request.getParameter("uuid");
		String[] urlSplitted=request.getRequestURI().split(";");
		ArrayList<String> profiles= new ArrayList<String>();
		if (urlSplitted.length>1){
			for(int i=1;i<urlSplitted.length;i++){
				profiles.add(urlSplitted[i]);
			}
		}else{
			profiles.add("default");
		}
		if(!availableProfiles.containsAll(profiles)){
			//Some profiles not available
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Profile not found!");
			profiles.retainAll(availableProfiles);
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		String sset=null;
		JSONObject conf= new JSONObject();
		InputStream fileStream = null;
		try {
		    ByteArrayOutputStream output = new ByteArrayOutputStream();
		    fileStream = new FileInputStream(this.getServletContext().
                    getRealPath("/profiles") + File.separator + profiles.get(0)
                    + ".conf");
            byte[] buffer = new byte[4096];
            int read;
            while ((read = fileStream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
			conf = new JSONObject(output.toString());
		} catch (JSONException e) {
			_logger.error(e.getMessage(), e);
		} finally {
		    if (fileStream != null) { try { fileStream.close(); } catch(IOException e) {}};
		}
		sset=conf.optString("stencilset");
		if(sset==null || "".equals(sset))
			sset=defaultSS;
		String extString="";
		JSONArray exts= conf.optJSONArray("stencilsetextension");
		if(exts==null)
			exts=new JSONArray();
		extString=exts.toString();
		String content = 
	        "<script type='text/javascript'>" +
	        "  if(!ORYX) var ORYX = {};\n" +
	        "  if(!ORYX.CONFIG) ORYX.CONFIG = {};\n" +
	        "  ORYX.CONFIG.PLUGINS_CONFIG = ORYX.CONFIG.PROFILE_PATH + '"+profiles.get(0)+".xml';\n" +
	        "  ORYX.CONFIG.PROFILE_CONFIG = ORYX.CONFIG.PROFILE_PATH + '"+profiles.get(0)+".conf';\n" +
	        "  ORYX.CONFIG.PROFILE_NAME = '"+profiles.get(0)+"';\n" +
	        "  ORYX.CONFIG.SSET='"+sset+"';\n"+ // sets the default stencil set depending on profile
	        "  ORYX.CONFIG.SSEXTS="+extString+";\n"+
	        "  if ('function' != typeof(onOryxResourcesLoaded)) {\n" +
	        "		window.onOryxResourcesLoaded = function() {" +
	        "			if (!(location.hash.slice(1).length == 0 || location.hash.slice(1).indexOf('new')!=-1)) {" +
	        "				Ext.Msg.alert(ORYX.I18N.Oryx.title, ORYX.I18N.Oryx.noBackendDefined);\n" +
	        "			}" +
	        "			var stencilset = ORYX.Utils.getParamFromUrl('stencilset') || ORYX.CONFIG.SSET;" +
	        "           var uuid =  ORYX.Utils.getParamFromUrl('uuid');" + 
	        "           var params = {" +
            "               id: 'oryx-canvas123'," +
            "               stencilset: {" +
            "                   url: \"" + oryx_path + "\" + stencilset" +
            "               }" +
            "           };" +
            "           if (uuid) {" +
            "               params.uuid = uuid;" +
            "           };" +
	        "			var editor = new ORYX.Editor(params);" +
	        "           ORYX.EDITOR= editor;" +
	        "  };\n" +
	        "  ORYX.Log.warn('Not Implemented: onOryxResourcesLoaded OR body-script loaded before plugins');\n" +
	        "  }" +
          	"</script>";
		response.setContentType("application/xhtml+xml");
		
		response.getWriter().println(this.getOryxModel("Intalio|Process Designer", 
				content, this.getLanguageCode(request), 
				this.getCountryCode(request), profiles));
		response.setStatus(200);
	}
	protected String getOryxModel(String title, String content, 
    		String languageCode, String countryCode, ArrayList<String> profiles) {
    	
    	return getOryxModel(title, content, languageCode, countryCode, "", profiles);
    }
    
    protected String getOryxModel(String title, String content, 
    		String languageCode, String countryCode, String headExtentions, ArrayList<String> profiles) {
    	
    	String languageFiles = "";
    	String profileFiles="";
    	
    	if (new File(this.getOryxRootDirectory() + oryx_path + "i18n/translation_"+languageCode+".js").exists()) {
    		languageFiles += "<script src=\"" + oryx_path 
    		+ "i18n/translation_"+languageCode+".js\" type=\"text/javascript\" />\n";
    	}
    	
    	if (new File(this.getOryxRootDirectory() + oryx_path + "i18n/translation_" + languageCode+"_" + countryCode + ".js").exists()) {
    		languageFiles += "<script src=\"" + oryx_path 
    		+ "i18n/translation_" + languageCode+"_" + countryCode 
    		+ ".js\" type=\"text/javascript\" />\n";
    	}
    	for(String profile: profiles){
      	  	profileFiles=profileFiles+ "<script src=\"" + oryx_path+"profiles/" + profile+".js\" type=\"text/javascript\" />\n";

    	}
    	
    	String analytics = getServletContext().getInitParameter("ANALYTICS_SNIPPET");
    	if (null == analytics) {
    		analytics = "";
    	}
    	
    	
    	
      	return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
      	    + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
      	  	+ "<html xmlns=\"http://www.w3.org/1999/xhtml\"\n"
      	  	+ "xmlns:b3mn=\"http://b3mn.org/2007/b3mn\"\n"
      	  	+ "xmlns:ext=\"http://b3mn.org/2007/ext\"\n"
      	  	+ "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
      	  	+ "xmlns:atom=\"http://b3mn.org/2007/atom+xhtml\">\n"
      	  	+ "<head profile=\"http://purl.org/NET/erdf/profile\">\n"
      	  	+ "<title>" + title + "</title>\n"
      	  	+ "<!-- libraries -->\n"
      	  	+ "<script src=\"" + oryx_path + "lib/prototype-1.5.1.js\" type=\"text/javascript\" />\n"
      	  	+ "<script src=\"" + oryx_path + "lib/path_parser.js\" type=\"text/javascript\" />\n"
      	  	+ "<script src=\"" + oryx_path + "lib/ext-2.0.2/adapter/ext/ext-base.js\" type=\"text/javascript\" />\n"
      	  	+ "<script src=\"" + oryx_path + "lib/ext-2.0.2/ext-all.js\" type=\"text/javascript\" />\n"
      	  	+ "<script src=\"" + oryx_path + "lib/ext-2.0.2/color-field.js\" type=\"text/javascript\" />\n"
      	  	+ "<style media=\"screen\" type=\"text/css\">\n"
      	  	+ "@import url(\"" + oryx_path + "lib/ext-2.0.2/resources/css/ext-all.css\");\n"
      	  	+ "@import url(\"" + oryx_path + "lib/ext-2.0.2/resources/css/xtheme-gray.css\");\n"
      	  	+ "</style>\n"

      	  	+ "<!-- oryx editor -->\n"
      	  	// EN_US is default an base language
      	  	+ "<!-- language files -->\n"
      	  	+ "<script src=\"" + oryx_path + "i18n/translation_en_us.js\" type=\"text/javascript\" />\n"      	  	
      	  	+ languageFiles
      	  	// Handle different profiles
      	  	+ "<script src=\"" + oryx_path + "profiles/oryx.core.js\" type=\"text/javascript\" />\n"
      	  	+ profileFiles
      	  	+ headExtentions
      	  	
      	  	+ "<link rel=\"Stylesheet\" media=\"screen\" href=\"" + oryx_path + "css/theme_norm.css\" type=\"text/css\" />\n"

      	  	+ "<!-- erdf schemas -->\n"
      	  	+ "<link rel=\"schema.dc\" href=\"http://purl.org/dc/elements/1.1/\" />\n"
      	  	+ "<link rel=\"schema.dcTerms\" href=\"http://purl.org/dc/terms/\" />\n"
      	  	+ "<link rel=\"schema.b3mn\" href=\"http://b3mn.org\" />\n"
      	  	+ "<link rel=\"schema.oryx\" href=\"http://oryx-editor.org/\" />\n"
      	  	+ "<link rel=\"schema.raziel\" href=\"http://raziel.org/\" />\n"
      	  	
      	    + content
      	  	
      	  	+ "</head>\n"
      	  	
      	  	+ "<body style=\"overflow:hidden;\"><div class='processdata' style='display:none'>\n"
      	  	
      	  	+ "\n"
      	  	+ "</div>\n"
      	  	
      	  	+ analytics

      	  	+ "</body>\n"
      	  	+ "</html>";
    }
    protected String getOryxRootDirectory() {
    	String realPath = this.getServletContext().getRealPath("");
    	File backendDir = new File(realPath);
    	return backendDir.getParent();
    }
    protected String getCountryCode(HttpServletRequest req) {
    	return (String) req.getSession().getAttribute("countrycode");
    }
    protected String getLanguageCode(HttpServletRequest req) {
    	return (String) req.getSession().getAttribute("languagecode");
    }
	public Collection<String> getAvailableProfileNames() {
		Collection<String> profilNames = new ArrayList<String>();

		File handlerDir=null;
		try {
			handlerDir = new File(this.getServletContext().
					getRealPath("/profiles"));
		} catch (NullPointerException e) {
			return profilNames;
		}
		if(handlerDir==null)
			return profilNames;
		
		for (File source : handlerDir.listFiles()) {
			if (source.getName().endsWith(".js")) {
				profilNames.add(source.getName().substring(0, source.getName().lastIndexOf(".")));
			}
		}
		return profilNames;
	}
}
